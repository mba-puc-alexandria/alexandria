package com.pucsp.alexandria.adapter.in.rest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucsp.alexandria.adapter.in.rest.dto.CreateBookRequest;
import com.pucsp.alexandria.adapter.in.rest.dto.UpdateBookRequest;
import com.pucsp.alexandria.application.book.CreateBookUseCase;
import com.pucsp.alexandria.application.book.DeleteBookUseCase;
import com.pucsp.alexandria.application.book.GetBookUseCase;
import com.pucsp.alexandria.application.book.ListBooksUseCase;
import com.pucsp.alexandria.application.book.SearchBookByTitleUseCase;
import com.pucsp.alexandria.application.book.UpdateBookUseCase;
import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.book.dto.CreateBookInput;
import com.pucsp.alexandria.application.book.dto.CreateBookOutput;
import com.pucsp.alexandria.application.book.dto.SearchBookOutput;
import com.pucsp.alexandria.application.book.dto.UpdateBookInput;
import com.pucsp.alexandria.domain.book.BookId;
import com.pucsp.alexandria.domain.book.BookSource;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "jwt.secret=test-secret-key-for-tests-min-256-bits",
    "jwt.expiration-ms=86400000"
})
@AutoConfigureMockMvc
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CreateBookUseCase createBookUseCase;

    @MockitoBean
    private GetBookUseCase getBookUseCase;

    @MockitoBean
    private ListBooksUseCase listBooksUseCase;

    @MockitoBean
    private UpdateBookUseCase updateBookUseCase;

    @MockitoBean
    private DeleteBookUseCase deleteBookUseCase;

    @MockitoBean
    private SearchBookByTitleUseCase searchBookByTitleUseCase;

    @Test
    void shouldCreateBook() throws Exception {
        when(createBookUseCase.execute(any(CreateBookInput.class)))
                .thenReturn(new CreateBookOutput(List.of(1L)));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateBookRequest(1))))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetBookById() throws Exception {
        BookOutput bookOutput = new BookOutput(
                BookId.from(1L), "Dom Casmurro", "Machado de Assis",
                100L, "url", "url", "pt", "Fiction", 5000, null, BookSource.GUTENDEX.name());
        when(getBookUseCase.execute(1L)).thenReturn(bookOutput);

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Dom Casmurro"))
                .andExpect(jsonPath("$.author").value("Machado de Assis"))
                .andExpect(jsonPath("$.source").value("GUTENDEX"));
    }

    @Test
    void shouldListAllBooks() throws Exception {
        BookOutput book1 = new BookOutput(
                BookId.from(1L), "Book 1", "Author 1", null, null, null, null, null, null, 1L, BookSource.LOCAL.name());
        BookOutput book2 = new BookOutput(
                BookId.from(2L), "Book 2", "Author 2", null, null, null, null, null, null, 1L, BookSource.LOCAL.name());
        Page<BookOutput> page = new PageImpl<>(List.of(book1, book2));

        when(listBooksUseCase.execute(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/books")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Book 1"))
                .andExpect(jsonPath("$.content[1].title").value("Book 2"));
    }

    @Test
    void shouldUpdateBook() throws Exception {
        BookOutput updated = new BookOutput(
                BookId.from(1L), "Updated Title", "Author", null, null, null, null, null, null, 1L, BookSource.LOCAL.name());
        when(updateBookUseCase.execute(eq(1L), any(UpdateBookInput.class))).thenReturn(updated);

        mockMvc.perform(put("/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateBookRequest("Updated Title"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void shouldDeleteBook() throws Exception {
        doNothing().when(deleteBookUseCase).execute(1L);

        mockMvc.perform(delete("/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldSearchBooks() throws Exception {
        SearchBookOutput searchResult = new SearchBookOutput(
                1L, 100L, "Dom Casmurro", "Machado de Assis",
                "url", "url", "pt", "Fiction", 5000);
        Page<SearchBookOutput> page = new PageImpl<>(List.of(searchResult));
        when(searchBookByTitleUseCase.execute(eq("Dom"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/books/search")
                        .param("query", "Dom")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Dom Casmurro"));
    }

    @Test
    void shouldReturn404WhenBookNotFound() throws Exception {
        when(getBookUseCase.execute(99L))
                .thenThrow(new com.pucsp.alexandria.domain.book.exception.BookNotFoundException(99L));

        mockMvc.perform(get("/books/99"))
                .andExpect(status().isNotFound());
    }
}
