package com.pucsp.alexandria.adapter.out.persistence;

import com.pucsp.alexandria.adapter.out.persistence.mapper.RecommendationBatchMapper;
import com.pucsp.alexandria.domain.recommendation.*;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class RecommendationReadAdapter implements RecommendationReadPort {
  private final JdbcTemplate jdbc;
  private final RecommendationProjectionQueries collections;
  public RecommendationReadAdapter(JdbcTemplate jdbc) {
    this.jdbc = jdbc; this.collections = new RecommendationProjectionQueries(jdbc);
  }

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public Optional<DailySelection> readyToday(long userId, LocalDate date) {
    return jdbc.query("SELECT * FROM recommendation_batches WHERE user_id=? AND reference_date=? AND status='READY'",
        RecommendationBatchMapper::fromRow, userId, date).stream().findFirst().map(this::hydrate);
  }

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public Optional<DailySelection> latestReady(long userId, LocalDate beforeDate) {
    return jdbc.query("""
        SELECT * FROM recommendation_batches WHERE user_id=? AND reference_date<? AND status='READY'
        ORDER BY reference_date DESC LIMIT 1
        """, RecommendationBatchMapper::fromRow, userId, beforeDate).stream().findFirst().map(this::hydrate);
  }

  private DailySelection hydrate(RecommendationBatch batch) {
    var rows = jdbc.query("""
        SELECT ri.*,b.title,b.cover_url FROM recommendation_items ri JOIN books b ON b.id=ri.book_id
          JOIN recommendation_batches rb ON rb.id=ri.batch_id
        WHERE ri.batch_id=? AND rb.user_id=? AND rb.status='READY' AND b.download_url IS NOT NULL
          AND TRIM(b.download_url)<>'' AND NOT EXISTS
          (SELECT 1 FROM user_books ub WHERE ub.user_id=? AND ub.book_id=ri.book_id)
        ORDER BY ri.position
        """, (rs, row) -> new RawItem(rs.getInt("position"), rs.getLong("book_id"), rs.getString("title"),
        rs.getString("cover_url"), new RecommendationCandidate.Reason(rs.getString("reason_type"),
        rs.getString("reason_value"), rs.getString("reason_text"), rs.getObject("source_book_id", Long.class),
        rs.getString("source_book_title"), rs.getInt("evidence_weight"))), batch.id(), batch.userId(), batch.userId());
    var authors = collections.authorsFor(rows.stream().map(RawItem::bookId).toList());
    List<DailySelection.Item> items = rows.stream().map(row -> new DailySelection.Item(row.position(),
        new DailySelection.Book(row.bookId(), row.title(), authors.getOrDefault(row.bookId(), Map.of()).entrySet()
            .stream().sorted(Map.Entry.comparingByKey()).map(a -> new DailySelection.Author(a.getKey(), a.getValue())).toList(),
            row.cover()), row.reason())).toList();
    return new DailySelection(batch.id(), batch.userId(), batch.referenceDate(), batch.generatedAt(),
        batch.algorithmVersion(), batch.selectedCount(), items);
  }
  private record RawItem(int position, long bookId, String title, String cover, RecommendationCandidate.Reason reason) {}
}
