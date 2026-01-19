package ai.acolite.agentsdk.core;

import lombok.Builder;

/**
 * RunItemStreamEvent
 *
 * <p>Emitted when a new item is added to the conversation (message, tool call, tool output, etc.)
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/events.ts
 */
@Builder
public record RunItemStreamEvent(RunItem item, int turnIndex) implements RunStreamEvent {
  @Override
  public String getType() {
    return switch (item) {
      case RunMessageOutputItem ignored -> "message_output_created";
      case RunToolCallItem ignored -> "tool_called";
      case RunToolCallOutputItem ignored -> "tool_output";
      case RunHandoffCallItem ignored -> "handoff_called";
      case RunHandoffOutputItem ignored -> "handoff_output";
      case null, default -> "item_created";
    };
  }
}
