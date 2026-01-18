package com.acoliteai.agentsdk.core;

/**
 * ToolUseBehavior
 *
 * <p>Defines how tool usage is handled. In TypeScript: 'run_llm_again' | 'stop_on_first_tool' | {
 * stopAtToolNames: string[] } | Function
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/agent.ts
 */
public interface ToolUseBehavior {
  // Marker interface
  // Implementations: RunLlmAgain, StopOnFirstTool, StopAtToolNames, etc.
}
