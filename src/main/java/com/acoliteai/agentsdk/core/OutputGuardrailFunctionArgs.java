package com.acoliteai.agentsdk.core;

import com.acoliteai.agentsdk.core.types.AgentOutputType;
import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * OutputGuardrailFunctionArgs
 *
 * <p>Arguments passed to output guardrails containing the final agent output and conversation
 * history.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/guardrail.ts">guardrail.ts</a>
 */
@Value
@Builder
public class OutputGuardrailFunctionArgs<TContext, TOutput extends AgentOutputType> {
  TOutput output;
  List<Object> input;
  RunContext<TContext> context;
}
