package com.acoliteai.agentsdk.core.tracing;

/**
 * Base interface for all span data types.
 *
 * <p>Sealed interface ensures type safety and exhaustive pattern matching. Each span type captures
 * different operation metadata.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/spans.ts">tracing/spans.ts</a>
 */
public sealed interface SpanData
    permits AgentSpanData,
        GenerationSpanData,
        FunctionSpanData,
        HandoffSpanData,
        CustomSpanData,
        GuardrailSpanData {

  /**
   * Get the span type identifier. Used for serialization and type discrimination.
   *
   * @return Span type (e.g., "agent", "generation", "function")
   */
  String getType();
}
