# Plano técnico — SaaS Alexandria (payment-api como microsserviço)

> Documento **refinado** com base no código atual do repositório.
> Vinculado a: [spec.md](./spec.md) · [current-state.md](./current-state.md) · [tasks.md](./tasks.md)

## 0. Pré-requisitos e bloqueios

Antes de começar a implementação, resolver os seguintes pontos encontrados no código:

1. **Conflito de merge commitado em `application.properties`** — o arquivo
   `alexandria-backend/src/main/resources/application.properties` contém marcadores
   `<<<<<<< HEAD`, `=======` e `>>>>>>> feature/infraestrutura` **commitados**.
   O conteúdo correto (lado `HEAD`) já é o do Alexandria. Corrigir o arquivo antes de
   adicionar as novas chaves de assinatura.
2. **Duas classes `@SpringBootApplication`** — `com.pucsp.alexandria.AlexandriaApplication`
   (ativa, com `@EnableAsync`) e `com.alexandria.alexandria_backend.AlexandriaBackendApplication`
   (legada). Definir `AlexandriaApplication` como única entrypoint e remover a legada.
3. **`iac/` é template de outro projeto** — a pasta `iac/` contém Terraform do projeto
   "linuxtips-sorteador" (FARGATE, serviço único, paths SSM `/linuxtips/*`). Não reutilizar
   como está; será substituída pela estrutura `infra/terraform` da seção 4.
   `infraestrutura/` contém apenas 2 arquivos `.tf` soltos; consolidar/remover.
4. **`payment-api` está em outro repositório** (Gohan Food). As mudanças da seção 1
   acontecem lá e precisam ser coordenadas separadamente.

## Decisão de arquitetura

Manter o payment-api como microsserviço separado, não embutir no monolito.

```
Frontend (Next.js)
   │ JWT (cookie auth-token)
   ▼
Alexandria backend (Spring Boot + MySQL)
   │ JWT (mesma secret)        ▲ callback HTTP (X-Webhook-Secret)
   ▼                           │
payment-api (Spring Boot + PostgreSQL)  ← repo separado
   │
   ▼
Mercado Pago (PIX + Cartão)
```

- **Alexandria → payment-api**: `RestClient` repassando o `Authorization: Bearer` do usuário logado.
- **payment-api → Alexandria**: callback `POST /subscriptions/payment-webhook` autenticado
  por header `X-Webhook-Secret` (secret compartilhada `SUBSCRIPTION_CALLBACK_SECRET`),
  mais simples que assinar JWT de serviço e suficiente para server-to-server.

## 1. payment-api (adaptações — repo separado)

> **Detalhes verificados no código** (`/Users/talitaalves/IdeaProjects/payment-api`):
> pacote `com.delivery.payment`, Spring Boot 3, PostgreSQL + Flyway, SDK Mercado Pago 2.x,
> Kafka, Bucket4j. Endpoints em `/api/v1/payments`: `POST /`, `POST /{id}/process`,
> `GET /{id}`, `GET /`, `POST /refund`, `POST /webhook` (IPN).
> `user_id` é `VARCHAR` (migration `V3`), `order_id` ainda é `UUID` (migration `V1`).

- Generalizar `orderId` (UUID) para `referenceId` (String) em request, domain, entity,
  mapper, services e migrations.
  - Onde aparece hoje: `CreatePaymentRequest`, `Payment`, `PaymentEntity`, `PaymentResponse`,
    `CreatePaymentService`, `PaymentRepository.findByOrderId(UUID)`,
    `KafkaPaymentProducer` (mensagem `{"paymentId","orderId"}`), migrations `V1`/`V2`.
  - Convenção usada pelo Alexandria: `referenceId = "subscription:{subscriptionId}"`.
  - Descrição hoje é gerada como `"Pedido #" + orderId` (PIX) e `"Pedido " + orderId` (cartão);
    trocar por `"Assinatura " + referenceId`.
- `JwtAuthenticationFilter`: aceitar claim `userId` do token do Alexandria
  (hoje lê `sub` ou claim `id`). Alexandria gera `sub=username` e `userId=Long`.
  - Como o controller usa `getCurrentUserId()` → `auth.getName()` como `userId` (String),
    o filtro deve popular o principal com `claims.get("userId").toString()`.
- `MercadoPagoProperties`/`MercadoPagoGateway`: permitir `MERCADOPAGO_ENVIRONMENT=production`
  e token `APP_USR-` (hoje `init()` lança `IllegalStateException` quando `isProductionToken()`).
  - Já existe a propriedade `mercadopago.environment`; falta deixar o `init()` aceitar produção
    quando o ambiente for `production`.
- Tornar Kafka opcional (feature flag) e adicionar callback HTTP:
  - Hoje o Kafka é sempre ativo fora do profile `test` (`KafkaConfig` com `@Profile("!test")`).
  - Adicionar `@ConditionalOnProperty`/flag para desligar em produção se não houver broker.
  - `SUBSCRIPTION_CALLBACK_URL` (ex.: `http://alexandria-backend:8080/subscriptions/payment-webhook`).
  - `SUBSCRIPTION_CALLBACK_SECRET` para o header `X-Webhook-Secret`.
  - Disparar em `payment.completed` e `payment.refunded` (mesmo lugar onde hoje chama
    `KafkaPaymentProducer.publishPaymentCompleted/Refunded`, no `PaymentStatusSyncService`).
- Contrato do callback (idempotente):
  ```json
  {
    "referenceId": "subscription:123",
    "paymentId": "<id payment-api ou gatewayTransactionId>",
    "status": "COMPLETED" | "REFUNDED",
    "paymentMethod": "PIX" | "CARD",
    "amount": 10.00,
    "occurredAt": "2026-08-30T12:00:00Z"
  }
  ```
- Manter PostgreSQL próprio do payment-api.

## 2. Alexandria backend — domínio de assinatura

### 2.1 Domínio (arquitetura hexagonal, seguir padrão existente)

Novos pacotes no layout atual (`com.pucsp.alexandria`):

```
domain/subscription/         Subscription, SubscriptionId, SubscriptionStatus, SubscriptionRepository
application/subscription/    use cases + DTOs
adapter/in/rest/subscription/ SubscriptionController + DTOs
adapter/out/persistence/     SubscriptionEntity, SubscriptionJpaRepository, SubscriptionRepositoryImpl, mapper
```

- Nova entidade `Subscription`:
  - `id`, `userId` (unique), `status`, `trialEndsAt`, `currentPeriodEndsAt`,
    `mpPaymentId`, `createdAt`, `updatedAt`.
  - Status: `TRIALING`, `ACTIVE`, `PAST_DUE`, `EXPIRED`, `CANCELED`.
- Transições:
  - cadastro/Google → `TRIALING` (`trialEndsAt = now + 15d`).
  - **Durante o trial**: somente cartão de crédito. Registra a intenção de pagamento e agenda
    a cobrança para o fim do trial (não cobra agora). Ao processar, `currentPeriodEndsAt = now + 30d`.
  - **Após o trial**: PIX ou cartão processam imediatamente → `ACTIVE`
    (`currentPeriodEndsAt = now + 30d`, `trialEndsAt = null`).
  - **Renovações seguintes** → `currentPeriodEndsAt = now + 30d`.
  - job diário → `TRIALING` vencido **com** pagamento agendado/confirmado → `ACTIVE`;
    `TRIALING` vencido **sem** pagamento → `EXPIRED`; `ACTIVE` vencido → `PAST_DUE`.
  - cancelamento → `CANCELED` (preserva `currentPeriodEndsAt` até o fim do período).

### 2.2 Config (`application.properties`)

```properties
subscription.trial-days=15
subscription.price=${SUBSCRIPTION_PRICE:10.00}
subscription.period-days=30
subscription.currency=BRL
payment-api.url=${PAYMENT_API_URL:http://localhost:8082}
subscription.callback-secret=${SUBSCRIPTION_CALLBACK_SECRET:dev-callback-secret}
```

### 2.3 Trial nos DOIS pontos de criação de usuário

- `RegisterUserUseCase.execute(...)`: após `userRepository.save(user)`, criar `Subscription` `TRIALING`.
- `GoogleAuthUseCase.execute(...)`: no bloco `orElseGet` (criação de novo usuário Google), criar `Subscription` `TRIALING`.
  - Login de usuário já existente **não** recria trial.
- Como hoje não há migration no Alexandria (`ddl-auto=update`, `flyway.enabled=false`):
  - **Recomendação**: habilitar Flyway e criar `V2__create_subscription.sql` para a nova tabela,
    ou manter `ddl-auto=update` e deixar o Hibernate criar (decisão a registrar na implementação).

### 2.4 Cliente HTTP para payment-api

- `PaymentApiClient` (adapter out) usando `RestClient`/`RestTemplate` com
  `Authorization: Bearer <token do usuário>`.
- O `SubscriptionController` repassa o header `Authorization` da request ao use case de checkout.

### 2.5 Endpoints

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| GET | `/subscriptions/me` | JWT | Retorna status, trial/prazo e preço. |
| POST | `/subscriptions/checkout` | JWT | `{ "paymentMethod": "PIX" \| "CARD", "cardToken"? }` → chama payment-api. |
| POST | `/subscriptions/payment-webhook` | `X-Webhook-Secret` | Callback do payment-api. **Idempotente** por `mpPaymentId`. |
| POST | `/subscriptions/cancel` | JWT | Marca `CANCELED`, mantém acesso até `currentPeriodEndsAt`. |

Fluxo de checkout:

1. Frontend `POST /subscriptions/checkout { paymentMethod }` (Bearer).
2. Backend resolve a `Subscription`, monta `referenceId = "subscription:{id}"`, chama
   `payment-api POST /api/v1/payments` (PIX ou CARD com `cardToken`).
3. **Durante o trial**: somente `CARD`. O backend agenda a cobrança para o fim do trial
   (não processa agora). Ao fim do trial, o job processa e ativa `currentPeriodEndsAt = now + 30d`.
4. **Após o trial**: `PIX` ou `CARD`, ambos imediatos.
   - PIX: armazena `mpPaymentId`, devolve `qrCode`/`qrCodeBase64`; ao confirmar,
     `currentPeriodEndsAt = now + 30d`.
   - Cartão: se `APPROVED`, ativa `currentPeriodEndsAt = now + 30d`.
5. Mercado Pago IPN → payment-api → callback `payment-webhook` → Alexandria aplica a mesma
   regra de período conforme o momento (durante ou após o trial).

### 2.6 Endpoint de leitura (gate no backend)

- `GET /books/{id}/epub` (autenticado):
  - Só retorna o EPUB se a assinatura estiver `TRIALING` (dentro do prazo) ou `ACTIVE`.
  - Senão: `402 Payment Required` ou `403` com corpo `{ "code": "SUBSCRIPTION_REQUIRED" }`.
  - Implementação: `Book` já possui `downloadUrl` (Gutendex). O endpoint faz proxy do
    arquivo (streaming) usando esse `downloadUrl`, sem expor a URL no frontend.
- Adicionar rota explicitamente no `SecurityConfig` (já coberta por `anyRequest().authenticated()`,
  mas registrar por clareza). `POST /subscriptions/payment-webhook` deve ser `permitAll` +
  validação do `X-Webhook-Secret` dentro do controller/use case.
- Adicionar `@EnableScheduling` (hoje só existe `@EnableAsync`) para o job.

### 2.7 Job de expiração

- `@Scheduled(cron = "0 0 3 * * *")` (diário):
  - `TRIALING` com `trialEndsAt < now` **e** `currentPeriodEndsAt` futuro → `ACTIVE` (pagou durante o trial).
  - `TRIALING` com `trialEndsAt < now` **sem** pagamento → `EXPIRED`.
  - `ACTIVE` com `currentPeriodEndsAt < now` → `PAST_DUE` (ou `EXPIRED`).
  - `PAST_DUE` além de um grace period → `EXPIRED`.

## 3. Alexandria frontend

### 3.0 Matriz de acesso (gating)

A tela inicial continua sendo **Explorar**. As únicas telas públicas são **Explorar**
(`/explorar`) e **Detalhes do livro** (`/explorar/[id]`). Todo o resto exige login e,
no caso da leitura, assinatura válida.

| Tela/Rota | Sem login | Logado (sem assinatura) | Logado + assinatura ativa |
|---|---|---|---|
| `/explorar` (listagem) | ✅ liberado | ✅ | ✅ |
| `/explorar/[id]` (detalhes) | ✅ liberado | ✅ | ✅ |
| Adicionar à biblioteca | 🔒 abre login | ✅ | ✅ |
| `/leitor/[id]` (ler EPUB) | 🔒 redireciona p/ login | 🔒 paywall | ✅ |
| `/biblioteca`, `/dashboard`, `/configuracoes` | 🔒 redireciona p/ login | ✅ | ✅ |
| `/planos` | ✅ liberado | ✅ | ✅ |
| `/checkout` | 🔒 redireciona p/ login | ✅ | ✅ |

Regras implementadas:

- Botão **"Adicionar à Biblioteca"** e **"Ler agora"** na página de detalhes exigem login
  (abrem o modal de login quando não há usuário).
- `proxy.ts` protege as rotas privadas no servidor, impedindo acesso direto por URL.
- A leitura do EPUB é dupla: login no frontend + gate de assinatura no backend
  (`GET /books/{id}/epub`), nunca apenas no frontend.

### 3.1 Páginas

- `/planos` — apresenta o plano **Alexandria Premium** (R$ 10,00/mês, 15 dias grátis) e CTA.
- `/checkout` — método depende do estado da assinatura:
  - Durante o trial: **somente cartão de crédito** (cadastro/pré-autorização; cobra ao fim do teste).
  - Após o trial: **PIX** (destaque, pagamento imediato) e **cartão**.
- `NEXT_PUBLIC_MERCADOPAGO_PUBLIC_KEY` para instanciar o SDK no browser.

### 3.2 `proxy.ts` (middleware)

- `PRIVATE_PATHS` = `/biblioteca`, `/dashboard`, `/configuracoes`, `/leitor`, `/checkout`.
- Público: `/explorar`, `/explorar/[id]`, `/planos`, `/login`, `/registrar`.

### 3.3 `AuthContext`

- Expor `subscription` (status, `trialEndsAt`, `currentPeriodEndsAt`).
- Buscar `GET /subscriptions/me` após login/registro e no load inicial quando houver token.

### 3.4 `api.ts`

- Adicionar tipos e funções: `Subscription`, `getSubscription()`, `createCheckout(payload)`, `cancelSubscription()`.
- `getAuthHeaders()` já lê `localStorage['auth-token']`; manter para chamadas JSON do browser.

### 3.5 `/api/epub` como proxy autenticado

- Trocar a assinatura atual `?url=<downloadUrl>` por `?bookId=<id>` (ou `/api/epub/[bookId]`).
- `app/api/epub/route.ts`:
  - Ler o cookie `auth-token` (server-side, **não** tem acesso a `localStorage`).
  - Repassar `Authorization: Bearer <token>` para `GET {API_URL}/books/{bookId}/epub`.
  - Retornar os bytes EPUB (`application/epub+zip`) ou propagar `402/403`.
- Atualizar `useEpub.ts` para chamar `/api/epub?bookId=...` em vez de passar o `downloadUrl`.

## 4. Infra/Deploy — Terraform + AWS ECS

A infraestrutura passa a viver em `infra/terraform/` versionada, provisionada com Terraform.
Alexandria e payment-api sobem como **dois serviços ECS separados**, cada um com seu banco (RDS).

### 4.1 Limpeza da infra existente

- Remover/substituir `iac/` (template `linuxtips-sorteador`).
- Consolidar ou remover `infraestrutura/` (2 arquivos soltos).
- `Kubernetes/`: fora do escopo desta fase; manter separado até decisão futura (Terraform/ECS é o alvo).

### 4.2 Estrutura da pasta `infra/`

```
infra/
├── terraform/
│   ├── environments/
│   │   ├── dev/
│   │   └── prod/
│   ├── modules/
│   │   ├── network/
│   │   ├── ecs-cluster/
│   │   ├── service-alexandria/   # task backend (8080) + frontend (3000)
│   │   ├── service-payment-api/  # task payment-api (8082)
│   │   ├── rds-alexandria/       # Aurora MySQL
│   │   ├── rds-payment-api/      # RDS PostgreSQL
│   │   ├── alb/                  # público (HTTPS/ACM) + interno
│   │   ├── ecr/
│   │   └── secrets/
│   └── backend.tf                # state remoto (S3 + DynamoDB lock)
└── README.md
```

### 4.3 Componentes por ambiente

| Recurso | Alexandria | payment-api |
|---|---|---|
| Serviço ECS | 1 task (backend `8080` + frontend `3000`) | 1 task (`8082`) |
| Banco | Aurora MySQL (privado) | RDS PostgreSQL (privado) |
| Computação | ECS **launch type EC2** (Auto Scaling) | idem |
| Imagens | ECR: `alexandria-backend`, `alexandria-frontend` | ECR: `payment-api` |
| Acesso | ALB público (HTTPS via ACM) | ALB interno (só o backend) |

> Nota: o `iac/` antigo usava FARGATE. O plano mantém a decisão por **EC2 + Auto Scaling**
> (controle de custo com baixa demanda). Se preferir simplicidade operacional, FARGATE é alternativa válida.

### 4.4 Detalhes

- **Rede**: VPC, subnets públicas/privadas, NAT Gateway, Internet Gateway.
- **ECR**: 3 repositórios.
- **ECS EC2**: ASG + Capacity Provider.
- **ALB público** → serviço `alexandria`; **ALB interno** → `payment-api` via DNS privado/Service Discovery.
- **Secrets** (Secrets Manager/SSM): `JWT_SECRET`, `MERCADOPAGO_ACCESS_TOKEN`,
  `MERCADOPAGO_PUBLIC_KEY`, `MERCADOPAGO_WEBHOOK_SECRET`, `SUBSCRIPTION_CALLBACK_SECRET`,
  credenciais dos bancos.
- **Ambientes**: `dev` e `prod` por diretório (sem workspaces).
- **State remoto**: S3 + DynamoDB lock, buckets por ambiente.

### 4.5 Desenvolvimento local (docker-compose)

- Adicionar serviços `payment-api` e `postgres-payment` ao `docker-compose.yml`.
- Compartilhar `JWT_SECRET` entre backend e payment-api.
- Adicionar `SUBSCRIPTION_CALLBACK_URL=http://backend:8080/subscriptions/payment-webhook`
  e `SUBSCRIPTION_CALLBACK_SECRET`.

### 4.6 CI/CD

- `ci.yml`: adicionar job de build/teste da imagem `payment-api` (repo separado ou subpasta se vier para o mono-repo).
- `cd.yml`: hoje está vazio (só dispara no fim do CI). Adicionar:
  - Push das imagens para ECR (`alexandria-backend`, `alexandria-frontend`, `payment-api`).
  - `terraform plan`/`apply` por ambiente (dev/prod).

## 5. Testes

- **Backend (Alexandria)**:
  - trial criado no registro e no primeiro login Google (não em login existente).
  - `GET /subscriptions/me` (estados do trial/ativo/vencido).
  - checkout cartão durante o trial (agenda cobrança, não processa) e pós-trial (processa e ativa).
  - checkout PIX pós-trial (`currentPeriodEndsAt = now + 30d`).
  - webhook/callback idempotente (duas chamadas com mesmo `mpPaymentId` → 1 ativação).
  - job de expiração (trial vencido → `EXPIRED`; ativo vencido → `PAST_DUE`).
  - `GET /books/{id}/epub`: `200` com assinatura válida, `402/403 SUBSCRIPTION_REQUIRED` sem.
- **payment-api**: `referenceId` (String), produção (`APP_USR-`), callback HTTP, Kafka opcional.
- **Frontend e2e**: paywall no `/leitor/[id]`, fluxo de checkout (PIX/cartão), proxy `/api/epub`.

## 6. Riscos

- **Vazamento do EPUB** se o gate ficar só no frontend → mitigado pelo gate no backend (`/books/{id}/epub`).
- **Dessincronia JWT** entre serviços → mesma secret e mesma claim `userId`.
- **PIX assíncrono** depende de IPN + callback → idempotência no `payment-webhook` e em `payment-api`.
- **Conflito de merge commitado** em `application.properties` → corrigir antes de tocar nas configs.
- **`iac/` legado** pode ser reutilizado por engano → substituir por `infra/terraform`.
- **Cookie vs `localStorage`**: o proxy `/api/epub` roda no servidor e só enxerga o cookie
  `auth-token`, não o `localStorage`; manter os dois em sincronia no `AuthContext`.
