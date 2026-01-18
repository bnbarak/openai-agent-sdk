package com.acoliteai.agentsdk.core;

import static org.junit.jupiter.api.Assertions.*;

import com.acoliteai.agentsdk.core.types.UnknownContext;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RunContextTest {

  @Test
  void constructor_withNullContext_usesUnknownContext() {
    RunContext<UnknownContext> context = new RunContext<>(null);

    assertNotNull(context.getContext());
    assertInstanceOf(UnknownContext.class, context.getContext());
  }

  @Test
  void constructor_withNoArgs_usesUnknownContext() {
    RunContext<UnknownContext> context = new RunContext<>();

    assertNotNull(context.getContext());
    assertInstanceOf(UnknownContext.class, context.getContext());
  }

  @Test
  void constructor_withCustomContext_storesContext() {
    TestContext customContext = new TestContext("user_123", "test_session");

    RunContext<TestContext> context = new RunContext<>(customContext);

    assertEquals("user_123", context.getContext().userId);
    assertEquals("test_session", context.getContext().sessionId);
  }

  @Test
  void constructor_initializesEmptyUsage() {
    RunContext<UnknownContext> context = new RunContext<>();

    Usage usage = context.getUsage();

    assertNotNull(usage);
    assertEquals(0.0, usage.getInputTokens());
    assertEquals(0.0, usage.getOutputTokens());
    assertEquals(0.0, usage.getTotalTokens());
  }

  @Test
  void constructor_initializesEmptyApprovals() {
    RunContext<UnknownContext> context = new RunContext<>();

    Boolean approved = context.isToolApproved("any_tool", "call_123");

    assertNull(approved);
  }

  @Test
  void isToolApproved_withNoRecord_returnsNull() {
    RunContext<UnknownContext> context = new RunContext<>();

    Boolean approved = context.isToolApproved("calculator", "call_123");

    assertNull(approved);
  }

  @Test
  void approveTool_withAlwaysApprove_approvesAllCalls() {
    RunContext<UnknownContext> context = new RunContext<>();
    RunToolApprovalItem item =
        RunToolApprovalItem.builder().toolName("calculator").toolCallId("call_123").build();

    context.approveTool(item, true);

    assertTrue(context.isToolApproved("calculator", "call_123"));
    assertTrue(context.isToolApproved("calculator", "call_456"));
    assertTrue(context.isToolApproved("calculator", "call_789"));
  }

  @Test
  void approveTool_withoutAlwaysApprove_approvesOnlySpecificCall() {
    RunContext<UnknownContext> context = new RunContext<>();
    RunToolApprovalItem item =
        RunToolApprovalItem.builder().toolName("calculator").toolCallId("call_123").build();

    context.approveTool(item, false);

    assertTrue(context.isToolApproved("calculator", "call_123"));
    assertNull(context.isToolApproved("calculator", "call_456"));
  }

  @Test
  void approveTool_defaultOverload_approvesOnlySpecificCall() {
    RunContext<UnknownContext> context = new RunContext<>();
    RunToolApprovalItem item =
        RunToolApprovalItem.builder().toolName("calculator").toolCallId("call_123").build();

    context.approveTool(item);

    assertTrue(context.isToolApproved("calculator", "call_123"));
    assertNull(context.isToolApproved("calculator", "call_456"));
  }

  @Test
  void approveTool_withMultipleCalls_approvesAllSpecifiedCalls() {
    RunContext<UnknownContext> context = new RunContext<>();
    RunToolApprovalItem item1 =
        RunToolApprovalItem.builder().toolName("calculator").toolCallId("call_123").build();
    RunToolApprovalItem item2 =
        RunToolApprovalItem.builder().toolName("calculator").toolCallId("call_456").build();

    context.approveTool(item1);
    context.approveTool(item2);

    assertTrue(context.isToolApproved("calculator", "call_123"));
    assertTrue(context.isToolApproved("calculator", "call_456"));
    assertNull(context.isToolApproved("calculator", "call_789"));
  }

  @Test
  void approveTool_withDuplicateCall_doesNotAddTwice() {
    RunContext<UnknownContext> context = new RunContext<>();
    RunToolApprovalItem item =
        RunToolApprovalItem.builder().toolName("calculator").toolCallId("call_123").build();
    context.approveTool(item);

    context.approveTool(item);

    assertTrue(context.isToolApproved("calculator", "call_123"));
  }

  @Test
  void rejectTool_withAlwaysReject_rejectsAllCalls() {
    RunContext<UnknownContext> context = new RunContext<>();
    RunToolApprovalItem item =
        RunToolApprovalItem.builder().toolName("dangerous_tool").toolCallId("call_123").build();

    context.rejectTool(item, true);

    assertFalse(context.isToolApproved("dangerous_tool", "call_123"));
    assertFalse(context.isToolApproved("dangerous_tool", "call_456"));
    assertFalse(context.isToolApproved("dangerous_tool", "call_789"));
  }

  @Test
  void rejectTool_withoutAlwaysReject_rejectsOnlySpecificCall() {
    RunContext<UnknownContext> context = new RunContext<>();
    RunToolApprovalItem item =
        RunToolApprovalItem.builder().toolName("dangerous_tool").toolCallId("call_123").build();

    context.rejectTool(item, false);

    assertFalse(context.isToolApproved("dangerous_tool", "call_123"));
    assertNull(context.isToolApproved("dangerous_tool", "call_456"));
  }

  @Test
  void rejectTool_defaultOverload_rejectsOnlySpecificCall() {
    RunContext<UnknownContext> context = new RunContext<>();
    RunToolApprovalItem item =
        RunToolApprovalItem.builder().toolName("dangerous_tool").toolCallId("call_123").build();

    context.rejectTool(item);

    assertFalse(context.isToolApproved("dangerous_tool", "call_123"));
    assertNull(context.isToolApproved("dangerous_tool", "call_456"));
  }

  @Test
  void rejectTool_withMultipleCalls_rejectsAllSpecifiedCalls() {
    RunContext<UnknownContext> context = new RunContext<>();
    RunToolApprovalItem item1 =
        RunToolApprovalItem.builder().toolName("dangerous_tool").toolCallId("call_123").build();
    RunToolApprovalItem item2 =
        RunToolApprovalItem.builder().toolName("dangerous_tool").toolCallId("call_456").build();

    context.rejectTool(item1);
    context.rejectTool(item2);

    assertFalse(context.isToolApproved("dangerous_tool", "call_123"));
    assertFalse(context.isToolApproved("dangerous_tool", "call_456"));
    assertNull(context.isToolApproved("dangerous_tool", "call_789"));
  }

  @Test
  void approveAndReject_withDifferentTools_maintainsSeparateRecords() {
    RunContext<UnknownContext> context = new RunContext<>();
    RunToolApprovalItem approveItem =
        RunToolApprovalItem.builder().toolName("calculator").toolCallId("call_123").build();
    RunToolApprovalItem rejectItem =
        RunToolApprovalItem.builder().toolName("file_delete").toolCallId("call_456").build();

    context.approveTool(approveItem, true);
    context.rejectTool(rejectItem, true);

    assertTrue(context.isToolApproved("calculator", "call_123"));
    assertFalse(context.isToolApproved("file_delete", "call_456"));
  }

  @Test
  void addUsage_accumulatesTokens() {
    RunContext<UnknownContext> context = new RunContext<>();
    Usage usage1 = Usage.builder().inputTokens(100.0).outputTokens(50.0).totalTokens(150.0).build();
    Usage usage2 = Usage.builder().inputTokens(200.0).outputTokens(75.0).totalTokens(275.0).build();

    context.addUsage(usage1);
    context.addUsage(usage2);

    assertEquals(300.0, context.getUsage().getInputTokens());
    assertEquals(125.0, context.getUsage().getOutputTokens());
    assertEquals(425.0, context.getUsage().getTotalTokens());
  }

  @Test
  void addUsage_withNullUsage_doesNotThrow() {
    RunContext<UnknownContext> context = new RunContext<>();
    Usage initialUsage = context.getUsage();

    context.addUsage(null);

    assertEquals(initialUsage.getInputTokens(), context.getUsage().getInputTokens());
  }

  @Test
  void addUsage_withMultipleCalls_accumulatesCorrectly() {
    RunContext<UnknownContext> context = new RunContext<>();
    Usage usage1 = Usage.builder().inputTokens(10.0).outputTokens(5.0).build();
    Usage usage2 = Usage.builder().inputTokens(20.0).outputTokens(10.0).build();
    Usage usage3 = Usage.builder().inputTokens(30.0).outputTokens(15.0).build();

    context.addUsage(usage1);
    context.addUsage(usage2);
    context.addUsage(usage3);

    assertEquals(60.0, context.getUsage().getInputTokens());
    assertEquals(30.0, context.getUsage().getOutputTokens());
  }

  @Test
  void toJSON_containsContextUsageAndApprovals() {
    RunContext<TestContext> context = new RunContext<>(new TestContext("user_123", "session_456"));
    context.addUsage(Usage.builder().totalTokens(500.0).build());

    Map<String, Object> json = context.toJSON();

    assertNotNull(json.get("context"));
    assertNotNull(json.get("usage"));
    assertNotNull(json.get("approvals"));
  }

  @Test
  void toJSON_withApprovals_includesApprovalRecords() {
    RunContext<UnknownContext> context = new RunContext<>();
    RunToolApprovalItem item =
        RunToolApprovalItem.builder().toolName("calculator").toolCallId("call_123").build();
    context.approveTool(item);

    Map<String, Object> json = context.toJSON();

    @SuppressWarnings("unchecked")
    Map<String, ApprovalRecord> approvals = (Map<String, ApprovalRecord>) json.get("approvals");
    assertNotNull(approvals.get("calculator"));
  }

  @Test
  void toJSON_withCustomContext_preservesContextData() {
    TestContext customContext = new TestContext("user_789", "session_abc");
    RunContext<TestContext> context = new RunContext<>(customContext);

    Map<String, Object> json = context.toJSON();

    TestContext retrievedContext = (TestContext) json.get("context");
    assertEquals("user_789", retrievedContext.userId);
    assertEquals("session_abc", retrievedContext.sessionId);
  }

  @Test
  void rebuildApprovals_replacesExistingApprovals() {
    RunContext<UnknownContext> context = new RunContext<>();
    RunToolApprovalItem item1 =
        RunToolApprovalItem.builder().toolName("tool1").toolCallId("call_123").build();
    context.approveTool(item1);
    ApprovalRecord record = new ApprovalRecord();
    record.setApproved(true);
    Map<String, ApprovalRecord> newApprovals = Map.of("tool2", record);

    context.rebuildApprovals(newApprovals);

    assertNull(context.isToolApproved("tool1", "call_123"));
    assertTrue(context.isToolApproved("tool2", "call_456"));
  }

  @Test
  void rebuildApprovals_withNullMap_clearsApprovals() {
    RunContext<UnknownContext> context = new RunContext<>();
    RunToolApprovalItem item =
        RunToolApprovalItem.builder().toolName("calculator").toolCallId("call_123").build();
    context.approveTool(item);

    context.rebuildApprovals(null);

    assertNull(context.isToolApproved("calculator", "call_123"));
  }

  @Test
  void rebuildApprovals_withEmptyMap_clearsApprovals() {
    RunContext<UnknownContext> context = new RunContext<>();
    RunToolApprovalItem item =
        RunToolApprovalItem.builder().toolName("calculator").toolCallId("call_123").build();
    context.approveTool(item);

    context.rebuildApprovals(Map.of());

    assertNull(context.isToolApproved("calculator", "call_123"));
  }

  @Test
  void isToolApproved_withMixedApprovalModes_respectsBothModes() {
    RunContext<UnknownContext> context = new RunContext<>();
    RunToolApprovalItem item1 =
        RunToolApprovalItem.builder().toolName("tool1").toolCallId("call_123").build();
    RunToolApprovalItem item2 =
        RunToolApprovalItem.builder().toolName("tool2").toolCallId("call_456").build();
    context.approveTool(item1, false);
    context.approveTool(item2, true);

    Boolean tool1Call123 = context.isToolApproved("tool1", "call_123");
    Boolean tool1Call999 = context.isToolApproved("tool1", "call_999");
    Boolean tool2Call456 = context.isToolApproved("tool2", "call_456");
    Boolean tool2Call999 = context.isToolApproved("tool2", "call_999");

    assertTrue(tool1Call123);
    assertNull(tool1Call999);
    assertTrue(tool2Call456);
    assertTrue(tool2Call999);
  }

  static class TestContext {
    String userId;
    String sessionId;

    TestContext(String userId, String sessionId) {
      this.userId = userId;
      this.sessionId = sessionId;
    }
  }
}
