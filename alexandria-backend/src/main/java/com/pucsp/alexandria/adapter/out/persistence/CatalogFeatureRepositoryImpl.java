package com.pucsp.alexandria.adapter.out.persistence;

import com.pucsp.alexandria.domain.recommendation.CatalogBookFeatures;
import com.pucsp.alexandria.domain.recommendation.CatalogFeatureRepository;
import com.pucsp.alexandria.domain.recommendation.RecommendationFeatureNormalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CatalogFeatureRepositoryImpl implements CatalogFeatureRepository {
  private final JdbcTemplate jdbc;
  private final RecommendationProjectionQueries collections;
  public CatalogFeatureRepositoryImpl(JdbcTemplate jdbc) {
    this.jdbc = jdbc; this.collections = new RecommendationProjectionQueries(jdbc);
  }

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public List<CatalogBookFeatures> loadAll() {
    List<CatalogBookFeatures> books = new ArrayList<>();
    long after = 0;
    while (true) {
      var page = jdbc.query("""
          SELECT id,title,subjects,languages,download_count FROM books
          WHERE id>? AND download_url IS NOT NULL AND TRIM(download_url)<>'' ORDER BY id LIMIT 500
          """, (rs, row) -> new RawBook(rs.getLong("id"), rs.getString("title"), rs.getString("subjects"),
          rs.getString("languages"), rs.getInt("download_count")), after);
      if (page.isEmpty()) break;
      var authors = collections.authorsFor(page.stream().map(RawBook::id).toList());
      for (var book : page) books.add(new CatalogBookFeatures(book.id(), book.title(),
          authors.getOrDefault(book.id(), Map.of()), RecommendationFeatureNormalizer.subjects(book.subjects()),
          RecommendationFeatureNormalizer.languages(book.languages()), book.downloads()));
      after = page.get(page.size() - 1).id();
      if (page.size() < 500) break;
    }
    return List.copyOf(books);
  }
  private record RawBook(long id, String title, String subjects, String languages, int downloads) {}
}
