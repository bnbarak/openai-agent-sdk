package com.acoliteai.agentsdk.core.tracing;

import lombok.Builder;
import lombok.Value;

/**
 * Span data for agent handoffs.
 *
 * <p>Captures agent-to-agent transitions during multi-agent conversations, including the source
 * agent, destination agent, and handoff reason.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/spans.ts">tracing/spans.ts</a>
 */
@Value
@Builder
public final class HandoffSpanData implements SpanData {

  /** Source agent name (handing off from) */
  String fromAgent;

  /** Destination agent name (handing off to) */
  String toAgent;

  /** Reason for handoff */
  String reason;

  @Override
  public String getType() {
    return SpanTypes.HANDOFF;
  }
}
