package com.pucsp.alexandria.application.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pucsp.alexandria.adapter.out.payment.PaymentApiClient;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiCreateRequest;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiResult;
import com.pucsp.alexandria.config.SubscriptionProperties;
import com.pucsp.alexandria.config.jwt.JwtTokenProvider;
import com.pucsp.alexandria.domain.subscription.Subscription;
import com.pucsp.alexandria.domain.subscription.SubscriptionRepository;
import com.pucsp.alexandria.domain.subscription.SubscriptionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Risco R3 do plano mestre: cobrança duplicada ou não executada no mesmo ciclo.
 * Casos de teste CT-PAG-04, CT-PAG-05 e CT-PAG-06 (SCRUM-136 a SCRUM-138).
 *
 * <p>O repositório é simulado com as mesmas regras das consultas reais: só devolve trials
 * vencidos em TRIALING e assinaturas ACTIVE com período vencido.
 */
@ExtendWith(MockitoExtension.class)
class RecurringBillingUseCaseTest {

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @Mock
  private PaymentApiClient paymentApiClient;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  private SubscriptionProperties properties;
  private RecurringBillingUseCase useCase;

  @BeforeEach
  void setUp() {
    properties = new SubscriptionProperties();
    properties.setPrice(new BigDecimal("10.00"));
    properties.setPeriodDays(30);
    properties.setMaxFailedAttempts(3);
    properties.setRetryBaseHours(24);
    useCase = new RecurringBillingUseCase(subscriptionRepository, paymentApiClient, properties,
        jwtTokenProvider);
    lenient().when(jwtTokenProvider.generateToken(anyLong(), anyString(), anyString()))
        .thenReturn("scheduled-token");
    lenient().when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
  }

  private Subscription trialEndingWithCard() {
    return Subscription.restore(10L, 7L, SubscriptionStatus.TRIALING,
        LocalDateTime.now().minusMinutes(5), null, null, "cust-1", "card-1", null, 0, null,
        null, false, LocalDateTime.now().minusDays(15), LocalDateTime.now());
  }

  private void simulateRepositoryQueries(Subscription subscription) {
    lenient().when(subscriptionRepository.findTrialsEndingBefore(any())).thenAnswer(inv -> {
      LocalDateTime now = inv.getArgument(0);
      return subscription.getStatus() == SubscriptionStatus.TRIALING
          && subscription.getTrialEndsAt().isBefore(now) ? List.of(subscription) : List.of();
    });
    lenient().when(subscriptionRepository.findActiveEndingBefore(any())).thenAnswer(inv -> {
      LocalDateTime now = inv.getArgument(0);
      return subscription.getStatus() == SubscriptionStatus.ACTIVE
          && subscription.getCurrentPeriodEndsAt().isBefore(now) ? List.of(subscription) : List.of();
    });
    lenient().when(subscriptionRepository.findPastDueRetryingBefore(any())).thenAnswer(inv -> {
      LocalDateTime now = inv.getArgument(0);
      return subscription.getStatus() == SubscriptionStatus.PAST_DUE
          && subscription.getNextRetryAt() != null
          && subscription.getNextRetryAt().isBefore(now) ? List.of(subscription) : List.of();
    });
  }

  @Test
  @DisplayName("CT-PAG-04 e CT-PAG-06: cobrança aprovada ativa por 30 dias e o job seguinte não cobra de novo")
  void approvedChargeActivatesAndIsNotRepeatedInSameCycle() {
    Subscription subscription = trialEndingWithCard();
    simulateRepositoryQueries(subscription);
    when(paymentApiClient.createPayment(any(), anyString()))
        .thenReturn(new PaymentApiResult("pay-1", 99L, "COMPLETED", null, null, null));

    useCase.execute();
    useCase.execute();

    verify(paymentApiClient, times(1)).createPayment(any(), anyString());
    assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
    assertTrue(subscription.getCurrentPeriodEndsAt().isAfter(LocalDateTime.now().plusDays(29)));
  }

  @Test
  @DisplayName("CT-PAG-05: recusa leva a PAST_DUE e a retentativa usa a mesma referência do ciclo")
  void retriesOfSameCycleReuseTheSameReference() {
    Subscription subscription = trialEndingWithCard();
    simulateRepositoryQueries(subscription);
    when(paymentApiClient.createPayment(any(), anyString()))
        .thenReturn(new PaymentApiResult("pay-1", 1L, "REJECTED", null, null, null))
        .thenReturn(new PaymentApiResult("pay-2", 2L, "REJECTED", null, null, null));

    useCase.execute();
    assertEquals(SubscriptionStatus.PAST_DUE, subscription.getStatus());
    assertEquals(1, subscription.getFailedAttempts());
    // A próxima tentativa só vence em 24 h; o job rodando de novo agora não cobra.
    useCase.execute();
    verify(paymentApiClient, times(1)).createPayment(any(), anyString());
    // Retentativa manual do mesmo ciclo (botão "tentar de novo").
    useCase.retryNow(subscription, "user-token");

    ArgumentCaptor<PaymentApiCreateRequest> requests =
        ArgumentCaptor.forClass(PaymentApiCreateRequest.class);
    verify(paymentApiClient, times(2)).createPayment(requests.capture(), anyString());
    List<PaymentApiCreateRequest> sent = requests.getAllValues();
    assertEquals(sent.get(0).referenceId(), sent.get(1).referenceId());
    assertTrue(sent.get(0).referenceId().startsWith("subscription:10:period:"));
  }

  @Test
  @DisplayName("Após 3 falhas a assinatura expira e não é cobrada de novo")
  void subscriptionExpiresAfterMaxFailures() {
    Subscription subscription = trialEndingWithCard();
    simulateRepositoryQueries(subscription);
    when(paymentApiClient.createPayment(any(), anyString()))
        .thenReturn(new PaymentApiResult("pay-1", 1L, "REJECTED", null, null, null))
        .thenReturn(new PaymentApiResult("pay-2", 2L, "REJECTED", null, null, null))
        .thenReturn(new PaymentApiResult("pay-3", 3L, "REJECTED", null, null, null));

    useCase.execute();
    useCase.retryNow(subscription, "user-token");
    useCase.retryNow(subscription, "user-token");
    useCase.execute();

    assertEquals(SubscriptionStatus.EXPIRED, subscription.getStatus());
    verify(paymentApiClient, times(3)).createPayment(any(), anyString());
  }
}
