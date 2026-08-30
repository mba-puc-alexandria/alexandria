# 001 — SaaS: assinatura e cobrança (PIX/Cartão via Mercado Pago)

## Contexto

Alexandria hoje é um monolito gratuito que oferece curadoria e leitura de EPUB/PDF.
O objetivo é evoluir para um SaaS: trial gratuito de 15 dias e, após o período,
cobrança recorrente/pequena taxa pelo uso da plataforma — mesmo quando o livro é
de domínio público, pois o valor é pela estrutura de leitura (EPUB, progresso,
biblioteca, leitor).

## Objetivos de negócio

- Permitir que todo novo usuário comece com 15 dias de trial sem cartão.
- Cobrar uma taxa fixa de R$ 10,00 por mês (plano **Alexandria Premium**) via PIX ou cartão de crédito.
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
- US-04: Como usuário com o período de teste vencido, quero pagar via PIX e ter acesso imediato.
- US-05: Como usuário em trial, quero assinar com cartão e só ser cobrado quando o trial terminar.
- US-06: Como sistema, quero ativar a assinatura quando o Mercado Pago confirmar o pagamento.
- US-07: Como usuário, quero cancelar a assinatura e manter acesso até o fim do período.
- US-08: Como admin, quero rastrear pagamento, status e id do Mercado Pago.

## Requisitos funcionais

- RF-01: Registro cria Subscription com status TRIALING e trialEndsAt = now + 15 dias.
- RF-01a: O plano se chama **Alexandria Premium** e custa R$ 10,00 por período mensal.
- RF-02: Assinatura expira automaticamente quando passar do prazo sem pagamento.
- RF-03: Endpoint de leitura de EPUB exige assinatura ativa/trial válido.
- RF-03a: As telas públicas são apenas Explorar (`/explorar`) e Detalhes do livro
  (`/explorar/[id]`); adicionar à biblioteca e ler exigem login.
- RF-03b: A leitura do EPUB exige login **e** assinatura válida (trial ativo ou paga).
- RF-04: Checkout gera pagamento PIX (QR Code) ou cartão (CardToken) no Mercado Pago.
- RF-05: Pagamento aprovado ativa/renova a assinatura.
- RF-05a: **Durante o trial**, o único método disponível é **cartão de crédito** (PIX não é oferecido).
- RF-05b: **Cartão durante o trial**: não cobra na hora; agenda o processamento da
  cobrança para quando o trial terminar. Ao processar, concede 30 dias.
- RF-05c: **Após o trial**: PIX ou cartão processam imediatamente e concedem 30 dias
  (`currentPeriodEndsAt = agora + 30 dias`).
- RF-05d: **Renovações seguintes** concedem 30 dias.
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
- Renovação automática de ciclos futuros com cartão salvo (tokenização recorrente).
  - ⚠️ Exceção: o agendamento da **primeira cobrança pós-trial** via cartão (RF-05b)
    ainda é necessário nesta fase; o que fica fora é a recorrência automática dos
    meses seguintes sem nova confirmação do usuário.
