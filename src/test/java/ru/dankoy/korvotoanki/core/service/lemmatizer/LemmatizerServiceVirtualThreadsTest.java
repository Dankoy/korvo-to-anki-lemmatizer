package ru.dankoy.korvotoanki.core.service.lemmatizer;

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
    Vocabulary vocabulary = new Vocabulary("example word", null, 0, 0, 0, 0, null, null, 0);
    VocabularyLemmaFullDTO expectedDto =
        new VocabularyLemmaFullDTO("example word", "example word", null, 0, 0, 0, 0, null, null, 0);

    var coreDocMock = mock(CoreDocument.class);
    var tokenMock1 = mock(CoreLabel.class);
    var tokenMock2 = mock(CoreLabel.class);

    when(tokenMock1.lemma()).thenReturn("example");
    when(tokenMock2.lemma()).thenReturn("word");

    when(coreDocMock.tokens()).thenReturn(List.of(tokenMock1, tokenMock2));

    when(vocabularyMapper.addLemmaDto(vocabulary)).thenReturn(expectedDto);
    when(vocabularyMapper.updateLemmaDto(Mockito.anyString(), Mockito.eq(expectedDto)))
        .thenReturn(expectedDto);
    when(stanfordCoreNLP.processToCoreDocument("example word")).thenReturn(coreDocMock);

    // Act
    List<VocabularyLemmaFullDTO> result = lemmatizerService.lemmatize(List.of(vocabulary));

    // Assert
    assertEquals(1, result.size());
    assertEquals(expectedDto, result.get(0));
  }

  @Test
  public void testLemmatizeWithSingleWordWithoutWhitespaceVocabulary_expectsCorrectResult() {

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
    assertEquals(1, result.size());
    assertEquals(expectedDto, result.get(0));
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
    Vocabulary vocabulary1 = new Vocabulary("example word", null, 0, 0, 0, 0, null, null, 0);
    Vocabulary vocabulary2 = new Vocabulary("example-word", null, 0, 0, 0, 0, null, null, 0);

    VocabularyLemmaFullDTO expectedDto1 =
        new VocabularyLemmaFullDTO("example word", "example word", null, 0, 0, 0, 0, null, null, 0);
    VocabularyLemmaFullDTO expectedDto2 =
        new VocabularyLemmaFullDTO("example-word", "example-word", null, 0, 0, 0, 0, null, null, 0);

    var coreDocMock = mock(CoreDocument.class);
    var tokenMock1 = mock(CoreLabel.class);
    var tokenMock2 = mock(CoreLabel.class);

    when(tokenMock1.lemma()).thenReturn("example");
    when(tokenMock2.lemma()).thenReturn("word");

    when(coreDocMock.tokens()).thenReturn(List.of(tokenMock1, tokenMock2));

    when(vocabularyMapper.addLemmaDto(vocabulary1)).thenReturn(expectedDto1);
    when(vocabularyMapper.addLemmaDto(vocabulary2)).thenReturn(expectedDto2);
    when(vocabularyMapper.updateLemmaDto(Mockito.anyString(), Mockito.eq(expectedDto1)))
        .thenReturn(expectedDto1);
    when(stanfordCoreNLP.processToCoreDocument("example word")).thenReturn(coreDocMock);

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
}
