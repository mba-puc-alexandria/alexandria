# Specs — Alexandria

Diretório de especificações seguindo o estilo [Spec Kit](https://github.com/github/spec-kit).

## Índice

| Número | Título | Status |
|---|---|---|
| 001 | [SaaS: assinatura e cobrança (PIX/Cartão via Mercado Pago)](./001-saas-pagamentos/spec.md) | Planejado |

## Estrutura

Cada especificação possui:

- `spec.md` — o **quê** e o **porquê** (contexto, user stories, requisitos).
- `plan.md` — o **como** técnico (stack, arquitetura, decisões).
- `tasks.md` — lista de tarefas acionáveis para implementação.
- `current-state.md` — baseline do estado atual (opcional, útil para migrações/adaptações).

## Infraestrutura

A pasta `infra/` (a ser criada) conterá o Terraform que provisiona os serviços
no AWS ECS:

- `alexandria` (backend + frontend) com Aurora MySQL
- `payment-api` com RDS PostgreSQL

Veja a seção 4 do `plan.md` da especificação 001 para a estrutura detalhada.

## Fluxo

1. Criar/atualizar o `spec.md`.
2. Criar/atualizar o `plan.md`.
3. Derivar `tasks.md`.
4. Implementar.
5. Provisionar infraestrutura via `infra/`.
