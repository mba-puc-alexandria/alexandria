package com.pucsp.alexandria.adapter.in.rest.support;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pucsp.alexandria.config.jwt.JwtTokenProvider;
import com.pucsp.alexandria.domain.support.SupportAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "jwt.secret=test-secret-key-for-tests-min-256-bits",
    "jwt.expiration-ms=86400000"
})
@AutoConfigureMockMvc
class SupportControllerIntegrationTest {

  private static final String BODY = """
      {"messages":[{"role":"user","content":"Quando vence minha assinatura?"}]}
      """;

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @MockitoBean
  private SupportAgent supportAgent;

  @Test
  void shouldReplyUsingAuthenticatedUserId() throws Exception {
    when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken("valid-token")).thenReturn("john_doe");
    when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn(7L);
    when(jwtTokenProvider.getRoleFromToken("valid-token")).thenReturn("USER");
    when(supportAgent.reply(eq(7L), anyList())).thenReturn("Sua assinatura vence dia 10.");

    mockMvc.perform(post("/support/chat")
            .header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reply").value("Sua assinatura vence dia 10."));
  }

  @Test
  void shouldReturn401WithoutToken() throws Exception {
    mockMvc.perform(post("/support/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .content(BODY))
        .andExpect(status().isUnauthorized());
  }
}
