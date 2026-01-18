package ai.acolite.agentsdk.core.tracing;

import java.util.concurrent.CompletableFuture;

/**
 * Processor for trace and span lifecycle events.
 *
 * <p>Implementations can export traces to external systems (OpenAI, Jaeger, etc.), log to console,
 * or perform custom processing.
 *
 * <p>Lifecycle: - onTraceStart: Called when trace begins - onTraceEnd: Called when trace ends -
 * onSpanStart: Called when span begins - onSpanEnd: Called when span ends - forceFlush: Flush
 * pending data - shutdown: Gracefully shutdown processor
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/processor.ts">tracing/processor.ts</a>
 */
public interface TraceProcessor extends AutoCloseable {

  /** Called when a trace starts. Processors can export the trace immediately or buffer it. */
  void onTraceStart(Trace trace);

  /** Called when a trace ends. */
  void onTraceEnd(Trace trace);

  /** Called when a span starts. */
  void onSpanStart(Span<?> span);

  /** Called when a span ends. Most processors export spans on end to capture complete data. */
  void onSpanEnd(Span<?> span);

  /** Flush all pending traces/spans. Blocks until all data is exported. */
  CompletableFuture<Void> forceFlush();

  /**
   * Shutdown the processor gracefully. Flushes pending data and releases resources.
   *
   * @param timeoutMs Maximum time to wait for shutdown (milliseconds)
   */
  CompletableFuture<Void> shutdown(long timeoutMs);

  /** Default shutdown with 5 second timeout. */
  default CompletableFuture<Void> shutdown() {
    return shutdown(5000);
  }

  /** AutoCloseable implementation. Calls shutdown() and blocks. */
  @Override
  default void close() throws Exception {
    shutdown().join();
  }
}
