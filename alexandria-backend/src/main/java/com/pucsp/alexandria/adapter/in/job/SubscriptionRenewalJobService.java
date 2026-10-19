package com.pucsp.alexandria.adapter.in.job;

import com.pucsp.alexandria.application.subscription.RecurringBillingUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionRenewalJobService {

  private static final Logger log = LoggerFactory.getLogger(SubscriptionRenewalJobService.class);
  private final RecurringBillingUseCase recurringBillingUseCase;

  public SubscriptionRenewalJobService(RecurringBillingUseCase recurringBillingUseCase) {
    this.recurringBillingUseCase = recurringBillingUseCase;
  }

  @Scheduled(cron = "${subscription.billing-cron:0 */15 * * * *}")
  public void renewSubscriptions() {
    log.info("Iniciando job de cobrança recorrente de assinaturas.");
    recurringBillingUseCase.execute();
  }
}
