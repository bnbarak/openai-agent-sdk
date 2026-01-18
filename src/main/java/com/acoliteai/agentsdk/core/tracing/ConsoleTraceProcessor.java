package com.acoliteai.agentsdk.core.tracing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.concurrent.CompletableFuture;

/**
 * Simple trace processor that prints to console.
 *
 * <p>Useful for: - Development and debugging - Examples and demos - Testing without OpenAI API
 *
 * <p>Output format: Pretty-printed JSON
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/processor.ts">tracing/processor.ts</a>
 */
public class ConsoleTraceProcessor implements TraceProcessor {

  private final ObjectMapper mapper;
  private final boolean prettyPrint;

  /** Create console processor with pretty printing enabled */
  public ConsoleTraceProcessor() {
    this(true);
  }

  /**
   * Create console processor
   *
   * @param prettyPrint Whether to pretty-print JSON output
   */
  public ConsoleTraceProcessor(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
    this.mapper = new ObjectMapper();
    if (prettyPrint) {
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
  }

  @Override
  public void onTraceStart(Trace trace) {
    try {
      System.out.println("=== TRACE START ===");
      if (prettyPrint) {
        System.out.println(mapper.writeValueAsString(trace.toJson()));
      } else {
        System.out.println(trace.toJson());
      }
      System.out.println();
    } catch (Exception e) {
      System.err.println("Error printing trace: " + e.getMessage());
    }
  }

  @Override
  public void onTraceEnd(Trace trace) {
    try {
      System.out.println("=== TRACE END ===");
      System.out.println("Trace ID: " + trace.getTraceId());
      System.out.println("Duration: " + calculateDuration(trace));
      System.out.println();
    } catch (Exception e) {
      System.err.println("Error printing trace end: " + e.getMessage());
    }
  }

  @Override
  public void onSpanStart(Span<?> span) {
    try {
      System.out.println("--- SPAN START ---");
      System.out.println("Span ID: " + span.getSpanId());
      System.out.println("Type: " + span.getData().getType());
      if (span.getParentId() != null) {
        System.out.println("Parent: " + span.getParentId());
      }
      System.out.println();
    } catch (Exception e) {
      System.err.println("Error printing span start: " + e.getMessage());
    }
  }

  @Override
  public void onSpanEnd(Span<?> span) {
    try {
      System.out.println("--- SPAN END ---");
      if (prettyPrint) {
        System.out.println(mapper.writeValueAsString(span.toJson()));
      } else {
        System.out.println(span.toJson());
      }
      if (span.getError() != null) {
        System.out.println("ERROR: " + span.getError().getMessage());
      }
      System.out.println();
    } catch (Exception e) {
      System.err.println("Error printing span end: " + e.getMessage());
    }
  }

  @Override
  public CompletableFuture<Void> forceFlush() {
    System.out.flush();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> shutdown(long timeoutMs) {
    System.out.println("=== CONSOLE TRACE PROCESSOR SHUTDOWN ===");
    System.out.flush();
    return CompletableFuture.completedFuture(null);
  }

  private String calculateDuration(Trace trace) {
    if (trace.getStartedAt() == null || trace.getEndedAt() == null) {
      return "N/A";
    }
    long durationMs = trace.getEndedAt().toEpochMilli() - trace.getStartedAt().toEpochMilli();
    if (durationMs < 1000) {
      return durationMs + "ms";
    }
    return String.format("%.2fs", durationMs / 1000.0);
  }
}
