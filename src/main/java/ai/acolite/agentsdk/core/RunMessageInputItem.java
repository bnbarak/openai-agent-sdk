package ai.acolite.agentsdk.core;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * RunMessageInputItem
 *
 * <p>Represents a message input from the user. Used to store user messages in conversation history
 * (sessions).
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/items.ts">items.ts</a>
 */
@Getter
@SuperBuilder
@Jacksonized
public class RunMessageInputItem extends RunItemBase {
  /** The message content from the user */
  private final String content;

  /** The role is always "user" for input messages */
  private final String role;
}
