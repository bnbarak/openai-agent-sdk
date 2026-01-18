package com.acoliteai.agentsdk.core;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * RunResult
 *
 * <p>Result from running an agent.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/result.ts
 */
@Getter
@Builder
public class RunResult<TContext, TAgent> {
  // Fields from RunResultBase
  List<Object> input;
  List<Object> newItems;
  List<ModelResponse> rawResponses;
  String lastResponseId;
  TAgent lastAgent;
  Usage usage;

  // RunResult-specific field
  Object finalOutput;
}
