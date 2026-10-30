package com.pucsp.alexandria.domain.recommendation;
import java.time.LocalDate;
import java.util.Optional;
public interface RecommendationReadPort {
  Optional<DailySelection> readyToday(long userId, LocalDate date);
  Optional<DailySelection> latestReady(long userId, LocalDate beforeDate);
}
