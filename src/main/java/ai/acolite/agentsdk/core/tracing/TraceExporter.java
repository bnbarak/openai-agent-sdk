package ai.acolite.agentsdk.core.tracing;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * TraceExporter
 *
 * <p>Interface for exporting trace and span data to external systems. Implementations send traces
 * to OpenAI platform, logging systems, or other backends.
 *
 * <p>The exporter receives batches of items (Trace and Span objects) and is responsible for: -
 * Serializing items to the target format - Sending data via HTTP, gRPC, or other protocols -
 * Handling errors and retries
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/exporter.ts
 */
public interface TraceExporter {

  /**
   * Export a batch of trace items (Trace and Span objects).
   *
   * <p>The implementation should: 1. Serialize items to target format 2. Send to external system 3.
   * Handle errors appropriately (log or retry)
   *
   * @param items List of Trace and Span objects to export
   * @return CompletableFuture that completes when export finishes (or fails)
   */
  CompletableFuture<Void> export(List<Object> items);

  /**
   * Shutdown the exporter gracefully. Should flush any pending data and release resources.
   *
   * @param timeoutMs Maximum time to wait for shutdown
   * @return CompletableFuture that completes when shutdown finishes
   */
  default CompletableFuture<Void> shutdown(long timeoutMs) {
    return CompletableFuture.completedFuture(null);
  }
}
