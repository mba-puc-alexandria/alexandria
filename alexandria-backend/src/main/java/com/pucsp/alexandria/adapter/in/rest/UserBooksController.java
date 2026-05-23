package com.pucsp.alexandria.adapter.in.rest;

import com.pucsp.alexandria.adapter.in.rest.dto.AddUserBooksRequest;
import com.pucsp.alexandria.adapter.in.rest.dto.UpdateUserBooksRequest;
import com.pucsp.alexandria.adapter.in.rest.dto.UserBooksResponse;
import com.pucsp.alexandria.application.userbooks.AddUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.ListUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.RemoveUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.UpdateUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.dto.AddUserBooksInput;
import com.pucsp.alexandria.application.userbooks.dto.UpdateUserBooksInput;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/user-books")
@SecurityRequirement(name = "bearerAuth")
public class UserBooksController {

  private final AddUserBooksUseCase addUserBooksUseCase;
  private final ListUserBooksUseCase listUserBooksUseCase;
  private final UpdateUserBooksUseCase updateUserBooksUseCase;
  private final RemoveUserBooksUseCase removeUserBooksUseCase;

  public UserBooksController(
      AddUserBooksUseCase addUserBooksUseCase,
      ListUserBooksUseCase listUserBooksUseCase,
      UpdateUserBooksUseCase updateUserBooksUseCase,
      RemoveUserBooksUseCase removeUserBooksUseCase) {
    this.addUserBooksUseCase = addUserBooksUseCase;
    this.listUserBooksUseCase = listUserBooksUseCase;
    this.updateUserBooksUseCase = updateUserBooksUseCase;
    this.removeUserBooksUseCase = removeUserBooksUseCase;
  }

  @GetMapping
  public ResponseEntity<Page<UserBooksResponse>> list(
      Authentication authentication,
      @RequestParam(required = false) String status,
      Pageable pageable) {
    Long userId = (Long) authentication.getPrincipal();
    var page = listUserBooksUseCase.execute(userId, status, pageable);
    return ResponseEntity.ok(page.map(UserBooksResponse::from));
  }

  @PostMapping
  public ResponseEntity<UserBooksResponse> add(
      Authentication authentication,
      @RequestBody AddUserBooksRequest request) {
    Long userId = (Long) authentication.getPrincipal();
    var input = new AddUserBooksInput(request.bookId(), request.status());
    var output = addUserBooksUseCase.execute(userId, input);
    return ResponseEntity.status(HttpStatus.CREATED).body(UserBooksResponse.from(output));
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserBooksResponse> update(
      Authentication authentication,
      @PathVariable Long id,
      @RequestBody UpdateUserBooksRequest request) {
    Long userId = (Long) authentication.getPrincipal();
    var input = new UpdateUserBooksInput(request.status(), request.progress(), request.rating());
    var output = updateUserBooksUseCase.execute(userId, id, input);
    return ResponseEntity.ok(UserBooksResponse.from(output));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> remove(
      Authentication authentication,
      @PathVariable Long id) {
    Long userId = (Long) authentication.getPrincipal();
    removeUserBooksUseCase.execute(userId, id);
    return ResponseEntity.noContent().build();
  }
}
