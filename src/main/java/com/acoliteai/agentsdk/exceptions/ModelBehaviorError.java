package com.acoliteai.agentsdk.exceptions;

/**
 * ModelBehaviorError
 *
 * <p>Thrown when the model returns an unexpected or malformed response. This includes parsing
 * errors, invalid response formats, etc.
 *
 * <p>Follows TypeScript SDK pattern from @openai/agents-core Source:
 * https://openai.github.io/openai-agents-js/guides/running-agents/
 */
public class ModelBehaviorError extends AgentsError {
  public ModelBehaviorError(String message) {
    super(message);
  }

  public ModelBehaviorError(String message, Throwable cause) {
    super(message, cause);
  }
}
