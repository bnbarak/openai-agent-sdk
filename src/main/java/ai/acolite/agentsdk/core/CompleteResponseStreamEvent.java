package ai.acolite.agentsdk.core;

import lombok.Builder;
import lombok.Getter;

/**
 * CompleteResponseStreamEvent
 *
 * <p>Emitted at the end of streaming to provide the complete accumulated response including tool
 * calls.
 */
@Getter
@Builder
public class CompleteResponseStreamEvent implements StreamEvent {
  private final ModelResponse response;

  @Override
  public String getType() {
    return "response.complete";
  }
}
