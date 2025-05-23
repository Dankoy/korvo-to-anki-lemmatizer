package ru.dankoy.korvotoanki.core.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.Availability;
import org.springframework.shell.AvailabilityProvider;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.CommandAvailability;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.dto.VocabularyLemmaDTO;
import ru.dankoy.korvotoanki.core.dto.VocabularyLemmaFullDTO;
import ru.dankoy.korvotoanki.core.mapper.VocabularyMapper;
import ru.dankoy.korvotoanki.core.service.lemmatizer.LemmatizerService;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperService;
import ru.dankoy.korvotoanki.core.service.vocabulary.VocabularyService;

@RequiredArgsConstructor
@Command(group = "lemmatize")
public class LemmatizerCommand {

  private boolean duplicatesCleaned = false;
  private boolean duplicatesForExistingLemmasCleaned = false;
  private List<VocabularyLemmaDTO> duplicates = new ArrayList<>();
  private List<VocabularyLemmaDTO> alreadyExistingLemmas = new ArrayList<>();

  private final VocabularyService vocabularyService;
  private final LemmatizerService lemmatizerService;
  private final VocabularyMapper vocabularyMapper;
  private final ObjectMapperService objectMapperService;

  @CommandAvailability
  @Command(
      command = {"check-on-duplicates"},
      alias = "cod",
      description = "Check lemmas on duplicates")
  public String checkOnDuplicates() {

    List<Vocabulary> vocabulary = vocabularyService.getAll();
    List<VocabularyLemmaFullDTO> lemmatized = lemmatizerService.lemmatize(vocabulary);

    var dtos = lemmatized.stream().map(vocabularyMapper::fromFullDto).toList();

    Set<VocabularyLemmaDTO> elements = new HashSet<>();
    duplicates = dtos.stream().filter(n -> !elements.add(n)).toList();

    if (duplicates.isEmpty()) {
      duplicatesCleaned = true;
    }

    return objectMapperService.convertToStringPrettyPrint(duplicates);
  }

  @CommandAvailability(provider = "duplicatesExists")
  @Command(
      command = {"auto-delete-duplicates"},
      alias = "add",
      description = "Automatically delete duplicates. Do on your own risk.")
  public void deleteDuplicates() {

    List<Vocabulary> vocabsToDelete = duplicates.stream().map(vocabularyMapper::fromDto).toList();

    vocabularyService.delete(vocabsToDelete);
  }

  @CommandAvailability
  @Command(
      command = {"check-lemmas-if-exists"},
      alias = "clie",
      description = "Check if db contains lemmas that could be ignored")
  public String checkExistingLemmas() {

    List<Vocabulary> vocabulary = vocabularyService.getAll();
    List<VocabularyLemmaFullDTO> lemmatized = lemmatizerService.lemmatize(vocabulary);

    var dtos = lemmatized.stream().map(vocabularyMapper::fromFullDto).toList();

    alreadyExistingLemmas =
        dtos.stream()
            .filter(dto -> !dto.word().equals(dto.lemma()))
            .filter(
                dto ->
                    vocabulary.stream()
                        .anyMatch(
                            s -> s.word().equals(dto.lemma()))) // filter if lemma equals word in db
            .toList();

    if (alreadyExistingLemmas.isEmpty()) {
      duplicatesForExistingLemmasCleaned = true;
    }

    return objectMapperService.convertToStringPrettyPrint(alreadyExistingLemmas);
  }

  @CommandAvailability(provider = "lemmasExists")
  @Command(
      command = {"auto-delete-words-lemmas-exists"},
      alias = "adwle",
      description =
          "Automatically delete words that already has lemmas in db for. Do on your own risk.")
  public void deleteExistingLemmas() {

    var toDelete = alreadyExistingLemmas.stream().map(vocabularyMapper::fromDto).toList();

    vocabularyService.delete(toDelete);
  }

  @CommandAvailability(provider = {"lemmatizeAvailability"})
  @Command(
      command = {"lemmatize-all-vocabularies"},
      alias = "lav",
      description =
          """
          Lemmatize vocabularies.
          Should be used only if
            1) check-on-duplicates (cod) command returns empty list
            2) check-lemmas-if-exists (clie) command returns empty list
          """)
  public String lemmatizeAllVocabularies() {
    List<Vocabulary> vocabulary = vocabularyService.getAll();

    List<VocabularyLemmaFullDTO> res = lemmatizerService.lemmatize(vocabulary);

    var updated = vocabularyService.updateLemmas(res);

    var dtos = updated.stream().map(vocabularyMapper::fromFullDto).toList();

    return objectMapperService.convertToStringPrettyPrint(dtos);
  }

  @CommandAvailability(provider = {"lemmatizeAvailability"})
  @Command(
      command = {"lemmatize-all-vocabularies-keep-original"},
      alias = "lavko",
      description =
          """
          Lemmatize vocabularies and keep original word.
          Should be used only if
            1) check-on-duplicates (cod) command returns empty list
            2) check-lemmas-if-exists (clie) command returns empty list
          """)
  public String lemmatizeAllVocabulariesAndKeepOriginalWord() {
    List<Vocabulary> vocabulary = vocabularyService.getAll();

    List<VocabularyLemmaFullDTO> res = lemmatizerService.lemmatize(vocabulary);

    var keepOriginalDto = keepOriginalWordWithLemma(res);

    var updated = vocabularyService.updateLemmas(keepOriginalDto);

    var dtos = updated.stream().map(vocabularyMapper::fromFullDto).toList();

    return objectMapperService.convertToStringPrettyPrint(dtos);
  }

  @Bean
  public AvailabilityProvider lemmatizeAvailability() {
    return () ->
        (duplicatesCleaned && duplicatesForExistingLemmasCleaned)
            ? Availability.available()
            : Availability.unavailable("first check and fix all duplicates");
  }

  @Bean
  public AvailabilityProvider duplicatesExists() {
    return () ->
        !duplicates.isEmpty()
            ? Availability.available()
            : Availability.unavailable("no duplicates");
  }

  @Bean
  public AvailabilityProvider lemmasExists() {
    return () ->
        !alreadyExistingLemmas.isEmpty()
            ? Availability.available()
            : Availability.unavailable("no duplicates");
  }

  private List<VocabularyLemmaFullDTO> keepOriginalWordWithLemma(
      List<VocabularyLemmaFullDTO> dtoList) {
    return dtoList.stream()
        .filter(v -> v.word().split(" ").length == 1)
        .filter(v -> !v.word().contains("-"))
        .filter(dto -> !dto.word().equals(dto.lemma())) // filter if word is equals lemma
        .map(
            vcl -> {
              var originalWithNextContext =
                  vcl.nextContext() == null
                      ? String.format("(%s)", vcl.word())
                      : String.format("(%s)%s", vcl.word(), vcl.nextContext());
              return new VocabularyLemmaFullDTO(
                  vcl.word(),
                  vcl.lemma(),
                  vcl.title(),
                  vcl.createTime(),
                  vcl.reviewTime(),
                  vcl.dueTime(),
                  vcl.reviewCount(),
                  vcl.prevContext(),
                  originalWithNextContext,
                  vcl.streakCount());
            })
        .toList();
  }
}
