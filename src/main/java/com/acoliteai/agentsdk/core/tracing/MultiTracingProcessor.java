package com.acoliteai.agentsdk.core.tracing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;

/**
 * MultiTracingProcessor
 *
 * <p>Composite processor that delegates trace/span events to multiple processors. Allows combining
 * multiple tracing backends (console, cloud, custom).
 *
 * <p>Thread-safe: Uses CopyOnWriteArrayList for processor list.
 *
 * <p>Example:
 *
 * <pre>
 * MultiTracingProcessor multi = new MultiTracingProcessor();
 * multi.addTraceProcessor(new ConsoleTraceProcessor());
 * multi.addTraceProcessor(new BatchTraceProcessor(...));
 * </pre>
 *
 * Ported from TypeScript OpenAI Agents SDK Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/processor.ts
 */
@Slf4j
public class MultiTracingProcessor implements TraceProcessor {

  private final CopyOnWriteArrayList<TraceProcessor> processors = new CopyOnWriteArrayList<>();

  /** Add a processor to the list. Each processor will receive all trace/span events. */
  public void addTraceProcessor(TraceProcessor processor) {
    if (processor == null) {
      throw new IllegalArgumentException("Processor cannot be null");
    }

    processors.add(processor);
    log.debug("Added processor: {}", processor.getClass().getSimpleName());
  }

  /** Replace all processors with a new list. */
  public void setProcessors(List<TraceProcessor> newProcessors) {
    if (newProcessors == null) {
      throw new IllegalArgumentException("Processors list cannot be null");
    }

    processors.clear();
    processors.addAll(newProcessors);
    log.debug("Set {} processor(s)", newProcessors.size());
  }

  /** Get all registered processors (immutable copy). */
  public List<TraceProcessor> getProcessors() {
    return new ArrayList<>(processors);
  }

  /** Remove all processors. */
  public void clear() {
    processors.clear();
    log.debug("Cleared all processors");
  }

  @Override
  public void onTraceStart(Trace trace) {
    for (TraceProcessor processor : processors) {
      try {
        processor.onTraceStart(trace);
      } catch (Exception e) {
        log.error("Error in processor.onTraceStart: {}", processor.getClass().getSimpleName(), e);
      }
    }
  }

  @Override
  public void onTraceEnd(Trace trace) {
    for (TraceProcessor processor : processors) {
      try {
        processor.onTraceEnd(trace);
      } catch (Exception e) {
        log.error("Error in processor.onTraceEnd: {}", processor.getClass().getSimpleName(), e);
      }
    }
  }

  @Override
  public void onSpanStart(Span<?> span) {
    for (TraceProcessor processor : processors) {
      try {
        processor.onSpanStart(span);
      } catch (Exception e) {
        log.error("Error in processor.onSpanStart: {}", processor.getClass().getSimpleName(), e);
      }
    }
  }

  @Override
  public void onSpanEnd(Span<?> span) {
    for (TraceProcessor processor : processors) {
      try {
        processor.onSpanEnd(span);
      } catch (Exception e) {
        log.error("Error in processor.onSpanEnd: {}", processor.getClass().getSimpleName(), e);
      }
    }
  }

  @Override
  public CompletableFuture<Void> shutdown(long timeoutMs) {
    log.debug("Shutting down MultiTracingProcessor with {} processor(s)", processors.size());
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    for (TraceProcessor processor : processors) {
      try {
        CompletableFuture<Void> future = processor.shutdown(timeoutMs);
        futures.add(future);
      } catch (Exception e) {
        log.error("Error shutting down processor: {}", processor.getClass().getSimpleName(), e);
      }
    }

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
  }

  @Override
  public CompletableFuture<Void> forceFlush() {
    log.debug("Force flushing {} processor(s)", processors.size());
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    for (TraceProcessor processor : processors) {
      try {
        CompletableFuture<Void> future = processor.forceFlush();
        futures.add(future);
      } catch (Exception e) {
        log.error("Error flushing processor: {}", processor.getClass().getSimpleName(), e);
      }
    }

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
  }
}
