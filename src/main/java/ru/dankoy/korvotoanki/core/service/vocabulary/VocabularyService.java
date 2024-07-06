package ru.dankoy.korvotoanki.core.service.vocabulary;

import java.util.List;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.dto.VocabularyLemmaFullDTO;

public interface VocabularyService {

  List<Vocabulary> getAll();

  List<Vocabulary> getByTitle(Title title);

  List<VocabularyLemmaFullDTO> updateLemmas(List<VocabularyLemmaFullDTO> vocabularyLemmaFullDTOS);

  void delete(List<Vocabulary> vocabularies);

  long count();
}
