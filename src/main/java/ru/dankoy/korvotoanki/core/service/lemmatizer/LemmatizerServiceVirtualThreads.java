package ru.dankoy.korvotoanki.core.service.lemmatizer;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.dto.VocabularyLemmaFullDTO;
import ru.dankoy.korvotoanki.core.mapper.VocabularyMapper;

@ConditionalOnProperty(prefix = "korvo-to-anki", name = "async-type", havingValue = "vtcf")
@Slf4j
@Service
@RequiredArgsConstructor
public class LemmatizerServiceVirtualThreads implements LemmatizerService {

  private final StanfordCoreNLP stanfordCoreNLP;
  private final VocabularyMapper vocabularyMapper;

  @Override
  public List<VocabularyLemmaFullDTO> lemmatize(List<Vocabulary> vocabularies) {

    // async with virtual threads

    List<VocabularyLemmaFullDTO> dtos =
        vocabularies.stream().map(vocabularyMapper::addLemmaDto).toList();

    try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
      List<CompletableFuture<VocabularyLemmaFullDTO>> cfs =
          dtos.stream()
              .filter(v -> v.word().split(" ").length == 1)
              .filter(v -> !v.word().contains("-"))
              .map(
                  dto -> {
                    return CompletableFuture.supplyAsync(() -> process(dto), executorService)
                        .exceptionally(
                            ex -> {
                              log.error("Something went wrong with lemmatization of word: {}", ex);
                              return null;
                            });
                  })
              .toList();

      CompletableFuture<List<VocabularyLemmaFullDTO>> allOf =
          CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()]))
              .thenApply(
                  // map cfs results into list
                  future -> cfs.stream().map(CompletableFuture::join).flatMap(Stream::of).toList())
              .thenApply(
                  // filter words
                  resultList ->
                      resultList.stream().filter(v -> !v.word().equals(v.lemma())).toList());

      return allOf.join();
    }
  }

  private VocabularyLemmaFullDTO process(VocabularyLemmaFullDTO vocabulary) {

    // lemmatize vocabs containing only one word
    var coreDoc = stanfordCoreNLP.processToCoreDocument(vocabulary.word());
    return vocabularyMapper.updateLemmaDto(coreDoc.tokens().get(0).lemma(), vocabulary);
  }
}
