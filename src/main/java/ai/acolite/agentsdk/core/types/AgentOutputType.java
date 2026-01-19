package ai.acolite.agentsdk.core.types;

/**
 * AgentOutputType
 *
 * <p>Represents the type of output an agent can produce. In TypeScript this is a union type: 'text'
 * | ZodObjectLike | JsonSchemaDefinition | HandoffsOutput
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/agent.ts
 */
public interface AgentOutputType {
  // Marker interface for agent output types
  // Implementations: TextOutput, JsonSchemaOutput, etc.
}
