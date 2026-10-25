package com.pucsp.alexandria.application.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.BookSource;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import com.pucsp.alexandria.domain.subscription.Subscription;
import com.pucsp.alexandria.domain.subscription.SubscriptionRepository;
import com.pucsp.alexandria.domain.subscription.SubscriptionStatus;
import com.pucsp.alexandria.domain.subscription.exception.SubscriptionRequiredException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Risco R1 do plano mestre: leitura de EPUB liberada sem assinatura válida.
 * Casos de teste CT-ASS-02, 04, 05, 06 e 07 (SCRUM-118, 120 a 123).
 */
@ExtendWith(MockitoExtension.class)
class GetBookEpubUseCaseTest {

  private static final Long USER_ID = 7L;
  private static final Long BOOK_ID = 1L;
  private static final String EPUB_URL = "https://www.gutenberg.org/ebooks/1342.epub.images";

  @Mock
  private BookRepository bookRepository;

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @InjectMocks
  private GetBookEpubUseCase getBookEpubUseCase;

  @BeforeEach
  void setUp() {
    Book book = Book.restore(BOOK_ID, "Pride and Prejudice", Set.of(1L), 1342L, EPUB_URL,
        null, "en", null, null, null, BookSource.GUTENDEX);
    lenient().when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
  }

  private Subscription subscription(SubscriptionStatus status, LocalDateTime trialEndsAt,
      LocalDateTime periodEndsAt) {
    return Subscription.restore(10L, USER_ID, status, trialEndsAt, periodEndsAt, null, null,
        null, null, 0, null, null, false, LocalDateTime.now().minusDays(20),
        LocalDateTime.now());
  }

  private void givenSubscription(Subscription subscription) {
    when(subscriptionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(subscription));
  }

  @Test
  @DisplayName("CT-ASS-02: trial vigente libera a leitura")
  void trialActiveAllowsReading() {
    givenSubscription(subscription(SubscriptionStatus.TRIALING,
        LocalDateTime.now().plusDays(1), null));

    assertEquals(EPUB_URL, getBookEpubUseCase.execute(USER_ID, BOOK_ID));
  }

  @Test
  @DisplayName("CT-ASS-04: trial vencido bloqueia a leitura")
  void expiredTrialBlocksReading() {
    givenSubscription(subscription(SubscriptionStatus.TRIALING,
        LocalDateTime.now().minusMinutes(1), null));

    assertThrows(SubscriptionRequiredException.class,
        () -> getBookEpubUseCase.execute(USER_ID, BOOK_ID));
  }

  @Test
  @DisplayName("CT-ASS-05: assinatura ACTIVE vigente libera a leitura")
  void activeSubscriptionAllowsReading() {
    givenSubscription(subscription(SubscriptionStatus.ACTIVE,
        null, LocalDateTime.now().plusDays(10)));

    assertEquals(EPUB_URL, getBookEpubUseCase.execute(USER_ID, BOOK_ID));
  }

  @Test
  @DisplayName("CT-ASS-06: assinatura PAST_DUE bloqueia mesmo com data futura")
  void pastDueBlocksReading() {
    givenSubscription(subscription(SubscriptionStatus.PAST_DUE,
        null, LocalDateTime.now().plusDays(10)));

    assertThrows(SubscriptionRequiredException.class,
        () -> getBookEpubUseCase.execute(USER_ID, BOOK_ID));
  }

  @Test
  @DisplayName("CT-ASS-07: cancelada dentro do período pago ainda libera")
  void canceledWithinPaidPeriodAllowsReading() {
    givenSubscription(subscription(SubscriptionStatus.CANCELED,
        null, LocalDateTime.now().plusDays(3)));

    assertEquals(EPUB_URL, getBookEpubUseCase.execute(USER_ID, BOOK_ID));
  }

  @Test
  @DisplayName("CT-ASS-07: cancelada com período vencido bloqueia")
  void canceledAfterPaidPeriodBlocksReading() {
    givenSubscription(subscription(SubscriptionStatus.CANCELED,
        null, LocalDateTime.now().minusDays(1)));

    assertThrows(SubscriptionRequiredException.class,
        () -> getBookEpubUseCase.execute(USER_ID, BOOK_ID));
  }

  @Test
  @DisplayName("Assinatura EXPIRED bloqueia a leitura")
  void expiredSubscriptionBlocksReading() {
    givenSubscription(subscription(SubscriptionStatus.EXPIRED,
        null, LocalDateTime.now().minusDays(1)));

    assertThrows(SubscriptionRequiredException.class,
        () -> getBookEpubUseCase.execute(USER_ID, BOOK_ID));
  }

  @Test
  @DisplayName("Usuário sem assinatura cadastrada é bloqueado")
  void userWithoutSubscriptionIsBlocked() {
    when(subscriptionRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

    assertThrows(SubscriptionRequiredException.class,
        () -> getBookEpubUseCase.execute(USER_ID, BOOK_ID));
  }

  @Test
  @DisplayName("Livro sem EPUB disponível responde como não encontrado")
  void bookWithoutDownloadUrlIsNotFound() {
    Book withoutEpub = Book.restore(2L, "Sem EPUB", Set.of(1L), null, null,
        null, "en", null, null, null, BookSource.GUTENDEX);
    when(bookRepository.findById(2L)).thenReturn(Optional.of(withoutEpub));

    assertThrows(BookNotFoundException.class,
        () -> getBookEpubUseCase.execute(USER_ID, 2L));
  }
}
