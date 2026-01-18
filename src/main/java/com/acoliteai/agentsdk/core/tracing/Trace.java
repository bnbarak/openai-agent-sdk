package com.acoliteai.agentsdk.core.tracing;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * Represents a single end-to-end workflow trace.
 *
 * <p>A trace captures the execution of an entire workflow, containing multiple spans for individual
 * operations (agents, generations, tools, etc.).
 *
 * <p>Lifecycle: 1. Create trace with builder 2. Call start() to begin 3. Execute workflow (creates
 * spans) 4. Call end() to complete
 *
 * <p>Thread-safe: Yes
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/traces.ts">tracing/traces.ts</a>
 */
@Getter
@Builder
public class Trace {

  /** Unique trace identifier (format: trace_<uuid>) */
  private final String traceId;

  /** Trace name (e.g., "Agent workflow", "Data processing") */
  @Builder.Default private final String name = "Agent workflow";

  /** Group ID for organizing related traces */
  private final String groupId;

  /** Arbitrary metadata */
  @Builder.Default private final Map<String, Object> metadata = new HashMap<>();

  /** Optional API key for trace export */
  private final String tracingApiKey;

  /** Processor for lifecycle events */
  @Builder.Default private final TraceProcessor processor = NoopTraceProcessor.INSTANCE;

  /** Trace start time */
  private Instant startedAt;

  /** Trace end time */
  private Instant endedAt;

  /** Whether trace has started */
  private boolean started = false;

  /** Whether trace has ended */
  private boolean ended = false;

  /** Start the trace. Calls processor.onTraceStart(). Idempotent - multiple calls are safe. */
  public synchronized void start() {
    if (started) {
      return;
    }
    started = true;
    startedAt = Instant.now();
    processor.onTraceStart(this);
  }

  /** End the trace. Calls processor.onTraceEnd(). Idempotent - multiple calls are safe. */
  public synchronized void end() {
    if (ended) {
      return;
    }
    ended = true;
    endedAt = Instant.now();
    processor.onTraceEnd(this);
  }

  /**
   * Clone this trace with same properties. Useful for creating related traces with shared
   * configuration.
   */
  public Trace clone() {
    return Trace.builder()
        .traceId(traceId)
        .name(name)
        .groupId(groupId)
        .metadata(new HashMap<>(metadata))
        .tracingApiKey(tracingApiKey)
        .processor(processor)
        .build();
  }

  /**
   * Clone this trace with a different processor.
   *
   * @param processor Processor for lifecycle events
   * @return New trace instance with updated processor
   */
  public Trace withProcessor(TraceProcessor processor) {
    return Trace.builder()
        .traceId(traceId)
        .name(name)
        .groupId(groupId)
        .metadata(metadata != null ? new HashMap<>(metadata) : new HashMap<>())
        .tracingApiKey(tracingApiKey)
        .processor(processor)
        .build();
  }

  /**
   * Convert to JSON for export.
   *
   * @param includeTracingApiKey Whether to include API key in output
   * @return JSON representation
   */
  public Map<String, Object> toJson(boolean includeTracingApiKey) {
    Map<String, Object> json = new HashMap<>();

    // OpenAI format fields
    json.put("object", "trace");
    json.put("id", traceId);
    json.put("workflow_name", name);
    json.put("group_id", groupId); // Always include, null is OK
    json.put("metadata", metadata != null ? metadata : Map.of());

    // Internal routing field (removed before HTTP export)
    if (includeTracingApiKey && tracingApiKey != null) {
      json.put("tracingApiKey", tracingApiKey);
    }

    return json;
  }

  /** Convert to JSON (excludes API key by default for security) */
  public Map<String, Object> toJson() {
    return toJson(false);
  }
}
