# Tarefas — SaaS Alexandria

> Derivado do [plan.md](./plan.md) refinado. Reflete os pontos encontrados no código.

## 0. Bloqueios / limpeza inicial

- [x] Corrigir conflito de merge em `application.properties`
- [x] Manter `com.pucsp.alexandria.AlexandriaApplication` como entrypoint único
- [ ] Remover/substituir a pasta `iac/` (template do projeto "linuxtips-sorteador")
- [ ] Consolidar ou remover `infraestrutura/` (arquivos `.tf` soltos)
- [ ] Decidir sobre `Kubernetes/` (fora de escopo nesta fase; documentar)

## 1. payment-api (repo separado — Gohan Food)

> Local: `/Users/talitaalves/IdeaProjects/payment-api` · remote `git@github.com:GohanFood/payment-api.git`.

- [x] Generalizar `orderId` para `referenceId`, incluindo migrations e idempotência por usuário/referência
- [x] Padronizar a convenção `referenceId = "subscription:{subscriptionId}"`
- [x] Usar descrições de assinatura, aceitar claim `userId`, produção Mercado Pago e Kafka opcional
- [x] Adicionar callback HTTP com `PAYMENT_CALLBACK_URL`, `SUBSCRIPTION_CALLBACK_SECRET` e payload compatível
- [x] Criar endpoints Customer + Card e pagamento com `customerId` + `cardId`
- [ ] Cobrir callback com retry durável e executar contrato/E2E com Alexandria

## 2. Alexandria backend — domínio de assinatura

- [x] Criar domínio, persistência, Flyway, configuração de plano/trial e criação no registro/Google
- [x] Criar cliente HTTP `PaymentApiClient`, endpoints de assinatura e gate do EPUB
- [x] Adicionar campos de recorrência e job de renovação com referência estável por ciclo
- [ ] Implementar regra de métodos e período pago:
  - [ ] Durante o trial → somente cartão; salva Customer+Card no MP (`cardId`), não cobra na hora
  - [ ] Após o trial → PIX ou cartão imediatos; `currentPeriodEndsAt = now + 30d`
  - [ ] Renovação → `currentPeriodEndsAt = now + 30d`
- [ ] Criar `POST /subscriptions/payment-method` (trocar cartão; se `PAST_DUE`, dispara nova tentativa)
- [ ] Criar `POST /subscriptions/cancel` (encerra recorrência, preserva acesso até `currentPeriodEndsAt`)
- [ ] Criar `GET /books/{id}/epub` autenticado com gate de assinatura (`402/403 SUBSCRIPTION_REQUIRED`)
- [ ] Fazer proxy do EPUB usando `downloadUrl` do `Book` (sem expor URL)
- [ ] Adicionar `@EnableScheduling` (hoje só existe `@EnableAsync`)
- [ ] Criar job `@Scheduled` de cobrança recorrente (fim do trial + renovação mensal com `cardId`; idempotente por ciclo)
- [ ] Criar job `@Scheduled` de expiração/dunning (`TRIALING` sem `cardId` → `EXPIRED`; `ACTIVE` sem `cardId` → `PAST_DUE`; retry com backoff; N falhas → `CANCELED`)
- [ ] Atualizar `SecurityConfig` (liberar `payment-webhook`, proteger EPUB)

## 3. Alexandria frontend

- [x] Criar planos, checkout, `CardForm`, QR PIX, troca de cartão, auth e paywall
- [x] Proteger leitor/checkout e encaminhar EPUB autenticado ao backend
- [ ] Revalidar assinatura antes de servir EPUB salvo no cache local

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
- [x] Adicionar `payment-api` + `postgres-payment` ao `docker-compose.yml` local
- [x] Validar o contexto de build `../deliveryAPI/payment-api`
- [ ] Atualizar `ci.yml` (build/teste da imagem payment-api)
- [ ] Implementar `cd.yml` (push ECR + `terraform plan`/`apply`)
- [ ] Documentar env vars e comandos no `infra/README.md`

## 5. Testes

- [ ] Backend: cobrir trial, checkout, callback, dunning e recorrência com testes focados
- [ ] Backend: `GET /subscriptions/me` (trial/ativo/vencido)
- [ ] Backend: checkout cartão durante o trial (agenda cobrança, não processa) e pós-trial (processa e ativa)
- [ ] Backend: checkout PIX pós-trial (`currentPeriodEndsAt = now + 30d`)
- [ ] Backend: webhook/callback idempotente (mesmo `mpPaymentId` → 1 ativação)
- [ ] Backend: job de expiração (`TRIALING`→`EXPIRED`, `ACTIVE`→`PAST_DUE`)
- [ ] Backend: checkout trial salva `cardId` (não cobra) e pós-trial cobra com `cardId`
- [ ] Backend: cobrança recorrente idempotente (fim do trial + renovação, sem dupla cobrança)
- [ ] Backend: dunning (recusa → `PAST_DUE` + retry; N falhas → `CANCELED`)
- [ ] Backend: troca de cartão (atualiza `mpCardId`; `PAST_DUE` → nova tentativa)
- [ ] Backend: `GET /books/{id}/epub` (`200` válido, `402/403` sem assinatura)
- [x] payment-api: `referenceId`, produção, callback HTTP e Kafka opcional
- [ ] E2E: paywall, checkout PIX/cartão e proxy EPUB com ambos os serviços em execução
