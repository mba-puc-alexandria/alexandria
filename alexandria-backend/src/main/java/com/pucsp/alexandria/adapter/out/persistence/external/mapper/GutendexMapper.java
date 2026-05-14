package com.pucsp.alexandria.adapter.out.persistence.external.mapper;

import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexBookResponse;
import com.pucsp.alexandria.domain.book.external.BookData;
import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexAuthorResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GutendexMapper {

  public BookData toBookData(GutendexBookResponse response) {
    if (response == null) {
      return null;
    }

    List<String> authorNames = response.authors() != null && !response.authors().isEmpty()
        ? response.authors().stream()
            .map(author -> formatAuthorName(author.name()))
            .toList()
        : List.of();

    String authors = authorNames.isEmpty()
        ? "Unknown"
        : String.join(", ", authorNames);

    List<Integer> birthYears = response.authors() != null
        ? response.authors().stream()
            .map(GutendexAuthorResponse::birthYear)
            .toList()
        : List.of();

    List<Integer> deathYears = response.authors() != null
        ? response.authors().stream()
            .map(GutendexAuthorResponse::deathYear)
            .toList()
        : List.of();

    String downloadUrl = response.formats() != null && response.formats().applicationEpubZip() != null
        ? response.formats().applicationEpubZip()
        : null;

    String coverUrl = response.formats() != null && response.formats().imageJpeg() != null
        ? response.formats().imageJpeg()
        : null;

    String languages = response.languages() != null && !response.languages().isEmpty()
        ? String.join(",", response.languages())
        : null;

    String subjects = response.subjects() != null && !response.subjects().isEmpty()
        ? String.join(";", response.subjects())
        : null;

    return new BookData(
        -1L,
        response.id(),
        response.title(),
        authors,
        authorNames,
        birthYears,
        deathYears,
        downloadUrl,
        coverUrl,
        languages,
        subjects,
        response.downloadCount()
    );
  }

  private String formatAuthorName(String name) {
    if (name == null || name.isBlank()) {
      return name;
    }

    String trimmed = name.trim();

    if (trimmed.contains(",")) {
      String[] parts = trimmed.split(",", 2);
      String lastName = parts[0].trim();
      String firstName = parts.length > 1 ? parts[1].trim() : "";

      if (firstName.isEmpty()) {
        return lastName;
      }

      return firstName + " " + lastName;
    }

    return trimmed;
  }
}
