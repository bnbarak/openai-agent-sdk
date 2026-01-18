package ai.acolite.agentsdk.core;

import ai.acolite.agentsdk.core.runner.RunItemUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 * RunState
 *
 * <p>Manages conversation state across multiple turns of agent execution.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/runState.ts">runState.ts</a>
 */
public class RunState<TContext, TAgent> {

  @Getter @Setter private TAgent currentAgent;
  private final List<Object> originalInput;
  private final List<ModelResponse> modelResponses = new ArrayList<>();
  @Getter private final RunContext<TContext> context;
  private final List<RunItem> generatedItems = new ArrayList<>();
  @Getter private final int maxTurns;
  @Getter private Optional<ModelResponse> lastTurnResponse = Optional.empty();
  @Getter private int currentTurn = 0;

  @SuppressWarnings("unchecked")
  public RunState(TAgent agent, List<Object> input, RunConfig config) {
    this.currentAgent = agent;
    this.originalInput = new ArrayList<>(input);
    this.maxTurns = config.getEffectiveMaxTurns();
    this.context =
        config.getContext().map(ctx -> (RunContext<TContext>) ctx).orElse(new RunContext<>());
  }

  public List<Object> getOriginalInput() {
    return new ArrayList<>(originalInput);
  }

  public void addModelResponse(ModelResponse response) {
    modelResponses.add(response);
    lastTurnResponse = Optional.of(response);
    if (response.getUsage() != null) {
      context.addUsage(response.getUsage());
    }
  }

  public List<ModelResponse> getModelResponses() {
    return new ArrayList<>(modelResponses);
  }

  public void addGeneratedItem(RunItem item) {
    generatedItems.add(item);
  }

  public List<RunItem> getGeneratedItems() {
    return new ArrayList<>(generatedItems);
  }

  public List<Object> getAllItems() {
    List<Object> allItems = new ArrayList<>(originalInput);
    allItems.addAll(generatedItems);
    return allItems;
  }

  public void incrementTurn() {
    currentTurn++;
  }

  public boolean hasReachedMaxTurns() {
    return currentTurn >= maxTurns;
  }

  public boolean hasFinalOutput() {
    if (generatedItems.isEmpty()) {
      return false;
    }

    RunItem lastItem = generatedItems.get(generatedItems.size() - 1);
    if (lastItem instanceof RunMessageOutputItem) {
      return !RunItemUtils.hasPendingToolCalls(generatedItems);
    }

    return false;
  }
}
