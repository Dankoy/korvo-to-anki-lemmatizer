package ru.dankoy.korvotoanki.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.dankoy.korvotoanki.core.domain.Vocabulary;
import ru.dankoy.korvotoanki.core.dto.VocabularyLemmaDTO;
import ru.dankoy.korvotoanki.core.dto.VocabularyLemmaFullDTO;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VocabularyMapper {

  VocabularyLemmaFullDTO addLemmaDto(Vocabulary vocabulary);

  // creates new dto and adds lemma to it
  @Mapping(source = "lemma", target = "lemma")
  VocabularyLemmaFullDTO updateLemmaDto(String lemma, VocabularyLemmaFullDTO dto);

  @Mapping(source = "dto.title.name", target = "title")
  VocabularyLemmaDTO fromFullDto(VocabularyLemmaFullDTO dto);

  @Mapping(target = "title", ignore = true)
  Vocabulary fromDto(VocabularyLemmaDTO dto);
}
