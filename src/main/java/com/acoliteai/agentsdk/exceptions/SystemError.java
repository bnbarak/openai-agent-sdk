package com.acoliteai.agentsdk.exceptions;

/**
 * SystemError
 *
 * <p>Thrown for internal SDK failures that are not caused by user configuration. Examples:
 * serialization failures, internal state corruption, etc.
 *
 * <p>Follows TypeScript SDK pattern from @openai/agents-core Source:
 * https://openai.github.io/openai-agents-js/guides/running-agents/
 */
public class SystemError extends AgentsError {
  public SystemError(String message) {
    super(message);
  }

  public SystemError(String message, Throwable cause) {
    super(message, cause);
  }
}
