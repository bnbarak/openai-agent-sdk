package com.acoliteai.agentsdk.core;

import java.util.List;

/**
 * ApprovalRecord
 *
 * <p>Records approval/rejection status for tools.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/runContext.ts
 */
public class ApprovalRecord {
  private Object approved; // boolean | string[]
  private Object rejected; // boolean | string[]

  public ApprovalRecord() {
    this.approved = false;
    this.rejected = false;
  }

  public Object getApproved() {
    return approved;
  }

  public void setApproved(boolean approved) {
    this.approved = approved;
  }

  public void setApproved(List<String> approvedIds) {
    this.approved = approvedIds;
  }

  public Object getRejected() {
    return rejected;
  }

  public void setRejected(boolean rejected) {
    this.rejected = rejected;
  }

  public void setRejected(List<String> rejectedIds) {
    this.rejected = rejectedIds;
  }
}
