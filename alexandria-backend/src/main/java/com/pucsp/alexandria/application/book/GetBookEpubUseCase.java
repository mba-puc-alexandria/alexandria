package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import com.pucsp.alexandria.domain.subscription.Subscription;
import com.pucsp.alexandria.domain.subscription.SubscriptionRepository;
import com.pucsp.alexandria.domain.subscription.exception.SubscriptionRequiredException;
import java.time.LocalDateTime;

public class GetBookEpubUseCase {

  private final BookRepository bookRepository;
  private final SubscriptionRepository subscriptionRepository;

  public GetBookEpubUseCase(
      BookRepository bookRepository,
      SubscriptionRepository subscriptionRepository) {
    this.bookRepository = bookRepository;
    this.subscriptionRepository = subscriptionRepository;
  }

  /**
   * Valida a assinatura e retorna a URL de download do EPUB.
   *
   * <p>O gate é duplo: exige usuário autenticado (no controller) e assinatura ativa
   * (trial válido ou período pago vigente).
   */
  public String execute(Long userId, Long bookId) {
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new BookNotFoundException(bookId));

    if (book.getDownloadUrl() == null || book.getDownloadUrl().isBlank()) {
      throw new BookNotFoundException(bookId);
    }

    Subscription subscription = subscriptionRepository.findByUserId(userId)
        .orElseThrow(SubscriptionRequiredException::new);

    if (!subscription.isAccessActive(LocalDateTime.now())) {
      throw new SubscriptionRequiredException();
    }

    return book.getDownloadUrl();
  }
}
