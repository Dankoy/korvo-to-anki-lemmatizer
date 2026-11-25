package ru.dankoy.korvotoanki.core.service.lemmatizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.dto.VocabularyLemmaFullDTO;
import ru.dankoy.korvotoanki.core.mapper.VocabularyMapper;

@DisplayName("Test LemmatizerServiceVirtualThreads ")
@ExtendWith(MockitoExtension.class)
public class LemmatizerServiceVirtualThreadsTest {

  @Mock private StanfordCoreNLP stanfordCoreNLP;

  @Mock private VocabularyMapper vocabularyMapper;

  @InjectMocks private LemmatizerServiceVirtualThreads lemmatizerService;

  @Test
  public void testLemmatizeWithSingleWordWithWhitespaceVocabulary_expectsCorrectResult() {

    // Arrange
    Vocabulary vocabulary = new Vocabulary("gave back", null, 0, 0, 0, 0, null, null, 0);
    VocabularyLemmaFullDTO expectedDto =
        new VocabularyLemmaFullDTO("gave back", "give back", null, 0, 0, 0, 0, null, null, 0);

    var coreDocMock = mock(CoreDocument.class);
    var tokenMock1 = mock(CoreLabel.class);
    var tokenMock2 = mock(CoreLabel.class);

    when(tokenMock1.lemma()).thenReturn("give");
    when(tokenMock2.lemma()).thenReturn("back");

    when(coreDocMock.tokens()).thenReturn(List.of(tokenMock1, tokenMock2));

    when(vocabularyMapper.addLemmaDto(vocabulary)).thenReturn(expectedDto);
    when(vocabularyMapper.updateLemmaDto(Mockito.anyString(), Mockito.eq(expectedDto)))
        .thenReturn(expectedDto);
    when(stanfordCoreNLP.processToCoreDocument("gave back")).thenReturn(coreDocMock);

    // Act
    List<VocabularyLemmaFullDTO> result = lemmatizerService.lemmatize(List.of(vocabulary));

    // Assert
    assertEquals(1, result.size());
    assertEquals(expectedDto, result.get(0));
  }

  @Test
  public void testLemmatizeWithSingleWordWithoutWhitespaceVocabulary_expectsCorrectResult() {

    // Arrange
    Vocabulary vocabulary = new Vocabulary("examples", null, 0, 0, 0, 0, null, null, 0);
    VocabularyLemmaFullDTO expectedDto =
        new VocabularyLemmaFullDTO("examples", "example", null, 0, 0, 0, 0, null, null, 0);

    var coreDocMock = mock(CoreDocument.class);
    var tokenMock1 = mock(CoreLabel.class);

    when(tokenMock1.lemma()).thenReturn("example");

    when(coreDocMock.tokens()).thenReturn(List.of(tokenMock1));

    when(vocabularyMapper.addLemmaDto(vocabulary)).thenReturn(expectedDto);
    when(vocabularyMapper.updateLemmaDto(Mockito.anyString(), Mockito.eq(expectedDto)))
        .thenReturn(expectedDto);
    when(stanfordCoreNLP.processToCoreDocument("examples")).thenReturn(coreDocMock);

    // Act
    List<VocabularyLemmaFullDTO> result = lemmatizerService.lemmatize(List.of(vocabulary));

    // Assert
    assertEquals(1, result.size());
    assertEquals(expectedDto, result.get(0));
  }

  @Test
  public void testLemmatizeWithSingleWordVocabulary_expectsFilteringWordWithLemmaSameAsWord() {

    // Arrange
    Vocabulary vocabulary = new Vocabulary("example", null, 0, 0, 0, 0, null, null, 0);
    VocabularyLemmaFullDTO expectedDto =
        new VocabularyLemmaFullDTO("example", "example", null, 0, 0, 0, 0, null, null, 0);

    var coreDocMock = mock(CoreDocument.class);
    var tokenMock1 = mock(CoreLabel.class);

    when(tokenMock1.lemma()).thenReturn("example");

    when(coreDocMock.tokens()).thenReturn(List.of(tokenMock1));

    when(vocabularyMapper.addLemmaDto(vocabulary)).thenReturn(expectedDto);
    when(vocabularyMapper.updateLemmaDto(Mockito.anyString(), Mockito.eq(expectedDto)))
        .thenReturn(expectedDto);
    when(stanfordCoreNLP.processToCoreDocument("example")).thenReturn(coreDocMock);

    // Act
    List<VocabularyLemmaFullDTO> result = lemmatizerService.lemmatize(List.of(vocabulary));

    // Assert
    assertEquals(0, result.size());
  }

  @Test
  public void testLemmatizeWithMultipleWordVocabulary_expectsFilteringWordWithLemmaSameAsWord() {

    // Arrange
    Vocabulary vocabulary = new Vocabulary("give back", null, 0, 0, 0, 0, null, null, 0);
    VocabularyLemmaFullDTO expectedDto =
        new VocabularyLemmaFullDTO("give back", "give back", null, 0, 0, 0, 0, null, null, 0);

    var coreDocMock = mock(CoreDocument.class);
    var tokenMock1 = mock(CoreLabel.class);
    var tokenMock2 = mock(CoreLabel.class);

    when(tokenMock1.lemma()).thenReturn("give");
    when(tokenMock2.lemma()).thenReturn("back");

    when(coreDocMock.tokens()).thenReturn(List.of(tokenMock1, tokenMock2));

    when(vocabularyMapper.addLemmaDto(vocabulary)).thenReturn(expectedDto);
    when(vocabularyMapper.updateLemmaDto(Mockito.anyString(), Mockito.eq(expectedDto)))
        .thenReturn(expectedDto);
    when(stanfordCoreNLP.processToCoreDocument("give back")).thenReturn(coreDocMock);

    // Act
    List<VocabularyLemmaFullDTO> result = lemmatizerService.lemmatize(List.of(vocabulary));

    // Assert
    assertEquals(0, result.size());
  }

  @Test
  public void testLemmatizeWithOldLemmaAndItsWordForms_expectsOnlyWordFormsReturned() {

    // Arrange
    Vocabulary oldLemma = new Vocabulary("give back", null, 0, 0, 0, 0, null, null, 0);
    Vocabulary wordForm1 = new Vocabulary("gave back", null, 0, 0, 0, 0, null, null, 0);
    Vocabulary wordForm2 = new Vocabulary("giving back", null, 0, 0, 0, 0, null, null, 0);

    VocabularyLemmaFullDTO oldLemmaDto =
        new VocabularyLemmaFullDTO("give back", "give back", null, 0, 0, 0, 0, null, null, 0);
    VocabularyLemmaFullDTO wordForm1Dto =
        new VocabularyLemmaFullDTO("gave back", "give back", null, 0, 0, 0, 0, null, null, 0);
    VocabularyLemmaFullDTO wordForm2Dto =
        new VocabularyLemmaFullDTO("giving back", "give back", null, 0, 0, 0, 0, null, null, 0);

    var oldLemmaCoreDocMock = mock(CoreDocument.class);
    var oldLemmaTokenMock1 = mock(CoreLabel.class);
    var oldLemmaTokenMock2 = mock(CoreLabel.class);

    when(oldLemmaTokenMock1.lemma()).thenReturn("give");
    when(oldLemmaTokenMock2.lemma()).thenReturn("back");
    when(oldLemmaCoreDocMock.tokens()).thenReturn(List.of(oldLemmaTokenMock1, oldLemmaTokenMock2));

    var wordForm1CoreDocMock = mock(CoreDocument.class);
    var wordForm1TokenMock1 = mock(CoreLabel.class);
    var wordForm1TokenMock2 = mock(CoreLabel.class);

    when(wordForm1TokenMock1.lemma()).thenReturn("give");
    when(wordForm1TokenMock2.lemma()).thenReturn("back");
    when(wordForm1CoreDocMock.tokens())
        .thenReturn(List.of(wordForm1TokenMock1, wordForm1TokenMock2));

    var wordForm2CoreDocMock = mock(CoreDocument.class);
    var wordForm2TokenMock1 = mock(CoreLabel.class);
    var wordForm2TokenMock2 = mock(CoreLabel.class);

    when(wordForm2TokenMock1.lemma()).thenReturn("give");
    when(wordForm2TokenMock2.lemma()).thenReturn("back");
    when(wordForm2CoreDocMock.tokens())
        .thenReturn(List.of(wordForm2TokenMock1, wordForm2TokenMock2));

    when(vocabularyMapper.addLemmaDto(oldLemma)).thenReturn(oldLemmaDto);
    when(vocabularyMapper.addLemmaDto(wordForm1)).thenReturn(wordForm1Dto);
    when(vocabularyMapper.addLemmaDto(wordForm2)).thenReturn(wordForm2Dto);
    when(vocabularyMapper.updateLemmaDto(Mockito.anyString(), Mockito.eq(oldLemmaDto)))
        .thenReturn(oldLemmaDto);
    when(vocabularyMapper.updateLemmaDto(Mockito.anyString(), Mockito.eq(wordForm1Dto)))
        .thenReturn(wordForm1Dto);
    when(vocabularyMapper.updateLemmaDto(Mockito.anyString(), Mockito.eq(wordForm2Dto)))
        .thenReturn(wordForm2Dto);
    when(stanfordCoreNLP.processToCoreDocument("give back")).thenReturn(oldLemmaCoreDocMock);
    when(stanfordCoreNLP.processToCoreDocument("gave back")).thenReturn(wordForm1CoreDocMock);
    when(stanfordCoreNLP.processToCoreDocument("giving back")).thenReturn(wordForm2CoreDocMock);

    // Act
    List<VocabularyLemmaFullDTO> result =
        lemmatizerService.lemmatize(List.of(oldLemma, wordForm1, wordForm2));

    // Assert
    assertEquals(2, result.size());
    assertThat(result).containsExactlyInAnyOrder(wordForm1Dto, wordForm2Dto);
  }

  @Test
  public void testLemmatizeWithSingleWordVocabulary_expectsFilteringWordWithDash() {

    // Arrange
    Vocabulary vocabulary = new Vocabulary("example-word", null, 0, 0, 0, 0, null, null, 0);
    VocabularyLemmaFullDTO expectedDto =
        new VocabularyLemmaFullDTO("example-word", "example-word", null, 0, 0, 0, 0, null, null, 0);

    when(vocabularyMapper.addLemmaDto(vocabulary)).thenReturn(expectedDto);

    // Act
    List<VocabularyLemmaFullDTO> result = lemmatizerService.lemmatize(List.of(vocabulary));

    // Assert
    assertEquals(0, result.size());
  }

  @Test
  public void testLemmatizeWithMultipleWordVocabulary_expectsFilteringWordWithDash() {

    // Arrange
    Vocabulary vocabulary1 = new Vocabulary("gave back", null, 0, 0, 0, 0, null, null, 0);
    Vocabulary vocabulary2 = new Vocabulary("example-word", null, 0, 0, 0, 0, null, null, 0);

    VocabularyLemmaFullDTO expectedDto1 =
        new VocabularyLemmaFullDTO("gave back", "give back", null, 0, 0, 0, 0, null, null, 0);
    VocabularyLemmaFullDTO expectedDto2 =
        new VocabularyLemmaFullDTO("example-word", "example-word", null, 0, 0, 0, 0, null, null, 0);

    var coreDocMock = mock(CoreDocument.class);
    var tokenMock1 = mock(CoreLabel.class);
    var tokenMock2 = mock(CoreLabel.class);

    when(tokenMock1.lemma()).thenReturn("give");
    when(tokenMock2.lemma()).thenReturn("back");

    when(coreDocMock.tokens()).thenReturn(List.of(tokenMock1, tokenMock2));

    when(vocabularyMapper.addLemmaDto(vocabulary1)).thenReturn(expectedDto1);
    when(vocabularyMapper.addLemmaDto(vocabulary2)).thenReturn(expectedDto2);
    when(vocabularyMapper.updateLemmaDto(Mockito.anyString(), Mockito.eq(expectedDto1)))
        .thenReturn(expectedDto1);
    when(stanfordCoreNLP.processToCoreDocument("gave back")).thenReturn(coreDocMock);

    // Act
    List<VocabularyLemmaFullDTO> result =
        lemmatizerService.lemmatize(List.of(vocabulary1, vocabulary2));

    // Assert
    assertEquals(1, result.size());
    assertEquals(expectedDto1, result.get(0));
  }

  @Test
  public void testLemmatizeWithException() {
    // Arrange
    Vocabulary vocabulary = new Vocabulary("example-word", null, 0, 0, 0, 0, null, null, 0);
    when(vocabularyMapper.addLemmaDto(vocabulary)).thenReturn(null);

    // Act
    assertThatThrownBy(() -> lemmatizerService.lemmatize(List.of(vocabulary)))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void testLemmatizeWhenCoreNlpThrows_expectsWordSkippedWithoutException() {

    // Arrange
    Vocabulary vocabulary = new Vocabulary("examples", null, 0, 0, 0, 0, null, null, 0);
    VocabularyLemmaFullDTO dto =
        new VocabularyLemmaFullDTO("examples", null, null, 0, 0, 0, 0, null, null, 0);

    when(vocabularyMapper.addLemmaDto(vocabulary)).thenReturn(dto);
    when(stanfordCoreNLP.processToCoreDocument("examples"))
        .thenThrow(new RuntimeException("corenlp error"));

    // Act
    List<VocabularyLemmaFullDTO> result = lemmatizerService.lemmatize(List.of(vocabulary));

    // Assert
    assertEquals(0, result.size());
  }
}
