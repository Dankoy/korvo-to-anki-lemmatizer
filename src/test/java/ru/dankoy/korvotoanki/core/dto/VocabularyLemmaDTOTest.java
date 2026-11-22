package ru.dankoy.korvotoanki.core.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test VocabularyLemmaDTO ")
class VocabularyLemmaDTOTest {

  @Test
  void equalityAndHashCodeAreBasedOnLemmaOnly() {

    var dto1 = new VocabularyLemmaDTO("maintained", "maintain", "title1");
    var dto2 = new VocabularyLemmaDTO("maintains", "maintain", "title2");

    assertThat(dto1).isEqualTo(dto2).hasSameHashCodeAs(dto2);
  }

  @Test
  void differentLemmasAreNotEqual() {

    var dto1 = new VocabularyLemmaDTO("maintained", "maintain", "title1");
    var dto2 = new VocabularyLemmaDTO("dripping", "drip", "title1");

    assertThat(dto1).isNotEqualTo(dto2);
  }

  @Test
  void setDeduplicatesElementsByLemma() {

    Set<VocabularyLemmaDTO> elements = new HashSet<>();

    assertThat(elements.add(new VocabularyLemmaDTO("maintained", "maintain", "title1"))).isTrue();
    assertThat(elements.add(new VocabularyLemmaDTO("maintains", "maintain", "title2"))).isFalse();
  }
}
