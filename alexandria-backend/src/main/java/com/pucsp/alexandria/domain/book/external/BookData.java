package com.pucsp.alexandria.domain.book.external;

import java.util.List;

public record BookData(
    Long id,
    Long gutendexId,
    String title,
    String authors,
    List<AuthorData> authorDataList,
    String downloadUrl,
    String coverUrl,
    String languages,
    String subjects,
    Integer downloadCount
) {

}
