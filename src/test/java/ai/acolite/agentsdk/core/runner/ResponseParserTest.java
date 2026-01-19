package ai.acolite.agentsdk.core.runner;

import static org.junit.jupiter.api.Assertions.*;

import ai.acolite.agentsdk.core.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ResponseParserTest {

  @Test
  void parseResponseItems_withStringOutput_createsMessageItem() {
    ModelResponse response =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of("Hello, world!"))
            .responseId(Optional.empty())
            .providerData(Optional.empty())
            .build();

    List<RunItem> items = ResponseParser.parseResponseItems(response);

    assertEquals(1, items.size());
    assertInstanceOf(RunMessageOutputItem.class, items.get(0));
    RunMessageOutputItem message = (RunMessageOutputItem) items.get(0);
    assertEquals("Hello, world!", message.getContent());
    assertEquals("assistant", message.getRole());
  }

  @Test
  void parseResponseItems_withMultipleStrings_createsMultipleItems() {
    ModelResponse response =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of("First message", "Second message"))
            .responseId(Optional.empty())
            .providerData(Optional.empty())
            .build();

    List<RunItem> items = ResponseParser.parseResponseItems(response);

    assertEquals(2, items.size());
    assertInstanceOf(RunMessageOutputItem.class, items.get(0));
    assertInstanceOf(RunMessageOutputItem.class, items.get(1));
    assertEquals("First message", ((RunMessageOutputItem) items.get(0)).getContent());
    assertEquals("Second message", ((RunMessageOutputItem) items.get(1)).getContent());
  }

  @Test
  void parseResponseItems_withEmptyOutput_returnsEmptyList() {
    ModelResponse response =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(List.of())
            .responseId(Optional.empty())
            .providerData(Optional.empty())
            .build();

    List<RunItem> items = ResponseParser.parseResponseItems(response);

    assertTrue(items.isEmpty());
  }

  @Test
  void parseResponseItems_withNullOutput_returnsEmptyList() {
    ModelResponse response =
        ModelResponse.builder()
            .usage(Usage.empty())
            .output(null)
            .responseId(Optional.empty())
            .providerData(Optional.empty())
            .build();

    List<RunItem> items = ResponseParser.parseResponseItems(response);

    assertTrue(items.isEmpty());
  }

  @Test
  void extractFinalOutput_withMessageItems_returnsLastMessage() {
    List<RunItem> items =
        List.of(
            RunMessageOutputItem.builder().content("First message").role("assistant").build(),
            RunMessageOutputItem.builder().content("Final message").role("assistant").build());

    Object output = ResponseParser.extractFinalOutput(items);

    assertEquals("Final message", output);
  }

  @Test
  void extractFinalOutput_withMixedItems_returnsLastMessage() {
    List<RunItem> items =
        List.of(
            RunMessageOutputItem.builder().content("First message").role("assistant").build(),
            RunToolCallItem.builder().id("call_123").name("calculator").parameters(null).build(),
            RunMessageOutputItem.builder().content("Final message").role("assistant").build());

    Object output = ResponseParser.extractFinalOutput(items);

    assertEquals("Final message", output);
  }

  @Test
  void extractFinalOutput_withNoMessages_returnsEmpty() {
    List<RunItem> items =
        List.of(
            RunToolCallItem.builder().id("call_123").name("calculator").parameters(null).build());

    Object output = ResponseParser.extractFinalOutput(items);

    assertNull(output);
  }

  @Test
  void extractFinalOutput_withEmptyList_returnsNull() {
    Object output = ResponseParser.extractFinalOutput(List.of());

    assertNull(output);
  }

  @Test
  void extractFinalOutput_withNull_returnsNull() {
    Object output = ResponseParser.extractFinalOutput(null);

    assertNull(output);
  }
}
