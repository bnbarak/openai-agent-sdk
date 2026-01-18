package com.acoliteai.agentsdk.core;

import lombok.Builder;
import lombok.Value;

/**
 * ToolInputGuardrailFunctionArgs
 *
 * <p>Arguments passed to tool input guardrails before tool execution.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/toolGuardrail.ts">toolGuardrail.ts</a>
 */
@Value
@Builder
public class ToolInputGuardrailFunctionArgs<TContext> {
  String toolName;
  Object toolInput;
  RunContext<TContext> context;
}
