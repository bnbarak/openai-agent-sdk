package com.acoliteai.agentsdk.core.tracing;

import java.util.UUID;

/**
 * Utility functions for tracing.
 *
 * <p>Provides ID generation, timestamp formatting, and other helper methods.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/utils.ts">tracing/utils.ts</a>
 */
public final class TracingUtils {

  private TracingUtils() {
    // Utility class - prevent instantiation
  }

  /**
   * Generate a trace ID. Format: trace_<32-char-hex>
   *
   * <p>TypeScript: export function generateTraceId(): string
   *
   * @return Trace ID (e.g., "trace_a1b2c3d4e5f67890...")
   */
  public static String generateTraceId() {
    String uuid = UUID.randomUUID().toString().replace("-", "");
    return "trace_" + uuid;
  }

  /**
   * Generate a span ID. Format: span_<24-char-hex>
   *
   * <p>TypeScript: export function generateSpanId(): string
   *
   * @return Span ID (e.g., "span_a1b2c3d4e5f67890...")
   */
  public static String generateSpanId() {
    String uuid = UUID.randomUUID().toString().replace("-", "");
    // Take first 24 characters (TypeScript SDK pattern)
    return "span_" + uuid.substring(0, 24);
  }

  /**
   * Generate a group ID. Format: group_<24-char-hex>
   *
   * <p>TypeScript: export function generateGroupId(): string
   *
   * @return Group ID (e.g., "group_a1b2c3d4e5f67890...")
   */
  public static String generateGroupId() {
    String uuid = UUID.randomUUID().toString().replace("-", "");
    // Take first 24 characters (TypeScript SDK pattern)
    return "group_" + uuid.substring(0, 24);
  }
}
