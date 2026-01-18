package com.acoliteai.agentsdk.examples;

import com.acoliteai.agentsdk.core.Agent;
import com.acoliteai.agentsdk.core.RunConfig;
import com.acoliteai.agentsdk.core.RunResult;
import com.acoliteai.agentsdk.core.Runner;
import com.acoliteai.agentsdk.core.types.TextOutput;
import com.acoliteai.agentsdk.core.types.UnknownContext;

/**
 * AgentConfigurationExample
 *
 * <p>Example demonstrating various agent configuration options.
 *
 * <p>Usage: OPENAI_API_KEY=sk-... java com.acoliteai.agentsdk.examples.AgentConfigurationExample
 */
public class AgentConfigurationExample {

  public static void main(String[] args) {
    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Error: OPENAI_API_KEY environment variable not set");
      System.exit(1);
    }

    System.out.println("=== Agent Configuration Examples ===\n");

    basicAgent();
    System.out.println();
    agentWithCustomInstructions();
    System.out.println();
    agentWithCustomModel();
    System.out.println();
    agentWithRunConfig();
  }

  /** Example 1: Basic agent with minimal configuration */
  private static void basicAgent() {
    System.out.println("1. Basic Agent");
    System.out.println("--------------");

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("BasicAssistant")
            .instructions("You are a helpful assistant")
            .build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "What is 2 + 2?");

    System.out.println("Response: " + result.getFinalOutput());
    System.out.println("Model used: " + agent.getModel());
  }

  /** Example 2: Agent with custom instructions */
  private static void agentWithCustomInstructions() {
    System.out.println("2. Agent with Custom Instructions");
    System.out.println("---------------------------------");

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("CreativeAssistant")
            .instructions(
                "You are a creative writing assistant. Write in a vivid, descriptive style.")
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(agent, "Write a creative opening line for a sci-fi story");

    System.out.println("Response: " + result.getFinalOutput());
  }

  /** Example 3: Agent with specific model selection */
  private static void agentWithCustomModel() {
    System.out.println("3. Agent with Custom Model");
    System.out.println("--------------------------");

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("FastAssistant")
            .instructions("Answer quickly and concisely")
            .model("gpt-3.5-turbo")
            .build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "What's the capital of France?");

    System.out.println("Response: " + result.getFinalOutput());
    System.out.println("Model: " + agent.getModel());
  }

  /** Example 4: Agent with custom run configuration */
  private static void agentWithRunConfig() {
    System.out.println("4. Agent with Run Configuration");
    System.out.println("-------------------------------");

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("ConfiguredAssistant")
            .instructions("You are a helpful assistant")
            .build();

    RunConfig config = RunConfig.builder().maxTurns(5).build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "Hello!", config);

    System.out.println("Response: " + result.getFinalOutput());
    System.out.println("Max turns allowed: " + config.getEffectiveMaxTurns());
    System.out.println("Total tokens: " + result.getUsage().getTotalTokens());
  }
}
