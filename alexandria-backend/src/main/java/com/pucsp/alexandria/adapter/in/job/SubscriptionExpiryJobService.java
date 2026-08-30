package com.pucsp.alexandria.adapter.in.job;

import com.pucsp.alexandria.application.subscription.ExpireSubscriptionsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionExpiryJobService {

  private static final Logger log = LoggerFactory.getLogger(SubscriptionExpiryJobService.class);

  private final ExpireSubscriptionsUseCase expireSubscriptionsUseCase;

  public SubscriptionExpiryJobService(ExpireSubscriptionsUseCase expireSubscriptionsUseCase) {
    this.expireSubscriptionsUseCase = expireSubscriptionsUseCase;
  }

  @Scheduled(cron = "${subscription.expiry-cron:0 0 3 * * *}")
  public void expireSubscriptions() {
    log.info("Iniciando job de expiração de assinaturas.");
    expireSubscriptionsUseCase.execute();
    log.info("Job de expiração de assinaturas concluído.");
  }
}
