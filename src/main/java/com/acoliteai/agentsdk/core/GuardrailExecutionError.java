package com.acoliteai.agentsdk.core;

/**
 * GuardrailExecutionError
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/errors.ts">errors.ts</a>
 */
public class GuardrailExecutionError extends AgentsError {

  private Error error;

  public GuardrailExecutionError(String message) {
    super(message);
  }

  public GuardrailExecutionError(String message, Throwable cause) {
    super(message, cause);
  }
}
