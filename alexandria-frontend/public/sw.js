const CACHE_VERSION = "v2";
const STATIC_CACHE = `static-${CACHE_VERSION}`;
const BOOKS_CACHE = `books-api-${CACHE_VERSION}`;

function isPrivateRecommendation(request) {
  return /^\/recommendations(?:\/|$)/.test(new URL(request.url).pathname);
}

async function removeLegacyRecommendations() {
  for (const name of await caches.keys()) {
    const cache = await caches.open(name);
    const requests = await cache.keys();
    await Promise.all(requests.filter(isPrivateRecommendation).map((request) => cache.delete(request)));
  }
}

self.addEventListener("install", (event) => {
  event.waitUntil(self.skipWaiting());
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) => Promise.all(keys.filter((key) => key !== STATIC_CACHE && key !== BOOKS_CACHE).map((key) => caches.delete(key))))
      .then(removeLegacyRecommendations)
      .then(() => clients.claim()),
  );
});

self.addEventListener("fetch", (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // Personal recommendations must bypass every asset/cache/fallback branch.
  // Returning without respondWith leaves the original request network-only,
  // including offline failures; no previous account's response can be served.
  if (isPrivateRecommendation(request)) {
    return;
  }

  // EPUB → network-only (cache já gerenciado pelo epub-cache.ts)
  if (url.pathname.startsWith("/api/epub")) {
    return;
  }

  // Os chunks do Next têm nomes estáveis em alguns builds. Mantê-los em
  // cache-first pode servir uma interface antiga depois de um deploy.
  if (url.pathname.startsWith("/_next/")) {
    event.respondWith(fetch(request).catch(() => caches.match(request)));
    return;
  }

  // Assets estáticos → cache-first
  if (/\.(?:js|css|woff2?|ttf|otf|png|svg|ico)$/.test(url.pathname)) {
    event.respondWith(
      caches.open(STATIC_CACHE).then((cache) =>
        cache.match(request).then((cached) => cached ?? fetch(request).then((response) => {
          cache.put(request, response.clone());
          return response;
        })),
      ),
    );
    return;
  }

  // API pública de livros → network-first
  if (/\/books(?:\/|\?|$)/.test(url.pathname)) {
    event.respondWith(
      fetch(request)
        .then((response) => {
          const clone = response.clone();
          caches.open(BOOKS_CACHE).then((cache) => cache.put(request, clone));
          return response;
        })
        .catch(() => caches.match(request)),
    );
    return;
  }

  // Rotas autenticadas → network-only (nunca cachear)
  if (/^\/(?:user-books|profile|auth|jobs)\/?/.test(url.pathname)) {
    return;
  }

  // Páginas HTML → network-first
  event.respondWith(fetch(request).catch(() => caches.match(request)));
});
