# Contexto para agentes (Continue/CLI)

Instruções de trabalho para agentes atuando neste repositório.

## 1. Referência obrigatória antes de implementar

Sempre que for solicitado a **fazer algo** (implementar, planejar tarefa, corrigir, revisar progresso)
relacionado à feature **001 — SaaS Pagamentos**, consulte **primeiro** os arquivos abaixo, na ordem
de prioridade, e **não** re-indexe/varre o projeto inteiro:

1. `specs/001-saas-pagamentos/tasks.md`
2. `specs/001-saas-pagamentos/plan.md`
3. `specs/001-saas-pagamentos/spec.md`
4. `specs/001-saas-pagamentos/current-state.md`
5. `specs/001-saas-pagamentos/apresentacao-status.md`

Para questões de **arquitetura do backend** e **como adicionar um novo domínio**, consulte também:

6. `alexandria-backend/markdown/ARCHITECTURE.md`
7. `alexandria-backend/markdown/HOW_TO_ADD_NEW_DOMAIN.md`

Esses arquivos são a fonte de verdade do estado atual, das tarefas e das decisões de arquitetura.
Use-os como ponto de partida. Só leia o código-fonte dos módulos específicos necessários para a
tarefa em questão.

## 2. Estrutura importante

- **Alexandria backend**: `alexandria-backend/` (Spring Boot, Java 17, MySQL, arquitetura hexagonal,
  pacote raiz `com.pucsp.alexandria`).
- **Alexandria frontend**: `alexandria-frontend/` (Next.js 16, React 19, TypeScript, Tailwind).
- **payment-api**: repositório **separado** em `/Users/talitaalves/IdeaProjects/payment-api`
  (Spring Boot, PostgreSQL, Mercado Pago, Kafka). Branch de trabalho: `feature/001-saas-pagamentos`.
- **Infra/deploy**: `iac/` é template de outro projeto (linuxtips-sorteador) e será substituída por
  `infra/terraform/` (ainda não criada). `infraestrutura/` tem apenas 2 `.tf` soltos.
- **Kubernetes/**: fora de escopo nesta fase.

## 3. Build/teste

- **payment-api**: requer **Java 17** (o Java 24 default da máquina quebra o Lombok/compilação).
  ```bash
  cd /Users/talitaalves/IdeaProjects/payment-api
  export JAVA_HOME=$(/usr/libexec/java_home -v 17)
  ./mvnw ...   # ou mvn ...
  ```
- O teste de integração `PaymentApiIntegrationTest` depende de PostgreSQL local
  (`jdbc:postgresql://localhost:5434/paymentdb`). Para rodar os demais:
  `mvn test -Dtest='!PaymentApiIntegrationTest'`.

## 4. Estado atual (resumo — consultar specs para detalhes)

- Backend Alexandria: ~90% implementado.
- Frontend Alexandria: ~85% implementado.
- payment-api: em adaptação (seção 1 do plano). Já concluído: `orderId` (UUID) → `referenceId` (String).
- Infra/CI: 0%.
- Documentação: parcialmente atualizada.

## 5. Convenções da feature

- `referenceId` do payment-api usa a convenção `"subscription:{subscriptionId}"`.
- Descrições de pagamento: `"Assinatura ..."` (não `"Pedido #..."`).
- JWT compartilhado entre backend e payment-api (mesma secret + claim `userId`).
- Callback payment-api → backend: `POST /subscriptions/payment-webhook` com header `X-Webhook-Secret`.
