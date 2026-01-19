package ai.acolite.agentsdk.core;

import ai.acolite.agentsdk.core.types.AgentOutputType;

/**
 * AgentHookEvents
 *
 * <p>Event types for agent lifecycle hooks.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/lifecycle.ts
 */
public interface AgentHookEvents<TContext, TOutput extends AgentOutputType>
    extends EventEmitterEvents {
  // Marker interface for agent hook events
  // Events: agent_start, agent_end, agent_handoff, agent_tool_start, agent_tool_end
}
