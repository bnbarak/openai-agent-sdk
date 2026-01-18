package ai.acolite.agentsdk.core;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * RunRawModelStreamEvent
 *
 * <p>Emitted for raw events from the underlying model (e.g., streaming chunks) Useful for accessing
 * low-level model responses
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/events.ts
 */
@Getter
@Builder
public class RunRawModelStreamEvent implements RunStreamEvent {
  @NonNull private final StreamEvent modelEvent;

  @Override
  public String getType() {
    return "raw_model_event";
  }
}
