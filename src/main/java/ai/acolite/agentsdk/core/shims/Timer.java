package ai.acolite.agentsdk.core.shims;

/**
 * Timer
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/shims/interface.ts">shims/interface.ts</a>
 */
public interface Timer {

  /**
   * clearTimeout
   *
   * @param timeoutId Object
   */
  Void clearTimeout(Object timeoutId);
}
