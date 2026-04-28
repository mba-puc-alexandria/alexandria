package com.betrybe.alexandria.application.author;

import com.betrybe.alexandria.application.author.dto.UpdateAuthorInput;
import com.betrybe.alexandria.application.author.dto.AuthorOutput;
import com.betrybe.alexandria.domain.author.Author;
import com.betrybe.alexandria.domain.author.AuthorRepository;
import com.betrybe.alexandria.domain.author.exception.AuthorNotFoundException;

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

