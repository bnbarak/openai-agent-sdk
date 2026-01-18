package com.acoliteai.agentsdk.openai;

import com.acoliteai.agentsdk.core.RunMessageOutputItem;
import com.acoliteai.agentsdk.core.RunToolCallItem;
import com.acoliteai.agentsdk.core.RunToolCallOutputItem;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseInputItem;
import java.util.ArrayList;
import java.util.List;

/**
 * ConversionUtils
 *
 * <p>Static utility methods for converting between SDK types and OpenAI API types.
 */
public class ConversionUtils {

  private ConversionUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Convert conversation items to OpenAI ResponseInputItem format.
   *
   * <p>Handles: - String messages → text input (USER role) - RunToolCallItem → function call input
   * - RunToolCallOutputItem → function call output - RunMessageOutputItem → skipped (assistant
   * messages are outputs)
   *
   * @param items List of conversation items (String, RunItem, etc.)
   * @return List of ResponseInputItem objects for the API
   */
  public static List<ResponseInputItem> convertToResponseInputItems(List<Object> items) {
    List<ResponseInputItem> inputItems = new ArrayList<>();

    for (Object item : items) {
      if (item instanceof String text) {
        inputItems.add(
            ResponseInputItem.ofMessage(
                ResponseInputItem.Message.builder()
                    .addInputTextContent(text)
                    .role(ResponseInputItem.Message.Role.USER)
                    .build()));
      } else if (item instanceof RunToolCallItem toolCall) {
        ResponseFunctionToolCall functionCall =
            ResponseFunctionToolCall.builder()
                .callId(toolCall.getId())
                .name(toolCall.getName())
                .arguments(SerializationUtils.serializeToJson(toolCall.getParameters()))
                .build();
        inputItems.add(ResponseInputItem.ofFunctionCall(functionCall));
      } else if (item instanceof RunToolCallOutputItem toolOutput) {
        Object result =
            toolOutput.getResult() != null
                ? toolOutput.getResult()
                : toolOutput.getError().orElse("Error: no result");

        ResponseInputItem.FunctionCallOutput output =
            ResponseInputItem.FunctionCallOutput.builder()
                .callId(toolOutput.getToolCallId())
                .outputAsJson(result)
                .build();
        inputItems.add(ResponseInputItem.ofFunctionCallOutput(output));
      } else if (item instanceof RunMessageOutputItem) {
        // Skip assistant messages - they're outputs, not inputs
        // The API doesn't need these passed back
      }
    }

    return inputItems;
  }
}
