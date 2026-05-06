package com.pucsp.alexandria.adapter.out.persistence.external.gutendex;

import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexSearchResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GutendexClient {

  private static final String GUTENDEX_API_URL = "https://gutendex.com";
  private static final String SEARCH_BOOKS_ENDPOINT = "/books";
  private static final String SEARCH_BOOKS_DEFAULT_LANGUAGE = "pt";

  private final RestTemplate restTemplate;

  public GutendexClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public GutendexSearchResponse searchByTitle(String query) {
    String url = UriComponentsBuilder
        .fromUriString(GUTENDEX_API_URL + SEARCH_BOOKS_ENDPOINT)
        .queryParam("search", query)
        .queryParam("languages", SEARCH_BOOKS_DEFAULT_LANGUAGE)
        .toUriString();

    return restTemplate.getForObject(url, GutendexSearchResponse.class);
  }

  public GutendexSearchResponse getPage(int page) {
    String url = UriComponentsBuilder
        .fromUriString(GUTENDEX_API_URL + SEARCH_BOOKS_ENDPOINT)
        .queryParam("page", page)
        .queryParam("languages", SEARCH_BOOKS_DEFAULT_LANGUAGE)
        .toUriString();

    return restTemplate.getForObject(url, GutendexSearchResponse.class);
  }
}
