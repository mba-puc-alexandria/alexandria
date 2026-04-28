package com.betrybe.alexandria.adapter.in.rest;

import com.betrybe.alexandria.adapter.in.rest.dto.PublisherResponse;
import com.betrybe.alexandria.adapter.in.rest.dto.CreatePublisherRequest;
import com.betrybe.alexandria.adapter.in.rest.dto.UpdatePublisherRequest;
import com.betrybe.alexandria.application.publisher.CreatePublisherUseCase;
import com.betrybe.alexandria.application.publisher.DeletePublisherUseCase;
import com.betrybe.alexandria.application.publisher.GetPublisherUseCase;
import com.betrybe.alexandria.application.publisher.ListPublishersUseCase;
import com.betrybe.alexandria.application.publisher.UpdatePublisherUseCase;
import com.betrybe.alexandria.application.publisher.dto.CreatePublisherInput;
import com.betrybe.alexandria.application.publisher.dto.UpdatePublisherInput;
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
@RequestMapping("/publishers")
public class PublisherController {

  private final CreatePublisherUseCase createPublisherUseCase;
  private final GetPublisherUseCase getPublisherUseCase;
  private final ListPublishersUseCase listPublishersUseCase;
  private final UpdatePublisherUseCase updatePublisherUseCase;
  private final DeletePublisherUseCase deletePublisherUseCase;

  public PublisherController(
      CreatePublisherUseCase createPublisherUseCase,
      GetPublisherUseCase getPublisherUseCase,
      ListPublishersUseCase listPublishersUseCase,
      UpdatePublisherUseCase updatePublisherUseCase,
      DeletePublisherUseCase deletePublisherUseCase
  ) {
    this.createPublisherUseCase = createPublisherUseCase;
    this.getPublisherUseCase = getPublisherUseCase;
    this.listPublishersUseCase = listPublishersUseCase;
    this.updatePublisherUseCase = updatePublisherUseCase;
    this.deletePublisherUseCase = deletePublisherUseCase;
  }

  @PostMapping
  public ResponseEntity<PublisherResponse> create(@RequestBody CreatePublisherRequest request) {
    CreatePublisherInput input = new CreatePublisherInput(request.name(), request.address());
    var output = createPublisherUseCase.execute(input);
    var publisher = getPublisherUseCase.execute(output.id());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(PublisherResponse.from(publisher));
  }

  @GetMapping("/{id}")
  public ResponseEntity<PublisherResponse> getById(@PathVariable Long id) {
    var output = getPublisherUseCase.execute(id);
    return ResponseEntity.ok(PublisherResponse.from(output));
  }

  @GetMapping
  public ResponseEntity<Page<PublisherResponse>> getAll(Pageable pageable) {
    var page = listPublishersUseCase.execute(pageable);
    return ResponseEntity.ok(page.map(PublisherResponse::from));
  }

  @PutMapping("/{id}")
  public ResponseEntity<PublisherResponse> update(
      @PathVariable Long id,
      @RequestBody UpdatePublisherRequest request
  ) {
    UpdatePublisherInput input = new UpdatePublisherInput(request.name(), request.address());
    var output = updatePublisherUseCase.execute(id, input);
    return ResponseEntity.ok(PublisherResponse.from(output));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    deletePublisherUseCase.execute(id);
    return ResponseEntity.noContent().build();
  }
}

