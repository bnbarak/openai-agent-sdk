package ai.acolite.agentsdk.exceptions;

import lombok.Getter;

/**
 * ToolCallError
 *
 * <p>Thrown when a tool execution fails. Contains information about which tool failed and why.
 *
 * <p>Follows TypeScript SDK pattern from @openai/agents-core Source:
 * https://openai.github.io/openai-agents-js/guides/tools/
 */
@Getter
public class ToolCallError extends AgentsError {
  private final String toolName;
  private final String toolCallId;

  public ToolCallError(String toolName, String message) {
    super(String.format("Tool '%s' failed: %s", toolName, message));
    this.toolName = toolName;
    this.toolCallId = null;
  }

  public ToolCallError(String toolName, String toolCallId, String message, Throwable cause) {
    super(
        String.format("Tool '%s' (call_id: %s) failed: %s", toolName, toolCallId, message), cause);
    this.toolName = toolName;
    this.toolCallId = toolCallId;
  }
}
