package com.acoliteai.agentsdk.core.tracing;

import com.acoliteai.agentsdk.core.Usage;
import com.acoliteai.agentsdk.core.types.AgentInputItem;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

/**
 * Span data for LLM generation calls.
 *
 * <p>Captures model inputs, outputs, configuration, and token usage for each call to the language
 * model.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/spans.ts">tracing/spans.ts</a>
 */
@Value
@Builder
public final class GenerationSpanData implements SpanData {

  /** Model identifier (e.g., "gpt-4o", "gpt-4o-mini") */
  String model;

  /** Input messages/items to the model */
  List<AgentInputItem> input;

  /** Output from the model (can be text, objects, or structured data) */
  Object output;

  /** Model configuration settings (temperature, max_tokens, etc.) */
  Map<String, Object> modelConfig;

  /** Token usage statistics */
  Usage usage;

  @Override
  public String getType() {
    return SpanTypes.GENERATION;
  }
}
