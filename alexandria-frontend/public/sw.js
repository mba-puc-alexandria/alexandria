const CACHE_VERSION = "v1";
const STATIC_CACHE = `static-${CACHE_VERSION}`;
const BOOKS_CACHE = `books-api-${CACHE_VERSION}`;

self.addEventListener("install", (event) => {
  event.waitUntil(self.skipWaiting());
});

self.addEventListener("activate", (event) => {
  event.waitUntil(clients.claim());
});

self.addEventListener("fetch", (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // EPUB → network-only (cache já gerenciado pelo epub-cache.ts)
  if (url.pathname.startsWith("/api/epub")) {
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
