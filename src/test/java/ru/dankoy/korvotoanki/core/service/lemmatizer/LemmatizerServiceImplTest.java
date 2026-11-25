package ru.dankoy.korvotoanki.core.service.lemmatizer;

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

@DisplayName("Test LemmatizerServiceImpl ")
@ExtendWith(MockitoExtension.class)
public class LemmatizerServiceImplTest {

  @Mock private StanfordCoreNLP stanfordCoreNLP;

  @Mock private VocabularyMapper vocabularyMapper;

  @InjectMocks private LemmatizerServiceImpl lemmatizerService;

  @Test
  public void testLemmatizeWithSingleWordVocabulary_expectsCorrectResult() {

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
  public void testLemmatizeWithMultipleWordVocabulary_expectsCorrectResult() {

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
  public void testLemmatizeWhenCoreNlpThrows_expectsChunkSkippedWithoutHang() {

    // Arrange
    Vocabulary vocabulary = new Vocabulary("example", null, 0, 0, 0, 0, null, null, 0);
    VocabularyLemmaFullDTO dto =
        new VocabularyLemmaFullDTO("example", null, null, 0, 0, 0, 0, null, null, 0);

    when(vocabularyMapper.addLemmaDto(vocabulary)).thenReturn(dto);
    when(stanfordCoreNLP.processToCoreDocument("example"))
        .thenThrow(new RuntimeException("corenlp error"));

    // Act
    List<VocabularyLemmaFullDTO> result = lemmatizerService.lemmatize(List.of(vocabulary));

    // Assert
    assertEquals(0, result.size());
  }

  @Test
  public void testLemmatizeWithSingleWordVocabulary_expectsFilteringWordWithDash() {

    // Arrange
    Vocabulary vocabulary = new Vocabulary("example-word", null, 0, 0, 0, 0, null, null, 0);
    VocabularyLemmaFullDTO expectedDto =
        new VocabularyLemmaFullDTO("example-word", null, null, 0, 0, 0, 0, null, null, 0);

    when(vocabularyMapper.addLemmaDto(vocabulary)).thenReturn(expectedDto);

    // Act
    List<VocabularyLemmaFullDTO> result = lemmatizerService.lemmatize(List.of(vocabulary));

    // Assert
    assertEquals(0, result.size());
  }
}
