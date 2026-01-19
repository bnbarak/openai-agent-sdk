package ai.acolite.agentsdk.core;

/**
 * RunStreamEvent
 *
 * <p>Union of all stream event types.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/events.ts
 */
public interface RunStreamEvent {
  String getType();
}
