# language: pt

@draft
Funcionalidade: Cobrança recorrente de assinatura via cartão salvo (card_id)

  Como usuário premium do Alexandria
  Quero que minha assinatura seja cobrada automaticamente a cada 30 dias
  Usando o cartão que salvei no Mercado Pago (card_id), sem redigitar os dados

  Contexto:
    Dado que o plano "Alexandria Premium" custa R$ 10,00 por 30 dias
    E o trial gratuito dura 15 dias
    E o payment-api está integrado ao Mercado Pago Sandbox
    E o backend nunca armazena dados de cartão (número, CVV, validade)
    E o cartão salvo é referenciado apenas por customer_id e card_id

  @p1
  Cenário: Salvar cartão durante o trial sem cobrar na hora
    Dado um usuário "ana" com assinatura TRIALING
    Quando ela faz checkout com cartão informando um cardToken válido
    Então o backend cria um Customer e um Card no Mercado Pago
    E salva apenas customer_id e card_id na assinatura
    E NÃO gera cobrança imediata
    E a assinatura permanece TRIALING

  @p1
  Cenário: Cobrar no fim do trial com sucesso
    Dado um usuário com assinatura TRIALING e card_id salvo
    E o trial termina hoje
    Quando o job de cobrança recorrente executa
    Então ele cria um pagamento usando o card_id salvo
    E o Mercado Pago aprova
    E a assinatura vira ACTIVE com current_period_ends_at = agora + 30 dias
    E failed_attempts é zerado

  @p1
  Cenário: Cobrança no fim do trial recusada
    Dado um usuário com assinatura TRIALING e card_id salvo
    E o cartão é recusado pelo Mercado Pago
    Quando o job de cobrança recorrente executa
    Então a assinatura vira PAST_DUE
    E uma nova tentativa é agendada
    E nenhum acesso pago é concedido

  @p1
  Cenário: Renovação mensal automática
    Dado um usuário com assinatura ACTIVE e current_period_ends_at = hoje
    Quando o job de cobrança recorrente executa
    Então um novo pagamento de R$ 10,00 é criado com o card_id salvo
    E ao aprovar, current_period_ends_at passa a ser agora + 30 dias
    E a assinatura permanece ACTIVE

  @p1
  Cenário: Cobrança não é duplicada (idempotência)
    Dado um usuário com assinatura ACTIVE e current_period_ends_at = hoje
    E o job já cobrou o ciclo atual com sucesso
    Quando o job executa novamente no mesmo ciclo
    Então nenhuma nova cobrança é gerada
    E a assinatura não é estendida duas vezes

  @p1
  Cenário: Cancelar interrompe cobranças e preserva acesso
    Dado um usuário com assinatura ACTIVE e current_period_ends_at no futuro
    Quando ele cancela a assinatura
    Então a assinatura vira CANCELED
    E nenhuma cobrança futura é gerada pelo job
    E o acesso permanece válido até current_period_ends_at

  @p2
  Cenário: Trocar cartão para o próximo ciclo
    Dado um usuário com assinatura ACTIVE e um card_id antigo
    Quando ele informa um novo cardToken válido
    Então o backend cria um novo Card no Mercado Pago
    E atualiza o card_id da assinatura
    E o ciclo atual não é afetado

  @p2
  Cenário: Trocar cartão com cobrança em atraso dispara nova tentativa
    Dado um usuário com assinatura PAST_DUE
    Quando ele atualiza o cartão
    Então o backend dispara uma nova tentativa de cobrança imediata
    E ao aprovar, a assinatura volta a ACTIVE

  @p2
  Cenário: PIX após o trial (pagamento imediato)
    Dado um usuário com assinatura EXPIRED e sem cartão salvo
    Quando ele paga via PIX
    Então ao confirmar o pagamento, a assinatura vira ACTIVE por 30 dias
    E não há card_id associado

  @p3
  Cenário: Admin rastreia histórico de cobranças
    Dado um usuário com várias cobranças registradas
    Quando o admin consulta o histórico
    Então vê status, mpPaymentId e referenceId de cada cobrança
