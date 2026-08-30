# Tarefas — SaaS Alexandria

> Derivado do [plan.md](./plan.md) refinado. Reflete os pontos encontrados no código.

## 0. Bloqueios / limpeza inicial

- [ ] Corrigir conflito de merge commitado em `alexandria-backend/src/main/resources/application.properties` (marcadores `<<<<<<< HEAD` / `=======` / `>>>>>>> feature/infraestrutura`)
- [ ] Remover a classe legada `com.alexandria.alexandria_backend.AlexandriaBackendApplication` (manter `com.pucsp.alexandria.AlexandriaApplication`)
- [ ] Remover/substituir a pasta `iac/` (template do projeto "linuxtips-sorteador")
- [ ] Consolidar ou remover `infraestrutura/` (arquivos `.tf` soltos)
- [ ] Decidir sobre `Kubernetes/` (fora de escopo nesta fase; documentar)

## 1. payment-api (repo separado — Gohan Food)

> Local: `/Users/talitaalves/IdeaProjects/payment-api` · remote `git@github.com:GohanFood/payment-api.git`.

- [ ] Generalizar `orderId` (UUID) para `referenceId` (String) em `CreatePaymentRequest`, `Payment`, `PaymentEntity`, `PaymentResponse`, `CreatePaymentService`, `PaymentRepository.findByOrderId`, `KafkaPaymentProducer`, migrations
- [ ] Padronizar convenção `referenceId = "subscription:{subscriptionId}"`
- [ ] Trocar descrição `"Pedido #..."` por `"Assinatura ..."` (PIX e cartão)
- [ ] Aceitar claim `userId` no `JwtAuthenticationFilter` (hoje lê `sub`/`id`) e popular o principal com `claims.get("userId").toString()`
- [ ] Permitir Mercado Pago em produção: `MercadoPagoGateway.init()` aceitar `APP_USR-` quando `mercadopago.environment=production`
- [ ] Tornar Kafka opcional (`@ConditionalOnProperty`; hoje `KafkaConfig` é `@Profile("!test")`)
- [ ] Adicionar callback HTTP em `payment.completed`/`refunded` (no `PaymentStatusSyncService`, ao lado do `publishPaymentCompleted`):
  - [ ] `SUBSCRIPTION_CALLBACK_URL`
  - [ ] `SUBSCRIPTION_CALLBACK_SECRET` (header `X-Webhook-Secret`)
  - [ ] payload idempotente (`referenceId`, `paymentId`, `status`, `paymentMethod`, `amount`, `occurredAt`)
- [ ] Atualizar testes e migrations (`V4__change_order_id_to_reference_id.sql`)

## 2. Alexandria backend — domínio de assinatura

- [ ] Criar domínio `Subscription` (`Subscription`, `SubscriptionId`, `SubscriptionStatus`, `SubscriptionRepository`)
- [ ] Criar `SubscriptionEntity` + `SubscriptionJpaRepository` + `SubscriptionRepositoryImpl` + mapper
- [ ] Configurar plano/trial (`subscription.trial-days=15`, `price`, `period-days=30`, `currency`, `payment-api.url`, `subscription.callback-secret`)
- [ ] Decidir schema: habilitar Flyway (`V2__create_subscription.sql`) ou manter `ddl-auto=update`
- [ ] Iniciar trial (`TRIALING`) no `RegisterUserUseCase`
- [ ] Iniciar trial (`TRIALING`) no `GoogleAuthUseCase` (apenas na criação de novo usuário)
- [ ] Criar cliente HTTP `PaymentApiClient` (RestClient) repassando `Authorization: Bearer`
- [ ] Criar `GET /subscriptions/me`
- [ ] Criar `POST /subscriptions/checkout` (PIX/CARD, cardToken)
- [ ] Criar `POST /subscriptions/payment-webhook` (idempotente por `mpPaymentId`, valida `X-Webhook-Secret`)
- [ ] Implementar regra de métodos e período pago:
  - [ ] Durante o trial → somente cartão; agenda cobrança para o fim do trial (não processa na hora)
  - [ ] Após o trial → PIX ou cartão imediatos; `currentPeriodEndsAt = now + 30d`
  - [ ] Renovação → `currentPeriodEndsAt = now + 30d`
- [ ] Criar `POST /subscriptions/cancel` (preserva acesso até `currentPeriodEndsAt`)
- [ ] Criar `GET /books/{id}/epub` autenticado com gate de assinatura (`402/403 SUBSCRIPTION_REQUIRED`)
- [ ] Fazer proxy do EPUB usando `downloadUrl` do `Book` (sem expor URL)
- [ ] Adicionar `@EnableScheduling` (hoje só existe `@EnableAsync`)
- [ ] Criar job `@Scheduled` de expiração (`TRIALING` sem pagamento → `EXPIRED`; `TRIALING` com pagamento → `ACTIVE`; `ACTIVE`→`PAST_DUE`; grace period)
- [ ] Atualizar `SecurityConfig` (liberar `payment-webhook`, proteger EPUB)

## 3. Alexandria frontend

- [ ] Criar página `/planos` (público, CTA para checkout)
- [ ] Criar página `/checkout` (PIX QR Code + cartão via MercadoPago.js `CardForm`)
- [ ] Configurar `NEXT_PUBLIC_MERCADOPAGO_PUBLIC_KEY`
- [ ] Adicionar `/leitor` e `/checkout` a `PRIVATE_PATHS` no `proxy.ts` (login obrigatório)
- [ ] Manter `/explorar`, `/explorar/[id]` e `/planos` como rotas públicas
- [ ] Exigir login no botão "Ler agora" da página de detalhes (abrir modal de login)
- [ ] Expor `subscription` no `AuthContext` (buscar `GET /subscriptions/me`)
- [ ] Adicionar tipos/funções em `api.ts` (`Subscription`, `getSubscription`, `createCheckout`, `cancelSubscription`)
- [ ] Aplicar paywall no `/leitor/[id]`
- [ ] Transformar `/api/epub` em proxy autenticado (ler cookie `auth-token` → `GET /books/{bookId}/epub`)
- [ ] Atualizar `useEpub.ts` para usar `bookId` em vez de `downloadUrl`

## 4. Infra/Deploy — Terraform + ECS

- [ ] Criar pasta `infra/terraform/` (environments + modules)
- [ ] Configurar backend remoto (S3 + DynamoDB lock, por ambiente)
- [ ] Módulo `network` (VPC, subnets, NAT, IGW)
- [ ] Módulo `ecr` (`alexandria-backend`, `alexandria-frontend`, `payment-api`)
- [ ] Módulo `ecs-cluster` (launch type EC2 + Auto Scaling + Capacity Provider)
- [ ] Módulo `rds-alexandria` (Aurora MySQL)
- [ ] Módulo `rds-payment-api` (RDS PostgreSQL)
- [ ] Módulo `service-alexandria` (task backend `8080` + frontend `3000`)
- [ ] Módulo `service-payment-api` (task `8082`)
- [ ] Módulo `alb` (público HTTPS/ACM + interno)
- [ ] Módulo `secrets` (`JWT_SECRET`, `MERCADOPAGO_*`, `SUBSCRIPTION_CALLBACK_SECRET`, credenciais de banco)
- [ ] Ambiente `dev` e ambiente `prod`
- [ ] Adicionar `payment-api` + `postgres-payment` ao `docker-compose.yml` local
- [ ] Atualizar `ci.yml` (build/teste da imagem payment-api)
- [ ] Implementar `cd.yml` (push ECR + `terraform plan`/`apply`)
- [ ] Documentar env vars e comandos no `infra/README.md`

## 5. Testes

- [ ] Backend: trial no registro e no primeiro login Google (não em login existente)
- [ ] Backend: `GET /subscriptions/me` (trial/ativo/vencido)
- [ ] Backend: checkout cartão durante o trial (agenda cobrança, não processa) e pós-trial (processa e ativa)
- [ ] Backend: checkout PIX pós-trial (`currentPeriodEndsAt = now + 30d`)
- [ ] Backend: webhook/callback idempotente (mesmo `mpPaymentId` → 1 ativação)
- [ ] Backend: job de expiração (`TRIALING`→`EXPIRED`, `ACTIVE`→`PAST_DUE`)
- [ ] Backend: `GET /books/{id}/epub` (`200` válido, `402/403` sem assinatura)
- [ ] payment-api: `referenceId` (String), produção (`APP_USR-`), callback HTTP, Kafka opcional
- [ ] Frontend e2e: paywall no `/leitor/[id]`, checkout PIX/cartão, proxy `/api/epub`
