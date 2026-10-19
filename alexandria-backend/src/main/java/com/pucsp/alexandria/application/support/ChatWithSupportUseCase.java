package com.pucsp.alexandria.application.support;

import com.pucsp.alexandria.domain.support.ChatMessage;
import com.pucsp.alexandria.domain.support.SupportAgent;
import java.util.List;

public class ChatWithSupportUseCase {

  static final int MAX_HISTORY = 10;
  static final int MAX_MESSAGE_LENGTH = 1000;

  private final SupportAgent supportAgent;

  public ChatWithSupportUseCase(SupportAgent supportAgent) {
    this.supportAgent = supportAgent;
  }

  public String execute(Long userId, List<ChatMessage> history) {
    if (history == null || history.isEmpty() || !history.get(history.size() - 1).fromUser()) {
      throw new IllegalArgumentException("A conversa deve terminar com uma mensagem do usuário");
    }
    List<ChatMessage> recent = history.subList(Math.max(0, history.size() - MAX_HISTORY), history.size());
    for (ChatMessage message : recent) {
      if (message.content() == null || message.content().isBlank()) {
        throw new IllegalArgumentException("Mensagem vazia");
      }
      if (message.content().length() > MAX_MESSAGE_LENGTH) {
        throw new IllegalArgumentException("Mensagem muito longa (máximo " + MAX_MESSAGE_LENGTH + " caracteres)");
      }
    }
    return supportAgent.reply(userId, recent);
  }
}
