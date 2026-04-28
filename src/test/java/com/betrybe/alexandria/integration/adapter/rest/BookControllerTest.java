package com.betrybe.alexandria.integration.adapter.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.betrybe.alexandria.adapter.in.rest.dto.CreateBookRequest;
import com.betrybe.alexandria.adapter.in.rest.dto.UpdateBookRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("BookController Integration Tests")
class BookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private CreateBookRequest createRequest;
  private UpdateBookRequest updateRequest;

  @BeforeEach
  void setUp() {
    createRequest = new CreateBookRequest("Clean Code", "Programming", 1L);
    updateRequest = new UpdateBookRequest("The Pragmatic Programmer", "Software", 1L);
  }

  @Test
  @DisplayName("Should create a book and return 201")
  void testCreateBook() throws Exception {
    mockMvc.perform(post("/books")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.title").value("Clean Code"))
        .andExpect(jsonPath("$.genre").value("Programming"));
  }

  @Test
  @DisplayName("Should get book by id")
  void testGetBookById() throws Exception {
    var created = mockMvc.perform(post("/books")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(createRequest)));

    mockMvc.perform(get("/books/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Clean Code"));
  }

  @Test
  @DisplayName("Should return 404 when book not found")
  void testGetBookNotFound() throws Exception {
    mockMvc.perform(get("/books/999"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should list all books with pagination")
  void testListBooks() throws Exception {
    mockMvc.perform(post("/books")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/books?page=0&size=10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  @DisplayName("Should update a book")
  void testUpdateBook() throws Exception {
    mockMvc.perform(post("/books")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isCreated());

    mockMvc.perform(put("/books/1")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("The Pragmatic Programmer"));
  }

  @Test
  @DisplayName("Should delete a book")
  void testDeleteBook() throws Exception {
    mockMvc.perform(post("/books")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isCreated());

    mockMvc.perform(delete("/books/1"))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/books/1"))
        .andExpect(status().isNotFound());
  }
}

