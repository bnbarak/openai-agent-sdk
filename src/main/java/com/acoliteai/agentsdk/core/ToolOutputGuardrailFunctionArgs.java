package com.acoliteai.agentsdk.core;

import lombok.Builder;
import lombok.Value;

/**
 * ToolOutputGuardrailFunctionArgs
 *
 * <p>Arguments passed to tool output guardrails after tool execution.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/toolGuardrail.ts">toolGuardrail.ts</a>
 */
@Value
@Builder
public class ToolOutputGuardrailFunctionArgs<TContext> {
  String toolName;
  Object toolInput;
  Object toolOutput;
  RunContext<TContext> context;
}
