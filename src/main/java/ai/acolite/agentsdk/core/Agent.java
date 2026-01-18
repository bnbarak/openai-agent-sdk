package ai.acolite.agentsdk.core;

import ai.acolite.agentsdk.core.types.AgentOutputType;
import ai.acolite.agentsdk.core.types.JsonSchemaOutput;
import ai.acolite.agentsdk.core.types.ResolvedAgentOutput;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.exceptions.ModelBehaviorError;
import ai.acolite.agentsdk.exceptions.NotImplementedException;
import ai.acolite.agentsdk.openai.SerializationUtils;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Agent
 *
 * <p>Represents an AI agent with configuration and behavior.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/agent.ts">agent.ts</a>
 */
@Getter
@Builder
public class Agent<TContext, TOutput extends AgentOutputType> extends AgentHooks<TContext, TOutput>
    implements AgentConfiguration<TContext, TOutput> {

  /** The name of the agent (required) */
  @NonNull private final String name;

  /** Instructions for the agent's behavior (defaults to empty string) */
  @Builder.Default private final String instructions = "";

  /** The model to use (defaults to "gpt-4.1" or OPENAI_MODEL env var) */
  @Builder.Default private final String model = getDefaultModel();

  /** Model-specific settings (temperature, max tokens, etc.) */
  private final ModelSettings modelSettings;

  /** Output type for the agent */
  private final TOutput outputType;

  /** Description for handoff scenarios (when other agents hand off to this one) */
  private final String handoffDescription;

  /**
   * List of agents this agent can hand off to (agents as tools - following TypeScript SDK pattern)
   */
  private final List<Agent<TContext, ?>> handoffs;

  // Additional fields for future implementation
  // Tools use wildcard to allow any context type
  private final List<Tool<?>> tools;
  private final List<MCPServer> mcpServers;
  private final List<InputGuardrail<TContext>> inputGuardrails;
  private final List<OutputGuardrail<TContext, TOutput>> outputGuardrails;
  private final List<ToolInputGuardrail<TContext>> toolInputGuardrails;
  private final List<ToolOutputGuardrail<TContext>> toolOutputGuardrails;
  private final ToolUseBehavior toolUseBehavior;
  private final Boolean resetToolChoice;

  /**
   * Checks if the agent has an explicit tool configuration
   *
   * @return true if tools are explicitly configured
   */
  public Boolean hasExplicitToolConfig() {
    throw new NotImplementedException("Not yet implemented");
  }

  /**
   * Processes the final output string into a resolved agent output
   *
   * @param output The output string to process
   * @return ResolvedAgentOutput wrapping the output type
   */
  public ResolvedAgentOutput<Object> processFinalOutput(Object output) {
    if (output == null) {
      return new ResolvedAgentOutput<>(null);
    }

    if (outputType == null || outputType instanceof TextOutput) {
      return new ResolvedAgentOutput<>(output);
    }

    if (outputType instanceof JsonSchemaOutput<?>) {
      JsonSchemaOutput<?> schemaOutput = (JsonSchemaOutput<?>) outputType;
      if (schemaOutput.getTargetClass().isInstance(output)) {
        return new ResolvedAgentOutput<>(output);
      }

      if (output instanceof String) {
        Object parsed =
            SerializationUtils.deserializeFromJson((String) output, schemaOutput.getTargetClass());
        return new ResolvedAgentOutput<>(parsed);
      }
    }

    if (output instanceof String) {
      try {
        Object parsed = SerializationUtils.deserializeFromJson((String) output, Object.class);
        return new ResolvedAgentOutput<>(parsed);
      } catch (RuntimeException e) {
        throw new ModelBehaviorError("Failed to parse output as JSON", e);
      }
    }

    return new ResolvedAgentOutput<>(output);
  }

  /**
   * Gets the list of enabled handoffs for this agent. In the future, this could filter based on
   * isEnabled predicates.
   *
   * @return List of agents this agent can hand off to
   */
  public List<Agent<TContext, ?>> getEnabledHandoffs() {
    return handoffs != null ? handoffs : List.of();
  }

  /**
   * Gets the default model from environment variable or fallback
   *
   * @return Default model name
   */
  private static String getDefaultModel() {
    String envModel = System.getenv("OPENAI_MODEL");
    return envModel != null && !envModel.isEmpty() ? envModel : "gpt-4.1";
  }
}
