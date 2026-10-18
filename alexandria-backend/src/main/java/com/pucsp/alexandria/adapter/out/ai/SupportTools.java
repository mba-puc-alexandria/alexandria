package com.pucsp.alexandria.adapter.out.ai;

import com.pucsp.alexandria.application.book.ListBooksUseCase;
import com.pucsp.alexandria.application.book.SearchBookByTitleUseCase;
import com.pucsp.alexandria.application.book.dto.BookOutput.AuthorInfo;
import com.pucsp.alexandria.application.book.dto.SearchBookOutput;
import com.pucsp.alexandria.application.subscription.GetSubscriptionUseCase;
import com.pucsp.alexandria.application.userbooks.ListUserBooksUseCase;
import com.pucsp.alexandria.domain.subscription.exception.SubscriptionNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
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
  private final ListBooksUseCase listBooks;
  private final GetSubscriptionUseCase getSubscription;
  private final ListUserBooksUseCase listUserBooks;

  public SupportTools(Long userId, SearchBookByTitleUseCase searchBooks, ListBooksUseCase listBooks,
      GetSubscriptionUseCase getSubscription, ListUserBooksUseCase listUserBooks) {
    this.userId = userId;
    this.searchBooks = searchBooks;
    this.listBooks = listBooks;
    this.getSubscription = getSubscription;
    this.listUserBooks = listUserBooks;
  }

  public record BookSummary(Long id, String title, String authors, String languages) {}

  public record UserBookSummary(String title, String status, Integer progress) {}

  @Tool(description = "Busca livros no acervo inteiro do Alexandria por título OU nome do autor. "
      + "Use para qualquer pergunta sobre um livro ou autor específico. Retorna até 5 resultados.")
  public List<BookSummary> buscarLivros(
      @ToolParam(description = "Parte do título original ou do nome do autor, ex.: 'Austen', 'Dracula'")
      String termo) {
    return searchBooks.execute(termo, PageRequest.of(0, 5)).stream()
        .map(b -> new BookSummary(b.id(), b.title(), b.authorsDisplay(), b.languages()))
        .toList();
  }

  @Tool(description = "Lista os primeiros 20 livros do acervo do Alexandria. "
      + "Use quando o usuário perguntar de forma geral quais livros existem no acervo.")
  public List<BookSummary> listarAcervo() {
    return listBooks.execute(PageRequest.of(0, 20)).stream()
        .map(b -> new BookSummary(b.id(), b.title(),
            b.authors().stream().map(AuthorInfo::name).collect(Collectors.joining(", ")), b.languages()))
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

  @Tool(description = "Lista até 10 livros da biblioteca PESSOAL do usuário logado (livros que ele "
      + "adicionou), com status e progresso. Não use para perguntas sobre o acervo.")
  public List<UserBookSummary> meusLivros() {
    return listUserBooks.execute(userId, null, PageRequest.of(0, 10)).stream()
        .map(ub -> new UserBookSummary(ub.book().title(), ub.status(), ub.progress()))
        .toList();
  }
}
