package com.acoliteai.agentsdk.core;

import java.util.Optional;

/**
 * TraceOptions
 *
 * <p>Options for creating traces.
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing
 */
public class TraceOptions {
  private Optional<String> workflowName;
  private Optional<String> traceId;
  private Optional<String> groupId;

  public TraceOptions() {
    this.workflowName = Optional.empty();
    this.traceId = Optional.empty();
    this.groupId = Optional.empty();
  }

  // Getters and setters
}
