package ai.acolite.agentsdk.core;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * InputGuardrailFunctionArgs
 *
 * <p>Arguments passed to input guardrails containing the user's initial input.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/guardrail.ts">guardrail.ts</a>
 */
@Value
@Builder
public class InputGuardrailFunctionArgs<TContext> {
  List<Object> input;
  RunContext<TContext> context;
}
