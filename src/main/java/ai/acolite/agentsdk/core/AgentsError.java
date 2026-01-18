package ai.acolite.agentsdk.core;

/**
 * AgentsError
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/errors.ts">...</a>
 */
public class AgentsError extends Error {
  public AgentsError(String message) {
    super(message, null);
  }

  public AgentsError(String message, Throwable cause) {}
}
