import { test, expect } from '@playwright/test';
import { writeFile } from 'node:fs/promises';
import os from 'node:os';
import { authenticate, authHeaders, fixtureCommand } from './fixtures/recommendations';

test('SC-006 — exibição em produção com catálogo integral e50leitores concorrentes', async ({ page, request, browser }, testInfo) => {
  test.setTimeout(3_900_000);
  if (process.env.E2E_PRODUCTION !== '1' || !process.env.E2E_BASE_URL) throw new Error('Este ensaio exige build/start de produção externo, E2E_PRODUCTION=1 e E2E_BASE_URL.');
  console.log('CAPACITY_START: preparando catálogo100k,1000leitores e1M vínculos isolados.');
  const fixture = await fixtureCommand({ action: 'capacity' });
  expect(fixture.dataset).toMatchObject({ books: 100_000, readers: 1_000, libraryLinksPerReader: 1_000 });
  const users = fixture.loadUsers?.slice(0, 50) || [];
  expect(new Set(users.map(user => user.userId)).size).toBe(50);
  console.log(`CAPACITY_READY: geração=${fixture.generationMillis}ms; iniciando5aquecimentos e100navegações com50leitores.`);
  await page.setViewportSize({ width: 1280, height: 800 });
  await authenticate(page, fixture.users.a);
  // Installed before every full navigation; the marks belong to that document.
  await page.addInitScript(() => {
    let done = false;
    const observer = new MutationObserver(check);
    function check() {
      if (done) return;
      const section = document.querySelector<HTMLElement>('[data-testid="recommendations"]');
      if (!section || !['READY', 'EMPTY'].includes(section.dataset.state || '')) return;
      if (section.querySelector('[data-testid="recommendations-loading"]')) return;
      const content = section.querySelector<HTMLElement>('article, [data-testid="recommendations-empty"]');
      const bounds = content?.getBoundingClientRect();
      if (!content || !bounds || bounds.width <= 0 || bounds.height <= 0 || getComputedStyle(content).visibility === 'hidden') return;
      done = true;
      observer.disconnect();
      requestAnimationFrame(() => requestAnimationFrame(() => performance.mark('recommendations-painted')));
    }
    observer.observe(document, { childList: true, subtree: true, attributes: true });
    document.addEventListener('DOMContentLoaded', check, { once: true });
    check();
  });
  let running = true;
  let concurrentFailures = 0;
  let concurrentReads = 0;
  const readers = users.map(user => (async () => {
    while (running) {
      try {
        const response = await request.get(`${fixture.baseUrl}/recommendations`, { headers: authHeaders(user), timeout: 10_000 });
        concurrentReads++;
        if (response.status() !== 200) concurrentFailures++;
        await response.dispose();
      } catch { concurrentFailures++; }
      if (running) await new Promise(resolve => setTimeout(resolve, 500));
    }
  })());
  const warmups: number[] = [];
  const samples: number[] = [];
  const failures: { attempt: number; message: string }[] = [];
  let result;
  try {
    for (let attempt = 0; attempt < 105; attempt++) {
      try {
        await page.goto('/dashboard', { waitUntil: 'domcontentloaded', timeout: 15_000 });
        await page.waitForFunction(() => performance.getEntriesByName('recommendations-painted').length > 0, undefined, { timeout: 10_000 });
        const duration = await page.evaluate(() => {
          const navigation = performance.getEntriesByType('navigation')[0];
          return performance.getEntriesByName('recommendations-painted')[0].startTime - navigation.startTime;
        });
        (attempt < 5 ? warmups : samples).push(duration);
        if (samples.length > 0 && samples.length % 25 === 0) console.log(`BROWSER_SAMPLES: ${samples.length}/100 válidas; falhas=${failures.length}.`);
      } catch (error) {
        failures.push({ attempt, message: error instanceof Error ? error.message : 'Falha de navegação' });
      }
    }
  } finally {
    running = false;
    await Promise.all(readers);
    const sorted = [...samples].sort((a, b) => a - b);
    const p95 = sorted.length ? sorted[Math.ceil(0.95 * sorted.length) - 1] : null;
    result = {
      recordedAt: new Date().toISOString(), protocol: 'navigation.startTime → conteúdo READY/EMPTY visível +2rAF',
      browser: browser.version(), machine: { platform: os.platform(), release: os.release(), architecture: os.arch(), cpu: os.cpus()[0]?.model, cpuCount: os.cpus().length, memoryBytes: os.totalmem() },
      viewport: { width: 1280, height: 800 }, network: 'local sem throttling', cache: 'normal do navegador; API no-store',
      dataset: fixture.dataset, seedMillis: fixture.seedMillis, generationMillis: fixture.generationMillis,
      simultaneousReaders: users.length, readThinkTimeMs: 500,
      concurrentReads, concurrentFailures, warmups, samples, failures, p95Ms: p95,
    };
    const filename = testInfo.outputPath('recommendation-performance.json');
    await writeFile(filename, JSON.stringify(result, null, 2), 'utf8');
    await testInfo.attach('amostras-e-ambiente', { path: filename, contentType: 'application/json' });
  }
  expect(result.warmups).toHaveLength(5);
  expect(result.samples).toHaveLength(100);
  expect(result.failures).toEqual([]);
  expect(result.concurrentFailures).toBe(0);
  expect(result.p95Ms).not.toBeNull();
  expect(result.p95Ms!).toBeLessThanOrEqual(2_000);
});
