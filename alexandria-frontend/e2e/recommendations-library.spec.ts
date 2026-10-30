import { test, expect } from '@playwright/test';
import { authHeaders, fixtureCommand, openReady, reset, section, selection } from './fixtures/recommendations';

test('inclui os30 selecionados entre40 candidatos e chega a EMPTY sem reposição', async ({ page, request }) => {
  test.setTimeout(90_000);
  const fixture = await reset('many');
  await openReady(page, fixture);
  for (let remaining = 30; remaining > 0; remaining--) {
    const article = section(page).locator('article');
    const id = Number(await article.getAttribute('data-book-id'));
    const add = article.getByRole('button', { name: 'Adicionar à biblioteca', exact: true });
    await add.focus();
    await page.keyboard.press('Enter');
    await expect.poll(async () => (await selection(request, fixture)).data.items.length).toBe(remaining - 1);
    await expect(section(page).locator(`article[data-book-id="${id}"]`)).toHaveCount(0);
    expect(await page.evaluate(() => document.activeElement?.tagName)).not.toBe('BODY');
  }
  await expect(section(page)).toHaveAttribute('data-state', 'EMPTY');
  await expect(section(page)).toContainText('próxima seleção');
  const libraryResponse = await request.get(`${fixture.baseUrl}/user-books?size=100`, { headers: authHeaders(fixture.users.a) });
  expect(libraryResponse.status()).toBe(200);
  const library = await libraryResponse.json();
  expect(library.content.filter((book: { status: string }) => book.status === 'toread')).toHaveLength(30);
  await page.reload();
  await expect(section(page)).toHaveAttribute('data-state', 'EMPTY');
  await expect(section(page).locator('article')).toHaveCount(0);
});

test('409 de inclusão concorrente retira cartão sem anunciar criação', async ({ page, request }) => {
  const fixture = await reset();
  await openReady(page, fixture);
  const id = Number(await section(page).locator('article').getAttribute('data-book-id'));
  const direct = await request.post(`${fixture.baseUrl}/user-books`, { headers: authHeaders(fixture.users.a), data: { bookId: id, status: 'TOREAD' } });
  expect(direct.status()).toBe(201);
  const duplicate = page.waitForResponse(response => response.request().method() === 'POST' && new URL(response.url()).pathname === '/user-books');
  await section(page).getByRole('button', { name: 'Adicionar à biblioteca', exact: true }).click();
  expect((await duplicate).status()).toBe(409);
  await expect(section(page)).toContainText('Este livro já está na sua biblioteca.');
  await expect(section(page)).not.toContainText('Livro adicionado à biblioteca.');
  expect((await selection(request, fixture)).data.items.some((item: { book: { id: number } }) => item.book.id === id)).toBe(false);
});

test('GET anterior atrasado não restaura livro após confirmação', async ({ page, request }) => {
  const fixture = await reset();
  await openReady(page, fixture);
  const id = Number(await section(page).locator('article').getAttribute('data-book-id'));
  // The delayed response is a real, complete backend snapshot captured before
  // the mutation. Only its delivery is controlled by this transport fixture.
  const oldPayload = (await selection(request, fixture)).data;
  let release!: () => void;
  const gate = new Promise<void>(resolve => { release = resolve; });
  let captured = false;
  let delivered = false;
  let first = true;
  await page.route('**/recommendations', async route => {
    if (route.request().method() !== 'GET' || !first) return route.continue();
    first = false;
    captured = true;
    await gate;
    try {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(oldPayload),
        headers: { 'Access-Control-Allow-Origin': process.env.E2E_BASE_URL || 'http://localhost:3100', 'Access-Control-Allow-Credentials': 'true', 'Cache-Control': 'no-store' } });
    } catch { /* Aborted by the confirmed mutation. */ }
    finally { delivered = true; }
  });
  await page.evaluate(() => window.dispatchEvent(new Event('focus')));
  await expect.poll(() => captured).toBe(true);
  await section(page).getByRole('button', { name: 'Adicionar à biblioteca', exact: true }).click();
  await expect(section(page).locator(`article[data-book-id="${id}"]`)).toHaveCount(0);
  release();
  await expect.poll(() => delivered).toBe(true);
  await expect(section(page).locator(`article[data-book-id="${id}"]`)).toHaveCount(0);
  await section(page).getByRole('button', { name: 'Próxima recomendação' }).click();
  await expect(section(page).locator(`article[data-book-id="${id}"]`)).toHaveCount(0);
});

test('remoção explícita da biblioteca reexibe selecionado após foco, sem novo ranking', async ({ page, request }) => {
  const fixture = await reset();
  await openReady(page, fixture);
  const before = (await selection(request, fixture)).data;
  const id = Number(await section(page).locator('article').getAttribute('data-book-id'));
  await section(page).getByRole('button', { name: 'Adicionar à biblioteca', exact: true }).click();
  await expect(section(page).locator(`article[data-book-id="${id}"]`)).toHaveCount(0);
  const libraryResponse = await request.get(`${fixture.baseUrl}/user-books?size=100`, { headers: authHeaders(fixture.users.a) });
  expect(libraryResponse.status()).toBe(200);
  const library = await libraryResponse.json();
  const added = library.content.find((book: { book: { id: number } }) => book.book.id === id);
  expect(added).toBeDefined();
  expect((await request.delete(`${fixture.baseUrl}/user-books/${added.id}`, { headers: authHeaders(fixture.users.a) })).status()).toBe(204);
  await page.evaluate(() => window.dispatchEvent(new Event('focus')));
  await expect.poll(async () => (await selection(request, fixture)).data.items.length).toBe(7);
  await expect(section(page)).toContainText('de 7');
  expect((await selection(request, fixture)).data.selection).toEqual(before.selection);
});

test('5xx e404 genérico simulados não confirmam inclusão nem remoção', async ({ page }) => {
  const fixture = await reset();
  await openReady(page, fixture);
  const id = await section(page).locator('article').getAttribute('data-book-id');
  for (const status of [500, 404]) {
    await page.route('**/user-books', async route => {
      if (route.request().method() !== 'POST') return route.continue();
      await route.fulfill({ status, contentType: 'application/json', body: JSON.stringify({ message: 'Erro não classificado' }), headers: { 'Access-Control-Allow-Origin': process.env.E2E_BASE_URL || 'http://localhost:3100', 'Access-Control-Allow-Credentials': 'true' } });
    });
    await section(page).getByRole('button', { name: 'Adicionar à biblioteca', exact: true }).click();
    await expect(section(page).getByRole('alert')).toContainText('Não foi possível adicionar');
    await expect(section(page).locator(`article[data-book-id="${id}"]`)).toHaveCount(1);
    await expect(section(page)).not.toContainText('Livro adicionado à biblioteca.');
    await page.unroute('**/user-books');
  }
});

test('livro removido após exibição sai por BOOK_NOT_FOUND sem reposição', async ({ page }) => {
  const fixture = await reset();
  await openReady(page, fixture);
  for (let index = 0; index < 6; index++) {
    const id = await section(page).locator('article').getAttribute('data-book-id');
    await section(page).getByRole('button', { name: 'Adicionar à biblioteca', exact: true }).click();
    await expect(section(page).locator(`article[data-book-id="${id}"]`)).toHaveCount(0);
  }
  const lastId = Number(await section(page).locator('article').getAttribute('data-book-id'));
  await fixtureCommand({ action: 'delete', bookId: lastId });
  await section(page).getByRole('button', { name: 'Adicionar à biblioteca', exact: true }).click();
  await expect(section(page)).toContainText('Este livro não está mais disponível.');
  await expect(section(page)).toHaveAttribute('data-state', 'EMPTY');
  await expect(section(page).locator('article')).toHaveCount(0);
  expect(await page.evaluate(() => document.activeElement?.tagName)).not.toBe('BODY');
});
