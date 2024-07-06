package ru.dankoy.korvotoanki.config.nlp;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class StanfordNLPConfig {

  private final NlpProperties nlpProperties;

  @Bean
  public StanfordCoreNLP getStanfordCoreNLP() {

    var props = new Properties();

    // set the list of annotators to run
    props.setProperty(NlpProp.ANNOTATORS.getName(),
        String.join(",", nlpProperties.getAnnotators()));

    // build pipeline
    return new StanfordCoreNLP(props);

  }

}
