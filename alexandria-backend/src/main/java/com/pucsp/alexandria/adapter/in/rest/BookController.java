package com.pucsp.alexandria.adapter.in.rest;

import com.pucsp.alexandria.adapter.in.rest.dto.BookResponse;
import com.pucsp.alexandria.adapter.in.rest.dto.CreateBookRequest;
import com.pucsp.alexandria.adapter.in.rest.dto.SearchBookResponse;
import com.pucsp.alexandria.adapter.in.rest.dto.UpdateBookRequest;
import com.pucsp.alexandria.application.book.CreateBookUseCase;
import com.pucsp.alexandria.application.book.DeleteBookUseCase;
import com.pucsp.alexandria.application.book.GetBookEpubUseCase;
import com.pucsp.alexandria.application.book.GetBookUseCase;
import com.pucsp.alexandria.application.book.ListBooksUseCase;
import com.pucsp.alexandria.application.book.SearchBookByTitleUseCase;
import com.pucsp.alexandria.application.book.UpdateBookUseCase;
import com.pucsp.alexandria.application.book.dto.CreateBookInput;
import com.pucsp.alexandria.application.book.dto.UpdateBookInput;
import com.pucsp.alexandria.domain.shared.valueobject.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/books")
public class BookController {

  private final CreateBookUseCase createBookUseCase;
  private final GetBookUseCase getBookUseCase;
  private final GetBookEpubUseCase getBookEpubUseCase;
  private final ListBooksUseCase listBooksUseCase;
  private final UpdateBookUseCase updateBookUseCase;
  private final DeleteBookUseCase deleteBookUseCase;
  private final SearchBookByTitleUseCase searchBookByTitleUseCase;
  private final RestTemplate restTemplate;

  public BookController(
      CreateBookUseCase createBookUseCase,
      GetBookUseCase getBookUseCase,
      GetBookEpubUseCase getBookEpubUseCase,
      ListBooksUseCase listBooksUseCase,
      UpdateBookUseCase updateBookUseCase,
      DeleteBookUseCase deleteBookUseCase,
      SearchBookByTitleUseCase searchBookByTitleUseCase,
      RestTemplate restTemplate
  ) {
    this.createBookUseCase = createBookUseCase;
    this.getBookUseCase = getBookUseCase;
    this.getBookEpubUseCase = getBookEpubUseCase;
    this.listBooksUseCase = listBooksUseCase;
    this.updateBookUseCase = updateBookUseCase;
    this.deleteBookUseCase = deleteBookUseCase;
    this.searchBookByTitleUseCase = searchBookByTitleUseCase;
    this.restTemplate = restTemplate;
  }

  @PostMapping
  public ResponseEntity<Void> create(@RequestBody CreateBookRequest request) {
    createBookUseCase.execute(new CreateBookInput(request.page()));
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<BookResponse> getById(@PathVariable Long id) {
    var output = getBookUseCase.execute(id);
    return ResponseEntity.ok(BookResponse.from(output));
  }

  @GetMapping("/{id}/epub")
  public ResponseEntity<byte[]> getEpub(
      @PathVariable Long id,
      Authentication authentication) {
    AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
    String downloadUrl = getBookEpubUseCase.execute(user.id(), id);

    byte[] bytes = restTemplate.getForObject(downloadUrl, byte[].class);
    if (bytes == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("application/epub+zip"));
    headers.setContentDispositionFormData("attachment", "book-" + id + ".epub");

    return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
  }

  @GetMapping
  public ResponseEntity<Page<BookResponse>> getAll(
      @RequestParam(required = false) String language,
      Pageable pageable) {
    var page = listBooksUseCase.execute(language, pageable);
    return ResponseEntity.ok(page.map(BookResponse::from));
  }

  @PutMapping("/{id}")
  public ResponseEntity<BookResponse> update(
      @PathVariable Long id,
      @RequestBody UpdateBookRequest request
  ) {
    UpdateBookInput input = new UpdateBookInput(request.title());
    var output = updateBookUseCase.execute(id, input);
    return ResponseEntity.ok(BookResponse.from(output));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    deleteBookUseCase.execute(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/search")
  public ResponseEntity<Page<SearchBookResponse>> search(
      @RequestParam String query,
      Pageable pageable) {
    var page = searchBookByTitleUseCase.execute(query, pageable);
    return ResponseEntity.ok(page.map(SearchBookResponse::from));
  }
}
