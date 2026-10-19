# Apresentação — Status da Feature 001: SaaS Alexandria (Alexandria Premium)

> Documento de apoio para apresentação de aula.
> Data de referência: 05/09/2026 (sábado)
> Vinculado a: [spec.md](./spec.md) · [plan.md](./plan.md) · [tasks.md](./tasks.md) · [current-state.md](./current-state.md)

---

## 1. O que é a feature

Transformar o Alexandria (hoje gratuito) em um SaaS com assinatura paga:

- **Trial**: todo novo usuário ganha **15 dias grátis** ao se cadastrar (ou no primeiro login Google).
- **Plano**: **Alexandria Premium** — **R$ 10,00/mês**.
- **Pagamento**: PIX ou cartão de crédito, via **Mercado Pago**.
- **Paywall**: sem assinatura válida, a leitura do EPUB é bloqueada.

---

## 2. Visão geral do progresso

| Camada | Status | Observação |
|---|---|---|
| Alexandria backend | 🟢 ~90% | Domínio, endpoints, gate de EPUB e job prontos |
| Alexandria frontend | 🟢 ~85% | Telas, gating e integração com a API prontos |
| payment-api | 🔴 0% | Branch criada, mas nenhuma alteração feita ainda |
| Infra/CI | 🔴 0% | Terraform, docker-compose e CI/CD pendentes |
| Documentação | 🟡 atualizada, não commitada | `README.md` e `specs/README.md` |

---

## 3. Arquitetura alvo

```
Frontend (Next.js)
   │ JWT (cookie auth-token)
   ▼
Alexandria backend (Spring Boot + MySQL)
   │ JWT (mesma secret)        ▲ callback HTTP (X-Webhook-Secret)
   ▼                           │
payment-api (Spring Boot + PostgreSQL)   ← repositório separado
   │
   ▼
Mercado Pago (PIX + Cartão)
```

- **Alexandria → payment-api**: `RestClient` repassando `Authorization: Bearer`.
- **payment-api → Alexandria**: callback `POST /subscriptions/payment-webhook`.

---

## 4. O que já foi implementado (código)

### 4.1 Backend — Alexandria

Commits na branch `feature/001-saas-pagamentos`:

| Commit | Entrega |
|---|---|
| `d7be8ec` | Domínio de assinatura, checkout protótipo e correções de merge |
| `b5d01d6` | Checkout + endpoints `/subscriptions` |
| `0ec4648` | Job diário de expiração |
| `09abe9f` | Trial no registro e primeiro login Google |
| `498a8da` | Gate de leitura de EPUB |
| `b3d18d6` | Integração frontend (AuthContext + proxy EPUB) |
| `bcf56de` | Checkout e configurações ligados ao backend real |

**Componentes implementados:**

- **Domínio** (`domain/subscription`):
  - `Subscription`, `SubscriptionId`, `SubscriptionStatus`, `SubscriptionRepository`.
  - Status: `TRIALING`, `ACTIVE`, `PAST_DUE`, `EXPIRED`, `CANCELED`.
- **Persistência**:
  - `SubscriptionEntity`, `SubscriptionJpaRepository`, `SubscriptionRepositoryImpl`, `SubscriptionMapper`.
- **Aplicação** (`application/subscription`):
  - `StartTrialUseCase` — cria trial de 15 dias.
  - `GetSubscriptionUseCase` — consulta status.
  - `CheckoutUseCase` — regras de pagamento por estado.
  - `ProcessPaymentWebhookUseCase` — idempotente, ativa assinatura no `COMPLETED`.
  - `CancelSubscriptionUseCase` — cancelamento preservando acesso.
  - `ExpireSubscriptionsUseCase` — expira trial/assinatura.
- **Integração** (`adapter/out/payment`):
  - `PaymentApiClient`, `RestPaymentApiClient` (HTTP para payment-api).
- **Endpoints**:
  - `GET /subscriptions/me`
  - `POST /subscriptions/checkout`
  - `POST /subscriptions/payment-webhook`
  - `POST /subscriptions/cancel`
  - `GET /books/{id}/epub` (gate de assinatura)
- **Job**:
  - `SubscriptionExpiryJobService` (`@Scheduled` diário).
- **Config/Segurança**:
  - `@EnableScheduling`.
  - `SubscriptionProperties`.
  - `SecurityConfig` liberando webhook e protegendo EPUB.
  - `GlobalExceptionHandler` com `402 SUBSCRIPTION_REQUIRED`.

### 4.2 Frontend — Alexandria

**Telas novas:**
- `/planos` — preço e CTA (Alexandria Premium).
- `/checkout` — PIX e cartão (integrado ao backend).
- `PaywallModal` — bloqueio de leitura.

**Integrações:**
- `AuthContext` carrega/expoe a assinatura (`GET /subscriptions/me`).
- `api.ts` com `getSubscription`, `createCheckout`, `cancelSubscription`.
- `proxy.ts` protege `/leitor`, `/checkout`, `/biblioteca`, etc.
- `/api/epub` virou proxy autenticado → `GET /books/{bookId}/epub`.
- `useEpub` usa `bookId` e dispara paywall em `subscription_required`.

---

## 5. Regras de negócio implementadas

### 5.1 Acesso à leitura

| Situação | Leitura |
|---|---|
| Sem login | 🔒 Bloqueada |
| Trial ativo | ✅ Liberada |
| Assinatura ativa | ✅ Liberada |
| Trial expirado / assinatura vencida | 🔒 Paywall |

O gate é **duplo**: login no frontend + validação de assinatura no backend.

### 5.2 Pagamento por estado

| Momento | Métodos disponíveis | Comportamento |
|---|---|---|
| Durante o trial | Somente cartão | Cobrança **agendada** para o fim do trial |
| Após o trial | PIX ou cartão | Pagamento **imediato** |

---

## 6. O que falta implementar

### 6.1 payment-api (0% — próximo grande bloco)

- [ ] `orderId` (UUID) → `referenceId` (String).
- [ ] `JwtAuthenticationFilter` aceitar claim `userId`.
- [ ] Mercado Pago produção (`APP_USR-` + `MERCADOPAGO_ENVIRONMENT=production`).
- [ ] Kafka opcional.
- [ ] Callback HTTP (`SUBSCRIPTION_CALLBACK_URL`/`SECRET`).
- [ ] Atualizar testes e migrations.

### 6.2 Backend — pendências de robustez

- [ ] Flyway para `subscriptions` (hoje usa `ddl-auto=update`).
- [ ] Processar cobrança de cartão agendada ao fim do trial (capturar via `/process`).
- [ ] Testes de unidade/integração dos novos use cases.

### 6.3 Frontend — pendências de integração

- [ ] MercadoPago.js `CardForm` (gerar `cardToken` real).
- [ ] Renderizar QR Code real (`qrCodeBase64`).
- [ ] Testes e2e (paywall, checkout, proxy EPUB).

### 6.4 Infra/CI (0%)

- [ ] `infra/terraform/` (VPC, ECS, RDS, ALB, ECR, secrets, dev/prod).
- [ ] `docker-compose` com `payment-api` + `postgres-payment`.
- [ ] CI/CD (`cd.yml` vazio; `ci.yml` sem payment-api).

---

## 7. Próximos passos sugeridos

1. Commitar documentação (`README.md` e `specs/README.md`).
2. Implementar a seção 1 do plano no **payment-api**.
3. Finalizar Flyway + captura de cartão agendado no backend.
4. Integrar MercadoPago.js (CardForm + QR) no frontend.
5. Provisionar infra (Terraform + ECS).
6. Testes de ponta a ponta.

---

## 8. Pontos-chave para destacar na aula

- A decisão de **manter o payment-api como microsserviço separado** (não embutir no monolito).
- O **gate de leitura no backend** evita vazamento do EPUB (não confiar só no frontend).
- A **idempotência do webhook/callback** é essencial para PIX (assíncrono).
- O modelo de **trial + cobrança pós-trial** e a regra de **PIX só após o trial**.
- A importância do **JWT compartilhado** (mesma secret + claim `userId`) entre os dois serviços.

---

## 9. Referência rápida de comandos

```bash
# Backend
cd alexandria-backend
mvn clean compile
mvn test

# Frontend
cd alexandria-frontend
npm run lint
npm run dev
```
