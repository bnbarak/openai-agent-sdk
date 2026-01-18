package com.acoliteai.agentsdk.core;

import com.acoliteai.agentsdk.core.types.AgentOutputType;

/**
 * RunHookEvents
 *
 * <p>Events for run-level hooks.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/lifecycle.ts
 */
public interface RunHookEvents<TContext, TOutput extends AgentOutputType>
    extends EventEmitterEvents {
  // Marker interface for run hook events
}
