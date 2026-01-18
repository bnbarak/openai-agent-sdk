package com.acoliteai.agentsdk.exceptions;

/**
 * UserError
 *
 * <p>Thrown when the user provides invalid configuration or usage. Examples: missing required
 * fields, invalid agent configuration, etc.
 *
 * <p>Follows TypeScript SDK pattern from @openai/agents-core Source:
 * https://openai.github.io/openai-agents-js/guides/running-agents/
 */
public class UserError extends AgentsError {
  public UserError(String message) {
    super(message);
  }

  public UserError(String message, Throwable cause) {
    super(message, cause);
  }
}
