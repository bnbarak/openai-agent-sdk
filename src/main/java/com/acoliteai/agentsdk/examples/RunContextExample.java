package com.acoliteai.agentsdk.examples;

import com.acoliteai.agentsdk.core.RunContext;
import com.acoliteai.agentsdk.core.RunToolApprovalItem;
import com.acoliteai.agentsdk.core.Usage;
import com.acoliteai.agentsdk.core.types.UnknownContext;

/**
 * RunContextExample demonstrates the three primary use cases of RunContext: 1. User Context Storage
 * - Passing application data to tools 2. Usage Tracking - Accumulating token costs across API calls
 * 3. Tool Approval Management - Controlling which tools can execute
 *
 * <p>This example shows the basic patterns without requiring a real agent or API calls.
 */
public class RunContextExample {

  public static void main(String[] args) {
    System.out.println("=== RunContext Examples ===\n");

    example1_BasicContextStorage();
    example2_UsageTracking();
    example3_ToolApprovalPerCall();
    example4_ToolApprovalPermanent();
    example5_ToolRejection();
    example6_MixedApprovalModes();
    example7_Serialization();
  }

  /**
   * Example 1: Basic Context Storage Shows how to create a context with custom application data.
   */
  static void example1_BasicContextStorage() {
    System.out.println("--- Example 1: Basic Context Storage ---");

    AppContext appContext = new AppContext();
    appContext.userId = "user_123";
    appContext.sessionId = "session_456";
    appContext.apiKey = "sk-...";

    RunContext<AppContext> context = new RunContext<>(appContext);

    System.out.println("User ID: " + context.getContext().userId);
    System.out.println("Session ID: " + context.getContext().sessionId);
    System.out.println("API Key: " + context.getContext().apiKey);
    System.out.println();
  }

  /** Example 2: Usage Tracking Shows how to accumulate token usage across multiple API calls. */
  static void example2_UsageTracking() {
    System.out.println("--- Example 2: Usage Tracking ---");

    RunContext<UnknownContext> context = new RunContext<>();

    Usage call1Usage =
        Usage.builder().inputTokens(100.0).outputTokens(50.0).totalTokens(150.0).build();
    context.addUsage(call1Usage);
    System.out.println("After call 1: " + context.getUsage().getTotalTokens() + " tokens");

    Usage call2Usage =
        Usage.builder().inputTokens(200.0).outputTokens(75.0).totalTokens(275.0).build();
    context.addUsage(call2Usage);
    System.out.println("After call 2: " + context.getUsage().getTotalTokens() + " tokens");

    Usage call3Usage =
        Usage.builder().inputTokens(150.0).outputTokens(100.0).totalTokens(250.0).build();
    context.addUsage(call3Usage);
    System.out.println("After call 3: " + context.getUsage().getTotalTokens() + " tokens");

    System.out.println("\nFinal accumulated usage:");
    System.out.println("  Input tokens: " + context.getUsage().getInputTokens());
    System.out.println("  Output tokens: " + context.getUsage().getOutputTokens());
    System.out.println("  Total tokens: " + context.getUsage().getTotalTokens());
    System.out.println();
  }

  /** Example 3: Per-Call Tool Approval Shows how to approve specific tool calls one by one. */
  static void example3_ToolApprovalPerCall() {
    System.out.println("--- Example 3: Per-Call Tool Approval ---");

    RunContext<UnknownContext> context = new RunContext<>();

    RunToolApprovalItem call1 =
        RunToolApprovalItem.builder().toolName("send_email").toolCallId("call_123").build();

    System.out.println("Before approval: " + context.isToolApproved("send_email", "call_123"));

    context.approveTool(call1);

    System.out.println(
        "After approval (call_123): " + context.isToolApproved("send_email", "call_123"));
    System.out.println(
        "Different call (call_456): " + context.isToolApproved("send_email", "call_456"));
    System.out.println();
  }

  /** Example 4: Permanent Tool Approval Shows how to approve all future calls to a tool. */
  static void example4_ToolApprovalPermanent() {
    System.out.println("--- Example 4: Permanent Tool Approval ---");

    RunContext<UnknownContext> context = new RunContext<>();

    RunToolApprovalItem approvalItem =
        RunToolApprovalItem.builder().toolName("calculator").toolCallId("call_123").build();

    context.approveTool(approvalItem, true);

    System.out.println("call_123 approved: " + context.isToolApproved("calculator", "call_123"));
    System.out.println("call_456 approved: " + context.isToolApproved("calculator", "call_456"));
    System.out.println("call_789 approved: " + context.isToolApproved("calculator", "call_789"));
    System.out.println("All future calls are automatically approved!");
    System.out.println();
  }

  /** Example 5: Tool Rejection Shows how to reject tool calls to block dangerous operations. */
  static void example5_ToolRejection() {
    System.out.println("--- Example 5: Tool Rejection ---");

    RunContext<UnknownContext> context = new RunContext<>();

    RunToolApprovalItem dangerousCall =
        RunToolApprovalItem.builder().toolName("delete_all_files").toolCallId("call_999").build();

    context.rejectTool(dangerousCall, true);

    System.out.println(
        "call_999 rejected: " + context.isToolApproved("delete_all_files", "call_999"));
    System.out.println(
        "Any call rejected: " + context.isToolApproved("delete_all_files", "call_111"));
    System.out.println("Tool is permanently blocked!");
    System.out.println();
  }

  /**
   * Example 6: Mixed Approval Modes Shows how different tools can have different approval rules.
   */
  static void example6_MixedApprovalModes() {
    System.out.println("--- Example 6: Mixed Approval Modes ---");

    RunContext<UnknownContext> context = new RunContext<>();

    RunToolApprovalItem safeToolCall =
        RunToolApprovalItem.builder().toolName("calculator").toolCallId("calc_001").build();
    context.approveTool(safeToolCall, true);

    RunToolApprovalItem emailCall =
        RunToolApprovalItem.builder().toolName("send_email").toolCallId("email_001").build();
    context.approveTool(emailCall, false);

    RunToolApprovalItem dangerousCall =
        RunToolApprovalItem.builder().toolName("delete_file").toolCallId("delete_001").build();
    context.rejectTool(dangerousCall, true);

    System.out.println("Calculator (permanent approval):");
    System.out.println("  Any call: " + context.isToolApproved("calculator", "any_id"));

    System.out.println("\nEmail (per-call approval):");
    System.out.println("  email_001: " + context.isToolApproved("send_email", "email_001"));
    System.out.println("  email_002: " + context.isToolApproved("send_email", "email_002"));

    System.out.println("\nDelete file (permanent rejection):");
    System.out.println("  Any call: " + context.isToolApproved("delete_file", "any_id"));
    System.out.println();
  }

  /** Example 7: Serialization Shows how to serialize context state for persistence or debugging. */
  static void example7_Serialization() {
    System.out.println("--- Example 7: Serialization ---");

    AppContext appContext = new AppContext();
    appContext.userId = "user_789";
    RunContext<AppContext> context = new RunContext<>(appContext);

    context.addUsage(Usage.builder().totalTokens(500.0).build());

    RunToolApprovalItem approvalItem =
        RunToolApprovalItem.builder().toolName("calculator").toolCallId("call_123").build();
    context.approveTool(approvalItem);

    var json = context.toJSON();

    System.out.println("Serialized context:");
    System.out.println("  Context: " + json.get("context"));
    System.out.println("  Usage: " + json.get("usage"));
    System.out.println("  Approvals: " + json.get("approvals"));
    System.out.println();
  }

  /** Example application context with user-specific data. */
  static class AppContext {
    String userId;
    String sessionId;
    String apiKey;

    @Override
    public String toString() {
      return "AppContext{userId='" + userId + "', sessionId='" + sessionId + "'}";
    }
  }
}
