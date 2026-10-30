package com.pucsp.alexandria.adapter.out.persistence;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** Bounded collection hydration shared by projections; never fetch-joins a paginated collection. */
final class RecommendationProjectionQueries {
  private final NamedParameterJdbcTemplate jdbc;
  RecommendationProjectionQueries(JdbcTemplate jdbc) { this.jdbc = new NamedParameterJdbcTemplate(jdbc); }

  Map<Long, Map<Long, String>> authorsFor(List<Long> ids) {
    Map<Long, Map<Long, String>> result = new LinkedHashMap<>();
    for (int offset = 0; offset < ids.size(); offset += 500) {
      List<Long> batch = ids.subList(offset, Math.min(ids.size(), offset + 500));
      jdbc.query("""
              SELECT ba.book_id,a.id,a.name FROM book_authors ba JOIN authors a ON a.id=ba.author_id
              WHERE ba.book_id IN (:bookIds) ORDER BY ba.book_id,a.id
              """, Map.of("bookIds", batch),
          rs -> { result.computeIfAbsent(rs.getLong("book_id"), key -> new LinkedHashMap<>())
              .put(rs.getLong("id"), rs.getString("name")); });
    }
    return result;
  }
}
