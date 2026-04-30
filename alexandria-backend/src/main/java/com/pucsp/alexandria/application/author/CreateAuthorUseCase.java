package com.pucsp.alexandria.application.author;

import com.pucsp.alexandria.application.author.dto.CreateAuthorInput;
import com.pucsp.alexandria.application.author.dto.CreateAuthorOutput;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.author.exception.DuplicateAuthorException;

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

