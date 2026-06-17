package com.pucsp.alexandria.config;

import com.pucsp.alexandria.application.auth.AuthenticateUserUseCase;
import com.pucsp.alexandria.application.auth.GoogleAuthUseCase;
import com.pucsp.alexandria.application.auth.RegisterUserUseCase;
import com.pucsp.alexandria.application.book.CreateBookUseCase;
import com.pucsp.alexandria.application.book.DeleteBookUseCase;
import com.pucsp.alexandria.application.book.SyncAllGutendexBooksUseCase;
import com.pucsp.alexandria.application.book.GetBookUseCase;
import com.pucsp.alexandria.application.book.ListBooksUseCase;
import com.pucsp.alexandria.application.book.SearchBookByTitleUseCase;
import com.pucsp.alexandria.application.book.UpdateBookUseCase;
import com.pucsp.alexandria.application.profile.GetProfileUseCase;
import com.pucsp.alexandria.application.profile.UpdatePasswordUseCase;
import com.pucsp.alexandria.application.profile.UpdateProfileUseCase;
import com.pucsp.alexandria.application.userbooks.AddUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.ListUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.RemoveUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.UpdateUserBooksUseCase;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.external.BookApiClient;
import com.pucsp.alexandria.domain.user.UserRepository;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfiguration {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public CreateBookUseCase createBookUseCase(
      BookRepository bookRepository,
      AuthorRepository authorRepository,
      BookApiClient bookApiClient) {
    return new CreateBookUseCase(bookRepository, authorRepository, bookApiClient);
  }

  @Bean
  public GetBookUseCase getBookUseCase(
      BookRepository bookRepository,
      AuthorRepository authorRepository) {
    return new GetBookUseCase(bookRepository, authorRepository);
  }

  @Bean
  public ListBooksUseCase listBooksUseCase(
      BookRepository bookRepository,
      AuthorRepository authorRepository) {
    return new ListBooksUseCase(bookRepository, authorRepository);
  }

  @Bean
  public UpdateBookUseCase updateBookUseCase(
      BookRepository bookRepository,
      AuthorRepository authorRepository) {
    return new UpdateBookUseCase(bookRepository, authorRepository);
  }

  @Bean
  public DeleteBookUseCase deleteBookUseCase(BookRepository bookRepository) {
    return new DeleteBookUseCase(bookRepository);
  }

  @Bean
  public SearchBookByTitleUseCase searchBookByTitleUseCase(
      BookRepository bookRepository,
      AuthorRepository authorRepository) {
    return new SearchBookByTitleUseCase(bookRepository, authorRepository);
  }

  @Bean
  public AddUserBooksUseCase addUserBooksUseCase(
      UserBooksRepository userBooksRepository,
      BookRepository bookRepository,
      AuthorRepository authorRepository) {
    return new AddUserBooksUseCase(userBooksRepository, bookRepository, authorRepository);
  }

  @Bean
  public ListUserBooksUseCase listUserBooksUseCase(
      UserBooksRepository userBooksRepository,
      BookRepository bookRepository,
      AuthorRepository authorRepository) {
    return new ListUserBooksUseCase(userBooksRepository, bookRepository, authorRepository);
  }

  @Bean
  public UpdateUserBooksUseCase updateUserBooksUseCase(
      UserBooksRepository userBooksRepository,
      BookRepository bookRepository,
      AuthorRepository authorRepository) {
    return new UpdateUserBooksUseCase(userBooksRepository, bookRepository, authorRepository);
  }

  @Bean
  public RemoveUserBooksUseCase removeUserBooksUseCase(
      UserBooksRepository userBooksRepository) {
    return new RemoveUserBooksUseCase(userBooksRepository);
  }

  @Bean
  public RegisterUserUseCase registerUserUseCase(UserRepository userRepository) {
    return new RegisterUserUseCase(userRepository);
  }

  @Bean
  public GoogleAuthUseCase googleAuthUseCase(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      RestTemplate restTemplate,
      @Value("${google.client-id}") String googleClientId) {
    return new GoogleAuthUseCase(userRepository, passwordEncoder, restTemplate, googleClientId);
  }

  @Bean
  public AuthenticateUserUseCase authenticateUserUseCase(UserRepository userRepository,
                                                          PasswordEncoder passwordEncoder) {
    return new AuthenticateUserUseCase(userRepository, passwordEncoder);
  }

  @Bean
  public SyncAllGutendexBooksUseCase syncAllGutendexBooksUseCase(
      CreateBookUseCase createBookUseCase) {
    return new SyncAllGutendexBooksUseCase(createBookUseCase);
  }

  @Bean
  public GetProfileUseCase getProfileUseCase(UserRepository userRepository) {
    return new GetProfileUseCase(userRepository);
  }

  @Bean
  public UpdateProfileUseCase updateProfileUseCase(UserRepository userRepository) {
    return new UpdateProfileUseCase(userRepository);
  }

  @Bean
  public UpdatePasswordUseCase updatePasswordUseCase(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder) {
    return new UpdatePasswordUseCase(userRepository, passwordEncoder);
  }
}
