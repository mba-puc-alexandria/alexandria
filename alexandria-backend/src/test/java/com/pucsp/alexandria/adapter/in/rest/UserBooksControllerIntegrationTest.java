package com.pucsp.alexandria.adapter.in.rest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucsp.alexandria.adapter.in.rest.dto.AddUserBooksRequest;
import com.pucsp.alexandria.application.userbooks.AddUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.GetUserBookByBookIdUseCase;
import com.pucsp.alexandria.application.userbooks.ListUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.RemoveUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.UpdateUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.book.dto.BookOutput.AuthorInfo;
import com.pucsp.alexandria.config.jwt.JwtTokenProvider;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "jwt.secret=test-secret-key-for-tests-min-256-bits",
    "jwt.expiration-ms=86400000"
})
@AutoConfigureMockMvc
class UserBooksControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AddUserBooksUseCase addUserBooksUseCase;

    @MockitoBean
    private ListUserBooksUseCase listUserBooksUseCase;

    @MockitoBean
    private UpdateUserBooksUseCase updateUserBooksUseCase;

    @MockitoBean
    private RemoveUserBooksUseCase removeUserBooksUseCase;

    @MockitoBean
    private GetUserBookByBookIdUseCase getUserBookByBookIdUseCase;

    @Test
    void shouldGetUserBooksWithToken() throws Exception {
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid-token")).thenReturn("john_doe");
        when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn(1L);
        when(jwtTokenProvider.getRoleFromToken("valid-token")).thenReturn("USER");

        when(listUserBooksUseCase.execute(anyLong(), isNull(), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/user-books")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401WhenGettingUserBooksWithoutToken() throws Exception {
        mockMvc.perform(get("/user-books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAddUserBookWithToken() throws Exception {
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid-token")).thenReturn("john_doe");
        when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn(1L);
        when(jwtTokenProvider.getRoleFromToken("valid-token")).thenReturn("USER");

        BookOutput bookOutput = new BookOutput(1L, "Test Book", List.of(), null, null, null, null, null, null, null, null);
        when(addUserBooksUseCase.execute(anyLong(), any()))
                .thenReturn(new UserBooksOutput(1L, bookOutput, "toread", null, null));

        mockMvc.perform(post("/user-books")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddUserBooksRequest(1L, null))))
                .andExpect(status().isCreated());
    }
}

