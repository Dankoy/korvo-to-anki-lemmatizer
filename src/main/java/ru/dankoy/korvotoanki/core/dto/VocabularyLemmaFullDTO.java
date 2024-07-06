package ru.dankoy.korvotoanki.core.dto;

import java.util.Objects;
import ru.dankoy.korvotoanki.core.domain.Title;

public record VocabularyLemmaFullDTO(
    String word,
    String lemma,
    Title title,
    long createTime,
    long reviewTime,
    long dueTime,
    long reviewCount,
    String prevContext,
    String nextContext,
    long streakCount) {

  public VocabularyLemmaFullDTO copy(VocabularyLemmaFullDTO from) {
    Objects.requireNonNull(from);
    return new VocabularyLemmaFullDTO(
        from.word,
        from.lemma,
        from.title,
        from.createTime,
        from.reviewTime,
        from.dueTime,
        from.reviewCount,
        from.prevContext,
        from.nextContext,
        from.streakCount);
  }
}
