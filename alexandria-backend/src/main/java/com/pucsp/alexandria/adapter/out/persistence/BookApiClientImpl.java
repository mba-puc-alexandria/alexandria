package com.pucsp.alexandria.adapter.out.persistence;

import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.GutendexClient;
import com.pucsp.alexandria.adapter.out.persistence.external.mapper.GutendexMapper;
import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexSearchResponse;
import com.pucsp.alexandria.domain.book.external.BookApiClient;
import com.pucsp.alexandria.domain.book.external.BookData;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class BookApiClientImpl implements BookApiClient {

  private final GutendexClient gutendexClient;
  private final GutendexMapper gutendexMapper;

  public BookApiClientImpl(GutendexClient gutendexClient, GutendexMapper gutendexMapper) {
    this.gutendexClient = gutendexClient;
    this.gutendexMapper = gutendexMapper;
  }

  @Override
  public List<BookData> searchByTitle(String query) {
    GutendexSearchResponse response = gutendexClient.searchByTitle(query);

    if (response == null || response.results() == null || response.results().isEmpty()) {
      return List.of();
    }

    return response.results().stream()
        .map(gutendexMapper::toBookData)
        .toList();
  }

  @Override
  public List<BookData> getPage(int page) {
    GutendexSearchResponse response = gutendexClient.getPage(page);

    if (response == null || response.results() == null || response.results().isEmpty()) {
      return List.of();
    }

    return response.results().stream()
        .map(gutendexMapper::toBookData)
        .toList();
  }
}
