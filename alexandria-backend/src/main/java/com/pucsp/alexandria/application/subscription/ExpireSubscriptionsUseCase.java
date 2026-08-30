package com.pucsp.alexandria.application.subscription;

import com.pucsp.alexandria.domain.subscription.Subscription;
import com.pucsp.alexandria.domain.subscription.SubscriptionRepository;
import java.time.LocalDateTime;

public class ExpireSubscriptionsUseCase {

  private final SubscriptionRepository subscriptionRepository;

  public ExpireSubscriptionsUseCase(SubscriptionRepository subscriptionRepository) {
    this.subscriptionRepository = subscriptionRepository;
  }

  /**
   * Processa a expiração de trials e assinaturas ativas.
   *
   * <ul>
   *   <li>Trial vencido com período pago já definido no futuro → ativa (pagou durante o trial).</li>
   *   <li>Trial vencido sem pagamento → expira.</li>
   *   <li>Trial vencido com pagamento agendado → mantém aguardando processamento da cobrança.</li>
   *   <li>Assinatura ativa vencida → past due.</li>
   * </ul>
   */
  public void execute() {
    LocalDateTime now = LocalDateTime.now();

    for (Subscription subscription : subscriptionRepository.findTrialsEndingBefore(now)) {
      if (hasPaidPeriodStarting(subscription, now)) {
        subscription.activate(subscription.getMpPaymentId(), subscription.getCurrentPeriodEndsAt());
        subscriptionRepository.save(subscription);
      } else if (subscription.isPaymentScheduled()) {
        // Cobrança agendada: aguarda processamento (fora deste job).
        continue;
      } else {
        subscription.markExpired();
        subscriptionRepository.save(subscription);
      }
    }

    for (Subscription subscription : subscriptionRepository.findActiveEndingBefore(now)) {
      subscription.markPastDue();
      subscriptionRepository.save(subscription);
    }
  }

  private boolean hasPaidPeriodStarting(Subscription subscription, LocalDateTime now) {
    return subscription.getCurrentPeriodEndsAt() != null
        && subscription.getCurrentPeriodEndsAt().isAfter(now);
  }
}
