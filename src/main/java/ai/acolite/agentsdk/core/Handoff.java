package ai.acolite.agentsdk.core;

import ai.acolite.agentsdk.core.types.AgentOutputType;
import java.util.function.Function;

/**
 * Handoff
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/handoff.ts">handoff.ts</a>
 */
public class Handoff<TContext, TOutput extends AgentOutputType> {

  private String toolName;
  private String toolDescription;
  private JsonObjectSchema<Object> inputJsonSchema;
  private Boolean strictJsonSchema;
  private String agentName;
  private Agent<TContext, TOutput> agent;
  private Function<TContext, Boolean> isEnabled;
}
