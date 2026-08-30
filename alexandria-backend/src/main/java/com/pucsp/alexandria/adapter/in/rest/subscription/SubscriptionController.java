package com.pucsp.alexandria.adapter.in.rest.subscription;

import com.pucsp.alexandria.adapter.in.rest.subscription.dto.CheckoutRequest;
import com.pucsp.alexandria.adapter.in.rest.subscription.dto.PaymentWebhookRequest;
import com.pucsp.alexandria.application.subscription.CancelSubscriptionUseCase;
import com.pucsp.alexandria.application.subscription.CheckoutUseCase;
import com.pucsp.alexandria.application.subscription.GetSubscriptionUseCase;
import com.pucsp.alexandria.application.subscription.ProcessPaymentWebhookUseCase;
import com.pucsp.alexandria.application.subscription.dto.CheckoutInput;
import com.pucsp.alexandria.application.subscription.dto.CheckoutOutput;
import com.pucsp.alexandria.application.subscription.dto.PaymentWebhookInput;
import com.pucsp.alexandria.application.subscription.dto.SubscriptionOutput;
import com.pucsp.alexandria.config.SubscriptionProperties;
import com.pucsp.alexandria.domain.shared.valueobject.AuthenticatedUser;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscriptions")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

  private final GetSubscriptionUseCase getSubscriptionUseCase;
  private final CheckoutUseCase checkoutUseCase;
  private final ProcessPaymentWebhookUseCase processPaymentWebhookUseCase;
  private final CancelSubscriptionUseCase cancelSubscriptionUseCase;
  private final SubscriptionProperties properties;

  public SubscriptionController(
      GetSubscriptionUseCase getSubscriptionUseCase,
      CheckoutUseCase checkoutUseCase,
      ProcessPaymentWebhookUseCase processPaymentWebhookUseCase,
      CancelSubscriptionUseCase cancelSubscriptionUseCase,
      SubscriptionProperties properties) {
    this.getSubscriptionUseCase = getSubscriptionUseCase;
    this.checkoutUseCase = checkoutUseCase;
    this.processPaymentWebhookUseCase = processPaymentWebhookUseCase;
    this.cancelSubscriptionUseCase = cancelSubscriptionUseCase;
    this.properties = properties;
  }

  @GetMapping("/me")
  public ResponseEntity<SubscriptionOutput> getSubscription(Authentication authentication) {
    AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
    return ResponseEntity.ok(getSubscriptionUseCase.execute(user.id()));
  }

  @PostMapping("/checkout")
  public ResponseEntity<CheckoutOutput> checkout(
      Authentication authentication,
      @RequestBody CheckoutRequest request,
      HttpServletRequest httpRequest) {
    AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
    String bearerToken = extractBearerToken(httpRequest);

    CheckoutInput input = new CheckoutInput(
        user.id(),
        request.paymentMethod(),
        request.cardToken(),
        request.cardBrand(),
        request.installments(),
        request.payerEmail(),
        request.payerDocumentType(),
        request.payerDocumentNumber());

    CheckoutOutput output = checkoutUseCase.execute(input, bearerToken);
    return ResponseEntity.status(HttpStatus.CREATED).body(output);
  }

  @PostMapping("/payment-webhook")
  public ResponseEntity<Void> paymentWebhook(
      @RequestBody PaymentWebhookRequest request,
      @RequestHeader(value = "X-Webhook-Secret", required = false) String webhookSecret) {
    if (!isValidWebhookSecret(webhookSecret)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    PaymentWebhookInput input = new PaymentWebhookInput(
        request.referenceId(),
        request.paymentId(),
        request.status(),
        request.paymentMethod(),
        request.mpPaymentId(),
        request.occurredAt());

    processPaymentWebhookUseCase.execute(input);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/cancel")
  public ResponseEntity<Void> cancel(Authentication authentication) {
    AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
    cancelSubscriptionUseCase.execute(user.id());
    return ResponseEntity.noContent().build();
  }

  private String extractBearerToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return "";
  }

  private boolean isValidWebhookSecret(String secret) {
    String expected = properties.getCallbackSecret();
    if (expected == null || expected.isBlank()) {
      return false;
    }
    return expected.equals(secret);
  }
}
