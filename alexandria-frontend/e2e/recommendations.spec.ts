import { test, expect } from '@playwright/test';
import { createServer } from 'node:http';
import { readFileSync } from 'node:fs';
import path from 'node:path';
import type { AddressInfo } from 'node:net';
import { authenticate, openReady, reset, section, selection } from './fixtures/recommendations';

test('descobre candidatos personalizados, exclui a biblioteca e apresenta evidência histórica', async ({ page, request }, testInfo) => {
  const fixture = await reset();
  await openReady(page, fixture);
  await expect(section(page)).toHaveAttribute('data-state', 'READY');
  await expect(section(page).locator('article')).toHaveCount(1);
  await expect(section(page)).toContainText('1 de 7');
  await expect(section(page)).toContainText('30/10/2026');
  await expect(section(page).getByRole('link', { name: 'Ver livro', exact: true })).toBeVisible();
  const { response, data } = await selection(request, fixture);
  expect(response.status()).toBe(200);
  expect(response.headers()['cache-control']).toContain('no-store');
  expect(data.items).toHaveLength(7);
  expect(data.items.some((item: { book: { id: number } }) => item.book.id === fixture.books.source)).toBe(false);
  expect(new Set(data.items.map((item: { book: { id: number } }) => item.book.id)).size).toBe(7);
  expect(data.items.every((item: { reason: { text: string } }) => item.reason.text.length > 0)).toBe(true);
  expect(data).not.toHaveProperty('userId');
  expect(data).not.toHaveProperty('profileFingerprint');
  await section(page).screenshot({ path: testInfo.outputPath('recommendations-desktop.png') });
});

test('separa contas e explica NO_PROFILE sem pressupor biblioteca vazia', async ({ page, browser }) => {
  const fixture = await reset();
  await openReady(page, fixture);
  const other = await browser.newContext({ baseURL: process.env.E2E_BASE_URL || 'http://localhost:3100' });
  try {
    const otherPage = await other.newPage();
    await authenticate(otherPage, fixture.users.b);
    await otherPage.goto('/dashboard');
    await expect(section(otherPage)).toHaveAttribute('data-state', 'EMPTY');
    await expect(section(otherPage)).toContainText('Ainda não encontramos interesses suficientes');
    await expect(section(otherPage).locator('article')).toHaveCount(0);
    await expect(section(page).locator('article')).toHaveCount(1);
  } finally { await other.close(); }
});

test('controles e ações funcionam por teclado em390px sem hover', async ({ page }, testInfo) => {
  const fixture = await reset();
  await page.setViewportSize({ width: 390, height: 844 });
  await openReady(page, fixture);
  const next = section(page).getByRole('button', { name: 'Próxima recomendação' });
  await next.focus();
  await page.keyboard.press('Space');
  await expect(section(page)).toContainText('2 de 7');
  const previous = section(page).getByRole('button', { name: 'Recomendação anterior' });
  await previous.focus();
  await page.keyboard.press('Enter');
  await expect(section(page)).toContainText('1 de 7');
  await section(page).screenshot({ path: testInfo.outputPath('recommendations-mobile390.png') });
  const details = section(page).getByRole('link', { name: 'Ver livro', exact: true });
  const href = await details.getAttribute('href');
  await details.focus();
  await page.keyboard.press('Enter');
  await expect(page).toHaveURL(new RegExp(`${href}$`));
  await page.goBack();
  await expect(section(page)).toHaveAttribute('data-state', 'READY');
});

test('falha de recomendações preserva as demais seções do Painel', async ({ page }) => {
  const fixture = await reset('failed');
  await authenticate(page, fixture.users.a);
  await page.goto('/dashboard');
  await expect(section(page).getByRole('alert')).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Lendo Agora', exact: true })).toBeVisible();
  await expect(section(page).getByRole('button', { name: 'Tentar novamente', exact: true })).toBeEnabled();
});

test('atualização real do service worker remove cache privado legado e mantém rede obrigatória offline', async ({ page, context }) => {
  // This browser lifecycle test serves the exact production script on an
  // ephemeral local origin. React's automatic registration cannot race it.
  const currentScript = readFileSync(path.join(process.cwd(), 'public/sw.js'), 'utf8');
  const legacyScript = `
    self.addEventListener('install', event => event.waitUntil(self.skipWaiting()));
    self.addEventListener('activate', event => event.waitUntil(clients.claim()));
    self.addEventListener('fetch', event => event.respondWith(fetch(event.request).catch(() => caches.match(event.request))));
  `;
  expect(currentScript).not.toBe(legacyScript);
  const server = createServer((request, response) => {
    response.setHeader('Cache-Control', 'no-store');
    response.setHeader('Service-Worker-Allowed', '/');
    if (request.url === '/legacy.js' || request.url === '/sw.js') {
      response.setHeader('Content-Type', 'application/javascript');
      response.end(request.url === '/legacy.js' ? legacyScript : currentScript);
    } else {
      response.setHeader('Content-Type', 'text/html');
      response.end('<!doctype html><html><body>Service worker lifecycle fixture</body></html>');
    }
  });
  await new Promise<void>(resolve => { server.listen(0, '127.0.0.1', resolve); });
  const origin = `http://127.0.0.1:${(server.address() as AddressInfo).port}`;
  const expectController = async (scriptURL: string) => {
    await expect.poll(async () => page.evaluate(async () => {
      const registration = await navigator.serviceWorker.getRegistration();
      return { activeURL: registration?.active?.scriptURL ?? null, activeState: registration?.active?.state ?? null,
        controllerURL: navigator.serviceWorker.controller?.scriptURL ?? null, controllerState: navigator.serviceWorker.controller?.state ?? null };
    })).toEqual({ activeURL: scriptURL, activeState: 'activated', controllerURL: scriptURL, controllerState: 'activated' });
  };
  try {
    await page.goto(`${origin}/blank`);
    await page.evaluate(async () => { await navigator.serviceWorker.register('/legacy.js', { scope: '/' }); });
    await expectController(`${origin}/legacy.js`);
    await page.evaluate(async () => {
      const cache = await caches.open('static-v2');
      await cache.put(`${location.origin}/recommendations`, new Response(JSON.stringify({ legacyAccount: true })));
      await cache.put('http://localhost:18080/recommendations', new Response(JSON.stringify({ legacyAccount: true })));
      await cache.put(`${location.origin}/public-survives.svg`, new Response('<svg/>'));
    });
    expect(await page.evaluate(async () => !!await (await caches.open('static-v2')).match(`${location.origin}/recommendations`))).toBe(true);
    await page.evaluate(async () => { await navigator.serviceWorker.register('/sw.js', { scope: '/' }); });
    await expectController(`${origin}/sw.js`);
    const cacheState = await page.evaluate(async () => {
      const cache = await caches.open('static-v2');
      return { privatePresent: !!await cache.match(`${location.origin}/recommendations`),
        crossOriginPrivatePresent: !!await cache.match('http://localhost:18080/recommendations'),
        publicPresent: !!await cache.match(`${location.origin}/public-survives.svg`) };
    });
    expect(cacheState).toEqual({ privatePresent: false, crossOriginPrivatePresent: false, publicPresent: true });
    await context.setOffline(true);
    const outcome = await page.evaluate(async () => {
      try { await fetch('/recommendations', { cache: 'no-store' }); return 'served'; }
      catch { return 'network-required'; }
    });
    expect(outcome).toBe('network-required');
  } finally {
    await context.setOffline(false);
    await page.goto('about:blank');
    await new Promise<void>(resolve => { server.close(() => resolve()); server.closeAllConnections(); });
  }
});
