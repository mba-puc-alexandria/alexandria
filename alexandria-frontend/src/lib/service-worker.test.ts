import { readFileSync } from 'node:fs';
import path from 'node:path';
import { runInNewContext } from 'node:vm';
import { describe, expect, it, vi } from 'vitest';

type WorkerEvent = {
  request?: Request;
  respondWith?: (promise: unknown) => void;
  waitUntil?: (promise: Promise<unknown>) => void;
};
function worker() {
  const listeners = new Map<string, (event: WorkerEvent) => void>();
  const records = new Map<string, Request[]>([
    ['static-v2', [new Request('https://api.example.invalid/recommendations'), new Request('https://frontend.example.invalid/logo.svg')]],
    ['books-api-v2', [new Request('https://api.example.invalid/recommendations/generation'), new Request('https://api.example.invalid/books/1')]],
  ]);
  const deleted: string[] = [];
  const fetch = vi.fn().mockResolvedValue(new Response('{}'));
  const cacheMatch = vi.fn().mockResolvedValue(undefined);
  const claim = vi.fn().mockResolvedValue(undefined);
  const caches = {
    keys: async () => [...records.keys()],
    delete: async (name: string) => records.delete(name),
    match: cacheMatch,
    open: async (name: string) => ({
      keys: async () => records.get(name) || [],
      match: async () => undefined,
      put: async () => {},
      delete: async (request: Request) => {
        deleted.push(request.url);
        records.set(name, (records.get(name) || []).filter(entry => entry.url !== request.url));
        return true;
      },
    }),
  };
  const source = readFileSync(path.join(process.cwd(), 'public/sw.js'), 'utf8');
  runInNewContext(source, { self: { addEventListener: (type: string, listener: (event: WorkerEvent) => void) => listeners.set(type, listener), skipWaiting: vi.fn() }, caches, clients: { claim }, fetch, URL });
  return { listeners, records, deleted, fetch, cacheMatch, claim };
}

describe('service worker e privacidade de recomendações', () => {
  it.each([
    ['GET', '/recommendations'],
    ['POST', '/recommendations/generation'],
    ['GET', '/recommendations/private.png'],
  ])('%s %s permanece network-only, inclusive offline', async (method, pathname) => {
    const service = worker();
    const respondWith = vi.fn();
    service.listeners.get('fetch')!({ request: new Request(`https://api.example.invalid${pathname}`, { method }), respondWith });
    expect(respondWith).not.toHaveBeenCalled();
    expect(service.fetch).not.toHaveBeenCalled();
    expect(service.cacheMatch).not.toHaveBeenCalled();
  });

  it('ativação remove respostas privadas legadas sem apagar livros públicos ou assets', async () => {
    const service = worker();
    let activation: Promise<unknown> | undefined;
    service.listeners.get('activate')!({ waitUntil: value => { activation = value; } });
    await activation;
    expect(service.deleted).toEqual(expect.arrayContaining(['https://api.example.invalid/recommendations', 'https://api.example.invalid/recommendations/generation']));
    expect(service.records.get('static-v2')?.map(request => request.url)).toEqual(['https://frontend.example.invalid/logo.svg']);
    expect(service.records.get('books-api-v2')?.map(request => request.url)).toEqual(['https://api.example.invalid/books/1']);
    expect(service.claim).toHaveBeenCalledOnce();
  });
});
