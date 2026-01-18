package ai.acolite.agentsdk.examples;

import ai.acolite.agentsdk.core.*;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;

/**
 * MultiTurnExecutionExample
 *
 * <p>Example demonstrating multi-turn agent execution with detailed tracking.
 *
 * <p>This example shows: - Configuring execution with RunConfig - Setting max turns limit -
 * Tracking usage across multiple turns - Monitoring generated conversation items - Accessing
 * detailed execution metrics
 *
 * <p>Usage: OPENAI_API_KEY=sk-... java ai.acolite.agentsdk.examples.MultiTurnExecutionExample
 */
public class MultiTurnExecutionExample {

  public static void main(String[] args) {
    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Error: OPENAI_API_KEY environment variable not set");
      System.err.println(
          "Usage: OPENAI_API_KEY=sk-... java ai.acolite.agentsdk.examples.MultiTurnExecutionExample");
      System.exit(1);
    }

    System.out.println("=== Multi-Turn Execution Example ===\n");

    simpleMultiTurn();
    System.out.println("\n" + "=".repeat(60) + "\n");
    detailedTracking();
  }

  /** Example 1: Simple multi-turn with max turns limit */
  private static void simpleMultiTurn() {
    System.out.println("Example 1: Simple Multi-Turn Execution");
    System.out.println("--------------------------------------");

    // region create-agent
    // Create an agent that thinks step by step
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("ThinkingAssistant")
            .instructions("Think step by step to provide thorough, well-reasoned answers.")
            .build();
    // endregion create-agent

    // region configure-run
    // Configure execution with max turns
    RunConfig config =
        RunConfig.builder()
            .maxTurns(5) // Allow up to 5 turns
            .build();
    // endregion configure-run

    // region execute-run
    // Run with a complex question
    RunResult<UnknownContext, ?> result =
        Runner.run(
            agent,
            "What are the key differences between object-oriented and functional programming?",
            config);
    // endregion execute-run

    // Display results
    System.out.println("Question:");
    System.out.println("What are the key differences between OOP and FP?");
    System.out.println();
    System.out.println("Agent response:");
    System.out.println(result.getFinalOutput());
    System.out.println();
    // region track-usage
    System.out.println("Execution summary:");
    System.out.println(
        "  Turns taken: "
            + result.getRawResponses().size()
            + " / "
            + config.getEffectiveMaxTurns());
    System.out.println("  Total tokens: " + result.getUsage().getTotalTokens());
    // endregion track-usage
  }

  /** Example 2: Detailed tracking of multi-turn execution */
  private static void detailedTracking() {
    System.out.println("Example 2: Detailed Multi-Turn Tracking");
    System.out.println("---------------------------------------");

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("DetailedAgent")
            .instructions("Provide comprehensive, detailed explanations with examples.")
            .model("gpt-4.1-mini") // Use a specific model
            .build();

    RunConfig config = RunConfig.builder().maxTurns(10).build();

    RunResult<UnknownContext, ?> result =
        Runner.run(
            agent,
            "Explain the concept of recursion with a real-world analogy and a code example.",
            config);

    // Display comprehensive tracking information
    System.out.println("Question:");
    System.out.println("Explain recursion with analogy and code example");
    System.out.println();
    System.out.println("Agent response:");
    System.out.println(result.getFinalOutput());
    System.out.println();

    // Detailed execution metrics
    System.out.println("Detailed Execution Metrics:");
    System.out.println("─".repeat(60));
    System.out.println(
        String.format("  %-30s %d", "Turns executed:", result.getRawResponses().size()));
    System.out.println(
        String.format("  %-30s %d", "Max turns allowed:", config.getEffectiveMaxTurns()));
    System.out.println(
        String.format("  %-30s %d", "Conversation items generated:", result.getNewItems().size()));
    System.out.println();

    // Usage breakdown
    System.out.println("Token Usage:");
    System.out.println("─".repeat(60));
    System.out.println(
        String.format("  %-30s %.0f", "Input tokens:", result.getUsage().getInputTokens()));
    System.out.println(
        String.format("  %-30s %.0f", "Output tokens:", result.getUsage().getOutputTokens()));
    System.out.println(
        String.format("  %-30s %.0f", "Total tokens:", result.getUsage().getTotalTokens()));
    System.out.println();

    // Per-turn breakdown
    System.out.println("Per-Turn Token Usage:");
    System.out.println("─".repeat(60));
    int turnNumber = 1;
    for (ModelResponse response : result.getRawResponses()) {
      System.out.println(
          String.format(
              "  Turn %d: %.0f tokens (in: %.0f, out: %.0f)",
              turnNumber,
              response.getUsage().getTotalTokens(),
              response.getUsage().getInputTokens(),
              response.getUsage().getOutputTokens()));
      turnNumber++;
    }
    System.out.println();

    // Conversation items breakdown
    System.out.println("Conversation Items:");
    System.out.println("─".repeat(60));
    long messageCount =
        result.getNewItems().stream().filter(item -> item instanceof RunMessageOutputItem).count();
    System.out.println(String.format("  %-30s %d", "Message outputs:", messageCount));

    // Response IDs
    System.out.println();
    System.out.println("Response Tracking:");
    System.out.println("─".repeat(60));
    System.out.println(
        String.format("  %-30s %s", "Last response ID:", result.getLastResponseId()));
    System.out.println(
        String.format("  %-30s %d", "Total responses tracked:", result.getRawResponses().size()));
  }
}
