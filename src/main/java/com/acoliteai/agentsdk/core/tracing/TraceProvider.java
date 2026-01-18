package com.acoliteai.agentsdk.core.tracing;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

/**
 * TraceProvider
 *
 * <p>Global singleton for trace/span creation and processor management.
 *
 * <p>Features: - Environment variable support: OPENAI_AGENTS_DISABLE_TRACING=1 or true - Returns
 * NoopTrace/NoopSpan when disabled (no performance overhead) - Manages multiple processors via
 * MultiTracingProcessor - Automatic shutdown on JVM exit
 *
 * <p>Usage:
 *
 * <pre>
 * // Get global instance
 * TraceProvider provider = TraceProvider.getGlobalTraceProvider();
 *
 * // Register processors
 * provider.registerProcessor(new ConsoleTraceProcessor());
 *
 * // Create traces
 * Trace trace = provider.createTrace(Trace.builder().name("My workflow").build());
 * </pre>
 *
 * Environment Variables: - OPENAI_AGENTS_DISABLE_TRACING=1 or true - Disables all tracing
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/provider.ts">tracing/provider.ts</a>
 */
@Slf4j
public class TraceProvider {

  private final MultiTracingProcessor multiProcessor;
  private final AtomicBoolean disabled;
  private final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);

  /** Create a new TraceProvider. Checks OPENAI_AGENTS_DISABLE_TRACING environment variable. */
  public TraceProvider() {
    this.multiProcessor = new MultiTracingProcessor();
    this.disabled = new AtomicBoolean(isTracingDisabledFromEnv());

    // Register shutdown hook to flush traces on JVM exit
    registerShutdownHook();

    log.debug("TraceProvider initialized (disabled={})", disabled.get());
  }

  /** Check if tracing is disabled via environment variable. */
  private static boolean isTracingDisabledFromEnv() {
    String envValue = System.getenv("OPENAI_AGENTS_DISABLE_TRACING");
    return "1".equals(envValue) || "true".equalsIgnoreCase(envValue);
  }

  /** Add a processor to the list of processors. Each processor will receive all traces/spans. */
  public void registerProcessor(TraceProcessor processor) {
    multiProcessor.addTraceProcessor(processor);
    log.info("Registered processor: {}", processor.getClass().getSimpleName());
  }

  /** Set the list of processors. This will replace any existing processors. */
  public void setProcessors(List<TraceProcessor> processors) {
    multiProcessor.setProcessors(processors);
    log.info("Set {} processor(s)", processors.size());
  }

  /** Get the current trace from ThreadLocal context. */
  public Optional<Trace> getCurrentTrace() {
    return TraceContext.getCurrentTrace();
  }

  /** Get the current span from ThreadLocal context. */
  public Optional<Span<?>> getCurrentSpan() {
    return TraceContext.getCurrentSpan();
  }

  /** Enable or disable tracing at runtime. */
  public void setDisabled(boolean disabled) {
    this.disabled.set(disabled);
    log.info("Tracing {}", disabled ? "disabled" : "enabled");
  }

  /** Check if tracing is currently disabled. */
  public boolean isDisabled() {
    return disabled.get();
  }

  /** Create a new trace. Returns NoopTrace if tracing is disabled. */
  public Trace createTrace(Trace trace) {
    if (disabled.get()) {
      log.debug("Tracing is disabled, returning NoopTrace");
      return NoopTrace.INSTANCE;
    }

    Trace preparedTrace = trace.withProcessor(multiProcessor);

    log.debug("Created trace: {} (name={})", preparedTrace.getTraceId(), preparedTrace.getName());
    return preparedTrace;
  }

  /** Create a new span. Returns NoopSpan if tracing is disabled or no active trace. */
  public <TData extends SpanData> Span<TData> createSpan(Span<TData> span) {
    if (disabled.get()) {
      log.debug("Tracing is disabled, returning NoopSpan");
      return NoopSpan.instance();
    }

    // Get current trace from context
    Optional<Trace> currentTrace = TraceContext.getCurrentTrace();
    if (currentTrace.isEmpty()) {
      log.warn(
          "No active trace, returning NoopSpan. Start a trace with TraceContext.withTrace() first.");
      return NoopSpan.instance();
    }

    Trace trace = currentTrace.get();
    if (trace instanceof NoopTrace) {
      log.debug("Current trace is NoopTrace, returning NoopSpan");
      return NoopSpan.instance();
    }

    // Get current span from context (if any)
    Optional<Span<?>> currentSpan = TraceContext.getCurrentSpan();
    String parentId = currentSpan.map(Span::getSpanId).orElse(null);

    Span<TData> preparedSpan =
        span.withTracingContext(
            trace.getTraceId(), parentId, trace.getTracingApiKey(), multiProcessor);

    log.debug(
        "Created span: {} (type={}, parent={})",
        preparedSpan.getSpanId(),
        preparedSpan.getData().getType(),
        parentId);
    return preparedSpan;
  }

  /** Shutdown all processors. Waits up to timeout for export to complete. */
  public CompletableFuture<Void> shutdown(long timeoutMs) {
    log.info("Shutting down TraceProvider");
    return multiProcessor.shutdown(timeoutMs);
  }

  /** Shutdown with default timeout (5 seconds). */
  public CompletableFuture<Void> shutdown() {
    return shutdown(5000);
  }

  /** Force flush all pending traces/spans immediately. */
  public CompletableFuture<Void> forceFlush() {
    log.debug("Force flushing all processors");
    return multiProcessor.forceFlush();
  }

  /** Register JVM shutdown hook to flush traces on exit. */
  private void registerShutdownHook() {
    if (shutdownHookRegistered.getAndSet(true)) {
      return; // Already registered
    }

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  log.info("JVM shutdown detected, flushing traces");
                  try {
                    shutdown(5000).get();
                  } catch (Exception e) {
                    log.error("Error during shutdown hook", e);
                  }
                },
                "TraceProvider-Shutdown"));
  }

  // ========== Global Singleton ==========

  private static volatile TraceProvider INSTANCE;
  private static final Object LOCK = new Object();

  /** Get the global TraceProvider singleton. Thread-safe lazy initialization. */
  public static TraceProvider getGlobalTraceProvider() {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        if (INSTANCE == null) {
          INSTANCE = new TraceProvider();
        }
      }
    }
    return INSTANCE;
  }

  /**
   * Initialize the global provider with default cloud tracing setup. Sets up BatchTraceProcessor +
   * OpenAITraceExporter.
   *
   * <p>This should be called once at application startup if you want automatic cloud tracing with
   * OpenAI.
   */
  public static void initializeWithDefaultCloudTracing() {
    TraceProvider provider = getGlobalTraceProvider();

    if (provider.multiProcessor.getProcessors().isEmpty()) {
      log.info("Initializing default cloud tracing (BatchTraceProcessor + OpenAITraceExporter)");

      // Create OpenAI exporter (reads API key from OPENAI_API_KEY env var)
      OpenAITraceExporter exporter = new OpenAITraceExporter();

      // Create batch processor with default config
      BatchTraceProcessor.Config config = BatchTraceProcessor.Config.builder().build();
      BatchTraceProcessor processor = new BatchTraceProcessor(exporter, config);

      provider.registerProcessor(processor);
    } else {
      log.debug("Processors already configured, skipping default cloud tracing setup");
    }
  }

  /** Reset the global singleton (for testing only). */
  static void resetGlobalProvider() {
    synchronized (LOCK) {
      if (INSTANCE != null) {
        try {
          INSTANCE.shutdown().get();
        } catch (Exception e) {
          log.error("Error shutting down provider during reset", e);
        }
        INSTANCE = null;
      }
    }
  }
}
