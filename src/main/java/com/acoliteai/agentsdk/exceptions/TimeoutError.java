package com.acoliteai.agentsdk.exceptions;

import lombok.Getter;

/**
 * TimeoutError
 *
 * <p>Thrown when an operation exceeds its timeout limit. This can occur during model calls, tool
 * execution, or overall run execution.
 *
 * <p>Follows TypeScript SDK pattern (AbortSignal cancellation) Source:
 * https://openai.github.io/openai-agents-js/guides/running-agents/
 */
@Getter
public class TimeoutError extends AgentsError {
  private final long timeoutMs;

  public TimeoutError(String operation, long timeoutMs) {
    super(String.format("%s timed out after %d ms", operation, timeoutMs));
    this.timeoutMs = timeoutMs;
  }

  public TimeoutError(String operation, long timeoutMs, Throwable cause) {
    super(String.format("%s timed out after %d ms", operation, timeoutMs), cause);
    this.timeoutMs = timeoutMs;
  }
}
