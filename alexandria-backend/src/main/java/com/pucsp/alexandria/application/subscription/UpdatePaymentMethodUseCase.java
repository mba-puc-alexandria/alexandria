package com.pucsp.alexandria.application.subscription;

import com.pucsp.alexandria.adapter.out.payment.PaymentApiClient;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiAddCardRequest;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiAddCardResult;
import com.pucsp.alexandria.domain.subscription.Subscription;
import com.pucsp.alexandria.domain.subscription.SubscriptionRepository;
import com.pucsp.alexandria.domain.subscription.SubscriptionStatus;
import com.pucsp.alexandria.domain.subscription.exception.InvalidSubscriptionException;
import com.pucsp.alexandria.domain.subscription.exception.SubscriptionNotFoundException;

public class UpdatePaymentMethodUseCase {

  private final SubscriptionRepository subscriptionRepository;
  private final PaymentApiClient paymentApiClient;
  private final RecurringBillingUseCase recurringBillingUseCase;

  public UpdatePaymentMethodUseCase(SubscriptionRepository subscriptionRepository,
      PaymentApiClient paymentApiClient, RecurringBillingUseCase recurringBillingUseCase) {
    this.subscriptionRepository = subscriptionRepository;
    this.paymentApiClient = paymentApiClient;
    this.recurringBillingUseCase = recurringBillingUseCase;
  }

  public void execute(Long userId, String cardToken, String cardBrand, String bearerToken) {
    if (cardToken == null || cardToken.isBlank()) {
      throw new InvalidSubscriptionException("Card token is required");
    }
    Subscription subscription = subscriptionRepository.findByUserId(userId)
        .orElseThrow(() -> new SubscriptionNotFoundException(userId));
    if (subscription.getMpCustomerId() == null || subscription.getMpCustomerId().isBlank()) {
      throw new InvalidSubscriptionException("Cadastre um cartão no checkout antes de trocá-lo.");
    }
    PaymentApiAddCardResult card = paymentApiClient.addCard(subscription.getMpCustomerId(),
        new PaymentApiAddCardRequest(cardToken, cardBrand), bearerToken);
    subscription.replacePaymentMethod(card.cardId());
    subscriptionRepository.save(subscription);
    if (subscription.getStatus() == SubscriptionStatus.PAST_DUE) {
      recurringBillingUseCase.retryNow(subscription, bearerToken);
    }
  }
}
