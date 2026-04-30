package com.pucsp.alexandria.application.author;

import com.pucsp.alexandria.application.author.dto.AuthorOutput;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.author.exception.AuthorNotFoundException;

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

