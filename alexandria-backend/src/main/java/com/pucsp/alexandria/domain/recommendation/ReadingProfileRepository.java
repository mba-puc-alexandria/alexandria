package com.pucsp.alexandria.domain.recommendation;
import java.util.List;
public interface ReadingProfileRepository {
  List<ReadingSignal> load(long userId);
  List<Long> userIdsAfter(long afterId, int limit);
}
