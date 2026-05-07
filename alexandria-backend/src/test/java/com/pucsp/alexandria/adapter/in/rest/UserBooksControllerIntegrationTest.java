package com.pucsp.alexandria.adapter.in.rest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucsp.alexandria.adapter.in.rest.dto.AddUserBooksRequest;
import com.pucsp.alexandria.adapter.in.rest.dto.UpdateUserBooksRequest;
import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.userbooks.AddUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.ListUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.RemoveUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.UpdateUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.dto.AddUserBooksInput;
import com.pucsp.alexandria.application.userbooks.dto.UpdateUserBooksInput;
import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import com.pucsp.alexandria.domain.book.BookId;
import com.pucsp.alexandria.domain.book.BookSource;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private AddUserBooksUseCase addUserBooksUseCase;

    @MockitoBean
    private ListUserBooksUseCase listUserBooksUseCase;

    @MockitoBean
    private UpdateUserBooksUseCase updateUserBooksUseCase;

    @MockitoBean
    private RemoveUserBooksUseCase removeUserBooksUseCase;

    private UsernamePasswordAuthenticationToken authWithLongPrincipal() {
        return new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
    }

    @Test
    void shouldAddUserBook() throws Exception {
        BookOutput bookOutput = new BookOutput(
                BookId.from(10L), "Dom Casmurro", "Machado", 100L,
                "url", "url", "pt", "Fiction", 5000, null, BookSource.GUTENDEX.name());
        UserBooksOutput ubOutput = new UserBooksOutput(1L, bookOutput, "toread", null, null);

        when(addUserBooksUseCase.execute(eq(1L), any(AddUserBooksInput.class)))
                .thenReturn(ubOutput);

        mockMvc.perform(post("/user-books")
                        .with(authentication(authWithLongPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddUserBooksRequest(10L, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("toread"))
                .andExpect(jsonPath("$.book.title").value("Dom Casmurro"));
    }

    @Test
    void shouldListUserBooks() throws Exception {
        BookOutput bookOutput = new BookOutput(
                BookId.from(10L), "Book Title", "Author", null, null, null, null, null, null, null, BookSource.GUTENDEX.name());
        UserBooksOutput ub1 = new UserBooksOutput(1L, bookOutput, "toread", null, null);
        UserBooksOutput ub2 = new UserBooksOutput(2L, bookOutput, "reading", 50, null);
        Page<UserBooksOutput> page = new PageImpl<>(List.of(ub1, ub2));

        when(listUserBooksUseCase.execute(eq(1L), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/user-books")
                        .with(authentication(authWithLongPrincipal()))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void shouldListUserBooksFilteredByStatus() throws Exception {
        BookOutput bookOutput = new BookOutput(
                BookId.from(10L), "Title", "Author", null, null, null, null, null, null, null, BookSource.GUTENDEX.name());
        UserBooksOutput ub = new UserBooksOutput(1L, bookOutput, "reading", 50, null);
        Page<UserBooksOutput> page = new PageImpl<>(List.of(ub));

        when(listUserBooksUseCase.execute(eq(1L), eq("reading"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/user-books")
                        .with(authentication(authWithLongPrincipal()))
                        .param("status", "reading")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("reading"));
    }

    @Test
    void shouldUpdateUserBook() throws Exception {
        BookOutput bookOutput = new BookOutput(
                BookId.from(10L), "Title", "Author", null, null, null, null, null, null, null, BookSource.GUTENDEX.name());
        UserBooksOutput ubOutput = new UserBooksOutput(1L, bookOutput, "done", null, 4);

        when(updateUserBooksUseCase.execute(eq(1L), eq(1L), any(UpdateUserBooksInput.class)))
                .thenReturn(ubOutput);

        mockMvc.perform(put("/user-books/1")
                        .with(authentication(authWithLongPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserBooksRequest("done", null, 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("done"))
                .andExpect(jsonPath("$.rating").value(4));
    }

    @Test
    void shouldDeleteUserBook() throws Exception {
        doNothing().when(removeUserBooksUseCase).execute(1L, 1L);

        mockMvc.perform(delete("/user-books/1")
                        .with(authentication(authWithLongPrincipal())))
                .andExpect(status().isNoContent());
    }
}
