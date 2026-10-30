package com.pucsp.alexandria.support;

import com.pucsp.alexandria.domain.recommendation.*;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public final class RecommendationFixtures {
  private RecommendationFixtures() {}
  public static List<CatalogBookFeatures> catalog(int count) {
    return IntStream.rangeClosed(1, count).mapToObj(i -> new CatalogBookFeatures(i, "Livro " + i,
        Map.of((long) (i % 5 + 1), "Autor " + (i % 5 + 1)), i % 7 == 0 ? Set.of() : Set.of("ficção " + i % 3),
        i % 11 == 0 ? Set.of() : Set.of(i % 2 == 0 ? "pt" : "en"), i)).toList();
  }
  public static List<ReadingSignal> library(int count) {
    int[] ratings = {0, 1, 3, 4, 5};
    return IntStream.rangeClosed(1, count).mapToObj(i -> new ReadingSignal(i, "Evidência " + i,
        i % 3 == 0 ? "reading" : i % 3 == 1 ? "toread" : "done", i % 3 == 2 ? ratings[i % ratings.length] : null,
        Map.of((long) (i % 5 + 1), "Autor " + (i % 5 + 1)), i % 7 == 0 ? null : "Ficção " + i % 3,
        i % 11 == 0 ? null : "pt,en")).toList();
  }
  public static RecommendationCandidate candidate(long bookId, long sourceBookId) {
    return new RecommendationCandidate(bookId, new RecommendationCandidate.Score(4,4000,0,0,0,0),
        new RecommendationCandidate.Reason("SUBJECT", "ficção", "Assunto da biblioteca na data da seleção",
            sourceBookId, "Evidência", 4));
  }
  public static Map<Long,List<ReadingSignal>> libraries() {
    return Map.of(1L,library(45),2L,List.of(new ReadingSignal(1001,"Ciências", "done",5,
        Map.of(99L,"Autora científica"),"Ciência; Física","en")));
  }
  public static final class MutableClock extends Clock {
    private final AtomicReference<Instant> value;
    private final ZoneId zone;
    public MutableClock(Instant initial) { this(new AtomicReference<>(initial), ZoneId.of("America/Sao_Paulo")); }
    private MutableClock(AtomicReference<Instant> value, ZoneId zone) { this.value=value; this.zone=zone; }
    @Override public ZoneId getZone() { return zone; }
    @Override public Clock withZone(ZoneId zone) { return new MutableClock(value, zone); }
    @Override public Instant instant() { return value.get(); }
    public void advance(Duration duration) { value.updateAndGet(now -> now.plus(duration)); }
    public void set(Instant value) { this.value.set(value); }
  }
}
