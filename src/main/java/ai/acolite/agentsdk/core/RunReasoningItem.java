package ai.acolite.agentsdk.core;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * RunReasoningItem
 *
 * <p>Represents reasoning content from the model (e.g., chain of thought).
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/items.ts">items.ts</a>
 */
@Getter
@SuperBuilder
public class RunReasoningItem extends RunItemBase {
  private final String content;
}
