package com.acoliteai.agentsdk.core.tracing;

import com.acoliteai.agentsdk.core.types.AgentOutputType;
import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * Span data for agent execution.
 *
 * <p>Captures metadata about an agent's configuration and capabilities during execution, including
 * available tools, handoffs, and output schema.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/spans.ts">tracing/spans.ts</a>
 */
@Value
@Builder
public final class AgentSpanData implements SpanData {

  /** Agent name */
  String agentName;

  /** Available handoff destinations (agent names) */
  List<String> handoffs;

  /** Available tools (tool names) */
  List<String> tools;

  /** Output type/schema */
  AgentOutputType outputType;

  @Override
  public String getType() {
    return SpanTypes.AGENT;
  }
}
