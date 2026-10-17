package com.pucsp.alexandria.adapter.in.rest.support;

import com.pucsp.alexandria.adapter.in.rest.support.dto.SupportChatRequest;
import com.pucsp.alexandria.adapter.in.rest.support.dto.SupportChatResponse;
import com.pucsp.alexandria.application.support.ChatWithSupportUseCase;
import com.pucsp.alexandria.domain.shared.valueobject.AuthenticatedUser;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/support")
@SecurityRequirement(name = "bearerAuth")
public class SupportController {

  private final ChatWithSupportUseCase chatWithSupportUseCase;

  public SupportController(ChatWithSupportUseCase chatWithSupportUseCase) {
    this.chatWithSupportUseCase = chatWithSupportUseCase;
  }

  @PostMapping("/chat")
  public ResponseEntity<SupportChatResponse> chat(
      Authentication authentication,
      @RequestBody SupportChatRequest request) {
    AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
    String reply = chatWithSupportUseCase.execute(user.id(), request.messages());
    return ResponseEntity.ok(new SupportChatResponse(reply));
  }
}
