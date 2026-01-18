package com.acoliteai.agentsdk.core;

import com.acoliteai.agentsdk.core.types.UnknownContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;

/**
 * RunContext manages execution state for agent runs.
 *
 * <p>This class serves three primary purposes:
 *
 * <ol>
 *   <li><b>User Context Storage</b> - Holds application-specific data (TContext) that tools need to
 *       access, such as user IDs, database connections, auth tokens, etc.
 *   <li><b>Usage Tracking</b> - Accumulates token usage statistics across all API calls in a
 *       conversation for cost monitoring and billing.
 *   <li><b>Tool Approval Management</b> - Provides a safety mechanism to approve or reject tool
 *       calls before execution, supporting both per-call and permanent approval modes.
 * </ol>
 *
 * <p><b>Example Usage:</b>
 *
 * <pre>{@code
 * // Create context with app-specific data
 * MyAppContext appContext = new MyAppContext();
 * appContext.userId = "user_123";
 * appContext.database = myDatabase;
 *
 * RunContext<MyAppContext> context = new RunContext<>(appContext);
 *
 * // Check tool approval
 * Boolean approved = context.isToolApproved("send_email", "call_123");
 * if (approved == null) {
 *     // Pending approval - pause execution
 * } else if (approved) {
 *     // Execute tool
 *     tool.invoke(context, parameters);
 * }
 *
 * // Track usage
 * context.addUsage(modelResponse.getUsage());
 * System.out.println("Total tokens: " + context.getUsage().getTotalTokens());
 * }</pre>
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/runContext.ts">runContext.ts</a>
 *
 * @param <TContext> The type of user-provided context object
 */
@Getter
public class RunContext<TContext> {

  private final TContext context;
  private Usage usage;
  private final Map<String, ApprovalRecord> approvals;

  /** Creates a RunContext with default (unknown) context. */
  public RunContext() {
    this(null);
  }

  /**
   * Creates a RunContext with the specified user context.
   *
   * @param context Application-specific context object, or null for default
   */
  @SuppressWarnings("unchecked")
  public RunContext(TContext context) {
    this.context = context != null ? context : (TContext) new UnknownContext();
    this.usage = Usage.empty();
    this.approvals = new ConcurrentHashMap<>();
  }

  /**
   * Checks if a tool call has been approved for execution.
   *
   * <p>This method supports three approval states:
   *
   * <ul>
   *   <li><b>true</b> - Tool call is approved and can execute
   *   <li><b>false</b> - Tool call is rejected and should be blocked
   *   <li><b>null</b> - No decision yet, execution should pause for user approval
   * </ul>
   *
   * <p>Approval can be either:
   *
   * <ul>
   *   <li><b>Permanent</b> - All calls to this tool are approved/rejected
   *   <li><b>Per-call</b> - Only specific call IDs are approved/rejected
   * </ul>
   *
   * @param toolName The name of the tool being checked
   * @param callId The unique identifier for this specific tool call
   * @return true if approved, false if rejected, null if pending decision
   */
  public Boolean isToolApproved(String toolName, String callId) {
    ApprovalRecord record = approvals.get(toolName);
    if (record == null) {
      return null;
    }

    if (Boolean.TRUE.equals(record.getApproved())) {
      return true;
    }

    if (Boolean.TRUE.equals(record.getRejected())) {
      return false;
    }

    if (record.getApproved() instanceof List) {
      @SuppressWarnings("unchecked")
      List<String> approvedIds = (List<String>) record.getApproved();
      if (approvedIds.contains(callId)) {
        return true;
      }
    }

    if (record.getRejected() instanceof List) {
      @SuppressWarnings("unchecked")
      List<String> rejectedIds = (List<String>) record.getRejected();
      if (rejectedIds.contains(callId)) {
        return false;
      }
    }

    return null;
  }

  /**
   * Approves a tool call for execution.
   *
   * <p>This method provides two modes:
   *
   * <ul>
   *   <li><b>Per-call approval</b> (alwaysApprove = false) - Only approves this specific call ID
   *   <li><b>Permanent approval</b> (alwaysApprove = true) - Approves all future calls to this tool
   * </ul>
   *
   * @param approvalItem The tool approval request containing tool name and call ID
   * @param alwaysApprove If true, permanently approves all calls to this tool
   */
  public void approveTool(RunToolApprovalItem approvalItem, boolean alwaysApprove) {
    String toolName = approvalItem.getToolName();

    if (alwaysApprove) {
      ApprovalRecord record = new ApprovalRecord();
      record.setApproved(true);
      record.setRejected(new ArrayList<>());
      approvals.put(toolName, record);
      return;
    }

    ApprovalRecord record =
        approvals.computeIfAbsent(
            toolName,
            k -> {
              ApprovalRecord r = new ApprovalRecord();
              r.setApproved(new ArrayList<>());
              r.setRejected(new ArrayList<>());
              return r;
            });

    if (record.getApproved() instanceof List) {
      @SuppressWarnings("unchecked")
      List<String> approvedIds = (List<String>) record.getApproved();
      if (!approvedIds.contains(approvalItem.getToolCallId())) {
        approvedIds.add(approvalItem.getToolCallId());
      }
    }
  }

  /**
   * Approves a tool call for execution (per-call mode).
   *
   * <p>This is a convenience method that approves only the specific call ID. Use {@link
   * #approveTool(RunToolApprovalItem, boolean)} for permanent approval.
   *
   * @param approvalItem The tool approval request
   */
  public void approveTool(RunToolApprovalItem approvalItem) {
    approveTool(approvalItem, false);
  }

  /**
   * Rejects a tool call, preventing its execution.
   *
   * <p>This method provides two modes:
   *
   * <ul>
   *   <li><b>Per-call rejection</b> (alwaysReject = false) - Only rejects this specific call ID
   *   <li><b>Permanent rejection</b> (alwaysReject = true) - Rejects all future calls to this tool
   * </ul>
   *
   * @param approvalItem The tool approval request containing tool name and call ID
   * @param alwaysReject If true, permanently rejects all calls to this tool
   */
  public void rejectTool(RunToolApprovalItem approvalItem, boolean alwaysReject) {
    String toolName = approvalItem.getToolName();

    if (alwaysReject) {
      ApprovalRecord record = new ApprovalRecord();
      record.setApproved(false);
      record.setRejected(true);
      approvals.put(toolName, record);
      return;
    }

    ApprovalRecord record =
        approvals.computeIfAbsent(
            toolName,
            k -> {
              ApprovalRecord r = new ApprovalRecord();
              r.setApproved(new ArrayList<>());
              r.setRejected(new ArrayList<>());
              return r;
            });

    if (record.getRejected() instanceof List) {
      @SuppressWarnings("unchecked")
      List<String> rejectedIds = (List<String>) record.getRejected();
      if (!rejectedIds.contains(approvalItem.getToolCallId())) {
        rejectedIds.add(approvalItem.getToolCallId());
      }
    }
  }

  /**
   * Rejects a tool call (per-call mode).
   *
   * <p>This is a convenience method that rejects only the specific call ID. Use {@link
   * #rejectTool(RunToolApprovalItem, boolean)} for permanent rejection.
   *
   * @param approvalItem The tool approval request
   */
  public void rejectTool(RunToolApprovalItem approvalItem) {
    rejectTool(approvalItem, false);
  }

  /**
   * Adds usage statistics from a model response to the accumulated total.
   *
   * <p>This method is called after each API call to track token consumption across the entire
   * conversation. The usage is immutable - each call creates a new Usage instance with summed
   * values.
   *
   * @param newUsage The usage statistics to add
   */
  public void addUsage(Usage newUsage) {
    this.usage = this.usage.add(newUsage);
  }

  /**
   * Serializes the context state to a map for persistence or debugging.
   *
   * <p>The returned map contains:
   *
   * <ul>
   *   <li><b>context</b> - The user-provided context object
   *   <li><b>usage</b> - Accumulated token usage statistics
   *   <li><b>approvals</b> - Tool approval/rejection records
   * </ul>
   *
   * @return Map representation of the context state
   */
  public Map<String, Object> toJSON() {
    Map<String, Object> json = new HashMap<>();
    json.put("context", context);
    json.put("usage", usage);
    json.put("approvals", approvals);
    return json;
  }

  /**
   * Rebuilds approval records from deserialized state.
   *
   * <p>This method is used when resuming a run from saved state. It clears existing approvals and
   * replaces them with the provided map.
   *
   * @param approvalsMap The approval records to restore
   */
  public void rebuildApprovals(Map<String, ApprovalRecord> approvalsMap) {
    this.approvals.clear();
    if (approvalsMap != null) {
      this.approvals.putAll(approvalsMap);
    }
  }
}
