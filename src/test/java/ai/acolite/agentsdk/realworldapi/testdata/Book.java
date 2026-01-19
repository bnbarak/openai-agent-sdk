package ai.acolite.agentsdk.realworldapi.testdata;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class Book {
  @JsonPropertyDescription("The title of the book")
  public String title;

  @JsonPropertyDescription("The author of the book")
  public String author;

  @JsonPropertyDescription("Year the book was published")
  public int publicationYear;
}
