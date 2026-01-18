package ai.acolite.agentsdk.core.tracing;

/**
 * No-op span that does nothing.
 *
 * <p>Used when tracing is disabled. All lifecycle methods are no-ops. Extends Span to maintain type
 * compatibility.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/spans.ts">tracing/spans.ts</a>
 */
public class NoopSpan<TSpanData extends SpanData> extends Span<TSpanData> {

  /** Create a no-op span instance. */
  public NoopSpan(TSpanData data) {
    super(
        "noop", // spanId
        "noop", // traceId
        null, // parentId
        data, // data
        NoopTraceProcessor.INSTANCE, // processor
        null, // previousSpan
        null, // tracingApiKey
        null, // startedAt
        null, // endedAt
        null, // error
        false, // started
        false // ended
        );
  }

  /** Create a no-op span instance with null data. Used when tracing is disabled. */
  @SuppressWarnings("unchecked")
  public static <T extends SpanData> NoopSpan<T> instance() {
    return new NoopSpan<>(null);
  }

  @Override
  public synchronized void start() {
    // No-op - don't call super
  }

  @Override
  public synchronized void end() {
    // No-op - don't call super
  }

  @Override
  public synchronized void setError(SpanError error) {
    // No-op - don't call super
  }

  @Override
  public synchronized void setError(Throwable throwable) {
    // No-op - don't call super
  }

  @Override
  public NoopSpan<TSpanData> clone() {
    return this;
  }

  @Override
  public java.util.Map<String, Object> toJson() {
    return null; // No-op span doesn't export
  }
}
