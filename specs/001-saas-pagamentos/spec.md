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
- US-09: Como usuário premium, quero atualizar o cartão salvo para a próxima cobrança sem interromper meu acesso atual.
- US-10: Como sistema, quero cobrar automaticamente a assinatura a cada 30 dias usando o cartão salvo (`card_id`).

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
- RF-05b: **Cartão durante o trial**: não cobra na hora; salva o cartão no Mercado Pago e
  agenda a cobrança para quando o trial terminar. Ao processar, concede 30 dias.
- RF-05c: **Após o trial**: PIX ou cartão processam imediatamente e concedem 30 dias
  (`currentPeriodEndsAt = agora + 30 dias`).
- RF-05d: **Renovações seguintes** concedem 30 dias.
- RF-06: Webhook do Mercado Pago e callback do payment-api atualizam status.
- RF-07: Cancelamento encerra a recorrência (nenhuma cobrança futura) e preserva o acesso
  até o fim do período pago.
- RF-08: No checkout com cartão durante o trial, o cartão é salvo no Mercado Pago
  (Customer + Card) e a assinatura guarda **apenas tokens** (`mp_customer_id`, `mp_card_id`).
  Nenhum dado de cartão é armazenado no backend e **não há cobrança imediata**.
- RF-09: Ao fim do trial e a cada ciclo de 30 dias, o sistema gera automaticamente uma
  nova cobrança no valor do plano usando o `card_id` salvo. Aprovação ativa/renova a
  assinatura por mais 30 dias.
- RF-10: Cobrança recusada move a assinatura para PAST_DUE e agenda novas tentativas
  (dunning/retry); após N falhas consecutivas, a assinatura é cancelada/expirada.
- RF-11: O usuário pode atualizar o meio de pagamento (novo cartão) para o próximo ciclo,
  sem afetar o ciclo já pago. Se estiver em atraso (PAST_DUE), a atualização dispara uma
  nova tentativa de cobrança.

## Requisitos não funcionais

- RNF-01: O gate de EPUB deve ser no backend (não apenas no frontend).
- RNF-02: payment-api e Alexandria compartilham a mesma chave JWT.
- RNF-03: Em produção, Mercado Pago deve aceitar credenciais de produção.
- RNF-04: Nenhum dado de cartão (número, CVV, validade) pode ser armazenado no backend;
  apenas tokens de referência do Mercado Pago (`cardToken` no checkout, `customer_id`/`card_id`
  para recorrência).
- RNF-05: A cobrança recorrente deve ser idempotente (nunca cobrar duas vezes o mesmo ciclo).

## Fora de escopo

- Cobrança de preços diferentes por livro.
- Assinatura por equipe/instituição.
- Reembolso automático / disputas de cartão (chargebacks) — tratado manualmente pelo admin.
- Troca de plano / upgrade-downgrade com prorrogação (pro-rata) no meio do ciclo.

> **Nota de refinamento:** a versão anterior desta spec mantinha "renovação automática de
> ciclos futuros com cartão salvo" **fora de escopo**. Esta revisão **traz a recorrência
> para dentro do escopo** (RF-08/RF-09/RF-10/RF-11), usando o modelo `card_id`
> (Customer + Card no Mercado Pago) em vez de reutilizar `cardToken`. O `card_id` é um
> token persistente e seguro (não é dado de cartão) e suporta trial de 15 dias e ciclos
> mensais de 30 dias.
