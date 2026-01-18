package ai.acolite.agentsdk.core;

import ai.acolite.agentsdk.core.memory.Session;
import ai.acolite.agentsdk.openai.OpenAIProvider;
import java.util.Optional;
import lombok.Builder;
import lombok.Value;

/**
 * RunConfig
 *
 * <p>Configuration for running agents.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/config.ts
 */
@Value
@Builder
public class RunConfig {
  /**
   * Maximum number of turns (agent-model interactions) before stopping. Defaults to 10 if not
   * specified (via getEffectiveMaxTurns()).
   */
  Integer maxTurns;

  /** Timeout in milliseconds for the entire run execution. If null, no timeout is applied. */
  Long timeoutMs;

  /**
   * Timeout in milliseconds for each model API call. Defaults to 60000ms (60 seconds) if not
   * specified.
   */
  Long modelTimeoutMs;

  /**
   * Model provider for obtaining model instances. Defaults to OpenAIProvider if not specified (via
   * getEffectiveModelProvider()).
   */
  ModelProvider modelProvider;

  /**
   * Optional model name override (e.g., "gpt-4.1", "gpt-4.1-mini"). If specified, overrides the
   * agent's model setting.
   */
  String model;

  @Builder.Default Optional<Boolean> stream = Optional.empty();
  @Builder.Default Optional<Object> context = Optional.empty();

  /**
   * Optional session for persistent conversation memory. When provided, Runner automatically loads
   * conversation history before execution and saves new items after execution completes.
   */
  Session session;

  @Builder.Default Optional<Object> signal = Optional.empty();
  @Builder.Default Optional<Boolean> traceIncludeSensitiveData = Optional.empty();

  /** Gets the maximum number of turns, defaulting to 10 if not set */
  public int getEffectiveMaxTurns() {
    return maxTurns != null ? maxTurns : 10;
  }

  /** Gets the model provider, defaulting to OpenAIProvider if not set */
  public ModelProvider getEffectiveModelProvider() {
    return modelProvider != null ? modelProvider : new OpenAIProvider();
  }

  /** Gets the model timeout in milliseconds, defaulting to 60 seconds if not set */
  public long getEffectiveModelTimeoutMs() {
    return modelTimeoutMs != null ? modelTimeoutMs : 60000L;
  }

  /** Gets the overall run timeout in milliseconds, or null if no timeout */
  public Long getEffectiveTimeoutMs() {
    return timeoutMs;
  }
}
