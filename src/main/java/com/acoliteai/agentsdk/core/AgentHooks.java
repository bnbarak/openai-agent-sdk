package com.acoliteai.agentsdk.core;

import com.acoliteai.agentsdk.core.types.AgentOutputType;

/**
 * AgentHooks
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/lifecycle.ts">lifecycle.ts</a>
 */
public class AgentHooks<TContext, TOutput extends AgentOutputType>
    extends EventEmitterDelegate<AgentHookEvents<TContext, TOutput>> {}
