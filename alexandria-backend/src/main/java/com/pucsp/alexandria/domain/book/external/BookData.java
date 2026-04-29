package com.pucsp.alexandria.domain.book.external;

public record BookData(
    Long gutenbergId,
    String title,
    String authors,
    String downloadUrl,
    String coverUrl,
    String languages,
    String subjects,
    Integer downloadCount
) {

}
