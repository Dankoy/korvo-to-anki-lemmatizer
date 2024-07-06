package ru.dankoy.korvotoanki.core.service.objectmapper;

public interface ObjectMapperService {

  String convertToString(Object object);

  String convertToStringPrettyPrint(Object object);
}
