package com.acoliteai.agentsdk.core;

import com.acoliteai.agentsdk.core.types.AgentOutputType;

/**
 * OutputGuardrailDefinition
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/guardrail.ts">guardrail.ts</a>
 */
public interface OutputGuardrailDefinition<TMeta, TOutput extends AgentOutputType>
    extends OutputGuardrailMetadata {}
