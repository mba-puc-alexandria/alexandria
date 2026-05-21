# Correções SonarCloud — Frontend Alexandria

## docker:S6470 — COPY recursivo no Dockerfile
**Arquivo:** `Dockerfile`  
**Problema:** `COPY . .` copiava tudo recursivamente, podendo incluir arquivos sensíveis.  
**Correção:** Substituído por cópias explícitas de `src/`, `public/` e arquivos de config. Adicionado `.dockerignore` excluindo `.env*.local`, `node_modules`, `.next` e `.git`.

---

## docker:S6471 — Container rodando como root
**Arquivo:** `Dockerfile`  
**Problema:** Imagem `node:20-alpine` usa root por padrão.  
**Correção:** Adicionado `USER node` na stage runner, com `--chown=node:node` nos `COPY`.

---

## docker:S6504 — Permissão de escrita nos arquivos copiados
**Arquivo:** `Dockerfile`  
**Problema:** Arquivos copiados para a imagem tinham permissão de escrita.  
**Correção:** Adicionado `--chmod=555` nos `COPY` da stage runner (leitura + execução, sem escrita).

---

## typescript:S3358 — Ternários aninhados
**Arquivos:** `biblioteca/page.tsx`, `dashboard/page.tsx`, `explorar/page.tsx`, `explorar/[id]/page.tsx`, `components/BookCard.tsx`  
**Problema:** Expressões `a ? b : c ? d : e` aninhadas dificultam leitura.  
**Correção:** Extraídos para funções independentes (`renderContent`, `renderReadingBooks`, `renderDesktopBooks`, `getAddIcon`, `getAddButtonLabel`, `getStateIcon`, `getBadgeLabel`).

---

## typescript:S6479 — Índice de array como `key` no JSX
**Arquivos:** `biblioteca/page.tsx`, `emprestimos/page.tsx`  
**Problema:** `key={i}` em listas pode causar bugs de reconciliação no React.  
**Correção:** Substituído por chaves estáveis (`SKELETON_KEYS` com strings fixas; `loan.title` nos empréstimos).

---

## typescript:S2004 — Funções aninhadas mais de 4 níveis
**Arquivo:** `leitor/[id]/page.tsx`  
**Problema:** Callback dentro de `setTimeout` dentro de `relocated` dentro de `handleRendition` ultrapassava 4 níveis.  
**Correção:** Extraído para a função `persistProgress` no nível do componente.

---

## typescript:S6582 — Optional chaining
**Arquivo:** `leitor/[id]/page.tsx`  
**Problema:** `!book || !book.downloadUrl` verboso.  
**Correção:** Simplificado para `!book?.downloadUrl`.

---

## typescript:S6481 — Context Provider recriando objeto a cada render
**Arquivo:** `contexts/AuthContext.tsx`  
**Problema:** O objeto `{ user, login, logout, isLoading }` era recriado em todo render, causando re-renders desnecessários nos consumidores.  
**Correção:** Envolvido em `useMemo` com dependências `[user, isLoading]`.

---

## typescript:S7776 — Array usado apenas para verificação de existência
**Arquivo:** `proxy.ts`  
**Problema:** `PUBLIC_PATHS` era um `Array` usado só com `.includes()`.  
**Correção:** Convertido para `Set` com `.has()`, mais eficiente para lookups.

---

## jsx-a11y — Label sem controle associado
**Arquivos:** `(auth)/login/page.tsx`, `(auth)/registrar/page.tsx`, `(auth)/layout.tsx`  
**Problema:** `<label>` sem `htmlFor` não estava associado ao `<input>` correspondente.  
**Correção:** Adicionado `htmlFor` em todos os labels e `id` correspondente em cada input. Props do layout marcadas como `Readonly<>`.
