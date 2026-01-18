package com.acoliteai.agentsdk.exceptions;

/**
 * AgentsError
 *
 * <p>Base exception class for all agent-related errors. Follows TypeScript SDK pattern
 * from @openai/agents-core
 *
 * <p>Source: https://openai.github.io/openai-agents-js/guides/running-agents/
 */
public class AgentsError extends RuntimeException {
  public AgentsError(String message) {
    super(message);
  }

  public AgentsError(String message, Throwable cause) {
    super(message, cause);
  }
}
