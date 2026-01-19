package ai.acolite.agentsdk.core;

/**
 * StreamEvent
 *
 * <p>Represents a streaming event from the model.
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/model.ts
 */
public interface StreamEvent {
  String getType();
}
