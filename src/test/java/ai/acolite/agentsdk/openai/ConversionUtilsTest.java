package ai.acolite.agentsdk.openai;

import static org.junit.jupiter.api.Assertions.*;

import ai.acolite.agentsdk.core.RunMessageOutputItem;
import ai.acolite.agentsdk.core.RunToolCallItem;
import ai.acolite.agentsdk.core.RunToolCallOutputItem;
import com.openai.models.responses.ResponseInputItem;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ConversionUtils.
 *
 * <p>Tests conversion of SDK types to OpenAI API types for request building.
 */
class ConversionUtilsTest {

  @Test
  void convertToResponseInputItems_emptyList_returnsEmpty() {
    List<ResponseInputItem> result = ConversionUtils.convertToResponseInputItems(List.of());

    assertTrue(result.isEmpty());
  }

  @Test
  void convertToResponseInputItems_stringMessage_createsUserMessage() {
    List<Object> items = List.of("Hello, world!");

    List<ResponseInputItem> result = ConversionUtils.convertToResponseInputItems(items);

    assertEquals(1, result.size());
    // Can't easily inspect the internals without reflection,
    // but we can verify it doesn't throw
    assertNotNull(result.get(0));
  }

  @Test
  void convertToResponseInputItems_toolCall_createsFunctionCall() {
    RunToolCallItem toolCall =
        RunToolCallItem.builder()
            .id("call_123")
            .name("calculator")
            .parameters("{\"operation\":\"add\",\"a\":1,\"b\":2}")
            .build();

    List<ResponseInputItem> result = ConversionUtils.convertToResponseInputItems(List.of(toolCall));

    assertEquals(1, result.size());
    assertNotNull(result.get(0));
  }

  @Test
  void convertToResponseInputItems_toolOutput_createsFunctionCallOutput() {
    RunToolCallOutputItem toolOutput =
        RunToolCallOutputItem.builder()
            .toolCallId("call_123")
            .result(42)
            .error(Optional.empty())
            .build();

    List<ResponseInputItem> result =
        ConversionUtils.convertToResponseInputItems(List.of(toolOutput));

    assertEquals(1, result.size());
    assertNotNull(result.get(0));
  }

  @Test
  void convertToResponseInputItems_toolOutputWithError_usesError() {
    RunToolCallOutputItem toolOutput =
        RunToolCallOutputItem.builder()
            .toolCallId("call_123")
            .result(null)
            .error(Optional.of("Tool failed"))
            .build();

    List<ResponseInputItem> result =
        ConversionUtils.convertToResponseInputItems(List.of(toolOutput));

    assertEquals(1, result.size());
    assertNotNull(result.get(0));
  }

  @Test
  void convertToResponseInputItems_messageOutput_skipped() {
    RunMessageOutputItem message =
        RunMessageOutputItem.builder().content("Assistant response").role("assistant").build();

    List<ResponseInputItem> result = ConversionUtils.convertToResponseInputItems(List.of(message));

    assertTrue(result.isEmpty());
  }

  @Test
  void convertToResponseInputItems_mixedItems_convertsCorrectly() {
    List<Object> items =
        List.of(
            "User message",
            RunToolCallItem.builder().id("call_1").name("calculator").parameters(null).build(),
            RunToolCallOutputItem.builder()
                .toolCallId("call_1")
                .result(42)
                .error(Optional.empty())
                .build(),
            RunMessageOutputItem.builder().content("Assistant message").role("assistant").build());

    List<ResponseInputItem> result = ConversionUtils.convertToResponseInputItems(items);

    assertEquals(3, result.size());
  }

  @Test
  void convertToResponseInputItems_multipleStrings_createsMultipleMessages() {
    List<Object> items = List.of("First message", "Second message", "Third message");

    List<ResponseInputItem> result = ConversionUtils.convertToResponseInputItems(items);

    assertEquals(3, result.size());
  }

  @Test
  void convertToResponseInputItems_toolCallWithNullParameters_handled() {
    RunToolCallItem toolCall =
        RunToolCallItem.builder().id("call_123").name("no_params_tool").parameters(null).build();

    List<ResponseInputItem> result = ConversionUtils.convertToResponseInputItems(List.of(toolCall));

    assertEquals(1, result.size());
    assertNotNull(result.get(0));
  }
}
