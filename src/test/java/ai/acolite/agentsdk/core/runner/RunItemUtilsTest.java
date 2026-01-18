package ai.acolite.agentsdk.core.runner;

import static org.junit.jupiter.api.Assertions.*;

import ai.acolite.agentsdk.core.RunItem;
import ai.acolite.agentsdk.core.RunMessageOutputItem;
import ai.acolite.agentsdk.core.RunToolCallItem;
import ai.acolite.agentsdk.core.RunToolCallOutputItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for RunItemUtils static utility methods.
 *
 * <p>Tests the logic extracted from RunState for analyzing conversation items, particularly around
 * tool call tracking.
 */
class RunItemUtilsTest {

  @Test
  void hasPendingToolCalls_emptyList_returnsFalse() {
    List<RunItem> items = List.of();

    boolean result = RunItemUtils.hasPendingToolCalls(items);

    assertFalse(result);
  }

  @Test
  void hasPendingToolCalls_noToolCalls_returnsFalse() {
    List<RunItem> items =
        List.of(RunMessageOutputItem.builder().content("Hello").role("assistant").build());

    boolean result = RunItemUtils.hasPendingToolCalls(items);

    assertFalse(result);
  }

  @Test
  void hasPendingToolCalls_toolCallWithOutput_returnsFalse() {
    List<RunItem> items =
        List.of(
            RunToolCallItem.builder().id("call_123").name("calculator").parameters(null).build(),
            RunToolCallOutputItem.builder()
                .toolCallId("call_123")
                .result(42)
                .error(Optional.empty())
                .build());

    boolean result = RunItemUtils.hasPendingToolCalls(items);

    assertFalse(result);
  }

  @Test
  void hasPendingToolCalls_toolCallWithoutOutput_returnsTrue() {
    List<RunItem> items =
        List.of(
            RunToolCallItem.builder().id("call_123").name("calculator").parameters(null).build());

    boolean result = RunItemUtils.hasPendingToolCalls(items);

    assertTrue(result);
  }

  @Test
  void hasPendingToolCalls_multipleToolCallsAllHaveOutputs_returnsFalse() {
    List<RunItem> items =
        List.of(
            RunToolCallItem.builder().id("call_1").name("calculator").parameters(null).build(),
            RunToolCallItem.builder().id("call_2").name("weather").parameters(null).build(),
            RunToolCallOutputItem.builder()
                .toolCallId("call_1")
                .result(42)
                .error(Optional.empty())
                .build(),
            RunToolCallOutputItem.builder()
                .toolCallId("call_2")
                .result("Sunny")
                .error(Optional.empty())
                .build());

    boolean result = RunItemUtils.hasPendingToolCalls(items);

    assertFalse(result);
  }

  @Test
  void hasPendingToolCalls_onePendingOutOfTwo_returnsTrue() {
    List<RunItem> items =
        List.of(
            RunToolCallItem.builder().id("call_1").name("calculator").parameters(null).build(),
            RunToolCallItem.builder().id("call_2").name("weather").parameters(null).build(),
            RunToolCallOutputItem.builder()
                .toolCallId("call_1")
                .result(42)
                .error(Optional.empty())
                .build());

    boolean result = RunItemUtils.hasPendingToolCalls(items);

    assertTrue(result);
  }

  @Test
  void hasPendingToolCalls_mixedItemTypes_correctlyIdentifiesPending() {
    List<RunItem> items =
        List.of(
            RunMessageOutputItem.builder()
                .content("Let me calculate that")
                .role("assistant")
                .build(),
            RunToolCallItem.builder().id("call_123").name("calculator").parameters(null).build(),
            RunMessageOutputItem.builder().content("Thinking...").role("assistant").build());

    boolean result = RunItemUtils.hasPendingToolCalls(items);

    assertTrue(result);
  }

  @Test
  void hasToolCallOutput_emptyList_returnsFalse() {
    List<RunItem> items = List.of();

    boolean result = RunItemUtils.hasToolCallOutput(items, "call_123");

    assertFalse(result);
  }

  @Test
  void hasToolCallOutput_noMatchingOutput_returnsFalse() {
    List<RunItem> items =
        List.of(
            RunToolCallOutputItem.builder()
                .toolCallId("call_999")
                .result(42)
                .error(Optional.empty())
                .build());

    boolean result = RunItemUtils.hasToolCallOutput(items, "call_123");

    assertFalse(result);
  }

  @Test
  void hasToolCallOutput_matchingOutput_returnsTrue() {
    List<RunItem> items =
        List.of(
            RunToolCallOutputItem.builder()
                .toolCallId("call_123")
                .result(42)
                .error(Optional.empty())
                .build());

    boolean result = RunItemUtils.hasToolCallOutput(items, "call_123");

    assertTrue(result);
  }

  @Test
  void hasToolCallOutput_multipleOutputs_findsCorrectOne() {
    List<RunItem> items =
        List.of(
            RunToolCallOutputItem.builder()
                .toolCallId("call_1")
                .result(42)
                .error(Optional.empty())
                .build(),
            RunToolCallOutputItem.builder()
                .toolCallId("call_2")
                .result("result")
                .error(Optional.empty())
                .build(),
            RunToolCallOutputItem.builder()
                .toolCallId("call_3")
                .result(true)
                .error(Optional.empty())
                .build());

    boolean hasCall2 = RunItemUtils.hasToolCallOutput(items, "call_2");
    boolean hasCall999 = RunItemUtils.hasToolCallOutput(items, "call_999");

    assertTrue(hasCall2);
    assertFalse(hasCall999);
  }

  @Test
  void hasToolCallOutput_mixedItemTypes_onlyChecksOutputs() {
    List<RunItem> items =
        List.of(
            RunMessageOutputItem.builder().content("Hello").role("assistant").build(),
            RunToolCallItem.builder().id("call_123").name("calculator").parameters(null).build(),
            RunToolCallOutputItem.builder()
                .toolCallId("call_123")
                .result(42)
                .error(Optional.empty())
                .build());

    boolean result = RunItemUtils.hasToolCallOutput(items, "call_123");

    assertTrue(result);
  }

  @Test
  void hasPendingToolCalls_mutableList_worksCorrectly() {
    List<RunItem> items = new ArrayList<>();
    items.add(RunToolCallItem.builder().id("call_1").name("calculator").parameters(null).build());

    boolean pendingBefore = RunItemUtils.hasPendingToolCalls(items);
    assertTrue(pendingBefore);

    items.add(
        RunToolCallOutputItem.builder()
            .toolCallId("call_1")
            .result(42)
            .error(Optional.empty())
            .build());

    boolean pendingAfter = RunItemUtils.hasPendingToolCalls(items);
    assertFalse(pendingAfter);
  }

  @Test
  void hasPendingToolCalls_outputBeforeCall_stillFindsMatch() {
    List<RunItem> items =
        List.of(
            RunToolCallOutputItem.builder()
                .toolCallId("call_123")
                .result(42)
                .error(Optional.empty())
                .build(),
            RunToolCallItem.builder().id("call_123").name("calculator").parameters(null).build());

    boolean result = RunItemUtils.hasPendingToolCalls(items);

    assertFalse(result);
  }
}
