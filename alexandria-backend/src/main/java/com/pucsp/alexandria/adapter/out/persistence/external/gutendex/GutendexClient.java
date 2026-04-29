package com.pucsp.alexandria.adapter.out.persistence.external.gutendex;

import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexSearchResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GutendexClient {

  private static final String GUTENDEX_API_URL = "https://gutendex.com";
  private static final String SEARCH_BOOKS_ENDPOINT = "/books";

  private final RestTemplate restTemplate;

  public GutendexClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public GutendexSearchResponse searchByTitle(String title) {
    String url = UriComponentsBuilder
        .fromUriString(GUTENDEX_API_URL + SEARCH_BOOKS_ENDPOINT)
        .queryParam("search", title)
        .toUriString();

    return restTemplate.getForObject(url, GutendexSearchResponse.class);
  }
}
