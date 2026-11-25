package ru.dankoy.korvotoanki.core.service.lemmatizer;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.core.aspect.annot.LogExecutionTime;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.dto.VocabularyLemmaFullDTO;
import ru.dankoy.korvotoanki.core.exceptions.KorvoRootException;
import ru.dankoy.korvotoanki.core.mapper.VocabularyMapper;

@ConditionalOnProperty(prefix = "korvo-to-anki", name = "async-type", havingValue = "latch")
@Slf4j
@Service
@RequiredArgsConstructor
public class LemmatizerServiceImpl implements LemmatizerService {

  private final StanfordCoreNLP stanfordCoreNLP;
  private final VocabularyMapper vocabularyMapper;

  @LogExecutionTime
  @Override
  public List<VocabularyLemmaFullDTO> lemmatize(List<Vocabulary> vocabularies) {

    List<VocabularyLemmaFullDTO> dtos =
        vocabularies.stream().map(vocabularyMapper::addLemmaDto).toList();
    List<VocabularyLemmaFullDTO> result = new CopyOnWriteArrayList<>();

    // async with splitting list into chunks
    int cores = Runtime.getRuntime().availableProcessors();
    List<List<VocabularyLemmaFullDTO>> split = splitToPartitions(dtos, cores);

    CountDownLatch latch = new CountDownLatch(split.size());

    try (ExecutorService executorService = Executors.newFixedThreadPool(cores)) {

      for (List<VocabularyLemmaFullDTO> sp : split) {
        executorService.execute(() -> process(sp, result, latch));
      }

      try {
        latch.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new KorvoRootException("Interrupted while waiting for task completion", e);
      }
    }

    // filter words
    return result.stream().filter(v -> !v.word().equals(v.lemma())).toList();
  }

  @Override
  public VocabularyLemmaFullDTO lemmatize(Vocabulary vocabulary) {
    return processOne(vocabularyMapper.addLemmaDto(vocabulary));
  }

  private void process(
      List<VocabularyLemmaFullDTO> vocabularies,
      List<VocabularyLemmaFullDTO> result,
      CountDownLatch latch) {

    try {
      List<VocabularyLemmaFullDTO> lemmatized =
          vocabularies.stream().filter(v -> !v.word().contains("-")).map(this::processOne).toList();

      result.addAll(lemmatized);
    } catch (Exception e) {
      log.error(
          "Something went wrong with lemmatization of a chunk of {} words", vocabularies.size(), e);
    } finally {
      latch.countDown();
    }
  }

  private VocabularyLemmaFullDTO processOne(VocabularyLemmaFullDTO dto) {

    var coreDoc = stanfordCoreNLP.processToCoreDocument(dto.word());

    // convert list of tokens to string of lemmatized words
    var lemma =
        coreDoc.tokens().stream().map(token -> token.lemma()).collect(Collectors.joining(" "));

    return vocabularyMapper.updateLemmaDto(lemma, dto);
  }

  private List<List<VocabularyLemmaFullDTO>> splitToPartitions(
      List<VocabularyLemmaFullDTO> list, int cores) {

    if (list.size() < cores) {
      List<List<VocabularyLemmaFullDTO>> l = new ArrayList<>();
      l.add(list);
      return l;
    }

    final int G = list.size() / cores; // chunks size
    final int NG = (list.size() + G - 1) / G;

    return IntStream.range(0, NG)
        .mapToObj(i -> list.subList(G * i, Math.min(G * i + G, list.size())))
        .toList();
  }
}
