package com.pucsp.alexandria.domain.book.external;

import java.util.List;

public interface BookApiClient {

  List<BookData> searchByTitle(String query);

  List<BookData> getPage(int page);
}
