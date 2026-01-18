package com.acoliteai.agentsdk.core.errors;

import com.acoliteai.agentsdk.core.AgentsError;
import lombok.Getter;

/**
 * ToolOutputGuardrailTripwireTriggered
 *
 * <p>Thrown when a tool output guardrail throws an exception, halting agent execution.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/errors.ts">errors.ts</a>
 */
@Getter
public class ToolOutputGuardrailTripwireTriggered extends AgentsError {

  private final String guardrailName;
  private final Object metadata;

  public ToolOutputGuardrailTripwireTriggered(String guardrailName, Object metadata) {
    super(
        "Tool output guardrail '"
            + guardrailName
            + "' triggered exception"
            + (metadata != null ? ": " + metadata : ""));
    this.guardrailName = guardrailName;
    this.metadata = metadata;
  }
}
