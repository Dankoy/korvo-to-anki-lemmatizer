package ru.dankoy.korvotoanki.core.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.dto.VocabularyLemmaFullDTO;
import ru.dankoy.korvotoanki.core.mapper.VocabularyMapper;
import ru.dankoy.korvotoanki.core.service.lemmatizer.LemmatizerService;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperService;
import ru.dankoy.korvotoanki.core.service.vocabulary.VocabularyService;

@DisplayName("Test LemmatizerCommand ")
@ExtendWith(MockitoExtension.class)
class LemmatizerCommandTest {

  @Mock private VocabularyService vocabularyService;

  @Mock private LemmatizerService lemmatizerService;

  @Mock private ObjectMapperService objectMapperService;

  private final VocabularyMapper vocabularyMapper = Mappers.getMapper(VocabularyMapper.class);

  private LemmatizerCommand lemmatizerCommand;

  @BeforeEach
  void setUp() {
    lemmatizerCommand =
        new LemmatizerCommand(
            vocabularyService, lemmatizerService, vocabularyMapper, objectMapperService);
  }

  @Test
  void checkOnDuplicates_expectsOnlySecondWordWithSameLemmaMarkedAsDuplicate() {

    // Arrange
    var maintained = new Vocabulary("maintained", null, 0, 0, 0, 0, null, null, 0);
    var maintains = new Vocabulary("maintains", null, 0, 0, 0, 0, null, null, 0);
    List<Vocabulary> vocabularies = List.of(maintained, maintains);

    given(vocabularyService.getAll()).willReturn(vocabularies);
    given(lemmatizerService.lemmatize(vocabularies))
        .willReturn(
            List.of(
                new VocabularyLemmaFullDTO(
                    "maintained", "maintain", null, 0, 0, 0, 0, null, null, 0),
                new VocabularyLemmaFullDTO(
                    "maintains", "maintain", null, 0, 0, 0, 0, null, null, 0)));
    given(objectMapperService.convertToStringPrettyPrint(any())).willReturn("pretty json");

    // Act
    var actual = lemmatizerCommand.checkOnDuplicates();

    // Assert
    assertThat(actual).isEqualTo("pretty json");

    lemmatizerCommand.deleteDuplicates();

    var captor = ArgumentCaptor.forClass(List.class);
    org.mockito.Mockito.verify(vocabularyService).delete(captor.capture());

    assertThat((List<Vocabulary>) captor.getValue())
        .extracting(Vocabulary::word)
        .containsExactly("maintains");
  }

  @Test
  void checkOnDuplicatesAndCheckExistingLemmasEmpty_expectsLemmatizeAvailable() {

    // Arrange
    given(vocabularyService.getAll()).willReturn(List.of());
    given(lemmatizerService.lemmatize(anyList())).willReturn(List.of());
    given(objectMapperService.convertToStringPrettyPrint(any())).willReturn("[]");

    // Assert lemmatize is not available before checks
    assertThat(lemmatizerCommand.lemmatizeAvailability().get().isAvailable()).isFalse();

    // Act
    lemmatizerCommand.checkOnDuplicates();
    lemmatizerCommand.checkExistingLemmas();

    // Assert
    assertThat(lemmatizerCommand.lemmatizeAvailability().get().isAvailable()).isTrue();
  }

  @Test
  void checkExistingLemmas_expectsOnlyWordFormsDeletedButNotLemmas() {

    // Arrange
    var drip = new Vocabulary("drip", null, 0, 0, 0, 0, null, null, 0);
    var dripping = new Vocabulary("dripping", null, 0, 0, 0, 0, null, null, 0);
    List<Vocabulary> vocabularies = List.of(drip, dripping);

    given(vocabularyService.getAll()).willReturn(vocabularies);
    // lemmatized contains the old lemma itself - the command has to filter it out
    given(lemmatizerService.lemmatize(vocabularies))
        .willReturn(
            List.of(
                new VocabularyLemmaFullDTO("drip", "drip", null, 0, 0, 0, 0, null, null, 0),
                new VocabularyLemmaFullDTO("dripping", "drip", null, 0, 0, 0, 0, null, null, 0)));
    given(objectMapperService.convertToStringPrettyPrint(any())).willReturn("pretty json");

    // Act
    var actual = lemmatizerCommand.checkExistingLemmas();

    // Assert
    assertThat(actual).isEqualTo("pretty json");

    lemmatizerCommand.deleteExistingLemmas();

    var captor = ArgumentCaptor.forClass(List.class);
    org.mockito.Mockito.verify(vocabularyService).delete(captor.capture());

    assertThat((List<Vocabulary>) captor.getValue())
        .extracting(Vocabulary::word)
        .containsExactly("dripping");
  }

  @Test
  void checkExistingLemmasEmpty_expectsDeleteExistingLemmasUnavailable() {

    // Arrange
    given(vocabularyService.getAll()).willReturn(List.of());
    given(lemmatizerService.lemmatize(anyList())).willReturn(List.of());
    given(objectMapperService.convertToStringPrettyPrint(any())).willReturn("[]");

    // Act
    lemmatizerCommand.checkExistingLemmas();

    // Assert
    assertThat(lemmatizerCommand.lemmasExists().get().isAvailable()).isFalse();
    assertThat(lemmatizerCommand.duplicatesExists().get().isAvailable()).isFalse();
  }

  @Test
  void lemmatizeAllVocabularies_expectsUpdateLemmasWithLemmatizedResult() {

    // Arrange
    var was = new Vocabulary("was", null, 0, 0, 0, 0, null, null, 0);
    List<Vocabulary> vocabularies = List.of(was);

    var lemmatized =
        List.of(new VocabularyLemmaFullDTO("was", "be", null, 0, 0, 0, 0, null, null, 0));

    given(vocabularyService.getAll()).willReturn(vocabularies);
    given(lemmatizerService.lemmatize(vocabularies)).willReturn(lemmatized);
    given(vocabularyService.updateLemmas(lemmatized)).willReturn(lemmatized);
    given(objectMapperService.convertToStringPrettyPrint(any())).willReturn("pretty json");

    // Act
    var actual = lemmatizerCommand.lemmatizeAllVocabularies();

    // Assert
    assertThat(actual).isEqualTo("pretty json");
    org.mockito.Mockito.verify(vocabularyService).updateLemmas(lemmatized);
  }

  @Test
  void lemmatizeAllVocabulariesAndKeepOriginalWord_expectsOriginalPrependedToNextContext() {

    // Arrange
    var was = new Vocabulary("was", null, 0, 0, 0, 0, null, null, 0);
    var wellKnown = new Vocabulary("well-known", null, 0, 0, 0, 0, null, null, 0);
    List<Vocabulary> vocabularies = List.of(was, wellKnown);

    var lemmatized =
        List.of(
            new VocabularyLemmaFullDTO("was", "be", null, 0, 0, 0, 0, null, null, 0),
            new VocabularyLemmaFullDTO(
                "well-known", "well-known", null, 0, 0, 0, 0, null, null, 0));

    given(vocabularyService.getAll()).willReturn(vocabularies);
    given(lemmatizerService.lemmatize(vocabularies)).willReturn(lemmatized);
    given(vocabularyService.updateLemmas(any()))
        .willAnswer(invocation -> invocation.getArgument(0));
    given(objectMapperService.convertToStringPrettyPrint(any())).willReturn("pretty json");

    // Act
    var actual = lemmatizerCommand.lemmatizeAllVocabulariesAndKeepOriginalWord();

    // Assert
    assertThat(actual).isEqualTo("pretty json");

    var captor = ArgumentCaptor.forClass(List.class);
    org.mockito.Mockito.verify(vocabularyService).updateLemmas(captor.capture());

    var captured = (List<VocabularyLemmaFullDTO>) captor.getValue();

    // words with dash are not lemmatized and not updated
    assertThat(captured).hasSize(1);
    assertThat(captured.get(0).word()).isEqualTo("was");
    assertThat(captured.get(0).lemma()).isEqualTo("be");
    assertThat(captured.get(0).nextContext()).isEqualTo("(was)");
  }

  @Test
  void lemmatizeWord_expectsLemmaReturnedEvenWhenSameAsWord() {

    // Arrange
    var vocab = new Vocabulary("example", null, 0, 0, 0, 0, null, null, 0);
    var dto = new VocabularyLemmaFullDTO("example", "example", null, 0, 0, 0, 0, null, null, 0);

    given(lemmatizerService.lemmatize(vocab)).willReturn(dto);
    given(objectMapperService.convertToStringPrettyPrint(any())).willReturn("pretty json");

    // Act
    var actual = lemmatizerCommand.lemmatizeWord("example");

    // Assert
    assertThat(actual).isEqualTo("pretty json");
    org.mockito.Mockito.verify(objectMapperService).convertToStringPrettyPrint(any());
  }
}
