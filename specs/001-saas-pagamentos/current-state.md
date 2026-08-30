# Estado atual — Alexandria + payment-api

Documento de baseline para a migração de Alexandria de monolito gratuito para SaaS.

> Vinculado a: [spec.md](./spec.md) · [plan.md](./plan.md) · [tasks.md](./tasks.md)

---

## 1. Alexandria (monolito atual)

### 1.1 Backend (`alexandria-backend`)

- **Stack**: Java 17, Spring Boot 3.4.4, arquitetura hexagonal (ports & adapters), MySQL, JPA.
- **Banco de dados**:
  - `spring.jpa.hibernate.ddl-auto=update`
  - `spring.flyway.enabled=false`
  - Ou seja, o schema é gerenciado pelo Hibernate; não há migrations Flyway ativas.
- **Autenticação**:
  - `JwtTokenProvider.generateToken(userId, username, role)` gera token com:
    - `subject = username`
    - claims: `userId` (Long) e `role`
  - `JwtAuthenticationFilter` monta o principal como `AuthenticatedUser(userId, username)`.
- **Registro de usuário**:
  - `RegisterUserUseCase` → cria apenas `User`.
  - `GoogleAuthUseCase` → também cria `User` no primeiro login via Google.
  - **Não existe** conceito de assinatura, trial ou plano.
- **Livros e leitura**:
  - `GET /books`, `GET /books/{id}` e `GET /books/search` são **públicos**.
  - **Não existe endpoint de EPUB no backend.**
  - O livro é baixado direto do Gutendex pelo frontend.
- **Biblioteca do usuário**:
  - `user-books` autenticado, com status (`reading`/`done`/`toread`), progresso e rating.

### 1.2 Frontend (`alexandria-frontend`)

- **Stack**: Next.js 16, React 19, TypeScript, Tailwind CSS.
- **Sessão**:
  - `AuthContext` persiste o token em `localStorage['auth-token']` e no cookie `auth-token`.
  - `apiFetch` envia `Authorization: Bearer` nas chamadas JSON.
- **Leitor** (`/leitor/[id]`):
  - `useEpub` chama `/api/epub?url=<downloadUrl>`.
  - `app/api/epub/route.ts` faz fetch direto do Gutendex, **sem autenticação**.
  - **Sem paywall**.
- **Proteção de rotas** (`proxy.ts`):
  - Protege apenas `/biblioteca`, `/dashboard` e `/configuracoes`.
  - ⚠️ `/leitor` **não** está protegido hoje.

### 1.3 Infra/CI/CD

- `docker-compose.yml` (raiz): mysql + redis + backend + frontend.
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

- **Stack**: Java 17, Spring Boot 3.4.4, arquitetura hexagonal, **PostgreSQL**, Flyway, **Kafka**, Mercado Pago SDK 2.8.0, Bucket4j.

### 2.2 Endpoints (`/api/v1/payments`)

| Método | Rota | Função |
|---|---|---|
| POST | `/` | Criar pagamento PIX ou cartão |
| POST | `/{id}/process` | Reprocessar pagamento PENDING |
| GET | `/{id}` | Buscar por ID |
| GET | `/` | Listar por usuário |
| POST | `/refund` | Reembolsar |
| POST | `/webhook` | Receber IPN do Mercado Pago |

### 2.3 Acoplamentos ao delivery

- `CreatePaymentRequest` exige `orderId` (UUID).
- `Payment` e `PaymentEntity` possuem `orderId UUID`.
- `user_id` é `String`.
- Descrições são geradas como `"Pedido #..."`.
- Kafka:
  - `KafkaPaymentProducer` publica `payment.created/completed/failed/refunded`.
  - `KafkaPaymentConsumer` escuta `order.created/order.cancelled` (apenas log).

### 2.4 JWT do payment-api

- `JwtAuthenticationFilter` lê `sub` ou claim `id`.
- ⚠️ **Não lê a claim `userId`** que o Alexandria gera.

### 2.5 Mercado Pago

- `MercadoPagoGateway` e `MercadoPagoProperties`:
  - **Só aceitam sandbox** (`TEST-`).
  - **Rejeitam token de produção** (`APP_USR-`).
  - Suportam PIX, cartão, reembolso e consulta.

### 2.6 Webhook/status

- `PaymentStatusSyncService`:
  - Recebe IPN → busca por `gatewayTransactionId` → consulta status no MP → marca como `COMPLETED`.
  - Publica evento Kafka (`publishPaymentCompleted`) ao aprovar.
  - **Não possui callback HTTP** para outro serviço.
- `KafkaConfig` é `@Profile("!test")`: fora dos testes o Kafka está sempre ativo.

---

## 3. Gap analysis: reutilizar, adaptar e criar

### 3.1 Reutilizável quase sem mudanças

- `MercadoPagoGateway` (PIX, cartão, reembolso, consulta).
- `Payment` (domain), `PaymentStatus`, DTOs e use cases.
- `MercadoPagoWebhookValidator`.
- `SecurityConfig`, rate limiting (Bucket4j) e `JwtConfig`.
- Migrations base da tabela `payments`.

### 3.2 Precisa adaptar no payment-api

| Ponto | Hoje | Necessário |
|---|---|---|
| `orderId` | `UUID` obrigatório | `referenceId` (String) genérico |
| JWT | lê `sub`/`id` | aceitar claim `userId` |
| Mercado Pago | só sandbox | permitir produção (`APP_USR-` + `MERCADOPAGO_ENVIRONMENT=production`) |
| Kafka | obrigatório | opcional ou substituível por callback HTTP |
| PostgreSQL | separado | manter separado (não migrar o Alexandria) |

### 3.3 Precisa criar no Alexandria backend

- Entidade/domínio `Subscription` + repository.
- Config de plano: `trial-days=15`, `price`, `period-days=30`.
- Iniciar trial no **registro** e no **login Google** (dois pontos de criação de usuário).
- Endpoints:
  - `GET /subscriptions/me`
  - `POST /subscriptions/checkout`
  - `POST /subscriptions/payment-webhook`
  - `POST /subscriptions/cancel`
- `GET /books/{id}/epub` autenticado, com bloqueio por assinatura.
- Job `@Scheduled` para expirar trial/assinatura.
- Cliente HTTP (`RestClient`) para o payment-api.
- Atualizar `SecurityConfig`.

### 3.4 Precisa criar no Frontend

- Páginas `/planos` e `/checkout`.
- Status da assinatura no `AuthContext`.
- Paywall no `/leitor/[id]`.
- Transformar `/api/epub` em proxy autenticado → backend.
- Variável `NEXT_PUBLIC_MERCADOPAGO_PUBLIC_KEY`.

### 3.5 Precisa ajustar na Infra/CI/CD

- Adicionar `payment-api` + `postgres-payment` ao `docker-compose.yml`.
- Compartilhar `JWT_SECRET` entre backend e payment-api.
- Adicionar envs do Mercado Pago, `SUBSCRIPTION_CALLBACK_URL` e `SUBSCRIPTION_CALLBACK_SECRET`.
- Substituir `iac/` (template de outro projeto) por `infra/terraform` (dev/prod).
- Implementar o `cd.yml` (hoje vazio): push ECR + `terraform plan`/`apply`.
- Atualizar CI para construir/testar a imagem do payment-api.

---

## 4. Riscos e observações

1. **O gate do EPUB precisa estar no backend.** Hoje a rota `/api/epub` é aberta e busca direto do Gutendex. Se o bloqueio ficar só no frontend, é contornável.
2. **Dois pontos de criação de usuário**: `RegisterUserUseCase` e `GoogleAuthUseCase` — o trial precisa ser criado nos dois.
3. **JWT entre serviços**: compartilhar a mesma `secret` e alinhar a claim `userId`.
4. **PIX é assíncrono**: cartão ativa na hora; PIX depende de webhook/callback para ativar.
5. **`/leitor` não está protegido hoje** no `proxy.ts`.
6. **`ddl-auto=update` + Flyway desabilitado** no Alexandria: decidir entre migration Flyway para `Subscription` ou deixar o Hibernate criar.
