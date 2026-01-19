package ai.acolite.agentsdk.realworldapi;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import ai.acolite.agentsdk.core.*;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

/**
 * Real integration tests for multi-turn agent execution.
 *
 * <p>These tests verify that the multi-turn execution loop works correctly with the actual OpenAI
 * API, including turn tracking, usage accumulation, and proper termination conditions.
 *
 * <p>Requirements: 1. OPENAI_API_KEY environment variable must be set 2. Multi-turn execution loop
 * must be implemented
 *
 * <p>Usage: OPENAI_API_KEY=sk-... mvn test -Dtest=MultiTurnRealAPITest
 */
@SetEnvironmentVariable(key = "OPENAI_MODEL", value = "gpt-4.1-nano")
class MultiTurnRealAPITest {

  @BeforeAll
  static void checkApiKey() {
    System.setProperty("OPENAI_MODEL", "gpt-4.1-nano");
    String apiKey = System.getenv("OPENAI_API_KEY");
    assumeTrue(
        apiKey != null && !apiKey.isEmpty(), "OPENAI_API_KEY environment variable must be set");
  }

  @Test
  void singleTurn_completesInOneTurn() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("QuickAssistant")
            .instructions("Answer questions concisely in one sentence.")
            .build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "What is the capital of France?");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());

    boolean mentionsParis = result.getFinalOutput().toString().toLowerCase().contains("paris");
    assertTrue(mentionsParis);

    boolean completedInSingleTurn = result.getRawResponses().size() == 1;
    assertTrue(completedInSingleTurn);

    assertNotNull(result.getUsage());
    boolean trackedTokenUsage = result.getUsage().getTotalTokens() > 0;
    assertTrue(trackedTokenUsage);
  }

  @Test
  void complexTask_tracksMultipleResponses() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("ThinkingAssistant")
            .instructions("Think step by step to solve problems.")
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(agent, "Explain the concept of recursion with a simple example.");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());

    String output = result.getFinalOutput().toString();
    boolean providedDetailedExplanation = output.length() > 50;
    assertTrue(providedDetailedExplanation);

    assertNotNull(result.getRawResponses());
    assertFalse(result.getRawResponses().isEmpty());
    assertNotNull(result.getLastResponseId());
  }

  @Test
  void maxTurnsLimit_isRespected() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("LimitedAgent")
            .instructions("You are a helpful assistant.")
            .build();

    RunConfig config = RunConfig.builder().maxTurns(2).build();

    RunResult<UnknownContext, ?> result =
        Runner.run(agent, "Tell me about artificial intelligence.", config);

    assertNotNull(result);
    boolean withinMaxTurns = result.getRawResponses().size() <= 2;
    assertTrue(withinMaxTurns);
  }

  @Test
  void usageAccumulation_tracksAllTurns() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("UsageTrackingAgent")
            .instructions("Provide detailed, thoughtful answers.")
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(agent, "What are the benefits and drawbacks of functional programming?");

    assertNotNull(result);
    assertNotNull(result.getUsage());

    boolean trackedInputTokens = result.getUsage().getInputTokens() > 0;
    assertTrue(trackedInputTokens);

    boolean trackedOutputTokens = result.getUsage().getOutputTokens() > 0;
    assertTrue(trackedOutputTokens);

    assertEquals(
        result.getUsage().getInputTokens() + result.getUsage().getOutputTokens(),
        result.getUsage().getTotalTokens(),
        0.01);

    if (result.getRawResponses().size() > 1) {
      double summedTotal =
          result.getRawResponses().stream().mapToDouble(r -> r.getUsage().getTotalTokens()).sum();
      assertEquals(summedTotal, result.getUsage().getTotalTokens(), 0.01);
    }
  }

  @Test
  void inputPreservation_maintainsOriginalInput() {
    String originalInput = "Explain quantum computing in simple terms.";

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("InputPreservationAgent")
            .instructions("You are a helpful science communicator.")
            .build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, originalInput);

    assertNotNull(result);
    assertNotNull(result.getInput());

    boolean hasSingleInput = result.getInput().size() == 1;
    assertTrue(hasSingleInput);
    assertEquals(originalInput, result.getInput().getFirst());
  }

  @Test
  void newItemsGeneration_capturesConversation() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("ConversationAgent")
            .instructions("You are a friendly assistant.")
            .build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "What is machine learning?");

    assertNotNull(result);
    assertNotNull(result.getNewItems());
    assertFalse(result.getNewItems().isEmpty());

    boolean hasMessageOutput =
        result.getNewItems().stream().anyMatch(item -> item instanceof RunMessageOutputItem);
    assertTrue(hasMessageOutput);
  }

  @Test
  void differentModels_multiTurnBehavior() {
    Agent<UnknownContext, TextOutput> gpt4Agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("GPT4MultiTurn")
            .instructions("Provide comprehensive answers.")
            .model("gpt-4.1-mini")
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(gpt4Agent, "What are the key principles of object-oriented programming?");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());

    String output = result.getFinalOutput().toString();
    boolean providedDetailedAnswer = output.length() > 100;
    assertTrue(providedDetailedAnswer);
    assertNotNull(result.getLastResponseId());
  }

  @Test
  void emptyResponse_handledGracefully() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MinimalAgent")
            .instructions("Respond with just 'OK'")
            .build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "Acknowledge this message.");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());

    String output = result.getFinalOutput().toString();
    boolean hasOutput = !output.isEmpty();
    assertTrue(hasOutput);
  }

  @Test
  void largeOutput_handledCorrectly() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("VerboseAgent")
            .instructions("Provide a comprehensive, detailed explanation with examples.")
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(
            agent,
            "Explain the history and evolution of programming languages from the 1950s to today.");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());

    String output = result.getFinalOutput().toString();
    boolean providedDetailedResponse = output.length() > 200;
    assertTrue(providedDetailedResponse);

    boolean trackedTokensForLargeOutput = result.getUsage().getOutputTokens() > 50;
    assertTrue(trackedTokensForLargeOutput);
  }
}
