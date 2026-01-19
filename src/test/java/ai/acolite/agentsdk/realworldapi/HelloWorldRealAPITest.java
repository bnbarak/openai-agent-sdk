package ai.acolite.agentsdk.realworldapi;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import ai.acolite.agentsdk.core.Agent;
import ai.acolite.agentsdk.core.RunConfig;
import ai.acolite.agentsdk.core.RunResult;
import ai.acolite.agentsdk.core.Runner;
import ai.acolite.agentsdk.core.types.JsonSchemaOutput;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import ai.acolite.agentsdk.openai.OpenAIProvider;
import ai.acolite.agentsdk.realworldapi.testdata.MathResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

/**
 * Real integration test that calls the actual OpenAI API.
 *
 * <p>Requirements: 1. OPENAI_API_KEY environment variable must be set 2. Runner.executeRun() must
 * be implemented 3. OpenAIProvider must be implemented 4. OpenAIResponsesModel must be implemented
 *
 * <p>This test is gated by API key presence.
 *
 * <p>Usage: OPENAI_API_KEY=sk-... mvn test -Dtest=HelloWorldRealAPITest
 */
@SetEnvironmentVariable(key = "OPENAI_MODEL", value = "gpt-4.1-nano")
class HelloWorldRealAPITest {

  @BeforeAll
  static void checkApiKey() {
    String apiKey = System.getenv("OPENAI_API_KEY");
    assumeTrue(
        apiKey != null && !apiKey.isEmpty(), "OPENAI_API_KEY environment variable must be set");
  }

  @Test
  void helloWorld_withRealAPI_returnsHaiku() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Assistant")
            .instructions("You are a helpful assistant")
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(agent, "Write a haiku about recursion in programming.");

    assertNotNull(result, "Result should not be null");
    assertNotNull(result.getFinalOutput(), "Final output should not be null");
    String output = result.getFinalOutput().toString();
    assertFalse(output.isEmpty(), "Output should not be empty");
    String[] lines = output.split("\n");
    assertTrue(lines.length >= 3, "Haiku should have at least 3 lines, got: " + lines.length);
    assertNotNull(result.getUsage(), "Usage should be tracked");
    assertTrue(result.getUsage().getTotalTokens() > 0, "Total tokens should be greater than 0");
    assertTrue(result.getUsage().getInputTokens() > 0, "Input tokens should be greater than 0");
    assertTrue(result.getUsage().getOutputTokens() > 0, "Output tokens should be greater than 0");
  }

  @Test
  void simpleQuestion_withRealAPI_returnsAnswer() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MathAssistant")
            .instructions("You are a helpful math assistant. Keep answers concise.")
            .model("gpt-4.1-mini")
            .build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "What is 2 + 2?");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());
    String output = result.getFinalOutput().toString();
    assertTrue(output.contains("4"), "Answer should contain '4'");
  }

  @Test
  void multipleModels_canBeUsed() {
    Agent<UnknownContext, TextOutput> gpt4Agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("GPT4Assistant")
            .instructions("Say 'Hello from GPT-4'")
            .model("gpt-4.1-mini")
            .build();

    RunResult<UnknownContext, ?> result4 = Runner.run(gpt4Agent, "Respond");

    assertNotNull(result4);
    Agent<UnknownContext, TextOutput> gpt35Agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("GPT35Assistant")
            .instructions("Say 'Hello from GPT-3.5'")
            .model("gpt-3.5-turbo")
            .build();

    RunResult<UnknownContext, ?> result35 = Runner.run(gpt35Agent, "Respond");

    assertNotNull(result35);
  }

  @Test
  void longPrompt_handledCorrectly() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("SummaryAssistant")
            .instructions("Summarize the input in one sentence.")
            .build();
    String longPrompt =
        "The OpenAI Agents SDK is a framework for building AI agents "
            + "that can interact with various AI models. It provides abstractions for "
            + "agent configuration, model providers, and execution flows. The SDK supports "
            + "both TypeScript and Java implementations.";

    RunResult<UnknownContext, ?> result = Runner.run(agent, longPrompt);

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());
    String summary = result.getFinalOutput().toString();
    assertTrue(summary.length() < longPrompt.length(), "Summary should be shorter than original");
  }

  @Test
  void structuredOutput_withMathResult_returnsStructuredData() {
    JsonSchemaOutput<MathResult> outputType = JsonSchemaOutput.of(MathResult.class);
    Agent<UnknownContext, JsonSchemaOutput<MathResult>> agent =
        Agent.<UnknownContext, JsonSchemaOutput<MathResult>>builder()
            .name("MathAssistant")
            .instructions(
                "You are a math assistant that returns answers in the specified JSON format.")
            .outputType(outputType)
            .build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "What is 15 + 27?");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());
    assertInstanceOf(
        MathResult.class,
        result.getFinalOutput(),
        "Output should be MathResult, got: " + result.getFinalOutput().getClass());
    MathResult mathResult = (MathResult) result.getFinalOutput();
    assertEquals(42, mathResult.answer, "Answer should be 42");
    assertNotNull(mathResult.explanation, "Should have explanation");
    assertFalse(mathResult.explanation.isEmpty(), "Explanation should not be empty");
  }

  @Test
  void errorHandling_invalidAPIKey_throwsException() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("TestAgent")
            .instructions("You are a test assistant")
            .build();
    OpenAIProvider invalidProvider = new OpenAIProvider("sk-invalid-test-key-that-will-fail");
    RunConfig config = RunConfig.builder().modelProvider(invalidProvider).build();

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> Runner.run(agent, "Hello", config));

    String message = exception.getMessage() != null ? exception.getMessage().toLowerCase() : "";
    Throwable cause = exception.getCause();
    String causeMessage =
        cause != null && cause.getMessage() != null ? cause.getMessage().toLowerCase() : "";
    boolean hasAuthError =
        message.contains("401")
            || message.contains("unauthorized")
            || message.contains("authentication")
            || message.contains("api key")
            || causeMessage.contains("401")
            || causeMessage.contains("unauthorized")
            || causeMessage.contains("authentication")
            || causeMessage.contains("api key");
    assertTrue(
        hasAuthError,
        "Expected authentication error, got: "
            + exception.getClass().getName()
            + " - "
            + message
            + (cause != null ? " (cause: " + causeMessage + ")" : ""));
  }
}
