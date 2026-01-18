package com.acoliteai.agentsdk.core.tracing;

import lombok.Builder;
import lombok.Value;

/**
 * Span data for guardrail checks.
 *
 * <p>Captures security and safety guardrail evaluations, including whether the guardrail was
 * triggered and why.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/spans.ts">tracing/spans.ts</a>
 */
@Value
@Builder
public final class GuardrailSpanData implements SpanData {

  /** Guardrail name */
  String guardrailName;

  /** Whether the guardrail was triggered */
  boolean triggered;

  /** Reason for trigger (if triggered) */
  String reason;

  @Override
  public String getType() {
    return SpanTypes.GUARDRAIL;
  }
}
