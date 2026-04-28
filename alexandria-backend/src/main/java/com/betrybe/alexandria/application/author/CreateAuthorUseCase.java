package com.betrybe.alexandria.application.author;

import com.betrybe.alexandria.application.author.dto.CreateAuthorInput;
import com.betrybe.alexandria.application.author.dto.CreateAuthorOutput;
import com.betrybe.alexandria.domain.author.Author;
import com.betrybe.alexandria.domain.author.AuthorRepository;
import com.betrybe.alexandria.domain.author.exception.DuplicateAuthorException;

public class CreateAuthorUseCase {

  private final AuthorRepository authorRepository;

  public CreateAuthorUseCase(AuthorRepository authorRepository) {
    this.authorRepository = authorRepository;
  }

  public CreateAuthorOutput execute(CreateAuthorInput input) {
    if (authorRepository.existsByName(input.name())) {
      throw new DuplicateAuthorException(input.name());
    }

    Author author = Author.create(input.name(), input.biography());
    Author saved = authorRepository.save(author);

    return new CreateAuthorOutput(saved.getId().getValue());
  }
}

