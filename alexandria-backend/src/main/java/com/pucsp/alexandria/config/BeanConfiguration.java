package com.pucsp.alexandria.config;

import com.pucsp.alexandria.application.book.CreateBookUseCase;
import com.pucsp.alexandria.application.book.DeleteBookUseCase;
import com.pucsp.alexandria.application.book.GetBookUseCase;
import com.pucsp.alexandria.application.book.ListBooksUseCase;
import com.pucsp.alexandria.application.book.SearchBookByTitleUseCase;
import com.pucsp.alexandria.application.book.UpdateBookUseCase;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.external.BookApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfiguration {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public CreateBookUseCase createBookUseCase(BookRepository bookRepository, BookApiClient bookApiClient) {
    return new CreateBookUseCase(bookRepository, bookApiClient);
  }

  @Bean
  public GetBookUseCase getBookUseCase(BookRepository bookRepository) {
    return new GetBookUseCase(bookRepository);
  }

  @Bean
  public ListBooksUseCase listBooksUseCase(BookRepository bookRepository) {
    return new ListBooksUseCase(bookRepository);
  }

  @Bean
  public UpdateBookUseCase updateBookUseCase(BookRepository bookRepository) {
    return new UpdateBookUseCase(bookRepository);
  }

  @Bean
  public DeleteBookUseCase deleteBookUseCase(BookRepository bookRepository) {
    return new DeleteBookUseCase(bookRepository);
  }

  @Bean
  public SearchBookByTitleUseCase searchBookByTitleUseCase(BookRepository bookRepository) {
    return new SearchBookByTitleUseCase(bookRepository);
  }
}











