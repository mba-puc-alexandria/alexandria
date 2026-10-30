package com.pucsp.alexandria.adapter.out.persistence;

import com.pucsp.alexandria.domain.recommendation.ReadingProfileRepository;
import com.pucsp.alexandria.domain.recommendation.ReadingSignal;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ReadingProfileRepositoryImpl implements ReadingProfileRepository {
  private final JdbcTemplate jdbc;
  private final RecommendationProjectionQueries collections;
  public ReadingProfileRepositoryImpl(JdbcTemplate jdbc) {
    this.jdbc = jdbc; this.collections = new RecommendationProjectionQueries(jdbc);
  }

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public List<ReadingSignal> load(long userId) {
    var rows = jdbc.query("""
        SELECT b.id,b.title,ub.status,ub.rating,b.subjects,b.languages FROM user_books ub
          JOIN books b ON b.id=ub.book_id WHERE ub.user_id=? ORDER BY b.id
        """, (rs, row) -> new ReadingSignal(rs.getLong("id"), rs.getString("title"), rs.getString("status"),
        rs.getObject("rating", Integer.class), Map.of(), rs.getString("subjects"), rs.getString("languages")), userId);
    var authors = collections.authorsFor(rows.stream().map(ReadingSignal::bookId).toList());
    return rows.stream().map(row -> new ReadingSignal(row.bookId(), row.title(), row.status(), row.rating(),
        authors.getOrDefault(row.bookId(), Map.of()), row.subjects(), row.languages())).toList();
  }

  @Override
  public List<Long> userIdsAfter(long afterId, int limit) {
    if (limit <= 0) throw new IllegalArgumentException("Limit must be positive");
    return jdbc.queryForList("SELECT id FROM users WHERE id>? ORDER BY id LIMIT ?", Long.class, afterId, limit);
  }
}
