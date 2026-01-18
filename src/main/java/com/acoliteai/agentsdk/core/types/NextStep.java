package com.acoliteai.agentsdk.core.types;

/**
 * NextStep
 *
 * <p>Represents the next step in agent execution.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/runner/types.ts
 */
public enum NextStep {
  NEXT_STEP_RUN_AGAIN,
  NEXT_STEP_COMPLETE,
  NEXT_STEP_INTERRUPT
}
