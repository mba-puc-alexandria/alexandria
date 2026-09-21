# Estado atual — Alexandria + payment-api

Documento de baseline e auditoria estática da integração Alexandria + payment-api.

> Estado verificado em 20/09/2026 nos commits `262a36d` (Alexandria) e `6b3b725`
> (payment-api). Esta auditoria não substitui um teste de ponta a ponta com Mercado Pago.

> Vinculado a: [spec.md](./spec.md) · [plan.md](./plan.md) · [tasks.md](./tasks.md)

---

## 1. Alexandria (monolito atual)

### 1.1 Backend (`alexandria-backend`)

- **Stack**: Java 17, Spring Boot 3.4.4, arquitetura hexagonal (ports & adapters), MySQL, JPA.
- **Banco de dados**: `spring.jpa.hibernate.ddl-auto=validate` e Flyway ativo, com
  `baseline-on-migrate=true`, `baseline-version=2` e migration
  `V3__create_subscriptions_table.sql`.
- **Autenticação**:
  - `JwtTokenProvider.generateToken(userId, username, role)` gera token com:
    - `subject = username`
    - claims: `userId` (Long) e `role`
  - `JwtAuthenticationFilter` monta o principal como `AuthenticatedUser(userId, username)`.
- **Registro de usuário**:
  - `RegisterUserUseCase` cria o `User` e inicia o trial.
  - `GoogleAuthUseCase` faz o mesmo apenas na primeira autenticação Google.
  - Há domínio `Subscription`, com `TRIALING`, `ACTIVE`, `PAST_DUE`, `EXPIRED` e
    `CANCELED`; o plano padrão é R$ 10,00 por 30 dias, com trial de 15 dias.
- **Livros e leitura**:
  - `GET /books`, `GET /books/{id}` e `GET /books/search` são **públicos**.
  - `GET /books/{id}/epub` exige JWT e assinatura válida; retorna a URL de download
    somente após esse gate.
- **Biblioteca do usuário**:
  - `user-books` autenticado, com status (`reading`/`done`/`toread`), progresso e rating.

### 1.2 Frontend (`alexandria-frontend`)

- **Stack**: Next.js 16, React 19, TypeScript, Tailwind CSS.
- **Sessão**:
  - `AuthContext` persiste o token em `localStorage['auth-token']` e no cookie `auth-token`.
  - `apiFetch` envia `Authorization: Bearer` nas chamadas JSON.
- **Leitor** (`/leitor/[id]`):
  - `useEpub` chama `/api/epub?bookId=<id>`.
  - `app/api/epub/route.ts` envia o JWT do cookie ao backend e converte `402/403`
    em `402` para o paywall.
  - `/leitor` e `/checkout` estão em `PRIVATE_PATHS`; há `PaywallModal`, checkout,
    página de planos e `MercadoPagoCardForm` baseado no SDK do Mercado Pago.
- **Proteção de rotas** (`proxy.ts`):
  - Protege `/biblioteca`, `/dashboard`, `/configuracoes`, `/leitor` e `/checkout`.
  - As páginas públicas continuam sendo explorar, detalhes e planos.

### 1.3 Infra/CI/CD

- `docker-compose.yml` (raiz): PostgreSQL + `payment-api` + MySQL + backend + frontend.
  O contexto `../deliveryAPI/payment-api` existe e corresponde ao checkout local atual do
  payment-api.
- `backups-wf/`: contém `docker-compose.aws.yml` e `docker-compose.ci.yml` (backups de workflows antigos).
- `.github/workflows/`:
  - `ci.yml`: build/teste do backend, lint/test/build do frontend e build Docker de backend/frontend.
  - `main.yml`: scan de imagem (Hadolint/Trivy/Cosign) e push para ECR.
  - `cd.yml`: **vazio** (apenas dispara ao final do CI).
- ⚠️ **`iac/`** é Terraform de outro projeto (`linuxtips-sorteador`): FARGATE, serviço único,
  data sources SSM `/linuxtips/*`. Não reutilizar; será substituída por `infra/terraform`.
- ⚠️ **`infraestrutura/`** contém apenas 2 arquivos `.tf` soltos (`nat-gateway.tf`, `public-subnet.tf`).
- `Kubernetes/`: manifestos (backend/frontend/mysql/redis) fora do escopo atual.
- **Deploy**: constrói imagens backend/frontend, copia para EC2 e sobe containers manualmente.

---

## 2. payment-api (Gohan Food)

### 2.1 Estrutura

- **Stack**: Java 17, Spring Boot 3.4.4, arquitetura hexagonal, **PostgreSQL**, Flyway,
  Mercado Pago SDK 2.8.0, Bucket4j e Kafka opcional.

### 2.2 Endpoints (`/api/v1/payments`)

| Método | Rota | Função |
|---|---|---|
| POST | `/` | Criar pagamento PIX ou cartão |
| POST | `/{id}/process` | Reprocessar pagamento PENDING |
| GET | `/{id}` | Buscar por ID |
| GET | `/` | Listar por usuário |
| POST | `/refund` | Reembolsar |
| POST | `/webhook` | Receber IPN do Mercado Pago |

Também existem `POST /api/v1/customers`, `POST /api/v1/customers/{customerId}/cards`
e `DELETE /api/v1/customers/{customerId}/cards/{cardId}` para Customer + cartão salvo.

### 2.3 Contrato SaaS implementado

- `referenceId` é `String`; o Alexandria usa `subscription:{subscriptionId}` para
  checkout imediato e adiciona `:period:{fimDoCiclo}` nas cobranças recorrentes.
- Há restrição única `(user_id, reference_id)`, que dá idempotência no payment-api.
- O request aceita `gatewayToken` para cartão novo, ou `customerId` + `cardId` para
  recorrência. As respostas expõem `id`, `status`, `mpPaymentId` e dados PIX.
- Customer + Card são criados/trocados no Mercado Pago; Alexandria persiste apenas os IDs.

### 2.4 JWT do payment-api

- `JwtAuthenticationFilter` prioriza a claim `userId`, com fallback para `sub` e `id`.
  Isso é compatível com o JWT emitido pelo Alexandria, desde que ambos usem o mesmo `JWT_SECRET`.

### 2.5 Mercado Pago

- `MercadoPagoGateway` aceita token `TEST-` em sandbox e `APP_USR-` quando
  `MERCADOPAGO_ENVIRONMENT=production`; rejeita o uso de token de produção em sandbox.
  Suporta PIX, cartão, cartão salvo, reembolso e consulta.

### 2.6 Webhook/status

- `PaymentStatusSyncService` recebe IPN, consulta o Mercado Pago e atualiza o pagamento.
  Para status final, o `SubscriptionPaymentCallbackClient` chama
  `POST /subscriptions/payment-webhook` com `X-Webhook-Secret`.
- Kafka é condicionado por `payment.kafka.enabled` (padrão `false`).

---

## 3. Gap analysis: reutilizar, adaptar e criar

### 3.1 Compatibilidade verificada

- O cliente Alexandria e os endpoints do payment-api coincidem para pagamentos,
  criação/troca de cartão, JWT e callback.
- Os nomes JSON relevantes também coincidem: `referenceId`, `gatewayToken`, `cardId`,
  `customerId`, `mpPaymentId`, `qrCode`, `qrCodeBase64` e `ticketUrl`.

### 3.2 Integração ainda não comprovada em execução

- Não há teste de contrato ou E2E que suba os dois serviços e cubra checkout, IPN e
  callback. Os testes recentes disponíveis no payment-api passam; o conjunto do backend
  Alexandria não pôde concluir neste ambiente por falha de auto-attach do Mockito/Byte Buddy.
- O callback é síncrono e só registra falha em log; não há mecanismo durável de retry/outbox.

### 3.3 Pontos pendentes no Alexandria backend

- O domínio, endpoints, gate EPUB, cliente HTTP e job de renovação já existem.
- A dunning não cobre o caso `PAST_DUE` sem cartão: após a primeira tentativa, o job não
  agenda novas falhas nem expira a assinatura. O `ExpireSubscriptionsUseCase` existe, mas
  não há job que o invoque.
- Um cartão recusado no checkout imediato pode retornar `FAILED` sem `mpPaymentId` do
  payment-api; `CheckoutUseCase` então chama `recordPendingPayment(null)`, que rejeita o
  valor. Esse caminho precisa de teste e correção antes de produção.
- `paymentWebhook` faz `secret.getBytes(...)` sem tratar header ausente; uma chamada sem
  `X-Webhook-Secret` pode resultar em erro 500, não 401.

### 3.4 Pontos pendentes no frontend

- As telas, `AuthContext`, proxy e `CardForm` já existem.
- `useEpub` lê primeiro o EPUB do cache local, sem revalidar assinatura. Assim, conteúdo
  baixado durante um período válido pode continuar acessível localmente após a expiração.

### 3.5 Precisa ajustar na Infra/CI/CD

- Adicionar `payment-api` + `postgres-payment` ao `docker-compose.yml`.
- Compartilhar `JWT_SECRET` entre backend e payment-api.
- Adicionar envs do Mercado Pago, `SUBSCRIPTION_CALLBACK_URL` e `SUBSCRIPTION_CALLBACK_SECRET`.
- Substituir `iac/` (template de outro projeto) por `infra/terraform` (dev/prod).
- Implementar o `cd.yml` (hoje vazio): push ECR + `terraform plan`/`apply`.
- Atualizar CI para construir/testar a imagem do payment-api.

---

## 4. Riscos e observações

1. O gate remoto do EPUB está no backend e o leitor é privado, mas o cache offline é um
   bypass do gate depois do primeiro download.
2. JWT e callback estão alinhados por código; a configuração efetiva deve manter
   `JWT_SECRET` e `SUBSCRIPTION_CALLBACK_SECRET` iguais em ambos os serviços.
3. PIX é assíncrono; cartão pode finalizar na resposta do checkout ou via callback.
4. Antes de produção, corrigir o compose, cobrir o fluxo ponta a ponta e tratar as três
   falhas de robustez registradas nas seções 3.2--3.4.
