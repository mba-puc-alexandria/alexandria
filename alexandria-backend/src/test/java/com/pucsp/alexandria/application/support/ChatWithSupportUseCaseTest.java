package com.pucsp.alexandria.application.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.pucsp.alexandria.domain.support.ChatMessage;
import com.pucsp.alexandria.domain.support.SupportAgent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatWithSupportUseCaseTest {

  @Mock
  private SupportAgent supportAgent;

  @InjectMocks
  private ChatWithSupportUseCase useCase;

  @Test
  void sendsOnlyTheLastMessagesToTheAgent() {
    List<ChatMessage> history = new ArrayList<>();
    for (int i = 0; i < 15; i++) {
      history.add(new ChatMessage(i % 2 == 0 ? "user" : "assistant", "msg " + i));
    }

    useCase.execute(1L, history);

    verify(supportAgent).reply(1L, history.subList(5, 15));
  }

  @Test
  void rejectsConversationNotEndingWithUser() {
    List<ChatMessage> history = List.of(new ChatMessage("assistant", "oi"));
    assertThatThrownBy(() -> useCase.execute(1L, history))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsTooLongMessage() {
    List<ChatMessage> history = List.of(new ChatMessage("user", "a".repeat(1001)));
    assertThatThrownBy(() -> useCase.execute(1L, history))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
