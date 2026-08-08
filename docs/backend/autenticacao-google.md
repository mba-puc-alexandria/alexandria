# Autenticação com Google OAuth 2.0

## Visão Geral

O Alexandria suporta login via conta Google. O fluxo utiliza **Google Identity Services** no frontend e valida o token no backend chamando a API pública do Google — sem nenhuma biblioteca de terceiros adicional.

```
Usuário clica "Entrar com Google"
  → Google Identity Services gera um id_token (JWT)
  → Frontend envia o id_token para POST /auth/google
  → Backend valida o token na API do Google (tokeninfo)
  → Backend busca ou cria o usuário no banco
  → Retorna JWT próprio da aplicação
```

---

## Frontend

### Dependência

```bash
npm install @react-oauth/google
```

### Variável de ambiente

**`alexandria-frontend/.env.local`**
```
NEXT_PUBLIC_GOOGLE_CLIENT_ID=<client_id_do_google_cloud_console>
```

### Arquivos modificados/criados

| Arquivo | Mudança |
|---------|---------|
| `src/components/GoogleProvider.tsx` | Novo — `GoogleOAuthProvider` wrapping global |
| `src/app/layout.tsx` | Envolveu `AuthProvider` com `GoogleProvider` |
| `src/lib/api.ts` | Nova função `loginWithGoogle(credential)` — `POST /auth/google` |
| `src/contexts/AuthContext.tsx` | Nova função `loginWithGoogle` no contexto; lógica de sessão extraída para `persistSession` |
| `src/components/LoginModal.tsx` | Botão `<GoogleLogin>` com callback `handleGoogleSuccess` |

### Fluxo no modal de login

```tsx
<GoogleLogin
  onSuccess={({ credential }) => handleGoogleSuccess(credential)}
  onError={() => toast.error("Falha no login com Google")}
/>
```

Após o login bem-sucedido, o comportamento é idêntico ao login por e-mail/senha: token JWT salvo no `localStorage`, usuário persistido no contexto.

---

## Backend

### Sem novas dependências

A validação do token usa `RestTemplate` (já presente no projeto) para chamar a API pública do Google. O endpoint `/tokeninfo` retorna as claims do token sem exigir nenhuma dependência extra.

### Propriedade de configuração

**`src/main/resources/application.properties`**
```properties
google.client-id=${GOOGLE_CLIENT_ID:<client_id_padrao>}
```

**`src/test/resources/application.properties`**
```properties
google.client-id=test-google-client-id
```

### Arquivos criados

#### `application/auth/GoogleAuthUseCase.java`

Responsável por validar o token e retornar ou criar o usuário.

**Fluxo:**
1. Chama `GET https://oauth2.googleapis.com/tokeninfo?id_token=<token>`
2. Verifica que o campo `aud` do payload bate com o `google.client-id` configurado
3. Extrai `email`, `given_name` e `family_name`
4. Se o usuário com esse e-mail já existe → retorna seus dados
5. Se não existe → cria com `username` derivado do e-mail e senha aleatória (BCrypt)

**Saída:**
```java
public record Output(Long userId, String username) {}
```

#### `adapter/in/rest/auth/dto/GoogleAuthRequest.java`

```java
public record GoogleAuthRequest(String credential) {}
```

### Arquivos modificados

| Arquivo | Mudança |
|---------|---------|
| `AuthController.java` | Novo endpoint `POST /auth/google` |
| `BeanConfiguration.java` | Bean `GoogleAuthUseCase` com injeção de `RestTemplate` e `googleClientId` |
| `UserRepository.java` | Novo método `findByEmail(String email)` |
| `UserJpaRepository.java` | Novo método `findByEmail(String email)` |
| `UserRepositoryImpl.java` | Implementação de `findByEmail` |

### Endpoint

```
POST /auth/google
Content-Type: application/json

{ "credential": "<id_token_do_google>" }
```

**Resposta de sucesso (200):**
```json
{
  "token": "<jwt_da_aplicacao>",
  "type": "Bearer",
  "userId": 42,
  "username": "joao_silva"
}
```

**Resposta de erro (401):** token inválido, expirado ou de outra aplicação.

---

## Configuração no Google Cloud Console

1. Acesse [console.cloud.google.com](https://console.cloud.google.com)
2. Crie ou selecione um projeto
3. Vá em **APIs & Serviços → Credenciais**
4. Crie uma credencial do tipo **ID do cliente OAuth 2.0** → Aplicativo da Web
5. Adicione as origens autorizadas:
   - `http://localhost:3000` (desenvolvimento)
   - `https://bibliotecaalexandria.com.br` (produção)
6. Copie o **Client ID** e configure nas variáveis de ambiente do frontend e backend

O serviço é **gratuito** — o Google não cobra pelo uso do Google Identity Services.

---

## Testes

O `GoogleAuthUseCase` é mockado nos testes de integração que carregam o contexto completo do Spring:

```java
@MockitoBean
private GoogleAuthUseCase googleAuthUseCase;
```

A propriedade `google.client-id=test-google-client-id` em `src/test/resources/application.properties` garante que o bean real também pode ser criado sem falha caso não seja mockado.
