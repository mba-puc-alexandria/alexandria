package com.pucsp.alexandria.adapter.out.persistence.mapper;

import com.pucsp.alexandria.domain.recommendation.RecommendationBatch;
import com.pucsp.alexandria.domain.recommendation.RecommendationBatchStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;

public final class RecommendationBatchMapper {
  private RecommendationBatchMapper() {}

  public static Instant instant(ResultSet rs, String name) throws SQLException {
    var value = rs.getTimestamp(name, Calendar.getInstance(TimeZone.getTimeZone("UTC")));
    return value == null ? null : value.toInstant();
  }

  /** DATETIME is timezone-free; bind its UTC representation explicitly. */
  public static String utc(Instant instant) {
    return DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSSSS")
        .format(instant.atOffset(ZoneOffset.UTC));
  }

  public static RecommendationBatch fromRow(ResultSet rs, int row) throws SQLException {
    return new RecommendationBatch(rs.getLong("id"), rs.getLong("user_id"),
        rs.getObject("reference_date", LocalDate.class),
        RecommendationBatchStatus.valueOf(rs.getString("status")), rs.getString("algorithm_version"),
        rs.getInt("selected_count"), instant(rs, "generated_at"), rs.getString("lease_token"),
        instant(rs, "lease_until"), rs.getInt("attempt_count"), rs.getInt("automatic_attempt_count"),
        instant(rs, "failed_at"), instant(rs, "next_attempt_at"), instant(rs, "created_at"),
        instant(rs, "operational_pending_since"), rs.getString("failure_code"));
  }
}
