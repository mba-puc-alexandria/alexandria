package com.pucsp.alexandria.domain.recommendation;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class RecommendationFeatureNormalizer {
  private RecommendationFeatureNormalizer() {}
  public static String canonical(String value) {
    if (value == null) return "";
    return Normalizer.normalize(value,Normalizer.Form.NFKC).toLowerCase(Locale.ROOT)
        .replaceAll("[\\p{Z}\\s]+"," ").strip();
  }
  public static Set<String> subjects(String value) { return split(value,";"); }
  public static Set<String> languages(String value) { return split(value,","); }
  private static Set<String> split(String value,String delimiter) {
    if(value==null)return Set.of();
    return Arrays.stream(value.split(delimiter)).map(RecommendationFeatureNormalizer::canonical)
        .filter(s->!s.isEmpty()).collect(Collectors.toUnmodifiableSet());
  }
}
