package com.acoliteai.agentsdk.core.tracing;

import java.util.Map;
import lombok.Builder;
import lombok.Value;

/**
 * Error information captured in spans.
 *
 * <p>Stores error message and optional additional metadata when an operation fails during span
 * execution.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/spans.ts">tracing/spans.ts</a>
 */
@Value
@Builder
public class SpanError {

  /** Error message */
  String message;

  /** Additional error metadata (stack trace, error code, etc.) */
  Map<String, Object> data;

  /** Convenience constructor from Throwable */
  public static SpanError fromThrowable(Throwable throwable) {
    return SpanError.builder()
        .message(
            throwable.getMessage() != null
                ? throwable.getMessage()
                : throwable.getClass().getName())
        .data(
            Map.of(
                "type", throwable.getClass().getName(),
                "stackTrace", getStackTraceString(throwable)))
        .build();
  }

  private static String getStackTraceString(Throwable throwable) {
    StackTraceElement[] elements = throwable.getStackTrace();
    if (elements == null || elements.length == 0) {
      return "";
    }
    // Return first 5 stack frames
    StringBuilder sb = new StringBuilder();
    int limit = Math.min(5, elements.length);
    for (int i = 0; i < limit; i++) {
      sb.append(elements[i].toString());
      if (i < limit - 1) {
        sb.append("\n");
      }
    }
    return sb.toString();
  }
}
