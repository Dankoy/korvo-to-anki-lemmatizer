package ru.dankoy.korvotoanki.core.service.lemmatizer;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
  private CountDownLatch latch;

  @Override
  public List<VocabularyLemmaFullDTO> lemmatize(List<Vocabulary> vocabularies) {

    List<VocabularyLemmaFullDTO> dtos =
        vocabularies.stream().map(vocabularyMapper::addLemmaDto).toList();
    List<VocabularyLemmaFullDTO> result = new CopyOnWriteArrayList<>();

    // async with splitting list into chunks
    int cores = Runtime.getRuntime().availableProcessors();
    List<List<VocabularyLemmaFullDTO>> splitted = splitToPartitions(dtos, cores);

    latch = new CountDownLatch(splitted.size());

    try (ExecutorService executorService = Executors.newFixedThreadPool(cores)) {

      for (List<VocabularyLemmaFullDTO> sp : splitted) {
        executorService.execute(() -> process(sp, result));
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

  private void process(
      List<VocabularyLemmaFullDTO> vocabularies, List<VocabularyLemmaFullDTO> result) {

    // lemmatize vocabs containing only one word
    Set<VocabularyLemmaFullDTO> lemmatized =
        vocabularies.stream()
            .filter(v -> v.word().split(" ").length == 1)
            .filter(v -> !v.word().contains("-"))
            .map(
                v -> {
                  var coreDoc = stanfordCoreNLP.processToCoreDocument(v.word());
                  return vocabularyMapper.updateLemmaDto(coreDoc.tokens().get(0).lemma(), v);
                })
            .collect(Collectors.toSet());

    result.addAll(lemmatized);

    latch.countDown();
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
