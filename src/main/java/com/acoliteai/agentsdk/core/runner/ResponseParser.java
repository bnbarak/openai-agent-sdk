package com.acoliteai.agentsdk.core.runner;

import com.acoliteai.agentsdk.core.ModelResponse;
import com.acoliteai.agentsdk.core.RunItem;
import com.acoliteai.agentsdk.core.RunMessageOutputItem;
import com.acoliteai.agentsdk.core.RunToolCallItem;
import com.acoliteai.agentsdk.openai.SerializationUtils;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseFunctionWebSearch;
import com.openai.models.responses.ResponseOutputItem;
import java.util.ArrayList;
import java.util.List;

/**
 * ResponseParser
 *
 * <p>Utility for parsing ModelResponse output into RunItem objects. Separates parsing logic from
 * execution flow.
 */
public class ResponseParser {

  /**
   * Parse ModelResponse output into RunItem objects
   *
   * @param response The model response to parse
   * @return List of RunItem objects extracted from the response
   */
  public static List<RunItem> parseResponseItems(ModelResponse response) {
    List<RunItem> items = new ArrayList<>();

    if (response.getOutput() == null || response.getOutput().isEmpty()) {
      return items;
    }

    for (Object outputItem : response.getOutput()) {
      RunItem item = parseOutputItem(outputItem);
      if (item != null) {
        items.add(item);
      }
    }

    return items;
  }

  /**
   * Parse a single output item into a RunItem
   *
   * @param outputItem The output item from the model response
   * @return Parsed RunItem or null if type not recognized
   */
  private static RunItem parseOutputItem(Object outputItem) {
    if (outputItem == null) {
      return null;
    }

    if (outputItem instanceof RunItem) {
      return (RunItem) outputItem;
    }

    if (outputItem instanceof String) {
      return RunMessageOutputItem.builder().content(outputItem).role("assistant").build();
    }

    if (outputItem instanceof ResponseFunctionToolCall functionCall) {
      Object parameters = parseToolArguments(functionCall.arguments());

      return RunToolCallItem.builder()
          .id(functionCall.callId())
          .name(functionCall.name())
          .parameters(parameters)
          .build();
    }

    // Handle hosted tool calls
    if (outputItem instanceof ResponseFunctionWebSearch webSearchCall) {
      return RunToolCallItem.builder()
          .id(webSearchCall.id())
          .name("web_search")
          .parameters(webSearchCall.action())
          .build();
    }

    if (outputItem instanceof ResponseOutputItem.ImageGenerationCall imageGenCall) {
      return RunToolCallItem.builder()
          .id(imageGenCall.id())
          .name("image_generation")
          .parameters(null)
          .build();
    }

    // TODO: Add handlers for other hosted tool types when class names are confirmed
    // For now, if it's an unknown type (likely another hosted tool), convert to message
    return RunMessageOutputItem.builder().content(outputItem).role("assistant").build();
  }

  /** Parse tool call arguments from JSON string */
  private static Object parseToolArguments(String argumentsJson) {
    if (argumentsJson == null || argumentsJson.isEmpty()) {
      return null;
    }

    return SerializationUtils.deserializeFromJson(argumentsJson);
  }

  /**
   * Extract the final output from a list of RunItems Content can be a String (for text responses)
   * or a typed object (for structured outputs).
   *
   * @param items List of RunItems from conversation
   * @return The final output, or null if none found
   */
  public static Object extractFinalOutput(List<RunItem> items) {
    if (items == null || items.isEmpty()) {
      return null;
    }

    for (int i = items.size() - 1; i >= 0; i--) {
      RunItem item = items.get(i);
      if (item instanceof RunMessageOutputItem) {
        return ((RunMessageOutputItem) item).getContent();
      }
    }

    return null;
  }
}
