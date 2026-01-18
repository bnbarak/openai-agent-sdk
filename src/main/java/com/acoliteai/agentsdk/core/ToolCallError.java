package com.acoliteai.agentsdk.core;

/**
 * ToolCallError
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/errors.ts">errors.ts</a>
 */
public class ToolCallError extends AgentsError {

  private Error error;

  public ToolCallError(String message) {
    super(message);
  }

  public ToolCallError(String message, Throwable cause) {
    super(message, cause);
  }
}
