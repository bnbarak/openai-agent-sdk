package com.acoliteai.agentsdk.core;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * RunToolApprovalItem
 *
 * <p>Represents a request for user approval before tool execution.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/items.ts">items.ts</a>
 */
@Getter
@SuperBuilder
public class RunToolApprovalItem extends RunItemBase {
  private final String toolCallId;
  private final String toolName;
  private final Object parameters;
  private final boolean approved;
}
