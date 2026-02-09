package ru.dankoy.korvotoanki.core.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;
import ru.dankoy.korvotoanki.core.domain.Title;
import ru.dankoy.korvotoanki.core.service.objectmapper.ObjectMapperService;
import ru.dankoy.korvotoanki.core.service.title.TitleService;

@RequiredArgsConstructor
@Component
public class TitleCommand {

  private final TitleService titleService;
  private final ObjectMapperService objectMapperService;

  @Command(
      group = "Title commands",
      name = "title-count",
      alias = "tc",
      description = "Count all titles")
  public String count() {
    Long count = titleService.count();
    return objectMapperService.convertToString(count);
  }

  @Command(
      group = "Title commands",
      name = "title-get-by-id",
      alias = "tgbi",
      description = "Get title by id")
  public String getById(@Option(required = true, description = "id", longName = "id") long id) {
    Title title = titleService.getById(id);
    return objectMapperService.convertToString(title);
  }

  @Command(
      group = "Title commands",
      name = "title-get-all",
      alias = "tga",
      description = "Get all titles")
  public String getAll() {
    List<Title> titles = titleService.getAll();
    return objectMapperService.convertToString(titles);
  }

  @Command(
      group = "Title commands",
      name = "title-insert",
      alias = "ti",
      description = "Insert new title")
  public String insert(
      @Option(required = true, description = "title name", longName = "titleName")
          String titleName) {
    long id = titleService.insert(titleName);
    return objectMapperService.convertToString(id);
  }

  @Command(
      group = "Title commands",
      name = "title-delete",
      alias = "td",
      description = "Delete title by id")
  public String deleteById(@Option(required = true, description = "id", longName = "id") long id) {
    titleService.deleteById(id);
    return String.format("Deleted title with id - %d", id);
  }

  @Command(
      group = "Title commands",
      name = "title-update",
      alias = "tu",
      description = "Update title")
  public String update(
      @Option(required = true, description = "id", longName = "id") long id,
      @Option(required = true, description = "author name", longName = "authorName")
          String authorName,
      @Option(required = true, description = "filter", longName = "filter") long filter) {
    Title title = new Title(id, authorName, filter);
    titleService.update(title);
    return String.format("Updated title - %s", objectMapperService.convertToString(title));
  }
}
