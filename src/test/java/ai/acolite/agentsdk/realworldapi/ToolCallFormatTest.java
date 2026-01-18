package ai.acolite.agentsdk.realworldapi;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import ai.acolite.agentsdk.core.Agent;
import ai.acolite.agentsdk.core.ModelResponse;
import ai.acolite.agentsdk.core.RunResult;
import ai.acolite.agentsdk.core.Runner;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import ai.acolite.agentsdk.examples.tools.CalculatorTool;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

/**
 * Integration test to inspect the actual format of tool calls from OpenAI Responses API.
 *
 * <p>Helps understand the structure of tool calls, what fields are present, and how to parse them
 * correctly.
 *
 * <p>Usage: OPENAI_API_KEY=sk-... mvn test -Dtest=ToolCallFormatTest
 */
@SetEnvironmentVariable(key = "OPENAI_MODEL", value = "gpt-4.1-nano")
class ToolCallFormatTest {

  @BeforeAll
  static void checkApiKey() {
    String apiKey = System.getenv("OPENAI_API_KEY");
    assumeTrue(
        apiKey != null && !apiKey.isEmpty(), "OPENAI_API_KEY environment variable must be set");
  }

  @Test
  void inspectToolCallFormat() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MathAssistant")
            .instructions(
                "You are a math assistant. Use the calculator tool to perform calculations.")
            .tools(List.of(new CalculatorTool()))
            .build();

    System.out.println("\n=== Inspecting Tool Call Format ===");
    System.out.println("Asking: What is 123 multiplied by 456?");

    RunResult<UnknownContext, ?> result =
        Runner.run(agent, "What is 123 multiplied by 456? Use the calculator tool.");

    printRawResponses(result);
    printConversationItems(result);
    printFinalOutput(result);

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());

    System.out.println("\n=== End Tool Call Format Inspection ===\n");
  }

  @Test
  void simpleMathWithoutToolUse() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("BasicAgent")
            .instructions("You are a helpful assistant.")
            .build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "What is 2 + 2?");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());
    boolean containsFour = result.getFinalOutput().toString().contains("4");
    assertTrue(containsFour);
  }

  private void printRawResponses(RunResult<UnknownContext, ?> result) {
    System.out.println("\n--- Raw Responses ---");
    for (int i = 0; i < result.getRawResponses().size(); i++) {
      ModelResponse response = result.getRawResponses().get(i);
      System.out.println("\nTurn " + (i + 1) + ":");
      System.out.println("  Response ID: " + response.getResponseId().orElse("none"));
      System.out.println("  Output items: " + response.getOutput().size());

      printOutputItems(response);
    }
  }

  private void printOutputItems(ModelResponse response) {
    for (int j = 0; j < response.getOutput().size(); j++) {
      Object outputItem = response.getOutput().get(j);
      System.out.println("\n  Output " + (j + 1) + ":");
      System.out.println("    Type: " + outputItem.getClass().getName());
      System.out.println("    Value: " + outputItem);

      if (outputItem instanceof java.util.Map<?, ?> map) {
        System.out.println("    Map keys: " + map.keySet());
        for (var entry : map.entrySet()) {
          System.out.println("      " + entry.getKey() + " = " + entry.getValue());
        }
      }
    }
  }

  private void printConversationItems(RunResult<UnknownContext, ?> result) {
    System.out.println("\n--- Conversation Items ---");
    System.out.println("Total items: " + result.getNewItems().size());
    for (int i = 0; i < result.getNewItems().size(); i++) {
      Object item = result.getNewItems().get(i);
      System.out.println("\nItem " + (i + 1) + ":");
      System.out.println("  Type: " + item.getClass().getSimpleName());
      System.out.println("  Value: " + item);
    }
  }

  private void printFinalOutput(RunResult<UnknownContext, ?> result) {
    System.out.println("\n--- Final Output ---");
    System.out.println(result.getFinalOutput());
  }
}
