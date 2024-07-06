package ru.dankoy.korvotoanki.core.exceptions;

public class VocabularyDaoException extends KorvoRootException {

  public VocabularyDaoException(Exception e) {
    super(e);
  }

  public VocabularyDaoException(String message) {
    super(message);
  }
}
