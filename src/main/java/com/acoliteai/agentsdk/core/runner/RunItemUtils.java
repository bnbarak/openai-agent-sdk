package com.acoliteai.agentsdk.core.runner;

import com.acoliteai.agentsdk.core.RunItem;
import com.acoliteai.agentsdk.core.RunToolCallItem;
import com.acoliteai.agentsdk.core.RunToolCallOutputItem;
import java.util.List;

/**
 * RunItemUtils
 *
 * <p>Static utility methods for analyzing lists of RunItems. Extracted from RunState for better
 * testability and reusability.
 */
public class RunItemUtils {

  private RunItemUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Checks if there are any tool calls without corresponding outputs.
   *
   * @param items List of conversation items to check
   * @return true if there are pending tool calls, false otherwise
   */
  public static boolean hasPendingToolCalls(List<RunItem> items) {
    for (int i = items.size() - 1; i >= 0; i--) {
      RunItem item = items.get(i);
      if (item instanceof RunToolCallItem toolCall) {
        if (!hasToolCallOutput(items, toolCall.getId())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks if a specific tool call has a corresponding output.
   *
   * @param items List of conversation items to search
   * @param toolCallId ID of the tool call to find output for
   * @return true if output exists, false otherwise
   */
  public static boolean hasToolCallOutput(List<RunItem> items, String toolCallId) {
    for (RunItem item : items) {
      if (item instanceof RunToolCallOutputItem output) {
        if (toolCallId.equals(output.getToolCallId())) {
          return true;
        }
      }
    }
    return false;
  }
}
