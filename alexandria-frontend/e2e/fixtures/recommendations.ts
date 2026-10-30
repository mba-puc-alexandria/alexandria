import { expect, type Page, type APIRequestContext } from '@playwright/test';
import { randomUUID } from 'node:crypto';
import { readFile } from 'node:fs/promises';
import path from 'node:path';
import { writeControlCommand } from './control-file';

export interface FixtureUser { userId: number; username: string; token: string }
export interface FixtureReady {
  baseUrl: string;
  users: { a: FixtureUser; b: FixtureUser };
  books: { source: number; first: number; last: number };
  referenceDate: string;
  loadUsers?: FixtureUser[];
  dataset?: { books: number; readers: number; libraryLinksPerReader: number };
  seedMillis?: number;
  generationMillis?: number;
}
export type Scenario = 'default' | 'many' | 'no_profile' | 'no_matches' | 'ready_empty' | 'stale_failed' | 'stale_empty' | 'failed' | 'generating';
const directory = () => {
  const value = process.env.E2E_FIXTURE_DIR;
  if (!value) throw new Error('E2E_FIXTURE_DIR é obrigatório: iniciar o RecommendationE2EServer isolado antes da suíte.');
  return value;
};
export async function fixtureCommand<T = FixtureReady>(command: { action: 'reset' | 'advance' | 'delete' | 'complete' | 'capacity' | 'restart'; scenario?: Scenario; instant?: string; bookId?: number; jobsEnabled?: boolean; recover?: boolean }): Promise<T> {
  const id = randomUUID();
  await writeControlCommand(directory(), { id, ...command });
  let reply: { id: string; ok: boolean; result: T; error?: string } | undefined;
  await expect.poll(async () => {
    try { reply = JSON.parse(await readFile(path.join(directory(), 'response.json'), 'utf8')); } catch { return false; }
    return reply?.id === id;
  }, { timeout: command.action === 'capacity' ? 3_600_000 : command.action === 'restart' ? 90_000 : 30_000, message: `Resposta do fixture para ${command.action}` }).toBe(true);
  if (!reply?.ok) throw new Error(`Fixture falhou: ${reply?.error || command.action}`);
  return reply.result;
}
export async function reset(scenario: Scenario = 'default'): Promise<FixtureReady> {
  return fixtureCommand({ action: 'reset', scenario });
}
export async function authenticate(page: Page, user: FixtureUser) {
  const baseURL = process.env.E2E_BASE_URL || `http://localhost:${process.env.E2E_FRONTEND_PORT || 3100}`;
  await page.context().addCookies([{ name: 'auth-token', value: user.token, url: baseURL, sameSite: 'Lax' }]);
  await page.addInitScript(({ token, userId, username }) => {
    localStorage.setItem('auth-token', token);
    localStorage.setItem('auth-user', JSON.stringify({ userId, username }));
  }, user);
}
export const section = (page: Page) => page.getByTestId('recommendations');
export const authHeaders = (user: FixtureUser) => ({ Authorization: `Bearer ${user.token}` });
export async function selection(request: APIRequestContext, fixture: FixtureReady, user = fixture.users.a) {
  const response = await request.get(`${fixture.baseUrl}/recommendations`, { headers: authHeaders(user) });
  return { response, data: await response.json() };
}
export async function openReady(page: Page, fixture: FixtureReady, user = fixture.users.a) {
  await authenticate(page, user);
  await page.goto('/dashboard');
  await expect(section(page)).toHaveAttribute('data-state', /READY|EMPTY|STALE/);
}
