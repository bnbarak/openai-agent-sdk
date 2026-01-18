package ai.acolite.agentsdk.core;

import ai.acolite.agentsdk.core.types.AgentOutputType;
import java.util.Optional;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * RunHandoffOutputItem
 *
 * <p>Represents the result of an agent handoff. Created after a successful handoff execution,
 * contains both source and target agents.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/items.ts">items.ts</a>
 */
@Getter
@SuperBuilder
public class RunHandoffOutputItem extends RunItemBase {
  /** The tool call ID that triggered this handoff */
  private final String toolCallId;

  /** The agent that initiated the handoff */
  private final Agent<?, ? extends AgentOutputType> sourceAgent;

  /** The agent receiving the handoff */
  private final Agent<?, ? extends AgentOutputType> targetAgent;

  /** Error message if handoff failed (e.g., agent not found) */
  private final Optional<String> error;

  /** Get source agent name */
  public String getFromAgent() {
    return sourceAgent != null ? sourceAgent.getName() : null;
  }

  /** Get target agent name */
  public String getToAgent() {
    return targetAgent != null ? targetAgent.getName() : null;
  }
}
