package ai.acolite.agentsdk.core;

/**
 * Span
 *
 * <p>Tracing span for observability.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/spans.ts
 */
public interface Span<TData> {
  String getSpanId();

  String getTraceId();

  void end();
}
