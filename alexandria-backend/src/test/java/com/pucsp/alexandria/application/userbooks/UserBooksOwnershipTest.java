package com.pucsp.alexandria.application.userbooks;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pucsp.alexandria.application.userbooks.dto.UpdateUserBooksInput;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.exception.UserBooksNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Risco R2 do plano mestre: BOLA (Broken Object Level Authorization) na biblioteca.
 * Casos de teste CT-BIB-08 e CT-BIB-09 (SCRUM-125 e SCRUM-126).
 */
@ExtendWith(MockitoExtension.class)
class UserBooksOwnershipTest {

  private static final Long OWNER_ID = 1L;
  private static final Long ATTACKER_ID = 2L;
  private static final Long USER_BOOK_ID = 50L;

  @Mock
  private UserBooksRepository userBooksRepository;

  @Mock
  private BookRepository bookRepository;

  @Mock
  private AuthorRepository authorRepository;

  private UserBooks ownersItem() {
    return UserBooks.restore(USER_BOOK_ID, OWNER_ID, 10L, "reading", 40, null,
        LocalDateTime.now().minusDays(3));
  }

  @Test
  @DisplayName("CT-BIB-09: usuário B não altera o progresso de um item do usuário A")
  void anotherUserCannotUpdateItem() {
    when(userBooksRepository.findById(USER_BOOK_ID)).thenReturn(Optional.of(ownersItem()));
    UpdateUserBooksUseCase useCase =
        new UpdateUserBooksUseCase(userBooksRepository, bookRepository, authorRepository);

    assertThrows(UserBooksNotFoundException.class,
        () -> useCase.execute(ATTACKER_ID, USER_BOOK_ID,
            new UpdateUserBooksInput("done", null, 5)));

    verify(userBooksRepository, never()).save(any());
  }

  @Test
  @DisplayName("CT-BIB-09: usuário B não remove um item do usuário A")
  void anotherUserCannotRemoveItem() {
    when(userBooksRepository.findById(USER_BOOK_ID)).thenReturn(Optional.of(ownersItem()));
    RemoveUserBooksUseCase useCase = new RemoveUserBooksUseCase(userBooksRepository);

    assertThrows(UserBooksNotFoundException.class,
        () -> useCase.execute(ATTACKER_ID, USER_BOOK_ID));

    verify(userBooksRepository, never()).delete(any());
  }

  @Test
  @DisplayName("O dono do item continua conseguindo remover")
  void ownerCanRemoveItem() {
    UserBooks item = ownersItem();
    when(userBooksRepository.findById(USER_BOOK_ID)).thenReturn(Optional.of(item));
    RemoveUserBooksUseCase useCase = new RemoveUserBooksUseCase(userBooksRepository);

    useCase.execute(OWNER_ID, USER_BOOK_ID);

    verify(userBooksRepository).delete(item);
  }
}
