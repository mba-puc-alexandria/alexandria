package com.pucsp.alexandria.adapter.out.ai;

import com.pucsp.alexandria.application.book.SearchBookByTitleUseCase;
import com.pucsp.alexandria.application.book.dto.SearchBookOutput;
import com.pucsp.alexandria.application.subscription.GetSubscriptionUseCase;
import com.pucsp.alexandria.application.userbooks.ListUserBooksUseCase;
import com.pucsp.alexandria.domain.subscription.exception.SubscriptionNotFoundException;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;

/**
 * Ferramentas que o modelo pode chamar. O userId vem do JWT e fica fixo no construtor: o modelo
 * nunca escolhe de qual usuário ler os dados (menor privilégio).
 */
public class SupportTools {

  private final Long userId;
  private final SearchBookByTitleUseCase searchBooks;
  private final GetSubscriptionUseCase getSubscription;
  private final ListUserBooksUseCase listUserBooks;

  public SupportTools(Long userId, SearchBookByTitleUseCase searchBooks,
      GetSubscriptionUseCase getSubscription, ListUserBooksUseCase listUserBooks) {
    this.userId = userId;
    this.searchBooks = searchBooks;
    this.getSubscription = getSubscription;
    this.listUserBooks = listUserBooks;
  }

  public record BookSummary(Long id, String title, String authors, String languages) {}

  public record UserBookSummary(String title, String status, Integer progress) {}

  @Tool(description = "Busca livros no acervo do Alexandria pelo título. Retorna até 5 resultados.")
  public List<BookSummary> buscarLivros(@ToolParam(description = "Parte do título do livro") String titulo) {
    return searchBooks.execute(titulo, PageRequest.of(0, 5)).stream()
        .map(b -> new BookSummary(b.id(), b.title(), b.authorsDisplay(), b.languages()))
        .toList();
  }

  @Tool(description = "Consulta a assinatura do usuário logado: status, fim do teste grátis, "
      + "fim do período atual, preço e situação do último pagamento.")
  public Object minhaAssinatura() {
    try {
      return getSubscription.execute(userId);
    } catch (SubscriptionNotFoundException e) {
      return "O usuário não possui assinatura.";
    }
  }

  @Tool(description = "Lista até 10 livros da biblioteca pessoal do usuário logado, com status e progresso de leitura.")
  public List<UserBookSummary> meusLivros() {
    return listUserBooks.execute(userId, null, PageRequest.of(0, 10)).stream()
        .map(ub -> new UserBookSummary(ub.book().title(), ub.status(), ub.progress()))
        .toList();
  }
}
