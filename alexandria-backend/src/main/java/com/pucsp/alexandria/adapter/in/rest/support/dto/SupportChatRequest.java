package com.pucsp.alexandria.adapter.in.rest.support.dto;

import com.pucsp.alexandria.domain.support.ChatMessage;
import java.util.List;

public record SupportChatRequest(List<ChatMessage> messages) {}
