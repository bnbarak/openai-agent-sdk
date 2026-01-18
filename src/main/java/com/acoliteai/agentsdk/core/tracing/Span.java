package com.acoliteai.agentsdk.core.tracing;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * Represents a timed operation within a trace.
 *
 * <p>Spans capture individual operations (agent execution, LLM generation, tool calls, etc.) with
 * start/end timestamps, parent relationships, and operation-specific data.
 *
 * <p>Lifecycle: 1. Create span with builder 2. Call start() to begin timing 3. Execute operation 4.
 * Call end() to finish (or setError() if failed)
 *
 * <p>Thread-safe: Yes
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/spans.ts">tracing/spans.ts</a>
 */
@Getter
@Builder
public class Span<TData extends SpanData> {

  /** Unique span identifier (format: span_<uuid>) */
  private final String spanId;

  /** Parent trace identifier */
  private final String traceId;

  /** Parent span identifier (null for root spans) */
  private final String parentId;

  /** Span data (operation-specific) */
  private final TData data;

  /** Processor for lifecycle events */
  @Builder.Default private final TraceProcessor processor = NoopTraceProcessor.INSTANCE;

  /** Previous span in the stack (for context management) */
  private Span<?> previousSpan;

  /** Optional API key for trace export */
  private final String tracingApiKey;

  /** Span start time */
  private Instant startedAt;

  /** Span end time */
  private Instant endedAt;

  /** Error information (if operation failed) */
  private SpanError error;

  /** Whether span has started */
  private boolean started = false;

  /** Whether span has ended */
  private boolean ended = false;

  /** Start the span. Calls processor.onSpanStart(). Idempotent - multiple calls are safe. */
  public synchronized void start() {
    if (started) {
      return;
    }
    started = true;
    startedAt = Instant.now();
    processor.onSpanStart(this);
  }

  /** End the span. Calls processor.onSpanEnd(). Idempotent - multiple calls are safe. */
  public synchronized void end() {
    if (ended) {
      return;
    }
    ended = true;
    endedAt = Instant.now();
    processor.onSpanEnd(this);
  }

  /** Set error information. Call this before end() when operation fails. */
  public synchronized void setError(SpanError error) {
    this.error = error;
  }

  /** Convenience method to set error from Throwable */
  public synchronized void setError(Throwable throwable) {
    this.error = SpanError.fromThrowable(throwable);
  }

  /** Clone this span with same properties. */
  public Span<TData> clone() {
    return Span.<TData>builder()
        .spanId(spanId)
        .traceId(traceId)
        .parentId(parentId)
        .data(data)
        .processor(processor)
        .tracingApiKey(tracingApiKey)
        .build();
  }

  /** Convert to JSON for export. */
  public Map<String, Object> toJson() {
    Map<String, Object> json = new HashMap<>();

    // OpenAI format fields
    json.put("object", "trace.span");
    json.put("id", spanId);
    json.put("trace_id", traceId);
    json.put("parent_id", parentId); // Always include, null is OK

    if (startedAt != null) {
      json.put("started_at", startedAt.toString());
    }

    if (endedAt != null) {
      json.put("ended_at", endedAt.toString());
    }

    // Serialize span data
    if (data != null) {
      json.put("span_data", serializeSpanData(data));
    }

    // Always include error field (null if no error)
    json.put("error", error);

    return json;
  }

  /**
   * Serialize SpanData to map for JSON export. Creates a map with type field and all data fields.
   */
  private Map<String, Object> serializeSpanData(TData data) {
    Map<String, Object> spanData = new HashMap<>();
    spanData.put("type", data.getType());

    // Add type-specific fields based on SpanData type
    if (data instanceof AgentSpanData agent) {
      spanData.put("name", agent.getAgentName());
      spanData.put("handoffs", agent.getHandoffs() != null ? agent.getHandoffs() : List.of());
      spanData.put("tools", agent.getTools() != null ? agent.getTools() : List.of());
      if (agent.getOutputType() != null) {
        spanData.put("output_type", agent.getOutputType().toString().toLowerCase());
      }
    } else if (data instanceof GenerationSpanData gen) {
      spanData.put("model", gen.getModel());
      spanData.put("input", gen.getInput());
      spanData.put("output", gen.getOutput());
      if (gen.getModelConfig() != null) {
        spanData.put("model_config", gen.getModelConfig());
      }
      if (gen.getUsage() != null) {
        spanData.put("usage", gen.getUsage());
      }
    } else if (data instanceof FunctionSpanData func) {
      spanData.put("function_name", func.getFunctionName());
      spanData.put("input", func.getInput());
      spanData.put("output", func.getOutput());
      if (func.getMcpServer() != null) {
        spanData.put("mcp_server", func.getMcpServer());
      }
    } else if (data instanceof HandoffSpanData handoff) {
      spanData.put("from_agent", handoff.getFromAgent());
      spanData.put("to_agent", handoff.getToAgent());
      if (handoff.getReason() != null) {
        spanData.put("reason", handoff.getReason());
      }
    } else if (data instanceof CustomSpanData custom) {
      spanData.put("name", custom.getName());
      if (custom.getData() != null) {
        spanData.put("data", custom.getData());
      }
    } else if (data instanceof GuardrailSpanData guardrail) {
      spanData.put("guardrail_name", guardrail.getGuardrailName());
      spanData.put("triggered", guardrail.isTriggered());
      if (guardrail.getReason() != null) {
        spanData.put("reason", guardrail.getReason());
      }
    }

    return spanData;
  }
}
