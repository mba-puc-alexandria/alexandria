package com.betrybe.alexandria.application.author;

import com.betrybe.alexandria.application.author.dto.AuthorOutput;
import com.betrybe.alexandria.domain.author.AuthorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ListAuthorsUseCase {

  private final AuthorRepository authorRepository;

  public ListAuthorsUseCase(AuthorRepository authorRepository) {
    this.authorRepository = authorRepository;
  }

  public Page<AuthorOutput> execute(Pageable pageable) {
    return authorRepository.findAll(pageable)
        .map(AuthorOutput::from);
  }
}

