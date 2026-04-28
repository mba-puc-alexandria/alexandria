package com.betrybe.alexandria.adapter.in.rest;

import com.betrybe.alexandria.adapter.in.rest.dto.AuthorResponse;
import com.betrybe.alexandria.adapter.in.rest.dto.CreateAuthorRequest;
import com.betrybe.alexandria.adapter.in.rest.dto.UpdateAuthorRequest;
import com.betrybe.alexandria.application.author.CreateAuthorUseCase;
import com.betrybe.alexandria.application.author.DeleteAuthorUseCase;
import com.betrybe.alexandria.application.author.GetAuthorUseCase;
import com.betrybe.alexandria.application.author.ListAuthorsUseCase;
import com.betrybe.alexandria.application.author.UpdateAuthorUseCase;
import com.betrybe.alexandria.application.author.dto.CreateAuthorInput;
import com.betrybe.alexandria.application.author.dto.UpdateAuthorInput;
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
@RequestMapping("/authors")
public class AuthorController {

  private final CreateAuthorUseCase createAuthorUseCase;
  private final GetAuthorUseCase getAuthorUseCase;
  private final ListAuthorsUseCase listAuthorsUseCase;
  private final UpdateAuthorUseCase updateAuthorUseCase;
  private final DeleteAuthorUseCase deleteAuthorUseCase;

  public AuthorController(
      CreateAuthorUseCase createAuthorUseCase,
      GetAuthorUseCase getAuthorUseCase,
      ListAuthorsUseCase listAuthorsUseCase,
      UpdateAuthorUseCase updateAuthorUseCase,
      DeleteAuthorUseCase deleteAuthorUseCase
  ) {
    this.createAuthorUseCase = createAuthorUseCase;
    this.getAuthorUseCase = getAuthorUseCase;
    this.listAuthorsUseCase = listAuthorsUseCase;
    this.updateAuthorUseCase = updateAuthorUseCase;
    this.deleteAuthorUseCase = deleteAuthorUseCase;
  }

  @PostMapping
  public ResponseEntity<AuthorResponse> create(@RequestBody CreateAuthorRequest request) {
    CreateAuthorInput input = new CreateAuthorInput(request.name(), request.biography());
    var output = createAuthorUseCase.execute(input);
    var author = getAuthorUseCase.execute(output.id());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(AuthorResponse.from(author));
  }

  @GetMapping("/{id}")
  public ResponseEntity<AuthorResponse> getById(@PathVariable Long id) {
    var output = getAuthorUseCase.execute(id);
    return ResponseEntity.ok(AuthorResponse.from(output));
  }

  @GetMapping
  public ResponseEntity<Page<AuthorResponse>> getAll(Pageable pageable) {
    var page = listAuthorsUseCase.execute(pageable);
    return ResponseEntity.ok(page.map(AuthorResponse::from));
  }

  @PutMapping("/{id}")
  public ResponseEntity<AuthorResponse> update(
      @PathVariable Long id,
      @RequestBody UpdateAuthorRequest request
  ) {
    UpdateAuthorInput input = new UpdateAuthorInput(request.name(), request.biography());
    var output = updateAuthorUseCase.execute(id, input);
    return ResponseEntity.ok(AuthorResponse.from(output));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    deleteAuthorUseCase.execute(id);
    return ResponseEntity.noContent().build();
  }
}

