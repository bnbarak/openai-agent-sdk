package ai.acolite.agentsdk.realworldapi.testdata;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class MathResult {
  @JsonPropertyDescription("The numeric answer to the math problem")
  public int answer;

  @JsonPropertyDescription("A brief explanation of how the answer was calculated")
  public String explanation;
}
