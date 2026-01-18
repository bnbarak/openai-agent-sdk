package com.acoliteai.agentsdk.core;

import lombok.Builder;
import lombok.Value;

/**
 * OutputGuardrailResult
 *
 * <p>Result of executing an output guardrail.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/guardrail.ts">guardrail.ts</a>
 */
@Value
@Builder
public class OutputGuardrailResult {
  String guardrailName;
  GuardrailFunctionOutput output;
}
