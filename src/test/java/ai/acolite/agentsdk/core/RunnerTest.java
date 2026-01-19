package ai.acolite.agentsdk.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ai.acolite.agentsdk.core.shims.ReadableStreamAsyncIterator;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import ai.acolite.agentsdk.exceptions.MaxTurnsExceededError;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

/** Unit tests for Runner class */
class RunnerTest {

  @Test
  void run_callsExecuteRun() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("TestAgent")
            .instructions("Test instructions")
            .build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));
    ModelResponse mockResponse =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of("Test response"))
            .responseId(Optional.of("test-response-id"))
            .providerData(Optional.empty())
            .build();
    when(mockModel.getResponse(any(ModelRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(mockResponse));
    RunConfig config = RunConfig.builder().modelProvider(mockProvider).build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "Hello", config);

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());
    verify(mockProvider).getModel(anyString());
    verify(mockModel).getResponse(any(ModelRequest.class));
  }

  @Test
  void runAsync_returnsCompletableFuture() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("TestAgent").build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));
    ModelResponse mockResponse =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of("Async test response"))
            .responseId(Optional.of("test-response-id"))
            .providerData(Optional.empty())
            .build();
    when(mockModel.getResponse(any(ModelRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(mockResponse));
    RunConfig config = RunConfig.builder().modelProvider(mockProvider).build();

    CompletableFuture<RunResult<UnknownContext, ?>> future =
        Runner.runAsync(agent, "Hello", config);

    assertNotNull(future);
    RunResult<UnknownContext, ?> result = future.join();
    assertNotNull(result);
  }

  @Test
  void runWithConfig_callsExecuteRun() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("TestAgent").build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));
    ModelResponse mockResponse =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of("Config test response"))
            .responseId(Optional.of("test-response-id"))
            .providerData(Optional.empty())
            .build();
    when(mockModel.getResponse(any(ModelRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(mockResponse));
    RunConfig config = RunConfig.builder().maxTurns(10).modelProvider(mockProvider).build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "Hello", config);

    assertNotNull(result);
    assertEquals(10, config.getEffectiveMaxTurns());
  }

  @Test
  void runAsyncWithConfig_returnsCompletableFuture() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("TestAgent").build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));
    ModelResponse mockResponse =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of("Async config test response"))
            .responseId(Optional.of("test-response-id"))
            .providerData(Optional.empty())
            .build();
    when(mockModel.getResponse(any(ModelRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(mockResponse));
    RunConfig config = RunConfig.builder().maxTurns(10).modelProvider(mockProvider).build();

    CompletableFuture<RunResult<UnknownContext, ?>> future =
        Runner.runAsync(agent, "Hello", config);

    assertNotNull(future);
    RunResult<UnknownContext, ?> result = future.join();
    assertNotNull(result);
  }

  @Test
  void runAsync_usesDefaultConfigWhenNotProvided() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("TestAgent").build();

    CompletableFuture<RunResult<UnknownContext, ?>> future = Runner.runAsync(agent, "Hello");

    assertNotNull(future);
  }

  @Test
  void staticMethods_accessibleWithoutInstance() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("TestAgent").build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));
    ModelResponse mockResponse =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of("Static methods test"))
            .responseId(Optional.of("test-response-id"))
            .providerData(Optional.empty())
            .build();
    when(mockModel.getResponse(any(ModelRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(mockResponse));
    RunConfig config = RunConfig.builder().modelProvider(mockProvider).build();

    assertDoesNotThrow(() -> Runner.run(agent, "Test", config));
    assertDoesNotThrow(() -> Runner.runAsync(agent, "Test", config).join());
  }

  @Test
  void runnerConstruction_allowed() {
    Runner runner = new Runner();

    assertNotNull(runner);
  }

  @Test
  void run_multiTurnExecution_stopsAtFinalOutput() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MultiTurnAgent")
            .instructions("Test multi-turn")
            .build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));
    ModelResponse mockResponse =
        ModelResponse.builder()
            .usage(Usage.builder().totalTokens(10.0).build())
            .output(List.of("Final answer"))
            .responseId(Optional.of("resp-1"))
            .providerData(Optional.empty())
            .build();
    when(mockModel.getResponse(any(ModelRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(mockResponse));
    RunConfig config = RunConfig.builder().maxTurns(5).modelProvider(mockProvider).build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "Question", config);

    assertNotNull(result);
    assertEquals("Final answer", result.getFinalOutput());
    verify(mockModel, times(1)).getResponse(any(ModelRequest.class));
  }

  @Test
  void run_reachesMaxTurns_stops() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MaxTurnsAgent")
            .instructions("Test max turns")
            .build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));

    // Create responses with tool calls to make loop continue
    RunToolCallItem toolCall =
        RunToolCallItem.builder()
            .id("call-1")
            .name("calculator")
            .parameters(Map.of("a", 1, "b", 2))
            .build();

    ModelResponse mockResponse =
        ModelResponse.builder()
            .usage(Usage.builder().totalTokens(5.0).build())
            .output(List.of(toolCall)) // Return tool call instead of text
            .responseId(Optional.of("resp-1"))
            .providerData(Optional.empty())
            .build();
    when(mockModel.getResponse(any(ModelRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(mockResponse));
    RunConfig config = RunConfig.builder().maxTurns(3).modelProvider(mockProvider).build();

    // Should stop at max turns and throw MaxTurnsExceededError
    MaxTurnsExceededError error =
        assertThrows(MaxTurnsExceededError.class, () -> Runner.run(agent, "Question", config));

    assertEquals(3, error.getMaxTurns());
    assertEquals(3, error.getCurrentTurn());
    verify(mockModel, times(3)).getResponse(any(ModelRequest.class));
  }

  @Test
  void run_accumulatesUsageAcrossTurns() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("UsageAgent")
            .instructions("Test usage accumulation")
            .build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));
    Usage turn1Usage =
        Usage.builder().inputTokens(10.0).outputTokens(20.0).totalTokens(30.0).build();
    Usage turn2Usage =
        Usage.builder().inputTokens(15.0).outputTokens(25.0).totalTokens(40.0).build();

    // First turn: model returns a tool call (not final)
    RunToolCallItem toolCall =
        RunToolCallItem.builder()
            .id("call-1")
            .name("get_info")
            .parameters(Map.of("query", "test"))
            .build();
    ModelResponse response1 =
        ModelResponse.builder()
            .usage(turn1Usage)
            .output(List.of(toolCall))
            .responseId(Optional.of("resp-1"))
            .providerData(Optional.empty())
            .build();

    // Second turn: model returns tool output AND final text
    // This simulates: tool was executed, result added, model responds with final answer
    RunToolCallOutputItem toolOutput =
        RunToolCallOutputItem.builder()
            .toolCallId("call-1")
            .result("Tool result")
            .error(Optional.empty())
            .build();
    ModelResponse response2 =
        ModelResponse.builder()
            .usage(turn2Usage)
            .output(List.of(toolOutput, "Final answer"))
            .responseId(Optional.of("resp-2"))
            .providerData(Optional.empty())
            .build();

    when(mockModel.getResponse(any(ModelRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(response1))
        .thenReturn(CompletableFuture.completedFuture(response2));
    RunConfig config = RunConfig.builder().maxTurns(3).modelProvider(mockProvider).build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "Question", config);

    assertNotNull(result);
    // Usage should be accumulated across both turns
    assertEquals(25.0, result.getUsage().getInputTokens());
    assertEquals(45.0, result.getUsage().getOutputTokens());
    assertEquals(70.0, result.getUsage().getTotalTokens());
  }

  @Test
  void run_tracksAllResponses() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("ResponsesAgent")
            .instructions("Test response tracking")
            .build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));

    // First response: tool call (causes loop to continue)
    RunToolCallItem toolCall =
        RunToolCallItem.builder()
            .id("call-1")
            .name("search")
            .parameters(Map.of("query", "test"))
            .build();
    ModelResponse response1 =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of(toolCall))
            .responseId(Optional.of("resp-1"))
            .providerData(Optional.empty())
            .build();

    // Second response: tool output and final text (loop stops - tool resolved, has final message)
    RunToolCallOutputItem toolOutput =
        RunToolCallOutputItem.builder()
            .toolCallId("call-1")
            .result("Search results")
            .error(Optional.empty())
            .build();
    ModelResponse response2 =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of(toolOutput, "Final answer"))
            .responseId(Optional.of("resp-2"))
            .providerData(Optional.empty())
            .build();

    when(mockModel.getResponse(any(ModelRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(response1))
        .thenReturn(CompletableFuture.completedFuture(response2));
    RunConfig config = RunConfig.builder().maxTurns(3).modelProvider(mockProvider).build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "Question", config);

    assertNotNull(result);
    // Should track both responses
    assertEquals(2, result.getRawResponses().size());
    assertEquals("resp-2", result.getLastResponseId());
  }

  @Test
  void run_withEmptyResponse_returnsEmpty() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("EmptyAgent")
            .instructions("Test empty response")
            .build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));
    ModelResponse mockResponse =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of())
            .responseId(Optional.of("resp-1"))
            .providerData(Optional.empty())
            .build();
    when(mockModel.getResponse(any(ModelRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(mockResponse));
    RunConfig config = RunConfig.builder().modelProvider(mockProvider).build();

    // With empty responses, the agent never produces final output and hits maxTurns
    MaxTurnsExceededError error =
        assertThrows(MaxTurnsExceededError.class, () -> Runner.run(agent, "Question", config));

    assertEquals(10, error.getMaxTurns());
    assertTrue(error.getCurrentTurn() >= 10);
  }

  @Test
  void run_preservesOriginalInput() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("InputAgent")
            .instructions("Test input preservation")
            .build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));
    ModelResponse mockResponse =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of("Answer"))
            .responseId(Optional.of("resp-1"))
            .providerData(Optional.empty())
            .build();
    when(mockModel.getResponse(any(ModelRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(mockResponse));
    RunConfig config = RunConfig.builder().modelProvider(mockProvider).build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "Original question", config);

    assertNotNull(result);
    assertEquals(1, result.getInput().size());
    assertEquals("Original question", result.getInput().get(0));
  }

  @Test
  void runStreamed_returnsStreamedResult() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("StreamingAgent")
            .instructions("Test streaming")
            .build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));
    AsyncIterable<StreamEvent> mockStreamEvents =
        () ->
            List.<StreamEvent>of(
                    TextDeltaStreamEvent.builder().delta("Hello").build(),
                    TextDeltaStreamEvent.builder().delta(" ").build(),
                    TextDeltaStreamEvent.builder().delta("world").build())
                .iterator();
    when(mockModel.getStreamedResponse(any(ModelRequest.class))).thenReturn(mockStreamEvents);
    RunConfig config = RunConfig.builder().modelProvider(mockProvider).build();

    StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
        Runner.runStreamed(agent, "Hello", config);

    assertNotNull(result);
    assertNotNull(result.getStream());
  }

  @Test
  void runStreamed_emitsTextStreamEvents() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("StreamAgent").build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));
    AsyncIterable<StreamEvent> mockStreamEvents =
        () ->
            List.<StreamEvent>of(
                    TextDeltaStreamEvent.builder().delta("First").build(),
                    TextDeltaStreamEvent.builder().delta(" chunk").build())
                .iterator();
    when(mockModel.getStreamedResponse(any(ModelRequest.class))).thenReturn(mockStreamEvents);
    RunConfig config = RunConfig.builder().modelProvider(mockProvider).build();

    StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
        Runner.runStreamed(agent, "Test", config);

    ReadableStreamAsyncIterator<String> textIterator = result.toTextStream().values();
    List<String> chunks = new ArrayList<>();
    while (textIterator.hasNext()) {
      String chunk = textIterator.next();
      if (chunk != null) {
        chunks.add(chunk);
      }
    }
    assertFalse(chunks.isEmpty());
  }

  @Test
  void runStreamed_withConfig_usesProvidedConfig() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("ConfigStreamAgent").build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));
    AsyncIterable<StreamEvent> mockStreamEvents =
        () -> List.<StreamEvent>of(TextDeltaStreamEvent.builder().delta("Response").build()).iterator();
    when(mockModel.getStreamedResponse(any(ModelRequest.class))).thenReturn(mockStreamEvents);
    RunConfig config = RunConfig.builder().maxTurns(5).modelProvider(mockProvider).build();

    StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
        Runner.runStreamed(agent, "Test", config);

    assertNotNull(result);
    assertEquals(5, config.getEffectiveMaxTurns());
  }
}
