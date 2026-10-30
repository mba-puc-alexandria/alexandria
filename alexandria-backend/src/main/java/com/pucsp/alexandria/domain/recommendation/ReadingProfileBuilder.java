package com.pucsp.alexandria.domain.recommendation;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import com.pucsp.alexandria.domain.recommendation.ReadingProfile.Evidence;

public final class ReadingProfileBuilder {
  public ReadingProfile build(List<ReadingSignal> signals) {
    var excluded=new HashSet<Long>(); var subjects=new TreeMap<String,Evidence>();
    var authors=new TreeMap<Long,Evidence>(); var languages=new TreeMap<String,Evidence>();
    MessageDigest digest;
    try { digest=MessageDigest.getInstance("SHA-256"); }
    catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    update(digest,Integer.toString(signals.size()));
    for(var signal:signals.stream().sorted(Comparator.comparingLong(ReadingSignal::bookId)).toList()) {
      excluded.add(signal.bookId());
      update(digest,Long.toString(signal.bookId())); update(digest,signal.title());
      update(digest,signal.status()); update(digest,String.valueOf(signal.rating()));
      update(digest,signal.subjects()); update(digest,signal.languages());
      update(digest,Integer.toString(signal.authors().size()));
      signal.authors().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry->{
        update(digest,entry.getKey().toString());update(digest,entry.getValue());
      });
      int weight=weight(signal);
      if(weight==0)continue;
      for(var key:RecommendationFeatureNormalizer.subjects(signal.subjects()))
        subjects.merge(key,evidence(weight,signal,key),ReadingProfileBuilder::strongest);
      signal.authors().forEach((id,name)->{
        if(!name.isBlank())authors.merge(id,evidence(weight,signal,name),ReadingProfileBuilder::strongest);
      });
      for(var key:RecommendationFeatureNormalizer.languages(signal.languages()))
        languages.merge(key,evidence(weight,signal,key),ReadingProfileBuilder::strongest);
    }
    return new ReadingProfile(excluded,subjects,authors,languages,HexFormat.of().formatHex(digest.digest()));
  }
  private static int weight(ReadingSignal signal) {
    return switch(RecommendationFeatureNormalizer.canonical(signal.status())) {
      case "reading" -> 3;
      case "toread" -> 2;
      case "done" -> signal.rating()!=null && signal.rating()>=4 && signal.rating()<=5?4:
          Integer.valueOf(3).equals(signal.rating())?1:0;
      default -> 0;
    };
  }
  private static Evidence evidence(int weight,ReadingSignal signal,String matched) {
    return new Evidence(weight,signal.bookId(),Objects.requireNonNullElse(signal.title(),""),matched);
  }
  private static Evidence strongest(Evidence a,Evidence b) {
    return a.weight()>b.weight() || a.weight()==b.weight() && a.sourceBookId()<b.sourceBookId()?a:b;
  }
  private static void update(MessageDigest digest,String value) {
    byte[] bytes=Objects.requireNonNullElse(value,"").getBytes(StandardCharsets.UTF_8);
    digest.update(ByteBuffer.allocate(4).putInt(bytes.length).array());digest.update(bytes);
  }
}
