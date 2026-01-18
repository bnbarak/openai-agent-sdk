package com.acoliteai.agentsdk.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * GuardrailFunctionOutput
 *
 * <p>Result of a guardrail execution containing tripwire status and optional metadata.
 *
 * <p>When tripwireTriggered is true, the agent execution will halt immediately.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/guardrail.ts">guardrail.ts</a>
 */
@Getter
@Builder
@AllArgsConstructor
public class GuardrailFunctionOutput {
  @Builder.Default boolean tripwireTriggered = false;
  Object metadata;

  public static GuardrailFunctionOutput safe() {
    return GuardrailFunctionOutput.builder().tripwireTriggered(false).build();
  }

  public static GuardrailFunctionOutput tripwire(Object metadata) {
    return GuardrailFunctionOutput.builder().tripwireTriggered(true).metadata(metadata).build();
  }
}
