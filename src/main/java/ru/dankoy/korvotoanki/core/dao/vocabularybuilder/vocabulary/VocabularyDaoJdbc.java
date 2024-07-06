package ru.dankoy.korvotoanki.core.dao.vocabularybuilder.vocabulary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.dto.VocabularyLemmaFullDTO;
import ru.dankoy.korvotoanki.core.exceptions.VocabularyDaoException;

@Slf4j
@Component
@RequiredArgsConstructor
public class VocabularyDaoJdbc implements VocabularyDao {

  private static final int BATCH_SIZE = 100;

  private static final String COLUMN_TITLE_ID = "title_id";

  private final NamedParameterJdbcOperations vocabularyJdbcOperations;

  @Override
  public List<Vocabulary> getAll() {

    String query =
        "select vocabulary.word as v_word, "
            + "vocabulary.title_id as v_title_id, "
            + "title.id as t_id, "
            + "title.name as t_name, "
            + "title.filter as t_filter, "
            + "vocabulary.create_time as v_create_time, "
            + "vocabulary.review_time as v_review_time, "
            + "vocabulary.due_time as v_due_time, "
            + "vocabulary.review_count as v_review_count, "
            + "vocabulary.prev_context as v_prev_context, "
            + "vocabulary.next_context as v_next_context, "
            + "vocabulary.streak_count as v_streak_count "
            + "from vocabulary, title "
            + "where v_title_id = t_id";

    Map<String, Vocabulary> books =
        vocabularyJdbcOperations.query(query, new BookResultSetExtractor());

    return new ArrayList<>(Objects.requireNonNull(books).values());
  }

  @Override
  public List<Vocabulary> getByTitle(Title title) {

    Map<String, Object> params = Collections.singletonMap(COLUMN_TITLE_ID, title.id());

    String query =
        "select vocabulary.word as v_word, "
            + "vocabulary.title_id as v_title_id, "
            + "title.id as t_id, "
            + "title.name as t_name, "
            + "title.filter as t_filter, "
            + "vocabulary.create_time as v_create_time, "
            + "vocabulary.review_time as v_review_time, "
            + "vocabulary.due_time as v_due_time, "
            + "vocabulary.review_count as v_review_count, "
            + "vocabulary.prev_context as v_prev_context, "
            + "vocabulary.next_context as v_next_context, "
            + "vocabulary.streak_count as v_streak_count "
            + "from vocabulary, title "
            + "where v_title_id = :title_id";

    Map<String, Vocabulary> books =
        vocabularyJdbcOperations.query(query, params, new BookResultSetExtractor());

    return new ArrayList<>(Objects.requireNonNull(books).values());
  }

  @Override
  public void batchUpdate(List<VocabularyLemmaFullDTO> vocabularies) {

    Collection<List<VocabularyLemmaFullDTO>> batches =
        vocabularies.stream()
            .collect(Collectors.groupingBy(it -> vocabularies.indexOf(it) / BATCH_SIZE))
            .values();

    int[] rowsUpdated;

    for (List<VocabularyLemmaFullDTO> batch : batches) {

      log.info("Batch size: {}", batch.size());
      log.info("Batch: {}", batch);

      SqlParameterSource[] batchParams = SqlParameterSourceUtils.createBatch(batch.toArray());
      KeyHolder keyHolder = new GeneratedKeyHolder();

      try {

        rowsUpdated =
            vocabularyJdbcOperations.batchUpdate(
                "update vocabulary set word = :lemma where word =:word", batchParams, keyHolder);

      } catch (Exception ex) {
        throw new VocabularyDaoException(ex);
      }

      checkBatch(rowsUpdated);
    }
  }

  @Override
  public void batchDelete(List<Vocabulary> vocabularies) {

    Collection<List<Vocabulary>> batches =
        vocabularies.stream()
            .collect(Collectors.groupingBy(it -> vocabularies.indexOf(it) / BATCH_SIZE))
            .values();

    int[] rowsUpdated;

    for (List<Vocabulary> batch : batches) {

      log.info("Batch size: {}", batch.size());
      log.info("Batch: {}", batch);

      SqlParameterSource[] batchParams = SqlParameterSourceUtils.createBatch(batch.toArray());
      KeyHolder keyHolder = new GeneratedKeyHolder();

      try {

        rowsUpdated =
            vocabularyJdbcOperations.batchUpdate(
                "delete from vocabulary where word = :word", batchParams, keyHolder);

      } catch (Exception ex) {
        throw new VocabularyDaoException(ex);
      }

      checkBatch(rowsUpdated);
    }
  }

  private void checkBatch(int[] rowsUpdated) {
    for (int i = 0; i < rowsUpdated.length; i++) {
      if (rowsUpdated[i] == -2) {
        log.error("Execution {}: unknown number of rows updated", i);
        throw new VocabularyDaoException("Batch update error");
      } else {
        log.debug("Execution {} successful: {} rows updated", i, rowsUpdated[i]);
      }
    }
  }

  @Override
  public long count() {
    Long count =
        vocabularyJdbcOperations
            .getJdbcOperations()
            .queryForObject("select count(*) from vocabulary", Long.class);
    return count == null ? 0 : count;
  }
}
