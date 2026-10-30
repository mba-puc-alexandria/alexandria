package com.pucsp.alexandria.domain.recommendation;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
public record DailySelection(long id, long userId, LocalDate referenceDate, Instant generatedAt,
    String algorithmVersion, int selectedCount, List<Item> items) {
  public DailySelection {
    Objects.requireNonNull(referenceDate); Objects.requireNonNull(generatedAt); Objects.requireNonNull(algorithmVersion);
    items = List.copyOf(items);
    if (selectedCount < 0 || selectedCount > 30 || items.size() > selectedCount) throw new IllegalArgumentException("Invalid selection size");
    var ids = new HashSet<Long>(); var positions = new HashSet<Integer>();
    for (var item : items) if (!ids.add(item.book().id()) || !positions.add(item.position())) throw new IllegalArgumentException("Duplicate selection item");
  }
  public record Item(int position, Book book, RecommendationCandidate.Reason reason) {
    public Item {
      if (position < 1 || position > 30) throw new IllegalArgumentException("Invalid position");
      Objects.requireNonNull(book); Objects.requireNonNull(reason);
    }
  }
  public record Book(long id, String title, List<Author> authors, String coverUrl) {
    public Book { authors = List.copyOf(authors); Objects.requireNonNull(title); }
  }
  public record Author(long id, String name) {}
}
