package ru.dankoy.korvotoanki.config.nlp;


import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "nlp")
public class NlpPropertiesConfig implements NlpProperties {

  private final List<String> annotators;

}
