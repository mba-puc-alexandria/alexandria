# Plano técnico — SaaS Alexandria (payment-api como microsserviço)

## Decisão de arquitetura

Manter o payment-api como microsserviço separado, não embutir no monolito.

```
Frontend (Next.js)
   │ JWT
   ▼
Alexandria backend (Spring Boot + MySQL)
   │ JWT (mesma secret)      ▲ callback HTTP de status
   ▼                         │
payment-api (Spring Boot + PostgreSQL)
   │
   ▼
Mercado Pago (PIX + Cartão)
```

## 1. payment-api (adaptações)

- Generalizar `orderId` (UUID) para `referenceId` (String) em request, domain,
  entity, mapper, services e migrations.
- `JwtAuthenticationFilter`: aceitar claim `userId` do token do Alexandria
  (hoje lê `sub`/`id`). Alexandria gera `sub=username` e `userId=Long`.
- `MercadoPagoProperties`/`MercadoPagoGateway`: permitir `production` e token
  `APP_USR-` (hoje bloqueia produção).
- Tornar Kafka opcional e adicionar callback HTTP (`SUBSCRIPTION_CALLBACK_URL`)
  para notificar o Alexandria em `payment.completed`/`refunded`.
- Manter PostgreSQL próprio do payment-api.

## 2. Alexandria backend — domínio de assinatura

- Nova entidade `Subscription`:
  - `id`, `userId` (unique), `status`, `trialEndsAt`, `currentPeriodEndsAt`,
    `mpPaymentId`, `createdAt`, `updatedAt`.
  - Status: `TRIALING`, `ACTIVE`, `PAST_DUE`, `EXPIRED`, `CANCELED`.
- Config (`application.properties`):
  - `subscription.trial-days=15`
  - `subscription.price=...`
  - `subscription.period-days=30`
  - `payment-api.url=http://payment-api:8082`
- `RegisterUserUseCase`: iniciar trial após criar usuário.
- Cliente HTTP `RestClient` para payment-api (header `Authorization: Bearer`).
- Endpoints:
  - `GET /subscriptions/me`
  - `POST /subscriptions/checkout` → PIX (QR) ou cartão
  - `POST /subscriptions/payment-webhook` → callback do payment-api
  - `POST /subscriptions/cancel`
- Endpoint de leitura:
  - `GET /books/{id}/epub` (autenticado) → só retorna EPUB se assinatura válida;
    senão `402/403 SUBSCRIPTION_REQUIRED`.
- Job `@Scheduled` para expirar trials/assinaturas vencidas.
- Atualizar `SecurityConfig` para liberar apenas o necessário e proteger EPUB.

## 3. Alexandria frontend

- Novas páginas:
  - `/planos` — preço e CTA.
  - `/checkout` — PIX (QR Code) e cartão (MercadoPago.js CardForm).
- `AuthContext`: carregar/expor `subscription` (status, data de vencimento).
- `LeitorPage` (`/leitor/[id]`): paywall se assinatura inválida.
- `/api/epub/route.ts`: virar proxy autenticado → chamar `GET /books/{id}/epub`
  no backend usando cookie `auth-token`.
- Variável: `NEXT_PUBLIC_MERCADOPAGO_PUBLIC_KEY`.

## 4. Infra/Deploy — Terraform + AWS ECS

A infraestrutura fica em uma pasta `infra/` versionada, provisionada com Terraform.
Alexandria e payment-api sobem como **dois serviços ECS separados**, cada um com seu
próprio banco gerenciado (RDS).

### Estrutura da pasta `infra/`

```
infra/
├── terraform/
│   ├── environments/
│   │   ├── dev/                 # ambiente de desenvolvimento
│   │   └── prod/                # ambiente de produção
│   ├── modules/
│   │   ├── network/             # VPC, subnets, NAT, internet gateway
│   │   ├── ecs-cluster/         # ECS (launch type EC2 + Auto Scaling)
│   │   ├── service-alexandria/  # task backend + frontend
│   │   ├── service-payment-api/ # task payment-api
│   │   ├── rds-alexandria/      # Aurora MySQL
│   │   ├── rds-payment-api/     # RDS PostgreSQL
│   │   ├── alb/                 # Application Load Balancer + HTTPS
│   │   ├── ecr/                 # repositórios de imagem
│   │   └── secrets/             # Secrets Manager / SSM
│   └── backend.tf               # state remoto (S3 + DynamoDB lock)
└── README.md
```

### Componentes por ambiente

| Recurso | Alexandria | payment-api |
|---|---|---|
| Serviço ECS | 1 task (backend `8080` + frontend `3000`) | 1 task (`8082`) |
| Banco | Aurora MySQL (privado) | RDS PostgreSQL (privado) |
| Computação | ECS com **launch type EC2** (Auto Scaling) | idem |
| Imagens | ECR: `alexandria-backend`, `alexandria-frontend` | ECR: `payment-api` |
| Acesso | ALB público (HTTPS via ACM) | ALB interno (só o backend acessa) |

### Detalhes de infra

- **Rede**: VPC com subnets públicas/privadas, NAT Gateway e Internet Gateway.
- **ECR**: 3 repositórios — `alexandria-backend`, `alexandria-frontend`, `payment-api`.
- **ECS EC2**: Auto Scaling Group + Capacity Provider para subir instâncias conforme demanda.
- **ALB público**: roteia para o serviço `alexandria` (frontend/backend).
- **ALB interno**: payment-api acessível apenas pelo backend via Service Discovery/DNS privado.
- **Secrets**: `JWT_SECRET`, `MERCADOPAGO_ACCESS_TOKEN`, `MERCADOPAGO_PUBLIC_KEY`,
  `MERCADOPAGO_WEBHOOK_SECRET`, credenciais dos bancos — via AWS Secrets Manager/SSM.
- **Ambientes**: `dev` e `prod` separados por diretório (não usar workspaces).
- **State remoto**: S3 + DynamoDB lock, buckets por ambiente.

### Variáveis comuns entre serviços

- `JWT_SECRET` (mesma do Alexandria e payment-api)
- `MERCADOPAGO_ACCESS_TOKEN`, `MERCADOPAGO_PUBLIC_KEY`
- `SUBSCRIPTION_CALLBACK_URL` (payment-api → Alexandria)

## 5. Testes

- Backend: trial, checkout, webhook, expiração, bloqueio de EPUB.
- payment-api: `referenceId`, produção, callback.
- Frontend e2e: paywall e checkout.

## Riscos

- Vazamento do EPUB se o gate ficar só no frontend → mitigar com gate no backend.
- Dessincronia JWT entre serviços → mesma secret e mesma claim `userId`.
- PIX depende de webhook/callback → garantir idempotência no callback.
