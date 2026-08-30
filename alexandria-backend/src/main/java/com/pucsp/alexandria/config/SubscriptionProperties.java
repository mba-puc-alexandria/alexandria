package com.pucsp.alexandria.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "subscription")
public class SubscriptionProperties {

  private int trialDays = 15;
  private BigDecimal price = new BigDecimal("10.00");
  private int periodDays = 30;
  private String currency = "BRL";
  private String callbackSecret;

  public int getTrialDays() {
    return trialDays;
  }

  public void setTrialDays(int trialDays) {
    this.trialDays = trialDays;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public int getPeriodDays() {
    return periodDays;
  }

  public void setPeriodDays(int periodDays) {
    this.periodDays = periodDays;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getCallbackSecret() {
    return callbackSecret;
  }

  public void setCallbackSecret(String callbackSecret) {
    this.callbackSecret = callbackSecret;
  }
}
