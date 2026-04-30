package com.pucsp.alexandria.domain.book.external;

public record BookData(
    Long gutendexId,
    String title,
    String authors,
    String downloadUrl,
    String coverUrl,
    String languages,
    String subjects,
    Integer downloadCount
) {

}
