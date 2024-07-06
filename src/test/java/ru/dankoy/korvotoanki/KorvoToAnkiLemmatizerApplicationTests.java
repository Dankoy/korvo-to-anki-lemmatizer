package ru.dankoy.korvotoanki;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import ru.dankoy.korvotoanki.core.command.TitleCommand;
import ru.dankoy.korvotoanki.core.command.VocabularyCommand;
import ru.dankoy.korvotoanki.core.dao.vocabularybuilder.title.TitleDaoJdbc;
import ru.dankoy.korvotoanki.core.dao.vocabularybuilder.vocabulary.VocabularyDaoJdbc;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperServiceImpl;
import ru.dankoy.korvotoanki.core.service.title.TitleServiceJdbc;
import ru.dankoy.korvotoanki.core.service.vocabulary.VocabularyServiceJdbc;

@DisplayName("Test default context ")
@TestPropertySource(
    properties = {
      "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration"
    })
@SpringBootTest
class KorvoToAnkiLemmatizerApplicationTests {

  @Autowired ApplicationContext context;

  @DisplayName("all default necessary beans should be created")
  @Test
  void contextLoads() {

    var objectMapper = context.getBean(ObjectMapper.class);
    var titleCommand = context.getBean(TitleCommand.class);
    var vocabularyCommand = context.getBean(VocabularyCommand.class);
    var titleDaoJdbc = context.getBean(TitleDaoJdbc.class);
    var vocabularyDaoJdbc = context.getBean(VocabularyDaoJdbc.class);
    var objectMapperService = context.getBean(ObjectMapperServiceImpl.class);
    var titleServiceJdbc = context.getBean(TitleServiceJdbc.class);
    var vocabularyServiceJdbc = context.getBean(VocabularyServiceJdbc.class);

    assertNotNull(objectMapper);
    assertNotNull(objectMapperService);
    assertNotNull(titleCommand);
    assertNotNull(vocabularyCommand);
    assertNotNull(titleDaoJdbc);
    assertNotNull(vocabularyDaoJdbc);
    assertNotNull(titleServiceJdbc);
    assertNotNull(vocabularyServiceJdbc);
  }
}
