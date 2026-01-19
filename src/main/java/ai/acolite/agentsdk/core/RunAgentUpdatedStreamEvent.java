package ai.acolite.agentsdk.core;

import ai.acolite.agentsdk.core.types.AgentOutputType;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * RunAgentUpdatedStreamEvent
 *
 * <p>Emitted when the conversation switches to a different agent (handoff)
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/events.ts
 */
@Getter
@Builder
public class RunAgentUpdatedStreamEvent implements RunStreamEvent {
  @NonNull private final Agent<?, ? extends AgentOutputType> agent;

  @Override
  public String getType() {
    return "agent_updated";
  }
}
