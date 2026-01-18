package com.acoliteai.agentsdk.realworldapi;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.acoliteai.agentsdk.core.*;
import com.acoliteai.agentsdk.core.types.TextOutput;
import com.acoliteai.agentsdk.core.types.UnknownContext;
import com.acoliteai.agentsdk.examples.tools.CalculatorTool;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

/**
 * Real integration tests for tool calling with OpenAI API.
 *
 * <p>These tests verify that agents can successfully use tools during conversations, including tool
 * registration, execution, and result synthesis.
 *
 * <p>Usage: OPENAI_API_KEY=sk-... mvn test -Dtest=ToolCallingRealAPITest
 */
@SetEnvironmentVariable(key = "OPENAI_MODEL", value = "gpt-4.1-nano")
class ToolCallingRealAPITest {

  @BeforeAll
  static void checkApiKey() {
    String apiKey = System.getenv("OPENAI_API_KEY");
    assumeTrue(
        apiKey != null && !apiKey.isEmpty(), "OPENAI_API_KEY environment variable must be set");
  }

  @Test
  void calculatorTool_simpleMultiplication() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MathAssistant")
            .instructions("You are a math assistant. Use the calculator tool.")
            .tools(List.of(new CalculatorTool()))
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(agent, "What is 123 multiplied by 456? Use the calculator tool.");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());
    String output = result.getFinalOutput().toString();
    assertTrue(output.contains("56088") || output.contains("56,088"));
    assertTrue(result.getNewItems().stream().anyMatch(item -> item instanceof RunToolCallItem));
    assertTrue(
        result.getNewItems().stream().anyMatch(item -> item instanceof RunToolCallOutputItem));
  }

  @Test
  void calculatorTool_addition() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MathAssistant")
            .instructions("You are a math assistant. Use the calculator tool.")
            .tools(List.of(new CalculatorTool()))
            .build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "Calculate 789 plus 321");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());
    String output = result.getFinalOutput().toString();
    assertTrue(output.contains("1110") || output.contains("1,110"));
  }

  @Test
  void calculatorTool_division() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MathAssistant")
            .instructions("You are a math assistant. Use the calculator tool.")
            .tools(List.of(new CalculatorTool()))
            .build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "What is 100 divided by 4?");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());
    String output = result.getFinalOutput().toString();
    assertTrue(output.contains("25"));
  }

  @Test
  void calculatorTool_multipleOperations() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MathAssistant")
            .instructions("You are a math assistant. Use the calculator tool for each operation.")
            .tools(List.of(new CalculatorTool()))
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(agent, "First calculate 50 + 30, then multiply that result by 2.");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());
    String output = result.getFinalOutput().toString();
    assertTrue(output.contains("80") && output.contains("160"));
    long toolCallCount =
        result.getNewItems().stream().filter(item -> item instanceof RunToolCallItem).count();
    assertTrue(toolCallCount >= 2);
  }

  @Test
  void toolCallTracking_capturesToolCallItems() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MathAssistant")
            .instructions("Use the calculator tool.")
            .tools(List.of(new CalculatorTool()))
            .build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "Calculate 7 times 8");

    List<RunToolCallItem> toolCalls =
        result.getNewItems().stream()
            .filter(item -> item instanceof RunToolCallItem)
            .map(item -> (RunToolCallItem) item)
            .toList();

    assertFalse(toolCalls.isEmpty());
    RunToolCallItem toolCall = toolCalls.get(0);
    assertEquals("calculator", toolCall.getName());
    assertNotNull(toolCall.getId());
    assertNotNull(toolCall.getParameters());
  }

  @Test
  void toolOutputTracking_capturesResults() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MathAssistant")
            .instructions("Use the calculator tool.")
            .tools(List.of(new CalculatorTool()))
            .build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "What is 15 minus 8?");

    List<RunToolCallOutputItem> toolOutputs =
        result.getNewItems().stream()
            .filter(item -> item instanceof RunToolCallOutputItem)
            .map(item -> (RunToolCallOutputItem) item)
            .toList();

    assertFalse(toolOutputs.isEmpty());
    RunToolCallOutputItem output = toolOutputs.get(0);
    assertNotNull(output.getToolCallId());
    assertNotNull(output.getResult());
    assertTrue(output.getError().isEmpty());
  }

  @Test
  void withoutTools_stillWorks() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("BasicAssistant")
            .instructions("You are a helpful assistant.")
            .build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "What is 2 + 2?");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());
    String output = result.getFinalOutput().toString();
    assertTrue(output.contains("4"));
    assertTrue(result.getNewItems().stream().noneMatch(item -> item instanceof RunToolCallItem));
  }
}
