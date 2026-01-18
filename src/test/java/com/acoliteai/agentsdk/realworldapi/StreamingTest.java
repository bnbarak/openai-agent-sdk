package com.acoliteai.agentsdk.realworldapi;

import static com.acoliteai.agentsdk.testutil.StreamingTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.acoliteai.agentsdk.core.*;
import com.acoliteai.agentsdk.core.types.TextOutput;
import com.acoliteai.agentsdk.core.types.UnknownContext;
import com.acoliteai.agentsdk.testutil.StreamingTestUtil;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

/**
 * Integration tests for streaming functionality. These tests use the real OpenAI API to validate
 * end-to-end streaming behavior.
 */
@SetEnvironmentVariable(key = "OPENAI_MODEL", value = "gpt-4.1-nano")
class StreamingTest {

  @BeforeAll
  static void checkApiKey() {
    String apiKey = System.getenv("OPENAI_API_KEY");
    assumeTrue(
        apiKey != null && !apiKey.isEmpty(), "OPENAI_API_KEY not set - skipping streaming tests");
  }

  @Test
  void runStreamed_withToolCall_emitsAllEventTypes() {
    Agent<UnknownContext, TextOutput> agent = createMathAgent();

    StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
        Runner.runStreamed(agent, "What is 10 plus 20?");
    List<RunStreamEvent> events = collectAllEvents(result);

    assertFalse(events.isEmpty(), "Should receive at least one event");
    assertTrue(countEventsByType(events, "tool_called") > 0, "Should have tool_called events");
    assertTrue(countEventsByType(events, "tool_output") > 0, "Should have tool_output events");
    assertTrue(
        countEventsByType(events, "message_output_created") > 0,
        "Should have message_output_created events");
  }

  @Test
  void runStreamed_withToolCall_emitsToolCallBeforeOutput() {
    Agent<UnknownContext, TextOutput> agent = createMathAgent();

    StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
        Runner.runStreamed(agent, "Calculate 5 times 6");
    List<String> eventTypes = collectEventTypes(result);

    int toolCallIndex = eventTypes.indexOf("tool_called");
    int toolOutputIndex = eventTypes.indexOf("tool_output");
    assertTrue(toolCallIndex >= 0, "Should have tool_called event");
    assertTrue(toolOutputIndex >= 0, "Should have tool_output event");
    assertTrue(toolCallIndex < toolOutputIndex, "tool_called should come before tool_output");
  }

  @Test
  void runStreamed_toolCallEvent_containsToolName() {
    Agent<UnknownContext, TextOutput> agent = createMathAgent();

    StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
        Runner.runStreamed(agent, "What is 100 divided by 4?");
    List<RunStreamEvent> events = collectAllEvents(result);

    RunToolCallItem toolCall =
        events.stream()
            .filter(e -> e instanceof RunItemStreamEvent)
            .map(e -> ((RunItemStreamEvent) e).item())
            .filter(item -> item instanceof RunToolCallItem)
            .map(item -> (RunToolCallItem) item)
            .findFirst()
            .orElse(null);
    assertNotNull(toolCall, "Should find tool call event");
    assertEquals("calculator", toolCall.getName());
    assertNotNull(toolCall.getParameters());
  }

  @Test
  void runStreamed_toolOutputEvent_containsToolCallId() {
    Agent<UnknownContext, TextOutput> agent = createMathAgent();

    StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
        Runner.runStreamed(agent, "What is 100 divided by 4?");
    List<RunStreamEvent> events = collectAllEvents(result);

    RunToolCallOutputItem toolOutput =
        events.stream()
            .filter(e -> e instanceof RunItemStreamEvent)
            .map(e -> ((RunItemStreamEvent) e).item())
            .filter(item -> item instanceof RunToolCallOutputItem)
            .map(item -> (RunToolCallOutputItem) item)
            .findFirst()
            .orElse(null);
    assertNotNull(toolOutput, "Should find tool output event");
    assertNotNull(toolOutput.getToolCallId());
    assertTrue(
        toolOutput.getError().isEmpty() || toolOutput.getResult() != null,
        "Should have either result or error");
  }

  @Test
  void runStreamed_messageEvent_containsNonEmptyContent() {
    Agent<UnknownContext, TextOutput> agent = createMathAgent();

    StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
        Runner.runStreamed(agent, "What is 100 divided by 4?");
    List<RunStreamEvent> events = collectAllEvents(result);

    RunMessageOutputItem message =
        events.stream()
            .filter(e -> e instanceof RunItemStreamEvent)
            .map(e -> ((RunItemStreamEvent) e).item())
            .filter(item -> item instanceof RunMessageOutputItem)
            .map(item -> (RunMessageOutputItem) item)
            .findFirst()
            .orElse(null);
    assertNotNull(message, "Should find message event");
    assertNotNull(message.getContent());
    Object content = message.getContent();
    if (content instanceof String) {
      assertFalse(((String) content).isEmpty());
    }
  }

  @Test
  void toTextStream_filtersOnlyTextMessages() {
    Agent<UnknownContext, TextOutput> agent = createMathAgent();

    StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
        Runner.runStreamed(agent, "What is 7 plus 8?");
    List<String> textChunks = collectTextChunks(result);

    assertFalse(textChunks.isEmpty(), "Should receive text output");
    textChunks.forEach(
        chunk -> {
          assertNotNull(chunk);
          assertFalse(chunk.isEmpty());
        });
    String fullText = String.join("", textChunks);
    assertTrue(
        fullText.contains("15") || fullText.contains("fifteen"), "Text should contain the answer");
  }

  @Test
  void runStreamed_withoutTools_emitsMessageEvent() {
    Agent<UnknownContext, TextOutput> agent = createSimpleAgent();

    StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
        Runner.runStreamed(agent, "Say hello");
    List<RunStreamEvent> events = collectAllEvents(result);

    assertFalse(events.isEmpty());
    boolean hasMessage =
        events.stream().anyMatch(e -> e.getType().equals("message_output_created"));
    assertTrue(hasMessage, "Should have message output event");
  }

  @Test
  void runStreamed_multipleToolCalls_emitsAllToolEvents() {
    Agent<UnknownContext, TextOutput> agent = createMathAgent();

    StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
        Runner.runStreamed(agent, "Calculate 2+3 and then 5*4");
    List<RunStreamEvent> events = collectAllEvents(result);

    long toolCallCount = countEventsByType(events, "tool_called");
    long toolOutputCount = countEventsByType(events, "tool_output");
    assertTrue(toolCallCount >= 1, "Should have at least one tool call");
    assertEquals(toolCallCount, toolOutputCount, "Each tool call should have an output");
  }

  @Test
  void toStream_returnsSameInstanceOnMultipleCalls() {
    Agent<UnknownContext, TextOutput> agent = createSimpleAgent();
    StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
        Runner.runStreamed(agent, "Hello");

    var stream1 = result.toStream();
    StreamingTestUtil.collectAllEvents(result);
    var stream2 = result.toStream();

    assertNotNull(stream1);
    assertSame(stream1, stream2, "Should return the same stream instance");
  }
}
