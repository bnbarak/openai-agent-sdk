package ai.acolite.agentsdk.core;

import ai.acolite.agentsdk.core.types.AgentOutputType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * RunHandoffCallItem
 *
 * <p>Represents a request to hand off conversation to another agent. Created when the LLM calls a
 * handoff tool (e.g., transfer_to_<agent_name>).
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/items.ts">items.ts</a>
 */
@Getter
@SuperBuilder
public class RunHandoffCallItem extends RunItemBase {
  /** The tool call that triggered this handoff */
  private final RunToolCallItem toolCall;

  /** The agent that initiated the handoff */
  private final Agent<?, ? extends AgentOutputType> sourceAgent;

  /** Target agent name extracted from tool call */
  public String getTargetAgentName() {
    if (toolCall == null || toolCall.getName() == null) {
      return null;
    }
    // Extract agent name from transfer_to_<AgentName> format
    String toolName = toolCall.getName();
    if (toolName.startsWith("transfer_to_")) {
      return toolName.substring("transfer_to_".length());
    }
    return toolName;
  }
}
