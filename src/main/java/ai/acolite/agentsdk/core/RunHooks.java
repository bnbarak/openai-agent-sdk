package ai.acolite.agentsdk.core;

import ai.acolite.agentsdk.core.types.AgentOutputType;

/**
 * RunHooks
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/lifecycle.ts">lifecycle.ts</a>
 */
public class RunHooks<TContext, TOutput extends AgentOutputType>
    extends EventEmitterDelegate<RunHookEvents<TContext, TOutput>> {}
