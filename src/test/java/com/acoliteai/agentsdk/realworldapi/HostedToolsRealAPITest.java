package com.acoliteai.agentsdk.realworldapi;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.acoliteai.agentsdk.core.Agent;
import com.acoliteai.agentsdk.core.HostedTool;
import com.acoliteai.agentsdk.core.RunResult;
import com.acoliteai.agentsdk.core.Runner;
import com.acoliteai.agentsdk.core.types.TextOutput;
import com.acoliteai.agentsdk.core.types.UnknownContext;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

/**
 * Real integration tests for OpenAI hosted tools.
 *
 * <p>Usage: OPENAI_API_KEY=sk-... mvn test -Dtest=HostedToolsRealAPITest
 */
@SetEnvironmentVariable(key = "OPENAI_MODEL", value = "gpt-4.1-mini")
class HostedToolsRealAPITest {

  @BeforeAll
  static void checkApiKey() {
    String apiKey = System.getenv("OPENAI_API_KEY");
    assumeTrue(
        apiKey != null && !apiKey.isEmpty(), "OPENAI_API_KEY environment variable must be set");
  }

  @Test
  void webSearch() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("SearchAssistant")
            .instructions("You MUST use the web_search tool to search for current information.")
            .tools(List.of(HostedTool.webSearch()))
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(agent, "What is the weather in San Francisco today? You MUST web_search.");

    boolean toolWasInvoked =
        result.getNewItems().stream()
            .anyMatch(item -> item.getClass().getSimpleName().equals("RunToolCallItem"));
    assertTrue(toolWasInvoked, "Expected web_search tool to be invoked");
  }

  @Test
  void imageGeneration() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Artist")
            .instructions(
                "You MUST use the image_generation tool to create images. "
                    + "Always use the tool when asked to generate, create, or draw images.")
            .tools(List.of(HostedTool.imageGeneration()))
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(
            agent, "Please generate an image of a red circle. Use the image generation tool.");

    boolean toolWasInvoked =
        result.getNewItems().stream()
            .anyMatch(item -> item.getClass().getSimpleName().equals("RunToolCallItem"));
    assertTrue(toolWasInvoked, "Expected image_generation tool to be invoked");
  }

  @Test
  void codeInterpreter_throwsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> HostedTool.codeInterpreter(),
        "code_interpreter should throw UnsupportedOperationException");
  }

  @Test
  void fileSearch_throwsUnsupportedOperationException() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> HostedTool.fileSearch("vs_123"),
        "file_search should throw UnsupportedOperationException");
  }
}
