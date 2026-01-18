package com.acoliteai.agentsdk.realworldapi.testdata;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public class BookList {
  @JsonPropertyDescription("List of books")
  public List<Book> books;
}
