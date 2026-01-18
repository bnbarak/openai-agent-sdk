package com.acoliteai.agentsdk.core.tracing;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * TraceContext
 *
 * <p>ThreadLocal-based context management for traces and spans. Provides API for tracking the
 * current trace and span within a thread.
 *
 * <p>Key Features: - ThreadLocal storage for trace/span state - Helper methods for async context
 * propagation - Automatic cleanup with try-finally patterns
 *
 * <p>Context Propagation: CompletableFuture callbacks run on arbitrary threads and lose ThreadLocal
 * context. Use the provided helper methods (supplyAsync, runAsync) for automatic propagation.
 *
 * <p>Example:
 *
 * <pre>
 * Trace trace = Trace.builder()...build();
 * TraceContext.withTrace(trace, () -> {
 *     // This code runs with trace context
 *     return doWork();
 * });
 * </pre>
 *
 * Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/context.ts
 */
public class TraceContext {
  private static final ThreadLocal<TraceContextState> CONTEXT = new ThreadLocal<>();

  /** Get the current trace from ThreadLocal storage */
  public static Optional<Trace> getCurrentTrace() {
    TraceContextState state = CONTEXT.get();
    return state != null ? Optional.ofNullable(state.getTrace()) : Optional.empty();
  }

  /** Get the current span from ThreadLocal storage */
  public static Optional<Span<?>> getCurrentSpan() {
    TraceContextState state = CONTEXT.get();
    return state != null ? Optional.ofNullable(state.getCurrentSpan()) : Optional.empty();
  }

  /**
   * Set the current span in the context. The span maintains a reference to the previous span for
   * stack tracking.
   */
  public static void setCurrentSpan(Span<?> span) {
    TraceContextState state = CONTEXT.get();
    if (state != null) {
      state.setCurrentSpan(span);
    }
  }

  /**
   * Run synchronous code within a trace context. Automatically starts and ends the trace.
   *
   * @param trace The trace to set as current
   * @param action The code to run within the trace
   * @return The result of the action
   */
  public static <T> T withTrace(Trace trace, Supplier<T> action) {
    TraceContextState previousState = CONTEXT.get();
    try {
      CONTEXT.set(new TraceContextState(trace));
      trace.start();
      T result = action.get();
      trace.end();
      return result;
    } finally {
      CONTEXT.set(previousState);
    }
  }

  /**
   * Run asynchronous code within a trace context. Automatically starts and ends the trace when the
   * future completes.
   *
   * @param trace The trace to set as current
   * @param action The async code to run within the trace
   * @return CompletableFuture with the result
   */
  public static <T> CompletableFuture<T> withTraceAsync(
      Trace trace, Supplier<CompletableFuture<T>> action) {
    TraceContextState state = new TraceContextState(trace);
    CONTEXT.set(state);
    trace.start();

    return action
        .get()
        .whenComplete(
            (result, error) -> {
              trace.end();
              CONTEXT.remove();
            });
  }

  /**
   * Get the current trace, or create a new one if none exists
   *
   * @param name Name for the new trace (if created)
   * @param processor Processor for the new trace (if created)
   * @return The current or newly created trace
   */
  public static Trace getOrCreateTrace(String name, TraceProcessor processor) {
    return getCurrentTrace()
        .orElseGet(
            () -> {
              Trace trace =
                  Trace.builder()
                      .traceId(TracingUtils.generateTraceId())
                      .name(name)
                      .processor(processor)
                      .build();
              CONTEXT.set(new TraceContextState(trace));
              return trace;
            });
  }

  /** Reset the ThreadLocal context. Call this in finally blocks to prevent memory leaks. */
  public static void reset() {
    CONTEXT.remove();
  }

  /**
   * Capture the current context state for propagation to another thread. Returns a copy of the
   * state to avoid sharing mutable objects.
   *
   * @return Copy of current context state, or null if no context
   */
  public static TraceContextState captureState() {
    TraceContextState state = CONTEXT.get();
    return state != null ? state.copy() : null;
  }

  /**
   * Restore a previously captured context state. Used for propagating context to new threads.
   *
   * @param state The state to restore
   */
  public static void restoreState(TraceContextState state) {
    CONTEXT.set(state);
  }

  // ============================================
  // Async Context Propagation Helpers
  // ============================================

  /**
   * Create a CompletableFuture that preserves trace context. Use this instead of
   * CompletableFuture.supplyAsync() to maintain context.
   *
   * @param supplier The code to run asynchronously
   * @return CompletableFuture with context propagation
   */
  public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
    TraceContextState state = captureState();
    return CompletableFuture.supplyAsync(
        () -> {
          if (state != null) {
            restoreState(state);
          }
          try {
            return supplier.get();
          } finally {
            if (state != null) {
              reset();
            }
          }
        });
  }

  /**
   * Create a CompletableFuture that preserves trace context. Use this instead of
   * CompletableFuture.runAsync() to maintain context.
   *
   * @param runnable The code to run asynchronously
   * @return CompletableFuture with context propagation
   */
  public static CompletableFuture<Void> runAsync(Runnable runnable) {
    TraceContextState state = captureState();
    return CompletableFuture.runAsync(
        () -> {
          if (state != null) {
            restoreState(state);
          }
          try {
            runnable.run();
          } finally {
            if (state != null) {
              reset();
            }
          }
        });
  }

  /**
   * Wrap a CompletableFuture to ensure context is restored in continuations. Use this when you have
   * an existing CompletableFuture and want to add context-aware continuations.
   *
   * @param future The future to wrap
   * @return The same future (for chaining), but context will be restored in .thenApply(), etc.
   */
  public static <T> CompletableFuture<T> wrapFuture(CompletableFuture<T> future) {
    TraceContextState state = captureState();
    if (state == null) {
      return future;
    }

    return future.whenComplete(
        (result, error) -> {
          // This ensures continuations see the context
          restoreState(state);
        });
  }
}
