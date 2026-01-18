package com.acoliteai.agentsdk.core.errors;

import com.acoliteai.agentsdk.core.AgentsError;
import lombok.Getter;

/**
 * InputGuardrailTripwireTriggered
 *
 * <p>Thrown when an input guardrail triggers its tripwire, halting agent execution.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/errors.ts">errors.ts</a>
 */
@Getter
public class InputGuardrailTripwireTriggered extends AgentsError {

  private final String guardrailName;
  private final Object metadata;

  public InputGuardrailTripwireTriggered(String guardrailName, Object metadata) {
    super(
        "Input guardrail '"
            + guardrailName
            + "' triggered tripwire"
            + (metadata != null ? ": " + metadata : ""));
    this.guardrailName = guardrailName;
    this.metadata = metadata;
  }
}
