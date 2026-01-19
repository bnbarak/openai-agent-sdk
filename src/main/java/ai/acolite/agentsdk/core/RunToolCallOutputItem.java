package ai.acolite.agentsdk.core;

import java.util.Optional;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * RunToolCallOutputItem
 *
 * <p>Represents the result of a tool execution.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/items.ts">items.ts</a>
 */
@Getter
@SuperBuilder
@Jacksonized
public class RunToolCallOutputItem extends RunItemBase {
  private final String toolCallId;
  private final Object result;
  private final Optional<String> error;
}
