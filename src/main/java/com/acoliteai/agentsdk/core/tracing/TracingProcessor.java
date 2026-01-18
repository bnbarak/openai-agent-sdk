package com.acoliteai.agentsdk.core.tracing;

import java.util.concurrent.CompletableFuture;

/**
 * TracingProcessor
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/processor.ts">tracing/processor.ts</a>
 */
public interface TracingProcessor {

  /**
   * onTraceStart
   *
   * @param trace Trace
   * @return CompletableFuture<Void>
   */
  CompletableFuture<Void> onTraceStart(Trace trace);

  /**
   * onTraceEnd
   *
   * @param trace Trace
   * @return CompletableFuture<Void>
   */
  CompletableFuture<Void> onTraceEnd(Trace trace);

  /**
   * onSpanStart
   *
   * @param span Span
   * @return CompletableFuture<Void>
   */
  CompletableFuture<Void> onSpanStart(Span span);

  /**
   * onSpanEnd
   *
   * @param span Span
   * @return CompletableFuture<Void>
   */
  CompletableFuture<Void> onSpanEnd(Span span);

  /**
   * shutdown
   *
   * @param timeout Double
   * @return CompletableFuture<Void>
   */
  CompletableFuture<Void> shutdown(Double timeout);

  /**
   * forceFlush
   *
   * @return CompletableFuture<Void>
   */
  CompletableFuture<Void> forceFlush();
}
