package ai.acolite.agentsdk.core;

import lombok.Builder;
import lombok.Value;

/**
 * InputGuardrailResult
 *
 * <p>Result of executing an input guardrail.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/guardrail.ts">guardrail.ts</a>
 */
@Value
@Builder
public class InputGuardrailResult {
  String guardrailName;
  GuardrailFunctionOutput output;
}
