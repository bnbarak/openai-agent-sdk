package com.acoliteai.agentsdk.core;

/**
 * ModelBehaviorError
 *
 * <p>Base class for errors related to model behavior.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/errors.ts
 */
public class ModelBehaviorError extends RuntimeException {
  public ModelBehaviorError(String message) {
    super(message);
  }

  public ModelBehaviorError(String message, Throwable cause) {
    super(message, cause);
  }
}
