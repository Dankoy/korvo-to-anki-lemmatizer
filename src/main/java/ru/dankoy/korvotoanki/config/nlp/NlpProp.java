package ru.dankoy.korvotoanki.config.nlp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NlpProp {
  ANNOTATORS("annotators");

  private final String name;
}
