package ai.acolite.agentsdk.core;

import lombok.Builder;
import lombok.Getter;

/**
 * TextDeltaStreamEvent
 *
 * <p>Represents a text delta event from the model's streaming response.
 */
@Getter
@Builder
public class TextDeltaStreamEvent implements StreamEvent {
  private final String delta;

  @Override
  public String getType() {
    return "response.output_text.delta";
  }
}
