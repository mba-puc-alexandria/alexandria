package com.betrybe.alexandria.config;

import com.betrybe.alexandria.application.author.CreateAuthorUseCase;
import com.betrybe.alexandria.application.author.DeleteAuthorUseCase;
import com.betrybe.alexandria.application.author.GetAuthorUseCase;
import com.betrybe.alexandria.application.author.ListAuthorsUseCase;
import com.betrybe.alexandria.application.author.UpdateAuthorUseCase;
import com.betrybe.alexandria.application.book.CreateBookUseCase;
import com.betrybe.alexandria.application.book.DeleteBookUseCase;
import com.betrybe.alexandria.application.book.GetBookUseCase;
import com.betrybe.alexandria.application.book.ListBooksUseCase;
import com.betrybe.alexandria.application.book.UpdateBookUseCase;
import com.betrybe.alexandria.application.publisher.CreatePublisherUseCase;
import com.betrybe.alexandria.application.publisher.DeletePublisherUseCase;
import com.betrybe.alexandria.application.publisher.GetPublisherUseCase;
import com.betrybe.alexandria.application.publisher.ListPublishersUseCase;
import com.betrybe.alexandria.application.publisher.UpdatePublisherUseCase;
import com.betrybe.alexandria.domain.author.AuthorRepository;
import com.betrybe.alexandria.domain.book.BookRepository;
import com.betrybe.alexandria.domain.publisher.PublisherRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

  @Bean
  public CreateBookUseCase createBookUseCase(BookRepository bookRepository) {
    return new CreateBookUseCase(bookRepository);
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
  public CreateAuthorUseCase createAuthorUseCase(AuthorRepository authorRepository) {
    return new CreateAuthorUseCase(authorRepository);
  }

  @Bean
  public GetAuthorUseCase getAuthorUseCase(AuthorRepository authorRepository) {
    return new GetAuthorUseCase(authorRepository);
  }

  @Bean
  public ListAuthorsUseCase listAuthorsUseCase(AuthorRepository authorRepository) {
    return new ListAuthorsUseCase(authorRepository);
  }

  @Bean
  public UpdateAuthorUseCase updateAuthorUseCase(AuthorRepository authorRepository) {
    return new UpdateAuthorUseCase(authorRepository);
  }

  @Bean
  public DeleteAuthorUseCase deleteAuthorUseCase(AuthorRepository authorRepository) {
    return new DeleteAuthorUseCase(authorRepository);
  }

  @Bean
  public CreatePublisherUseCase createPublisherUseCase(PublisherRepository publisherRepository) {
    return new CreatePublisherUseCase(publisherRepository);
  }

  @Bean
  public GetPublisherUseCase getPublisherUseCase(PublisherRepository publisherRepository) {
    return new GetPublisherUseCase(publisherRepository);
  }

  @Bean
  public ListPublishersUseCase listPublishersUseCase(PublisherRepository publisherRepository) {
    return new ListPublishersUseCase(publisherRepository);
  }

  @Bean
  public UpdatePublisherUseCase updatePublisherUseCase(PublisherRepository publisherRepository) {
    return new UpdatePublisherUseCase(publisherRepository);
  }

  @Bean
  public DeletePublisherUseCase deletePublisherUseCase(PublisherRepository publisherRepository) {
    return new DeletePublisherUseCase(publisherRepository);
  }
}




