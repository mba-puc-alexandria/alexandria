package com.pucsp.alexandria.adapter.out.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pucsp.alexandria.application.book.ListBooksUseCase;
import com.pucsp.alexandria.application.book.SearchBookByTitleUseCase;
import com.pucsp.alexandria.application.subscription.GetSubscriptionUseCase;
import com.pucsp.alexandria.application.userbooks.ListUserBooksUseCase;
import com.pucsp.alexandria.domain.subscription.exception.SubscriptionNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupportToolsTest {

  @Mock
  private SearchBookByTitleUseCase searchBooks;

  @Mock
  private ListBooksUseCase listBooks;

  @Mock
  private GetSubscriptionUseCase getSubscription;

  @Mock
  private ListUserBooksUseCase listUserBooks;

  @Test
  void subscriptionToolAlwaysUsesTheUserFromTheToken() {
    when(getSubscription.execute(42L)).thenThrow(new SubscriptionNotFoundException(42L));
    SupportTools tools = new SupportTools(42L, searchBooks, listBooks, getSubscription, listUserBooks);

    Object result = tools.minhaAssinatura();

    verify(getSubscription).execute(42L);
    assertThat(result).isEqualTo("O usuário não possui assinatura.");
  }
}
