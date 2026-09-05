package com.pucsp.alexandria.application.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Estado atual da assinatura Alexandria Premium do usuário.")
public record SubscriptionOutput(
    @Schema(
        description = "Status da assinatura.",
        example = "TRIALING",
        allowableValues = {"TRIALING", "ACTIVE", "PAST_DUE", "EXPIRED", "CANCELED"})
    String status,

    @Schema(
        description = "Data/hora de término do período de teste.",
        example = "2026-09-20T00:00:00")
    LocalDateTime trialEndsAt,

    @Schema(
        description = "Data/hora de término do período pago atual.",
        example = "2026-10-05T00:00:00")
    LocalDateTime currentPeriodEndsAt,

    @Schema(
        description = "Valor mensal da assinatura.",
        example = "10.00")
    BigDecimal price,

    @Schema(
        description = "Moeda do valor.",
        example = "BRL")
    String currency,

    @Schema(
        description = "Duração do período pago em dias.",
        example = "30")
    int periodDays,

    @Schema(
        description = "Indica se há uma cobrança agendada (ex.: cartão cadastrado durante o trial).",
        example = "false")
    boolean paymentScheduled) {
}
