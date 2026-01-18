package com.acoliteai.agentsdk.core;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * RunResultBase
 *
 * <p>Base class for run results.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/result.ts
 */
@Getter
@SuperBuilder
public class RunResultBase<TContext, TAgent> {
  List<Object> input;
  List<Object> newItems;
  List<ModelResponse> rawResponses;
  String lastResponseId;
  TAgent lastAgent;
  Usage usage;
}
