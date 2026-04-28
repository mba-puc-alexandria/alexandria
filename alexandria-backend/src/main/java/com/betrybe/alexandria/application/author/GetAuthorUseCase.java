package com.betrybe.alexandria.application.author;

import com.betrybe.alexandria.application.author.dto.AuthorOutput;
import com.betrybe.alexandria.domain.author.Author;
import com.betrybe.alexandria.domain.author.AuthorRepository;
import com.betrybe.alexandria.domain.author.exception.AuthorNotFoundException;

public class GetAuthorUseCase {

  private final AuthorRepository authorRepository;

  public GetAuthorUseCase(AuthorRepository authorRepository) {
    this.authorRepository = authorRepository;
  }

  public AuthorOutput execute(Long id) {
    Author author = authorRepository.findById(id)
        .orElseThrow(() -> new AuthorNotFoundException(id));

    return AuthorOutput.from(author);
  }
}

