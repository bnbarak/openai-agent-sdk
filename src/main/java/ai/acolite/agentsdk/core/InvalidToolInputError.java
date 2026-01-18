package ai.acolite.agentsdk.core;

/**
 * InvalidToolInputError
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/errors.ts">errors.ts</a>
 */
public class InvalidToolInputError extends ModelBehaviorError {

  public InvalidToolInputError(String message) {
    super(message);
  }

  public InvalidToolInputError(String message, Throwable cause) {
    super(message, cause);
  }
}
