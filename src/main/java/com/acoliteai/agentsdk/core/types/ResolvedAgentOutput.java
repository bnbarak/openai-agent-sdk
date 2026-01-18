package com.acoliteai.agentsdk.core.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ResolvedAgentOutput
 *
 * <p>Resolved output from an agent based on its output type.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/types/helpers.ts
 */
@Getter
@AllArgsConstructor
public class ResolvedAgentOutput<TOutput> {
  private TOutput output;
}
