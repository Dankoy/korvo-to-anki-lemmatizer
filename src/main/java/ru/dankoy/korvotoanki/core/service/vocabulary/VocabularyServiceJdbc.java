package ru.dankoy.korvotoanki.core.service.vocabulary;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dankoy.korvotoanki.core.dao.vocabularybuilder.vocabulary.VocabularyDao;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.dto.VocabularyLemmaFullDTO;

@Service
@RequiredArgsConstructor
public class VocabularyServiceJdbc implements VocabularyService {

  private final VocabularyDao vocabularyDao;

  @Override
  public List<Vocabulary> getAll() {
    return vocabularyDao.getAll();
  }

  @Override
  public List<Vocabulary> getByTitle(Title title) {
    return vocabularyDao.getByTitle(title);
  }

  @Transactional
  @Override
  public List<VocabularyLemmaFullDTO> updateLemmas(
      List<VocabularyLemmaFullDTO> vocabularyLemmaFullDTOS) {

    var filtered =
        vocabularyLemmaFullDTOS.stream()
            .filter(dto -> !dto.word().equals(dto.lemma())) // filter if word is equals lemma
            .toList();

    vocabularyDao.batchUpdate(filtered);

    return filtered;
  }

  @Transactional
  @Override
  public void delete(List<Vocabulary> vocabularies) {

    vocabularyDao.batchDelete(vocabularies);
  }

  @Override
  public long count() {
    return vocabularyDao.count();
  }
}
