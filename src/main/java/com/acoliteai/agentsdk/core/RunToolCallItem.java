package com.acoliteai.agentsdk.core;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * RunToolCallItem
 *
 * <p>Represents a tool invocation request from the model.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/items.ts">items.ts</a>
 */
@Getter
@SuperBuilder
@Jacksonized
public class RunToolCallItem extends RunItemBase {
  private final String id;
  private final String name;
  private final Object parameters;
}
