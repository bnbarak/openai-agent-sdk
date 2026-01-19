package ai.acolite.agentsdk.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import com.openai.models.responses.ResponseFunctionToolCall;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

class HandoffTest {

  @Test
  void agent_withHandoffs_serializesAsTools() {
    Agent<UnknownContext, TextOutput> targetAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Specialist")
            .handoffDescription("Handles specialized queries")
            .build();
    Agent<UnknownContext, TextOutput> sourceAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Triage")
            .handoffs(List.of(targetAgent))
            .build();

    assertEquals(1, sourceAgent.getHandoffs().size());

    assertEquals("Specialist", sourceAgent.getHandoffs().get(0).getName());
  }

  @Test
  void agent_getEnabledHandoffs_returnsHandoffsList() {
    Agent<UnknownContext, TextOutput> agent1 =
        Agent.<UnknownContext, TextOutput>builder().name("Agent1").build();
    Agent<UnknownContext, TextOutput> agent2 =
        Agent.<UnknownContext, TextOutput>builder().name("Agent2").build();
    Agent<UnknownContext, TextOutput> sourceAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Source")
            .handoffs(List.of(agent1, agent2))
            .build();

    List<?> enabledHandoffs = sourceAgent.getEnabledHandoffs();

    assertEquals(2, enabledHandoffs.size());
  }

  @Test
  void agent_noHandoffs_returnsEmptyList() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("TestAgent").build();

    List<?> enabledHandoffs = agent.getEnabledHandoffs();

    assertNotNull(enabledHandoffs);
    assertTrue(enabledHandoffs.isEmpty());
  }

  @Test
  void handoffCallItem_extractsTargetAgentName() {
    RunToolCallItem toolCall =
        RunToolCallItem.builder()
            .id("call_123")
            .name("transfer_to_Specialist")
            .parameters("{}")
            .build();
    Agent<UnknownContext, TextOutput> sourceAgent =
        Agent.<UnknownContext, TextOutput>builder().name("Triage").build();
    RunHandoffCallItem handoffCall =
        RunHandoffCallItem.builder().toolCall(toolCall).sourceAgent(sourceAgent).build();

    String targetName = handoffCall.getTargetAgentName();

    assertEquals("Specialist", targetName);
  }

  @Test
  void handoffCallItem_withSpaces_extractsTargetAgentName() {
    RunToolCallItem toolCall =
        RunToolCallItem.builder()
            .id("call_456")
            .name("transfer_to_Technical_Support")
            .parameters("{}")
            .build();
    Agent<UnknownContext, TextOutput> sourceAgent =
        Agent.<UnknownContext, TextOutput>builder().name("Triage").build();
    RunHandoffCallItem handoffCall =
        RunHandoffCallItem.builder().toolCall(toolCall).sourceAgent(sourceAgent).build();

    String targetName = handoffCall.getTargetAgentName();

    assertEquals("Technical_Support", targetName);
  }

  @Test
  void handoffOutputItem_tracksSourceAndTarget() {
    Agent<UnknownContext, TextOutput> sourceAgent =
        Agent.<UnknownContext, TextOutput>builder().name("Source").build();
    Agent<UnknownContext, TextOutput> targetAgent =
        Agent.<UnknownContext, TextOutput>builder().name("Target").build();

    RunHandoffOutputItem outputItem =
        RunHandoffOutputItem.builder()
            .toolCallId("call_789")
            .sourceAgent(sourceAgent)
            .targetAgent(targetAgent)
            .error(Optional.empty())
            .build();

    assertEquals("Source", outputItem.getFromAgent());
    assertEquals("Target", outputItem.getToAgent());
    assertFalse(outputItem.getError().isPresent());
  }

  @Test
  void handoffOutputItem_withError_tracksError() {
    Agent<UnknownContext, TextOutput> sourceAgent =
        Agent.<UnknownContext, TextOutput>builder().name("Source").build();

    RunHandoffOutputItem outputItem =
        RunHandoffOutputItem.builder()
            .toolCallId("call_999")
            .sourceAgent(sourceAgent)
            .targetAgent(null)
            .error(Optional.of("Agent not found"))
            .build();

    assertEquals("Source", outputItem.getFromAgent());
    assertNull(outputItem.getToAgent());
    assertTrue(outputItem.getError().isPresent());
    assertEquals("Agent not found", outputItem.getError().get());
  }

  @Test
  void handoff_execution_switchesAgent() {
    Agent<UnknownContext, TextOutput> targetAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Specialist")
            .instructions("I am a specialist")
            .build();
    Agent<UnknownContext, TextOutput> sourceAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Triage")
            .instructions("I transfer to specialists")
            .handoffs(List.of(targetAgent))
            .build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));
    ResponseFunctionToolCall handoffToolCall =
        ResponseFunctionToolCall.builder()
            .callId("call_handoff_123")
            .name("transfer_to_Specialist")
            .arguments("{\"reason\": \"specialized question\"}")
            .build();
    ModelResponse firstResponse =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of(handoffToolCall))
            .responseId(Optional.of("resp_1"))
            .providerData(Optional.empty())
            .build();
    ModelResponse secondResponse =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of("As a specialist, here is my answer"))
            .responseId(Optional.of("resp_2"))
            .providerData(Optional.empty())
            .build();
    when(mockModel.getResponse(any(ModelRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(firstResponse))
        .thenReturn(CompletableFuture.completedFuture(secondResponse));
    RunConfig config = RunConfig.builder().modelProvider(mockProvider).maxTurns(5).build();

    RunResult<UnknownContext, ?> result = Runner.run(sourceAgent, "Test handoff", config);

    assertNotNull(result);
    long handoffCallCount =
        result.getNewItems().stream().filter(item -> item instanceof RunHandoffCallItem).count();
    long handoffOutputCount =
        result.getNewItems().stream().filter(item -> item instanceof RunHandoffOutputItem).count();
    assertTrue(handoffCallCount > 0, "Should have at least one handoff call");
    assertTrue(handoffOutputCount > 0, "Should have at least one handoff output");
    RunHandoffOutputItem handoffOutput =
        (RunHandoffOutputItem)
            result.getNewItems().stream()
                .filter(item -> item instanceof RunHandoffOutputItem)
                .findFirst()
                .orElse(null);
    assertNotNull(handoffOutput);
    assertEquals("Triage", handoffOutput.getFromAgent());
    assertEquals("Specialist", handoffOutput.getToAgent());
    assertNotNull(result.getFinalOutput());
  }

  @Test
  void handoff_agentNotFound_createsErrorOutput() {
    Agent<UnknownContext, TextOutput> sourceAgent =
        Agent.<UnknownContext, TextOutput>builder().name("Triage").handoffs(List.of()).build();
    Model mockModel = mock(Model.class);
    ModelProvider mockProvider = mock(ModelProvider.class);
    when(mockProvider.getModel(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockModel));
    ResponseFunctionToolCall handoffToolCall =
        ResponseFunctionToolCall.builder()
            .callId("call_invalid")
            .name("transfer_to_NonExistent")
            .arguments("{}")
            .build();
    ModelResponse firstResponse =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of(handoffToolCall))
            .responseId(Optional.of("resp_1"))
            .providerData(Optional.empty())
            .build();
    ModelResponse secondResponse =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of("I'll handle this myself"))
            .responseId(Optional.of("resp_2"))
            .providerData(Optional.empty())
            .build();
    when(mockModel.getResponse(any(ModelRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(firstResponse))
        .thenReturn(CompletableFuture.completedFuture(secondResponse));
    RunConfig config = RunConfig.builder().modelProvider(mockProvider).maxTurns(5).build();

    RunResult<UnknownContext, ?> result = Runner.run(sourceAgent, "Test", config);

    RunHandoffOutputItem handoffOutput =
        (RunHandoffOutputItem)
            result.getNewItems().stream()
                .filter(item -> item instanceof RunHandoffOutputItem)
                .findFirst()
                .orElse(null);
    assertNotNull(handoffOutput);
    assertTrue(handoffOutput.getError().isPresent());
    assertTrue(handoffOutput.getError().get().contains("not found"));
  }
}
