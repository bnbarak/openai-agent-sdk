package ai.acolite.agentsdk.core;

import static org.junit.jupiter.api.Assertions.*;

import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RunStateTest {

  @Test
  void constructor_initializesState() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("TestAgent").build();
    RunConfig config = RunConfig.builder().build();

    RunState<UnknownContext, Agent<UnknownContext, TextOutput>> state =
        new RunState<>(agent, List.of("Hello"), config);

    assertEquals(agent, state.getCurrentAgent());
    assertEquals(List.of("Hello"), state.getOriginalInput());
    assertTrue(state.getGeneratedItems().isEmpty());
    assertEquals(0, state.getCurrentTurn());
    assertNotNull(state.getContext());
  }

  @Test
  void addModelResponse_storesResponse() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("TestAgent").build();
    RunConfig config = RunConfig.builder().build();
    RunState<UnknownContext, Agent<UnknownContext, TextOutput>> state =
        new RunState<>(agent, List.of("Hello"), config);
    ModelResponse response =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of("Response"))
            .responseId(Optional.of("resp_123"))
            .providerData(Optional.empty())
            .build();

    state.addModelResponse(response);

    assertEquals(1, state.getModelResponses().size());
    assertEquals(response, state.getLastTurnResponse().get());
  }

  @Test
  void addGeneratedItem_storesItem() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("TestAgent").build();
    RunConfig config = RunConfig.builder().build();
    RunState<UnknownContext, Agent<UnknownContext, TextOutput>> state =
        new RunState<>(agent, List.of("Hello"), config);
    RunMessageOutputItem item =
        RunMessageOutputItem.builder().content("Response").role("assistant").build();

    state.addGeneratedItem(item);

    assertEquals(1, state.getGeneratedItems().size());
    assertEquals(item, state.getGeneratedItems().get(0));
  }

  @Test
  void getAllItems_combinesOriginalAndGenerated() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("TestAgent").build();
    RunConfig config = RunConfig.builder().build();
    RunState<UnknownContext, Agent<UnknownContext, TextOutput>> state =
        new RunState<>(agent, List.of("Hello"), config);
    RunMessageOutputItem item =
        RunMessageOutputItem.builder().content("Response").role("assistant").build();
    state.addGeneratedItem(item);

    List<Object> allItems = state.getAllItems();

    assertEquals(2, allItems.size());
    assertEquals("Hello", allItems.get(0));
    assertEquals(item, allItems.get(1));
  }

  @Test
  void hasReachedMaxTurns_checksTurnLimit() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("TestAgent").build();
    RunConfig config = RunConfig.builder().maxTurns(2).build();
    RunState<UnknownContext, Agent<UnknownContext, TextOutput>> state =
        new RunState<>(agent, List.of("Hello"), config);

    assertFalse(state.hasReachedMaxTurns());

    state.incrementTurn();
    assertFalse(state.hasReachedMaxTurns());

    state.incrementTurn();
    assertTrue(state.hasReachedMaxTurns());
  }

  @Test
  void hasFinalOutput_withMessageAndNoPendingTools_returnsTrue() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("TestAgent").build();
    RunConfig config = RunConfig.builder().build();
    RunState<UnknownContext, Agent<UnknownContext, TextOutput>> state =
        new RunState<>(agent, List.of("Hello"), config);
    RunMessageOutputItem message =
        RunMessageOutputItem.builder().content("Response").role("assistant").build();
    state.addGeneratedItem(message);

    assertTrue(state.hasFinalOutput());
  }

  @Test
  void hasFinalOutput_withPendingToolCalls_returnsFalse() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("TestAgent").build();
    RunConfig config = RunConfig.builder().build();
    RunState<UnknownContext, Agent<UnknownContext, TextOutput>> state =
        new RunState<>(agent, List.of("Hello"), config);
    RunToolCallItem toolCall =
        RunToolCallItem.builder().id("call_123").name("calculator").parameters(null).build();
    state.addGeneratedItem(toolCall);
    RunMessageOutputItem message =
        RunMessageOutputItem.builder().content("Response").role("assistant").build();
    state.addGeneratedItem(message);

    assertFalse(state.hasFinalOutput());
  }

  @Test
  void hasFinalOutput_withCompletedToolCalls_returnsTrue() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("TestAgent").build();
    RunConfig config = RunConfig.builder().build();
    RunState<UnknownContext, Agent<UnknownContext, TextOutput>> state =
        new RunState<>(agent, List.of("Hello"), config);
    RunToolCallItem toolCall =
        RunToolCallItem.builder().id("call_123").name("calculator").parameters(null).build();
    state.addGeneratedItem(toolCall);
    RunToolCallOutputItem toolOutput =
        RunToolCallOutputItem.builder()
            .toolCallId("call_123")
            .result(42)
            .error(Optional.empty())
            .build();
    state.addGeneratedItem(toolOutput);
    RunMessageOutputItem message =
        RunMessageOutputItem.builder().content("Response").role("assistant").build();
    state.addGeneratedItem(message);

    assertTrue(state.hasFinalOutput());
  }

  @Test
  void setCurrentAgent_updatesAgent() {
    Agent<UnknownContext, TextOutput> agent1 =
        Agent.<UnknownContext, TextOutput>builder().name("Agent1").build();
    Agent<UnknownContext, TextOutput> agent2 =
        Agent.<UnknownContext, TextOutput>builder().name("Agent2").build();
    RunConfig config = RunConfig.builder().build();
    RunState<UnknownContext, Agent<UnknownContext, TextOutput>> state =
        new RunState<>(agent1, List.of("Hello"), config);

    state.setCurrentAgent(agent2);

    assertEquals(agent2, state.getCurrentAgent());
  }
}
