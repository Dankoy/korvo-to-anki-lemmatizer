package ru.dankoy.korvotoanki.core.service.vocabulary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.dankoy.korvotoanki.core.dao.vocabularybuilder.vocabulary.VocabularyDao;
import ru.dankoy.korvotoanki.core.dao.vocabularybuilder.vocabulary.VocabularyDaoJdbc;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.dto.VocabularyLemmaFullDTO;

@DisplayName("Test VocabularyServiceJdbc ")
@SpringBootTest(classes = {VocabularyServiceJdbc.class, VocabularyDaoJdbc.class})
class VocabularyServiceJdbcTest {

  @MockitoBean private VocabularyDao vocabularyDao;

  @Autowired private VocabularyService vocabularyService;

  @DisplayName("getAll not empty")
  @Test
  void getAll() {

    List<Vocabulary> correct = correctVocabularies();

    given(vocabularyDao.getAll()).willReturn(correct);

    var actual = vocabularyService.getAll();

    assertThat(actual).isNotEmpty().hasSize(correct.size()).isEqualTo(correct);

    Mockito.verify(vocabularyDao, times(1)).getAll();
  }

  @DisplayName("getByTitle not empty")
  @Test
  void getByTitle() {

    var title = new Title(0L, "title", 1L);

    List<Vocabulary> correct = correctVocabularies();

    given(vocabularyDao.getByTitle(title)).willReturn(correct);

    var actual = vocabularyService.getByTitle(title);

    assertThat(actual).isNotEmpty().hasSize(correct.size()).isEqualTo(correct);

    Mockito.verify(vocabularyDao, times(1)).getByTitle(title);
  }

  @DisplayName("count non zero")
  @Test
  void count() {

    List<Vocabulary> correct = correctVocabularies();

    given(vocabularyDao.count()).willReturn((long) correct.size());

    var actual = vocabularyService.count();

    assertThat(actual).isEqualTo(correct.size());

    Mockito.verify(vocabularyDao, times(1)).count();
  }

  @DisplayName("updateLemmas filters dtos with word equals lemma")
  @Test
  void updateLemmas() {

    var wordEqualsLemma =
        new VocabularyLemmaFullDTO("word", "word", null, 0, 0, 0, 0, null, null, 0);
    var wordDiffersLemma = new VocabularyLemmaFullDTO("was", "be", null, 0, 0, 0, 0, null, null, 0);

    ArgumentCaptor<List<VocabularyLemmaFullDTO>> captor = ArgumentCaptor.forClass(List.class);

    var actual = vocabularyService.updateLemmas(List.of(wordEqualsLemma, wordDiffersLemma));

    verify(vocabularyDao, times(1)).batchUpdate(captor.capture());

    assertThat(captor.getValue()).containsExactly(wordDiffersLemma);
    assertThat(actual).containsExactly(wordDiffersLemma);
  }

  @DisplayName("delete delegates to dao")
  @Test
  void delete() {

    var toDelete = List.of(new Vocabulary("word", null, 0, 0, 0, 0, null, null, 0));

    vocabularyService.delete(toDelete);

    verify(vocabularyDao, times(1)).batchDelete(toDelete);
  }

  private List<Vocabulary> correctVocabularies() {

    var title = new Title(1L, "Title1", 1L);

    return Stream.of(
            new Vocabulary(
                "contemplating",
                title,
                1_695_239_837,
                1_695_239_837,
                1_695_240_137,
                0,
                "combined forces.” He hoped to the gods it didn’t come to that.\n"
                    + "She fell silent, ",
                " a gratifying slaughter. Maybe even the final battle that would confirm her"
                    + " mastery. Most of all",
                0),
            new Vocabulary(
                "word", title, 1_695_239_837, 1_695_239_837, 1_695_240_137, 0, null, null, 0))
        .toList();
  }
}
