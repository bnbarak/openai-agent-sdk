package ai.acolite.agentsdk.core;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * RunMessageOutputItem
 *
 * <p>Represents a message output from the agent/model. Content can be a String (for text responses)
 * or a typed object (for structured outputs).
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/items.ts">items.ts</a>
 */
@Getter
@SuperBuilder
@Jacksonized
public class RunMessageOutputItem extends RunItemBase {
  /** The message content. For text responses: String For structured outputs: Typed object */
  private final Object content;

  private final String role;
}
