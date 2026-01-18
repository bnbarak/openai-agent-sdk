package ai.acolite.agentsdk.core;

import java.util.List;

/**
 * ProcessedResponse
 *
 * <p>Processed model response with structured data.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/runner/types.ts
 */
public class ProcessedResponse<TContext> {
  private List<Object> newItems;
  private List<Object> handoffs;
  private List<Object> functions;
  private List<Object> computerActions;
  private List<Object> shellActions;
  private List<Object> applyPatchActions;
  private List<Object> mcpApprovalRequests;
  private List<String> toolsUsed;

  public ProcessedResponse() {}

  public boolean hasToolsOrApprovalsToRun() {
    // TODO: Implement logic
    return false;
  }

  // Getters and setters would go here
}
