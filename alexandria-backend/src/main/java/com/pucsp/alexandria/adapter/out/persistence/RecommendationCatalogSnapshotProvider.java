package com.pucsp.alexandria.adapter.out.persistence;

import com.pucsp.alexandria.domain.recommendation.CatalogBookFeatures;
import com.pucsp.alexandria.domain.recommendation.CatalogFeatureRepository;
import com.pucsp.alexandria.domain.recommendation.RecommendationCatalogSnapshots;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Component;

/** A process/date-scoped lazy projection. Ownership includes queued tasks, not only active workers. */
@Component
public class RecommendationCatalogSnapshotProvider implements RecommendationCatalogSnapshots {
  private final CatalogFeatureRepository catalog;
  private final Object monitor = new Object();
  private final Map<LocalDate, State> sessions = new HashMap<>();

  public RecommendationCatalogSnapshotProvider(CatalogFeatureRepository catalog) { this.catalog = catalog; }

  @Override
  public Session open(LocalDate referenceDate) {
    Objects.requireNonNull(referenceDate);
    synchronized (monitor) {
      var state = sessions.computeIfAbsent(referenceDate, State::new);
      state.owners++;
      return new Owner(state);
    }
  }

  private final class Owner implements Session {
    private final State state;
    private boolean closed;
    Owner(State state) { this.state = state; }
    @Override public Task register() {
      synchronized (monitor) {
        if (closed || state.disposed) throw new IllegalStateException("Catalog session is closed");
        state.tasks++;
        return new Registration(state);
      }
    }
    @Override public void close() {
      synchronized (monitor) {
        if (closed) return;
        closed = true; state.owners--; releaseIfUnused(state);
      }
    }
  }

  private final class Registration implements Task {
    private final State state;
    private boolean closed;
    Registration(State state) { this.state = state; }
    @Override public Handle acquire() {
      CompletableFuture<Snapshot> future;
      boolean construct;
      synchronized (monitor) {
        if (closed || state.disposed) throw new IllegalStateException("Catalog task is closed");
        state.handles++;
        construct = state.future == null;
        if (construct) state.future = new CompletableFuture<>();
        future = state.future;
      }
      // Construction and waiting are outside the registry lock; another date may progress independently.
      if (construct) {
        try {
          var books = List.copyOf(catalog.loadAll());
          future.complete(new Snapshot(books, fingerprint(books)));
        } catch (Throwable failure) {
          future.completeExceptionally(failure);
        }
      }
      try {
        try { return new Consumer(state, future.get()); }
        catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          var cancelled = new CancellationException("Catalog snapshot wait interrupted");
          cancelled.initCause(interrupted);
          throw cancelled;
        } catch (ExecutionException failed) {
          throw new CompletionException(failed.getCause());
        }
      }
      catch (RuntimeException | Error failure) {
        synchronized (monitor) { state.handles--; releaseIfUnused(state); }
        throw failure;
      }
    }
    @Override public void close() {
      synchronized (monitor) {
        if (closed) return;
        closed = true; state.tasks--; releaseIfUnused(state);
      }
    }
  }

  private final class Consumer implements Handle {
    private final State state;
    private Snapshot snapshot;
    Consumer(State state, Snapshot snapshot) { this.state = state; this.snapshot = snapshot; }
    @Override public List<CatalogBookFeatures> books() {
      synchronized (monitor) { return requireOpen().books(); }
    }
    @Override public String fingerprint() {
      synchronized (monitor) { return requireOpen().fingerprint(); }
    }
    private Snapshot requireOpen() {
      if (snapshot == null) throw new IllegalStateException("Catalog handle is closed");
      return snapshot;
    }
    @Override public void close() {
      synchronized (monitor) {
        if (snapshot == null) return;
        snapshot = null; state.handles--; releaseIfUnused(state);
      }
    }
  }

  private void releaseIfUnused(State state) {
    if (state.owners == 0 && state.tasks == 0 && state.handles == 0) {
      state.disposed = true;
      state.future = null;
      sessions.remove(state.referenceDate, state);
    }
  }

  int activeSessions() { synchronized (monitor) { return sessions.size(); } }

  private static final class State {
    final LocalDate referenceDate;
    int owners;
    int tasks;
    int handles;
    boolean disposed;
    CompletableFuture<Snapshot> future;
    State(LocalDate referenceDate) { this.referenceDate = referenceDate; }
  }
  private record Snapshot(List<CatalogBookFeatures> books, String fingerprint) {}

  private static String fingerprint(List<CatalogBookFeatures> books) {
    MessageDigest digest;
    try { digest = MessageDigest.getInstance("SHA-256"); }
    catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    for (var book : books.stream().sorted(Comparator.comparingLong(CatalogBookFeatures::bookId)).toList()) {
      token(digest, Long.toString(book.bookId())); token(digest, book.title());
      token(digest, Integer.toString(book.downloadCount()));
      token(digest, "AUTHORS");
      token(digest, Integer.toString(book.authors().size()));
      book.authors().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(author -> {
        token(digest, author.getKey().toString()); token(digest, author.getValue());
      });
      token(digest, "SUBJECTS"); token(digest, Integer.toString(book.canonicalSubjects().size()));
      book.canonicalSubjects().stream().sorted().forEach(subject -> token(digest, subject));
      token(digest, "LANGUAGES"); token(digest, Integer.toString(book.languageCodes().size()));
      book.languageCodes().stream().sorted().forEach(language -> token(digest, language));
      token(digest, "END_BOOK");
    }
    return HexFormat.of().formatHex(digest.digest());
  }
  private static void token(MessageDigest digest, String value) {
    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
    digest.update(bytes);
  }
}
