package com.pucsp.alexandria.application.author;

import com.pucsp.alexandria.application.author.dto.UpdateAuthorInput;
import com.pucsp.alexandria.application.author.dto.AuthorOutput;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.author.exception.AuthorNotFoundException;

public class UpdateAuthorUseCase {

  private final AuthorRepository authorRepository;

  public UpdateAuthorUseCase(AuthorRepository authorRepository) {
    this.authorRepository = authorRepository;
  }

  public AuthorOutput execute(Long id, UpdateAuthorInput input) {
    Author author = authorRepository.findById(id)
        .orElseThrow(() -> new AuthorNotFoundException(id));

    Author updated = author.updateWith(input.name(), input.biography());
    Author saved = authorRepository.save(updated);

    return AuthorOutput.from(saved);
  }
}

