package ru.dankoy.korvotoanki.core.service.lemmatizer;

import java.util.List;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.dto.VocabularyLemmaFullDTO;

public interface LemmatizerService {

  List<VocabularyLemmaFullDTO> lemmatize(List<Vocabulary> vocabularies);
}
