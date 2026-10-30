import { test, expect } from '@playwright/test';
import { authenticate, fixtureCommand, openReady, reset, section, selection } from './fixtures/recommendations';

test('top30 repete localmente e a recarga reutiliza a seleção pronta', async ({ page, request }) => {
  const fixture = await reset('many');
  await openReady(page, fixture);
  await expect(section(page)).toHaveAttribute('data-state', 'READY');
  const before = (await selection(request, fixture)).data;
  expect(before.items).toHaveLength(30);
  let requests = 0;
  page.on('request', request => { if (new URL(request.url()).pathname.startsWith('/recommendations')) requests++; });
  const next = section(page).getByRole('button', { name: 'Próxima recomendação' });
  const seen = new Set<number>();
  for (let index = 0; index < 60; index++) {
    for (const id of await section(page).locator('article').evaluateAll(articles => articles.map(article => Number(article.getAttribute('data-book-id'))))) seen.add(id);
    await next.click();
  }
  expect([...seen].sort((a, b) => a - b)).toEqual(before.items.map((item: { book: { id: number } }) => item.book.id).sort((a: number, b: number) => a - b));
  const position = section(page).getByText('1 de 30', { exact: true });
  await expect(position).toHaveAttribute('role', 'status');
  await expect(position).toHaveCSS('width', '1px');
  await expect(section(page).locator('article')).toHaveCount(5);
  expect(requests).toBe(0);
  await page.reload();
  await expect(section(page)).toHaveAttribute('data-state', 'READY');
  const after = (await selection(request, fixture)).data;
  expect(after.selection).toEqual(before.selection);
  expect(after.items).toEqual(before.items);
});

test('READY vazia permanece final do dia sem reposição', async ({ page, request }) => {
  const fixture = await reset('ready_empty');
  await openReady(page, fixture);
  await expect(section(page)).toHaveAttribute('data-state', 'EMPTY');
  const before = (await selection(request, fixture)).data;
  await page.reload();
  await expect(section(page)).toHaveAttribute('data-state', 'EMPTY');
  const after = (await selection(request, fixture)).data;
  expect(after.selection).toEqual(before.selection);
  await expect(section(page).getByRole('button', { name: 'Tentar novamente' })).toHaveCount(0);
});

test('STALE vazia mantém seleção histórica sem data visível e verifica conclusão usando GET', async ({ page, request }) => {
  const fixture = await reset('stale_empty');
  await authenticate(page, fixture.users.a);
  await page.goto('/dashboard');
  await expect(section(page)).toHaveAttribute('data-state', 'STALE');
  expect((await selection(request, fixture)).data.selection.referenceDate).toBe('2026-10-29');
  await expect(section(page)).not.toContainText('29/10/2026');
  await expect(section(page)).toContainText('A atualização está pendente');
  let posts = 0;
  page.on('request', request => { if (request.method() === 'POST' && new URL(request.url()).pathname === '/recommendations/generation') posts++; });
  await fixtureCommand({ action: 'complete' });
  await section(page).getByRole('button', { name: 'Verificar atualização', exact: true }).click();
  await expect(section(page)).toHaveAttribute('data-state', 'READY');
  expect(posts).toBe(0);
});

test('falha após orçamento AUTO permite recuperação MANUAL explícita', async ({ page }) => {
  const fixture = await reset('failed');
  await authenticate(page, fixture.users.a);
  await page.goto('/dashboard');
  const post = page.waitForResponse(response => response.request().method() === 'POST' && new URL(response.url()).pathname === '/recommendations/generation');
  await section(page).getByRole('button', { name: 'Tentar novamente', exact: true }).click();
  expect((await post).status()).toBe(202);
  await expect(section(page)).toHaveAttribute('data-state', 'READY', { timeout: 20_000 });
});

test('GENERATING respeita Retry-After legível entre origens e para em dez GETs', async ({ page }) => {
  const fixture = await reset('generating');
  await page.clock.install();
  await authenticate(page, fixture.users.a);
  let calls = 0;
  let delay = 3;
  page.on('response', async response => {
    if (new URL(response.url()).pathname === '/recommendations' && response.request().method() === 'GET') {
      calls++;
      delay = Number(response.headers()['retry-after'] || '3');
    }
  });
  await page.goto('/dashboard');
  await expect(section(page)).toHaveAttribute('data-state', 'GENERATING');
  const exposed = await page.evaluate(async ({ baseUrl, token }) => {
    const response = await fetch(`${baseUrl}/recommendations`, { headers: { Authorization: `Bearer ${token}` }, credentials: 'include' });
    return response.headers.get('Retry-After');
  }, { baseUrl: fixture.baseUrl, token: fixture.users.a.token });
  expect(Number(exposed)).toBeGreaterThanOrEqual(1);
  // This explicit CORS probe adds one GET beyond the ten automatic hook calls.
  await expect.poll(() => calls).toBe(2);
  for (let index = 0; index < 9; index++) {
    await page.clock.runFor(delay * 1_000);
    await expect.poll(() => calls).toBe(index + 3);
  }
  await page.clock.runFor(delay * 20_000);
  expect(calls).toBe(11);
  await expect(section(page)).toContainText('Verifique a atualização quando quiser');
});

test('virada controlada usa data nova sem alterar a seleção anterior', async ({ page, request }) => {
  const fixture = await reset();
  await openReady(page, fixture);
  const previous = (await selection(request, fixture)).data;
  await fixtureCommand({ action: 'advance', instant: '2026-10-31T12:00:00Z' });
  await page.reload();
  await expect.poll(async () => (await selection(request, fixture)).data.selection?.referenceDate).toBe('2026-10-31');
  if (await section(page).getByRole('button', { name: 'Verificar atualização', exact: true }).isVisible()) await section(page).getByRole('button', { name: 'Verificar atualização', exact: true }).click();
  await expect(section(page)).toHaveAttribute('data-state', 'READY');
  const current = (await selection(request, fixture)).data;
  expect(current.selection.id).not.toBe(previous.selection.id);
});

test('reinício real de contexto preserva READY e startup recupera lease expirado no novo dia', async ({ page, request }) => {
  test.setTimeout(120_000);
  const fixture = await reset();
  await openReady(page, fixture);
  const previous = (await selection(request, fixture)).data;
  await fixtureCommand({ action: 'restart', jobsEnabled: false });
  await page.reload();
  await expect(section(page)).toHaveAttribute('data-state', 'READY');
  expect((await selection(request, fixture)).data.selection).toEqual(previous.selection);
  await fixtureCommand({ action: 'advance', instant: '2026-10-31T12:00:00Z' });
  // No GET occurs between advancing the Clock and restarting. The fixture waits
  // on the database, so only startup/recovery can complete the expired batch.
  const recovered = await fixtureCommand<{ resumedExpired: boolean }>({ action: 'restart', jobsEnabled: true, recover: true });
  expect(recovered.resumedExpired).toBe(true);
  await page.reload();
  await expect(section(page)).toHaveAttribute('data-state', 'READY');
  const current = (await selection(request, fixture)).data;
  expect(current.selection.referenceDate).toBe('2026-10-31');
  expect(current.selection.id).not.toBe(previous.selection.id);
});
