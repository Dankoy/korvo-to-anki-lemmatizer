package ru.dankoy.korvotoanki.core.dto;

public record VocabularyLemmaDTO(
    String word,
    String lemma,
    String title) {

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VocabularyLemmaDTO that)) {
      return false;
    }

    return lemma.equals(that.lemma);
  }

  @Override
  public int hashCode() {
    return lemma.hashCode();
  }
}
