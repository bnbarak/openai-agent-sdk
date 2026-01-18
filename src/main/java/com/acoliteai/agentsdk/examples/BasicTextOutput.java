package com.acoliteai.agentsdk.examples;

import com.acoliteai.agentsdk.core.Agent;
import com.acoliteai.agentsdk.core.RunResult;
import com.acoliteai.agentsdk.core.Runner;
import com.acoliteai.agentsdk.core.types.TextOutput;
import com.acoliteai.agentsdk.core.types.UnknownContext;

/**
 * BasicTextOutput
 *
 * <p>Simplest example demonstrating text-based agent interactions.
 *
 * <p>This example shows: - Creating a basic agent with instructions - Running the agent with a text
 * prompt - Accessing the text response and usage statistics
 *
 * <p>Usage: OPENAI_API_KEY=sk-... java com.openai.agents.examples.BasicTextOutput
 */
public class BasicTextOutput {

  public static void main(String[] args) {
    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Error: OPENAI_API_KEY environment variable not set");
      System.err.println(
          "Usage: OPENAI_API_KEY=sk-... java com.openai.agents.examples.BasicTextOutput");
      System.exit(1);
    }

    System.out.println("=== Basic Text Output Example ===\n");

    // region create-agent
    // Create a simple agent
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Assistant")
            .instructions("You are a helpful assistant. Keep responses concise and clear.")
            .build();
    // endregion create-agent

    // region run-agent
    // Run the agent with a simple question
    RunResult<UnknownContext, ?> result =
        Runner.run(agent, "Explain what an AI agent is in one sentence.");
    // endregion run-agent

    // Display the response
    System.out.println("Question:");
    System.out.println("Explain what an AI agent is in one sentence.");
    System.out.println();
    System.out.println("Agent response:");
    System.out.println(result.getFinalOutput());
    System.out.println();

    // region check-usage
    // Display usage statistics
    System.out.println("Usage statistics:");
    System.out.println("  Total tokens: " + result.getUsage().getTotalTokens());
    System.out.println("  Input tokens: " + result.getUsage().getInputTokens());
    System.out.println("  Output tokens: " + result.getUsage().getOutputTokens());
    // endregion check-usage
  }
}
