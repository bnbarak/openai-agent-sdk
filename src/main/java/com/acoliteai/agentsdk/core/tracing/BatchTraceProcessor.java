package com.acoliteai.agentsdk.core.tracing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * BatchTraceProcessor
 *
 * <p>Buffers traces and spans in memory and exports them in batches. Provides configurable batching
 * behavior with automatic and manual flush.
 *
 * <p>Key Features: - In-memory buffering with bounded queue - Periodic batch export (every N
 * seconds) - Threshold-based export (when queue is 80% full) - Graceful shutdown with final flush -
 * Thread-safe operations
 *
 * <p>Thread Safety: - BlockingQueue handles concurrent enqueue from multiple threads - Synchronized
 * exportBatch() ensures only one export runs at a time - ScheduledExecutorService provides periodic
 * exports on single thread
 *
 * <p>Usage:
 *
 * <pre>
 * BatchTraceProcessor processor = new BatchTraceProcessor(
 *     exporter,
 *     BatchTraceProcessor.Config.builder()
 *         .maxBatchSize(100)
 *         .scheduleDelayMs(5000)
 *         .build()
 * );
 * </pre>
 *
 * Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/batcher.ts
 */
@Slf4j
public class BatchTraceProcessor implements TraceProcessor {
  private final TraceExporter exporter;
  private final BlockingQueue<Object> queue;
  private final ScheduledExecutorService scheduler;
  private final int maxQueueSize;
  private final int maxBatchSize;
  private final long scheduleDelayMs;
  private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

  // Lock for export synchronization
  private final Object exportLock = new Object();

  public BatchTraceProcessor(TraceExporter exporter, Config config) {
    this.exporter = exporter;
    this.maxQueueSize = config.maxQueueSize;
    this.maxBatchSize = config.maxBatchSize;
    this.scheduleDelayMs = config.scheduleDelayMs;
    this.queue = new ArrayBlockingQueue<>(maxQueueSize);
    this.scheduler =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread t = new Thread(r, "BatchTraceProcessor-Scheduler");
              t.setDaemon(true); // Don't block JVM shutdown
              return t;
            });

    // Start periodic export
    scheduler.scheduleWithFixedDelay(
        this::exportBatchSafe, scheduleDelayMs, scheduleDelayMs, TimeUnit.MILLISECONDS);
  }

  @Override
  public void onTraceStart(Trace trace) {
    // Export traces on start (matches TypeScript SDK behavior)
    enqueue(trace);
  }

  @Override
  public void onTraceEnd(Trace trace) {
    // Trace was already exported on start, no-op
  }

  @Override
  public void onSpanStart(Span<?> span) {
    // Spans are exported on end to capture complete data
  }

  @Override
  public void onSpanEnd(Span<?> span) {
    // Export spans on end
    enqueue(span);
  }

  /** Add item to queue. Thread-safe. If queue is 80% full, triggers immediate export. */
  private void enqueue(Object item) {
    if (shuttingDown.get()) {
      log.debug("Processor is shutting down, dropping item");
      return;
    }

    if (!queue.offer(item)) {
      log.warn(
          "Trace queue full ({}), dropping item: {}",
          maxQueueSize,
          item.getClass().getSimpleName());
      return;
    }

    // If queue is 80% full, export immediately
    // This prevents queue from filling up during high throughput
    if (queue.size() >= maxQueueSize * 0.8) {
      log.debug("Queue is 80% full, triggering immediate export");
      exportBatchSafe();
    }
  }

  /**
   * Safe wrapper for exportBatch that catches exceptions. Used by scheduler to prevent uncaught
   * exceptions from stopping scheduled tasks.
   */
  private void exportBatchSafe() {
    try {
      exportBatch();
    } catch (Exception e) {
      log.error("Unexpected error during batch export", e);
    }
  }

  /**
   * Export a batch of items. Thread-safe - only one export runs at a time. This method is
   * synchronized to prevent concurrent exports from scheduler and threshold-based triggers.
   */
  private void exportBatch() {
    synchronized (exportLock) {
      // Drain up to maxBatchSize items from queue
      List<Object> batch = new ArrayList<>(maxBatchSize);
      queue.drainTo(batch, maxBatchSize);

      if (batch.isEmpty()) {
        return;
      }

      log.debug("Exporting batch of {} items", batch.size());

      try {
        // Export asynchronously but block to ensure ordering
        exporter.export(batch).join();
        log.debug("Successfully exported {} items", batch.size());
      } catch (Exception e) {
        log.error("Failed to export batch of {} items", batch.size(), e);
        // Items are lost - we don't re-queue to avoid infinite loops
        // Production systems should have retry logic in the exporter
      }
    }
  }

  @Override
  public CompletableFuture<Void> forceFlush() {
    return CompletableFuture.runAsync(
        () -> {
          log.debug("Force flush requested");
          // Export all remaining items
          while (!queue.isEmpty()) {
            exportBatch();
          }
        });
  }

  @Override
  public CompletableFuture<Void> shutdown(long timeoutMs) {
    return CompletableFuture.runAsync(
        () -> {
          if (!shuttingDown.compareAndSet(false, true)) {
            log.debug("Shutdown already in progress");
            return;
          }

          log.info("Shutting down BatchTraceProcessor");

          // Stop accepting new items
          scheduler.shutdown();

          // Flush remaining items
          try {
            log.debug("Flushing {} remaining items", queue.size());
            while (!queue.isEmpty()) {
              exportBatch();
            }
          } catch (Exception e) {
            log.error("Error during shutdown flush", e);
          }

          // Wait for scheduler to finish
          try {
            if (!scheduler.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)) {
              log.warn("Scheduler did not terminate within timeout, forcing shutdown");
              scheduler.shutdownNow();
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduler.shutdownNow();
          }

          // Shutdown exporter
          try {
            exporter.shutdown(timeoutMs).join();
          } catch (Exception e) {
            log.error("Error shutting down exporter", e);
          }

          log.info("BatchTraceProcessor shutdown complete");
        });
  }

  /** Configuration for BatchTraceProcessor */
  @Value
  @Builder
  public static class Config {
    /** Maximum number of items in the queue before blocking/dropping Default: 1000 */
    @Builder.Default int maxQueueSize = 1000;

    /** Maximum number of items in a single batch export Default: 100 */
    @Builder.Default int maxBatchSize = 100;

    /** Interval between periodic exports (milliseconds) Default: 5000 (5 seconds) */
    @Builder.Default long scheduleDelayMs = 5000;
  }
}
