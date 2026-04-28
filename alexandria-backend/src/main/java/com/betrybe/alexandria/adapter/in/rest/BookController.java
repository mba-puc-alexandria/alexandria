package com.betrybe.alexandria.adapter.in.rest;

import com.betrybe.alexandria.adapter.in.rest.dto.BookResponse;
import com.betrybe.alexandria.adapter.in.rest.dto.CreateBookRequest;
import com.betrybe.alexandria.adapter.in.rest.dto.UpdateBookRequest;
import com.betrybe.alexandria.application.book.CreateBookUseCase;
import com.betrybe.alexandria.application.book.DeleteBookUseCase;
import com.betrybe.alexandria.application.book.GetBookUseCase;
import com.betrybe.alexandria.application.book.ListBooksUseCase;
import com.betrybe.alexandria.application.book.UpdateBookUseCase;
import com.betrybe.alexandria.application.book.dto.CreateBookInput;
import com.betrybe.alexandria.application.book.dto.UpdateBookInput;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
public class BookController {

  private final CreateBookUseCase createBookUseCase;
  private final GetBookUseCase getBookUseCase;
  private final ListBooksUseCase listBooksUseCase;
  private final UpdateBookUseCase updateBookUseCase;
  private final DeleteBookUseCase deleteBookUseCase;

  public BookController(
      CreateBookUseCase createBookUseCase,
      GetBookUseCase getBookUseCase,
      ListBooksUseCase listBooksUseCase,
      UpdateBookUseCase updateBookUseCase,
      DeleteBookUseCase deleteBookUseCase
  ) {
    this.createBookUseCase = createBookUseCase;
    this.getBookUseCase = getBookUseCase;
    this.listBooksUseCase = listBooksUseCase;
    this.updateBookUseCase = updateBookUseCase;
    this.deleteBookUseCase = deleteBookUseCase;
  }

  @PostMapping
  public ResponseEntity<BookResponse> create(@RequestBody CreateBookRequest request) {
    CreateBookInput input = new CreateBookInput(
        request.title(),
        request.genre(),
        request.publisherId()
    );
    var output = createBookUseCase.execute(input);
    var book = getBookUseCase.execute(output.id());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BookResponse.from(book));
  }

  @GetMapping("/{id}")
  public ResponseEntity<BookResponse> getById(@PathVariable Long id) {
    var output = getBookUseCase.execute(id);
    return ResponseEntity.ok(BookResponse.from(output));
  }

  @GetMapping
  public ResponseEntity<Page<BookResponse>> getAll(Pageable pageable) {
    var page = listBooksUseCase.execute(pageable);
    return ResponseEntity.ok(page.map(BookResponse::from));
  }

  @PutMapping("/{id}")
  public ResponseEntity<BookResponse> update(
      @PathVariable Long id,
      @RequestBody UpdateBookRequest request
  ) {
    UpdateBookInput input = new UpdateBookInput(
        request.title(),
        request.genre(),
        request.publisherId()
    );
    var output = updateBookUseCase.execute(id, input);
    return ResponseEntity.ok(BookResponse.from(output));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    deleteBookUseCase.execute(id);
    return ResponseEntity.noContent().build();
  }
}

