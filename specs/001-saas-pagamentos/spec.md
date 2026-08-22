# 001 — SaaS: assinatura e cobrança (PIX/Cartão via Mercado Pago)

## Contexto

Alexandria hoje é um monolito gratuito que oferece curadoria e leitura de EPUB/PDF.
O objetivo é evoluir para um SaaS: trial gratuito de 15 dias e, após o período,
cobrança recorrente/pequena taxa pelo uso da plataforma — mesmo quando o livro é
de domínio público, pois o valor é pela estrutura de leitura (EPUB, progresso,
biblioteca, leitor).

## Objetivos de negócio

- Permitir que todo novo usuário comece com 15 dias de trial sem cartão.
- Cobrar uma taxa fixa (período mensal) via PIX ou cartão de crédito.
- Bloquear a leitura quando a assinatura expirar ou o pagamento não for aprovado.
- Reutilizar o módulo de pagamento existente do Gohan Food (payment-api).

## Personas

- Leitor(a) casual: quer experimentar e, se gostar, pagar de forma simples.
- Usuário(a) premium: espera acesso contínuo e renovação transparente.
- Admin: precisa visualizar status e resolver pagamentos com o Mercado Pago.

## User Stories

- US-01: Como visitante, quero me cadastrar e ganhar 15 dias grátis automaticamente.
- US-02: Como usuário em trial, quero ver quantos dias restam e quando vence.
- US-03: Como usuário vencido, quero ser impedido de abrir EPUB e direcionado ao checkout.
- US-04: Como usuário, quero pagar via PIX e ver o QR Code para concluir.
- US-05: Como usuário, quero pagar via cartão e ter a assinatura ativada na hora.
- US-06: Como sistema, quero ativar a assinatura quando o Mercado Pago confirmar o pagamento.
- US-07: Como usuário, quero cancelar a assinatura e manter acesso até o fim do período.
- US-08: Como admin, quero rastrear pagamento, status e id do Mercado Pago.

## Requisitos funcionais

- RF-01: Registro cria Subscription com status TRIALING e trialEndsAt = now + 15 dias.
- RF-02: Assinatura expira automaticamente quando passar do prazo sem pagamento.
- RF-03: Endpoint de leitura de EPUB exige assinatura ativa/trial válido.
- RF-04: Checkout gera pagamento PIX (QR Code) ou cartão (CardToken) no Mercado Pago.
- RF-05: Pagamento aprovado ativa/renova a assinatura.
- RF-06: Webhook do Mercado Pago e callback do payment-api atualizam status.
- RF-07: Cancelamento preserva acesso até o fim do período pago.

## Requisitos não funcionais

- RNF-01: O gate de EPUB deve ser no backend (não apenas no frontend).
- RNF-02: payment-api e Alexandria compartilham a mesma chave JWT.
- RNF-03: Em produção, Mercado Pago deve aceitar credenciais de produção.
- RNF-04: Dados de pagamento não podem expor token/cartão no backend (só CardToken).

## Fora de escopo

- Cobrança de preços diferentes por livro.
- Assinatura por equipe/instituição.
- Renovação automática com cartão salvo (tokenização recorrente) nesta primeira fase.
