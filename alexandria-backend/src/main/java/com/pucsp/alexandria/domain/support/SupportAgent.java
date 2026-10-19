package com.pucsp.alexandria.domain.support;

import com.pucsp.alexandria.domain.shared.Port;
import java.util.List;

/**
 * Porta de saída para o modelo de linguagem do atendimento. A implementação decide o provedor
 * (Ollama local ou Amazon Bedrock) sem que o caso de uso precise saber.
 */
public interface SupportAgent extends Port {

  String reply(Long userId, List<ChatMessage> history);
}
