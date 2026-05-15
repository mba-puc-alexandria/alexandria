package com.pucsp.alexandria.adapter.out.persistence.external.mapper;

import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexBookResponse;
import com.pucsp.alexandria.domain.book.external.AuthorData;
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

    List<AuthorData> authorDataList = response.authors() != null
        ? response.authors().stream()
            .map(author -> new AuthorData(
                author.name(),
                author.birthYear(),
                author.deathYear()
            ))
            .toList()
        : List.of();

    String authors;
    if (authorDataList.isEmpty()) {
      authors = "Unknown";
    } else {
      List<String> formattedNames = authorDataList.stream()
          .map(AuthorData::getFormattedName)
          .filter(name -> name != null && !name.isBlank())
          .toList();
      authors = formattedNames.isEmpty() ? "Unknown" : String.join(", ", formattedNames);
    }

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
        authorDataList,
        downloadUrl,
        coverUrl,
        languages,
        subjects,
        response.downloadCount()
    );
  }
}
