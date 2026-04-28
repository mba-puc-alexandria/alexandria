package com.betrybe.alexandria.application.author;

import com.betrybe.alexandria.domain.author.Author;
import com.betrybe.alexandria.domain.author.AuthorRepository;
import com.betrybe.alexandria.domain.author.exception.AuthorNotFoundException;

public class DeleteAuthorUseCase {

  private final AuthorRepository authorRepository;

  public DeleteAuthorUseCase(AuthorRepository authorRepository) {
    this.authorRepository = authorRepository;
  }

  public void execute(Long id) {
    Author author = authorRepository.findById(id)
        .orElseThrow(() -> new AuthorNotFoundException(id));

    authorRepository.delete(author);
  }
}

