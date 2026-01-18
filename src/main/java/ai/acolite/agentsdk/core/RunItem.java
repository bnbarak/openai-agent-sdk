package ai.acolite.agentsdk.core;

import ai.acolite.agentsdk.core.types.AgentInputItem;

/**
 * RunItem
 *
 * <p>Union of all run item types that can occur during agent execution. In TypeScript:
 * RunMessageOutputItem | RunToolCallItem | RunReasoningItem | etc.
 *
 * <p>RunItems are also AgentInputItems because they can be used as input for subsequent agent turns
 * (conversation history).
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/items.ts
 */
public interface RunItem extends AgentInputItem {
  // Marker interface for all run items
  // Implementations already exist: RunMessageOutputItem, RunToolCallItem, etc.
}
