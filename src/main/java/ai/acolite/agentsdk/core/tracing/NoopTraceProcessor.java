package ai.acolite.agentsdk.core.tracing;

import java.util.concurrent.CompletableFuture;

/**
 * No-op trace processor that does nothing.
 *
 * <p>Used when tracing is disabled to avoid conditional checks throughout the codebase. All methods
 * are no-ops.
 *
 * <p>Singleton pattern for efficiency.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/processor.ts">tracing/processor.ts</a>
 */
public class NoopTraceProcessor implements TraceProcessor {

  /** Singleton instance */
  public static final NoopTraceProcessor INSTANCE = new NoopTraceProcessor();

  private NoopTraceProcessor() {
    // Private constructor for singleton
  }

  @Override
  public void onTraceStart(Trace trace) {
    // No-op
  }

  @Override
  public void onTraceEnd(Trace trace) {
    // No-op
  }

  @Override
  public void onSpanStart(Span<?> span) {
    // No-op
  }

  @Override
  public void onSpanEnd(Span<?> span) {
    // No-op
  }

  @Override
  public CompletableFuture<Void> forceFlush() {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> shutdown(long timeoutMs) {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void close() {
    // No-op
  }
}
