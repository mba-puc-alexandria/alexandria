# Tarefas — SaaS Alexandria

## payment-api

- [ ] Generalizar orderId para referenceId (String)
- [ ] Aceitar claim userId no JwtAuthenticationFilter
- [ ] Permitir Mercado Pago em produção (APP_USR-)
- [ ] Tornar Kafka opcional + callback HTTP de status
- [ ] Atualizar testes e migrations

## Alexandria backend

- [ ] Criar entidade Subscription + repository
- [ ] Configurar plano/trial (15 dias, preço, período)
- [ ] Iniciar trial no registro de usuário
- [ ] Criar endpoints de subscription (me/checkout/payment-webhook/cancel)
- [ ] Criar endpoint autenticado GET /books/{id}/epub com gate de assinatura
- [ ] Criar job de expiração de trial/assinatura
- [ ] Criar cliente HTTP RestClient para payment-api
- [ ] Atualizar SecurityConfig

## Alexandria frontend

- [ ] Criar página /planos
- [ ] Criar página /checkout (PIX + cartão)
- [ ] Expor status da assinatura no AuthContext
- [ ] Aplicar paywall no leitor /leitor/[id]
- [ ] Transformar /api/epub em proxy autenticado

## Infra/Deploy — Terraform + ECS

- [ ] Criar pasta `infra/` com estrutura Terraform (environments + modules)
- [ ] Configurar backend remoto (S3 + DynamoDB lock)
- [ ] Módulo `network` (VPC, subnets, NAT, IGW)
- [ ] Módulo `ecr` (alexandria-backend, alexandria-frontend, payment-api)
- [ ] Módulo `ecs-cluster` (launch type EC2 + Auto Scaling)
- [ ] Módulo `rds-alexandria` (Aurora MySQL)
- [ ] Módulo `rds-payment-api` (RDS PostgreSQL)
- [ ] Módulo `service-alexandria` (task backend + frontend)
- [ ] Módulo `service-payment-api` (task payment-api)
- [ ] Módulo `alb` (público + interno, HTTPS via ACM)
- [ ] Módulo `secrets` (JWT + Mercado Pago + credenciais de banco)
- [ ] Ambiente `dev` e ambiente `prod`
- [ ] Documentar env vars e comandos no `infra/README.md`

## Testes

- [ ] Testes backend (trial, checkout, webhook, expiração, EPUB)
- [ ] Testes payment-api (referenceId, produção, callback)
- [ ] Testes e2e frontend (paywall, checkout)
