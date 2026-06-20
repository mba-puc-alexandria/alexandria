# Plano de Implementação — PWA (Progressive Web App)

> **Objetivo:** Tornar o Alexandria instalável como app no celular/desktop e permitir navegação offline parcial.
>
> **Premissas:** Nada do que funciona pode quebrar. Testes e linter devem seguir passando. CI/CD e deploys não podem ser afetados.

---

## Índice

1. [Anatomia do Projeto (indexação)](#1-anatomia-do-projeto-indexação)
2. [O que será alterado](#2-o-que-será-alterado)
3. [Estratégia de Cache](#3-estratégia-de-cache)
4. [Fases de Implementação](#4-fases-de-implementação)
5. [Checklist](#5-checklist)
6. [Riscos e Mitigações](#6-riscos-e-mitigações)
7. [Perguntas em Aberto](#7-perguntas-em-aberto)

---

## 1. Anatomia do Projeto (indexação)

### Estrutura relevante do frontend

```
alexandria-frontend/
├── next.config.ts                    # output: "standalone" — sem plugins PWA
├── package.json                      # next@16.2.4 — sem libs de PWA
├── Dockerfile                        # node:20-alpine, copia public/ inteiro
├── tsconfig.json                     # paths: "@/*": ["./src/*"]
├── public/                           # só SVGs, sem manifest.json, sem ícones
├── src/
│   ├── app/
│   │   ├── layout.tsx                # metadata (título, descrição) — SEM manifest
│   │   ├── page.tsx                  # redirect("/explorar")
│   │   ├── api/epub/route.ts         # proxy de download de EPUB
│   │   └── (main)/                   # dashboard, biblioteca, explorar, leitor...
│   ├── lib/
│   │   ├── api.ts                    # fetch centralizado para o backend
│   │   └── epub-cache.ts             # cache offline de EPUB via IndexedDB + LRU
│   ├── hooks/useEpub.ts              # hook que consome epub-cache.ts
│   ├── contexts/AuthContext.tsx       # login/logout via localStorage + cookie
│   └── proxy.ts                      # 🗑️ ARQUIVO ÓRFÃO — nunca registrado como middleware
│
├── middleware.ts                     # ❌ NÃO EXISTE (proteção de rota via proxy.ts não efetiva)
└── docs/                             # diretório para documentação
```

### CI/CD (`.github/workflows/`)

| Workflow | Trigger | Jobs que tocam o frontend |
|----------|---------|---------------------------|
| `ci.yaml` | PR para `main`/`develop` + push em `feature/**` | `lint` (continue-on-error), `build`, `integration` (Playwright) |
| `deploy-aws.yml` | Disparado pelo CI após sucesso em `develop`/`main` | Build Docker + copia para EC2 |

> **Nota:** O deploy usa `docker build ./alexandria-frontend` com o Dockerfile existente. O build gera `.next/standalone/`, a imagem copia `.next/` e `public/` para produção. Nenhuma stage de build do CI depende de `dist/` — o artifact upload `alexandria-frontend/dist/` no CI é legacy e não afeta o deploy.

---

## 2. O que será alterado

### Arquivos a **CRIAR** (3)

| Arquivo | Finalidade |
|---------|------------|
| `public/manifest.json` | Metadados do PWA (nome, ícones, tema, `display: standalone`) |
| `public/icon-192x192.png` | Ícone 192×192 para instalação |
| `public/icon-512x512.png` | Ícone 512×512 para splash screen |

### Arquivos a **MODIFICAR** (2)

| Arquivo | O que muda |
|---------|------------|
| `src/app/layout.tsx` | Adicionar `manifest: "/manifest.json"` no objeto `metadata` |
| `next.config.ts` | Adicionar `@serwist/next` como wrapper (ou configurar manualmente) |

### Dependência a **INSTALAR**

| Pacote | Versão alvo | Motivo |
|--------|-------------|--------|
| `@serwist/next` | `latest` (9.5.11) + `serwist` | Geração do service worker e estratégias de cache |

> **⚠️ Se `@serwist/next` for incompatível com Next.js 16.2.4:** Migrar para abordagem manual com `public/sw.js` e `ServiceWorkerRegister.tsx` (detalhado na [seção 4.3 — alternativa manual](#alternativa-manual-sem-serwist)).

### Arquivos que **NÃO** serão alterados

| Arquivo | Motivo |
|---------|--------|
| `Dockerfile` | Já copia `public/` inteiro — SW estará lá |
| `src/lib/epub-cache.ts` | Cache de EPUB via IndexedDB — não conflita com SW |
| `src/hooks/useEpub.ts` | Continua usando IndexedDB — SW ignora rota `/api/epub` |
| `src/lib/api.ts` | Chamadas autenticadas não serão cacheadas |
| `src/contexts/AuthContext.tsx` | Não precisa de alterações |
| Nenhum arquivo de backend | PWA é só frontend |
| Nenhum `.github/workflows/*` | CI/CD não precisa de alterações |

---

## 3. Estratégia de Cache

### Matriz de rotas

| Grupo | Rotas | Handler | Justificativa |
|-------|-------|---------|---------------|
| 📄 **Páginas (HTML)** | `/explorar`, `/biblioteca`, `/dashboard`, `/leitor`, `/leitor/[id]`, `/configuracoes`, `/emprestimos`, `/suporte` | `NetworkFirst` | Tenta rede; fallback para cache se offline |
| 🎨 **Assets estáticos** | `*.js`, `*.css`, `*.woff2`, `*.ttf`, `*.otf`, `*.png`, `*.svg`, `*.ico` | `CacheFirst` | Imutáveis no build |
| 📚 **API pública** | `GET /books`, `GET /books/search`, `GET /books/{id}` | `NetworkFirst` | Dados mudam, mas fallback é melhor que nada |
| 🔒 **API autenticada** | `/user-books`, `/profile/*`, `/auth/*` | `NetworkOnly` | **Nunca cachear** — dados de usuário |
| 📖 **Proxy EPUB** | `/api/epub?url=...` | `NetworkOnly` | Cache já gerenciado pelo `epub-cache.ts` (IndexedDB) |
| ⚙️ **Jobs admin** | `/api/jobs/*` | `NetworkOnly` | Ação administrativa |

### Páginas que funcionarão offline

- **`/explorar`** — listagem pública de livros (via cache network-first)

### Páginas que NÃO funcionarão offline

- **`/biblioteca`**, **`/dashboard`** — dependem de dados autenticados (`/user-books`)
- **`/leitor/[id]`** — carrega página HTML do cache, mas EPUB depende de IndexedDB (já funciona offline)
- **`/configuracoes`**, **`/emprestimos`** — dados de usuário
- **`/login`**, **`/registrar`** — exigem rede

> **📌 Leitor de EPUB já funciona offline** graças ao cache IndexedDB implementado em `epub-cache.ts`. O SW não interfere com essa funcionalidade.

---

## 4. Fases de Implementação

### 4.1. Fase 0 — Prova de Compatibilidade ⏱️ 30 min

**Objetivo:** Validar se `@serwist/next` funciona com Next.js 16.2.4 e `output: "standalone"`.

**Passos:**

```bash
cd alexandria-frontend
npm install @serwist/next serwist
```

1. Adicionar o wrapper no `next.config.ts` (rascunho para testar)
2. Rodar `npm run build`
3. Verificar se o service worker foi gerado em `public/sw.js`
4. Verificar se `npm run lint` continua passando

**Critério de sucesso:** Build finaliza sem erros, `public/sw.js` existe.

**Se falhar:** Migrar para [abordagem manual](#alternativa-manual-sem-serwist).

---

### 4.2. Fase 1 — Manifest + Ícones ⏱️ 20 min

#### `public/manifest.json`

```json
{
  "name": "Alexandria — Biblioteca Digital",
  "short_name": "Alexandria",
  "description": "Sua biblioteca digital pessoal",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#FBF8F4",
  "theme_color": "#3A2921",
  "icons": [
    { "src": "/icon-192x192.png", "sizes": "192x192", "type": "image/png" },
    { "src": "/icon-512x512.png", "sizes": "512x512", "type": "image/png" }
  ]
}
```

> **cores:** `background_color`: `#FBF8F4` (cream), `theme_color`: `#3A2921` (brown) — extraídas do Tailwind config / `globals.css`.

#### Ícones PNG

Gerar com ferramenta online (pwa-asset-generator, favicon.io ou Illustrator):

- `public/icon-192x192.png` — 192×192, formato PNG
- `public/icon-512x512.png` — 512×512, formato PNG

#### `src/app/layout.tsx` — modificação

Adicionar 1 linha no objeto `metadata`:

```tsx
export const metadata: Metadata = {
  title: "Alexandria — Biblioteca Digital",
  description: "Sua biblioteca digital pessoal",
  manifest: "/manifest.json",             // ← NOVA LINHA
};
```

**Validação:** `npm run build` passa, `npm run lint` passa.

---

### 4.3. Fase 2 — Service Worker ⏱️ 1-2h

#### Abordagem principal: `@serwist/next`

#### `next.config.ts` — modificação

```ts
import type { NextConfig } from "next";
import withSerwist from "@serwist/next";

const serwistConfig = withSerwist({
  swSrc: "src/sw.ts",
  swDest: "public/sw.js",
});

const nextConfig: NextConfig = {
  output: "standalone",
};

export default serwistConfig(nextConfig);
```

#### `src/sw.ts` — CRIAR

```ts
import { defaultCache } from "@serwist/next/worker";
import { Serwist } from "serwist";

const serwist = new Serwist({
  precacheEntries: self.__SW_MANIFEST,
  skipWaiting: true,
  clientsClaim: true,
  navigationPreload: true,
  runtimeCaching: [
    // 1. Assets estáticos: cache-first (imutáveis)
    {
      urlPattern: /\.(?:js|css|woff2?|ttf|otf|png|svg|ico)$/,
      handler: "CacheFirst",
      options: { cacheName: "static-assets" },
    },
    // 2. API pública de livros: network-first
    //    Pattern captura GET /books, /books/search, /books/{id}
    {
      urlPattern: /\/books(?:\/|\?|$)/,
      handler: "NetworkFirst",
      options: { cacheName: "books-api" },
    },
    // 3. EPUB proxy: network-only (cache já é gerenciado pelo epub-cache.ts via IndexedDB)
    {
      urlPattern: /\/api\/epub/,
      handler: "NetworkOnly",
    },
    // 4. Rotas autenticadas: network-only
    {
      urlPattern: /\/(?:user-books|profile|auth)/,
      handler: "NetworkOnly",
    },
    // 5. Demais rotas (incluindo páginas HTML): network-first padrão do Serwist
    ...defaultCache,
  ],
});

serwist.addEventListeners();
```

**Detalhes importantes:**

| Config | Valor | Efeito |
|--------|-------|--------|
| `skipWaiting` | `true` | Atualiza SW imediatamente (sem esperar fechar abas) |
| `clientsClaim` | `true` | SW assume controle de páginas já abertas |
| `navigationPreload` | `true` | Paraleliza fetch do HTML enquanto SW inicializa |

---

#### Alternativa manual (sem Serwist)

Caso a Fase 0 aponte incompatibilidade, seguir esta rota:

#### `public/sw.js` — CRIAR (manual)

```js
const CACHE_VERSION = 'v1';
const STATIC_CACHE = `static-${CACHE_VERSION}`;
const BOOKS_CACHE = `books-api-${CACHE_VERSION}`;

self.addEventListener('install', (event) => {
  event.waitUntil(self.skipWaiting());
});

self.addEventListener('activate', (event) => {
  event.waitUntil(clients.claim());
});

self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // EPUB → network-only (cache já gerenciado pelo epub-cache.ts)
  if (url.pathname.startsWith('/api/epub')) {
    return;
  }

  // Assets estáticos → cache-first
  if (/\.(?:js|css|woff2?|ttf|otf|png|svg|ico)$/.test(url.pathname)) {
    event.respondWith(
      caches.match(request).then((cached) => cached ?? fetch(request))
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
        .catch(() => caches.match(request))
    );
    return;
  }

  // Rotas autenticadas → network-only (nunca cachear)
  if (/\/(?:user-books|profile|auth|jobs)\/?/.test(url.pathname)) {
    return;
  }

  // Páginas HTML → network-first
  event.respondWith(
    fetch(request).catch(() => caches.match(request))
  );
});
```

#### `src/components/ServiceWorkerRegister.tsx` — CRIAR

```tsx
"use client";

import { useEffect } from "react";

export function ServiceWorkerRegister() {
  useEffect(() => {
    if ("serviceWorker" in navigator) {
      navigator.serviceWorker.register("/sw.js");
    }
  }, []);

  return null;
}
```

#### `src/app/layout.tsx` — modificação adicional (apenas na abordagem manual)

Adicionar no JSX do `RootLayout`:

```tsx
import { ServiceWorkerRegister } from "@/components/ServiceWorkerRegister";

export default function RootLayout({ children }) {
  return (
    <html lang="pt-BR" ...>
      <body>
        <ServiceWorkerRegister />    {/* ← NOVA LINHA (apenas no manual) */}
        <GoogleProvider>
          <AuthProvider>{children}</AuthProvider>
        </GoogleProvider>
      </body>
    </html>
  );
}
```

---

### 4.4. Fase 3 — Validação ⏱️ 30 min

| Teste | Como testar | Esperado |
|-------|-------------|----------|
| Build | `npm run build` | ✅ Sucesso sem erros |
| Lint | `npm run lint` | ✅ Sem warnings novos |
| Manifest | Abrir `/manifest.json` no navegador | ✅ JSON válido |
| Lighthouse | Chrome DevTools → Lighthouse → PWA | ✅ Score > 90 |
| Instalação | Botão "Adicionar à tela inicial" | ✅ Aparece |
| Offline: explorar | DevTools → Network → Offline → navegar para `/explorar` | ✅ Carrega do cache |
| Offline: leitor EPUB | Offline → abrir livro já lido | ✅ Carrega do IndexedDB |
| Online: autenticado | Fazer login, acessar biblioteca | ✅ Funciona normalmente |
| Atualização SW | Deployar nova versão, recarregar | ✅ SW atualiza |

---

## 5. Checklist

- [ ] **Fase 0:** Instalar `@serwist/next serwist` e testar build
- [ ] **Fase 1:** Criar `public/manifest.json` com metadados do Alexandria
- [ ] **Fase 1:** Gerar e adicionar `public/icon-192x192.png` e `public/icon-512x512.png`
- [ ] **Fase 1:** Adicionar `manifest: "/manifest.json"` no `metadata` do `layout.tsx`
- [ ] **Fase 2:** Criar `src/sw.ts` com estratégias de cache (ou `public/sw.js` + `ServiceWorkerRegister.tsx` manual)
- [ ] **Fase 2:** Configurar `next.config.ts` com `@serwist/next` (ou registro manual)
- [ ] **Fase 3:** Rodar `npm run build` e validar
- [ ] **Fase 3:** Rodar `npm run lint` e validar
- [ ] **Fase 3:** Testar offline com DevTools
- [ ] **Fase 3:** Fazer push em branch `feature/*` e verificar CI verde

---

## 6. Riscos e Mitigações

| # | Risco | Probabilidade | Impacto | Mitigação |
|---|-------|:------------:|:--------:|-----------|
| 1 | `@serwist/next` incompatível com Next.js 16.2.4 | 🟡 Média | 🟡 Médio | Fase 0 detecta precocemente; alternativa manual documentada |
| 2 | Cache de API pública servir dados obsoletos | 🟡 Média | 🟢 Baixo | `NetworkFirst` sempre tenta rede primeiro |
| 3 | Double caching de EPUB (SW + IndexedDB) | 🟢 Baixa | 🟢 Baixo | SW explicitamente configurado como `NetworkOnly` para `/api/epub` |
| 4 | SW cachear rota autenticada e expor dados de outro usuário | 🟢 Baixa | 🔴 Alto | `NetworkOnly` explícito para `/user-books`, `/profile`, `/auth`, `/jobs` |
| 5 | Service Worker não atualizar em clientes existentes | 🟡 Média | 🟢 Baixo | `skipWaiting: true` + `clientsClaim: true` |
| 6 | Dockerfile não incluir SW | 🟢 Baixa | 🟡 Médio | Dockerfile copia `public/` inteiro — SW sempre incluído |

---

## 7. Perguntas em Aberto

| # | Pergunta | Impacto |
|---|----------|---------|
| 1 | O build do Dockerfile gera o SW corretamente dentro do container? O Dockerfile executa `npm run build`, então sim — mas **validar** que o `public/sw.js` aparece após o build. | 🟡 Confirmar |
| 2 | O CI faz upload de `alexandria-frontend/dist/` como artifact — essa pasta existe? | 🟢 Legacy, não afeta deploy. |

---

## Apêndice: Comparativo de Abordagens

| Aspecto | `@serwist/next` | Manual (`public/sw.js`) |
|---------|:---------------:|:-----------------------:|
| Esforço | ⭐ (pouco) | ⭐⭐⭐ (médio) |
| Geração de precache | Automática | Manual |
| Registro | Automático | Componente React |
| Tipagem | TypeScript (`src/sw.ts`) | JavaScript puro |
| Estratégias de cache | API declarativa | Event listeners manuais |
| Risco de incompatibilidade | 🟡 Médio | 🟢 Baixo (só JS) |
| Manutenção futura | 🟢 Fácil (atualiza versão) | 🟡 Média (código próprio) |

> **Recomendação:** Tentar `@serwist/next` primeiro (Fase 0). Se falhar, a alternativa manual está pronta para uso sem perder tempo.

---

*Documento gerado em 2026-06-19.*
