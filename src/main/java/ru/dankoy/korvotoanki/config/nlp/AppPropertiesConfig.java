package ru.dankoy.korvotoanki.config.nlp;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({NlpPropertiesConfig.class})
public class AppPropertiesConfig {}
