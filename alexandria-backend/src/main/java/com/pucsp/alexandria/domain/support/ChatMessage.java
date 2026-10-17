package com.pucsp.alexandria.domain.support;

/** Uma mensagem da conversa com o agente. role: "user" ou "assistant". */
public record ChatMessage(String role, String content) {

  public boolean fromUser() {
    return "user".equals(role);
  }
}
