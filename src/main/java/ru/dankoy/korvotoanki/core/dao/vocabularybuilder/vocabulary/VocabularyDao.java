package ru.dankoy.korvotoanki.core.dao.vocabularybuilder.vocabulary;

import java.util.List;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.dto.VocabularyLemmaFullDTO;

public interface VocabularyDao {

  List<Vocabulary> getAll();

  List<Vocabulary> getByTitle(Title title);

  void batchUpdate(List<VocabularyLemmaFullDTO> vocabularies);

  void batchDelete(List<Vocabulary> vocabularies);

  long count();
}
