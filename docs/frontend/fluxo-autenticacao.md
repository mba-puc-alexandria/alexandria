# Autenticação — Integração com o Backend

## Contexto

O backend já estava deployado na AWS EC2 com endpoints de autenticação via JWT.  
O frontend não tinha nenhuma lógica de autenticação. Este step implementa login, registro e logout completos.

---

## Arquivos criados

### `src/lib/api.ts`
Cliente HTTP centralizado.

- Lê a URL base do backend via `NEXT_PUBLIC_API_URL` (`.env.local`)
- Exporta `login(data)` → `POST /auth/login` → retorna `{ token, userId, username }`
- Exporta `register(data)` → `POST /auth/register` → retorna `{ userId, username, email }`
- Exporta `getAuthHeaders()` → lê o token do `localStorage` e retorna o header `Authorization: Bearer <token>`
- Exporta `apiFetch(path, init)` → wrapper para chamadas autenticadas (usado nas próximas features)

### `src/contexts/AuthContext.tsx`
Contexto React de autenticação (`'use client'`).

- Provê `user`, `login()`, `logout()` e `isLoading` para toda a árvore de componentes
- No login: salva o token em `localStorage` + cookie `auth-token` (necessário para o proxy)
- No logout: limpa `localStorage`, cookie e redireciona para `/login`
- Inicializa lendo o `localStorage` para manter sessão após refresh

### `src/proxy.ts`
Proteção de rotas (substitui o `middleware.ts`, depreciado no Next.js 16).

- Rotas públicas: `/login`, `/registrar`
- Qualquer outra rota sem cookie `auth-token` → redireciona para `/login`
- Se já autenticado e tentar acessar `/login` ou `/registrar` → redireciona para `/explorar`

### `src/app/(auth)/layout.tsx`
Layout para as telas de autenticação (sem sidebar, sem header).  
Centraliza o conteúdo na tela inteira.

### `src/app/(auth)/login/page.tsx`
Tela de login.

- Campos: usuário e senha
- Chama `login()` do `AuthContext`
- Exibe mensagem de sucesso quando vem da tela de registro (`?registered=1`)
- Link para `/registrar`

### `src/app/(auth)/registrar/page.tsx`
Tela de cadastro.

- Campos: nome, sobrenome, usuário, e-mail, senha e confirmação de senha
- Validação client-side de senha (mínimo 8 caracteres, confirmação igual)
- Chama `register()` da `api.ts` diretamente (não precisa logar automaticamente)
- Após sucesso redireciona para `/login?registered=1`
- Link para `/login`

### `.env.local`
Variável de ambiente com a URL do backend:
```
NEXT_PUBLIC_API_URL=https://api.bibliotecaalexandria.com.br
```

---

## Arquivos modificados

### `src/app/layout.tsx`
Adicionado o `<AuthProvider>` envolvendo todo o `{children}` para que o contexto de autenticação esteja disponível em todas as páginas.

### `src/components/Sidebar.tsx`
Adicionado ao rodapé da sidebar (desktop):
- Nome do usuário logado
- Botão **Sair** com ícone `LogOut` (lucide-react) que chama `logout()` do `AuthContext`

### `src/components/BottomNav.tsx`
Adicionado botão **Sair** na barra de navegação inferior (mobile), ao lado dos outros ícones.

---

## Endpoints do backend utilizados

| Método | Rota | Body | Retorno |
|--------|------|------|---------|
| `POST` | `/auth/register` | `{ username, firstName, lastName, email, password }` | `{ userId, username, email }` |
| `POST` | `/auth/login` | `{ username, password }` | `{ token, userId, username }` |

---

## Fluxo completo

```
Usuário acessa qualquer rota
        ↓
proxy.ts verifica cookie auth-token
        ↓
  Sem token → /login
  Com token → acessa a rota normalmente

/registrar → preenche dados → POST /auth/register
           → sucesso → /login?registered=1

/login → preenche usuário+senha → POST /auth/login
       → salva token (localStorage + cookie)
       → redireciona para /explorar

Sidebar/BottomNav → clica Sair
       → limpa localStorage + cookie
       → redireciona para /login
```

---

## Observação importante — CORS

O backend (`CorsConfig.java`) lê as origens permitidas da variável de ambiente `CORS_ALLOWED_ORIGINS`.

**Produção (workflow `deploy-aws.yml`):**
```
CORS_ALLOWED_ORIGINS=https://bibliotecaalexandria.com.br,https://api.bibliotecaalexandria.com.br,http://localhost:3000
```

**Desenvolvimento local (`docker-compose.aws.yml`):**
```
CORS_ALLOWED_ORIGINS: https://bibliotecaalexandria.com.br,https://api.bibliotecaalexandria.com.br
```

> 💡 Como o frontend (`bibliotecaalexandria.com.br`) e a API (`api.bibliotecaalexandria.com.br`) são subdomínios diferentes, o CORS é necessário. O Cloudflare cuida do SSL/HTTPS nas duas pontas.
