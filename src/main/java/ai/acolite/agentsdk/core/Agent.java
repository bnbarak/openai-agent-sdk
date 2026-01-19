package ai.acolite.agentsdk.core;

import ai.acolite.agentsdk.core.types.AgentOutputType;
import ai.acolite.agentsdk.core.types.JsonSchemaOutput;
import ai.acolite.agentsdk.core.types.ResolvedAgentOutput;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.exceptions.ModelBehaviorError;
import ai.acolite.agentsdk.exceptions.NotImplementedException;
import ai.acolite.agentsdk.openai.SerializationUtils;
import java.util.List;
import lombok.Getter;

/**
 * Agent
 *
 * <p>Represents an AI agent with configuration and behavior.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/agent.ts">agent.ts</a>
 */
@Getter
public class Agent<TContext, TOutput extends AgentOutputType> extends AgentHooks<TContext, TOutput>
    implements AgentConfiguration<TContext, TOutput> {

  /** The name of the agent (required) */
  private String name;

  /** Instructions for the agent's behavior (defaults to empty string) */
  private String instructions = "";

  /** The model to use (defaults to "gpt-4.1" or OPENAI_MODEL env var) */
  private String model = getDefaultModel();

  /** Model-specific settings (temperature, max tokens, etc.) */
  private ModelSettings modelSettings;

  /** Output type for the agent */
  private TOutput outputType;

  /** Description for handoff scenarios (when other agents hand off to this one) */
  private String handoffDescription;

  /**
   * List of agents this agent can hand off to (agents as tools - following TypeScript SDK pattern)
   */
  private List<Agent<TContext, ?>> handoffs;

  // Additional fields for future implementation
  // Tools use wildcard to allow any context type
  private List<Tool<?>> tools;
  private List<MCPServer> mcpServers;
  private List<InputGuardrail<TContext>> inputGuardrails;
  private List<OutputGuardrail<TContext, TOutput>> outputGuardrails;
  private List<ToolInputGuardrail<TContext>> toolInputGuardrails;
  private List<ToolOutputGuardrail<TContext>> toolOutputGuardrails;
  private ToolUseBehavior toolUseBehavior;
  private Boolean resetToolChoice;

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

  /**
   * Creates a new builder for constructing Agent instances.
   *
   * @param <TContext> The context type
   * @param <TOutput> The output type
   * @return A new AgentBuilder instance
   */
  public static <TContext, TOutput extends AgentOutputType>
      AgentBuilder<TContext, TOutput> builder() {
    return new AgentBuilder<>();
  }

  /**
   * Builder for Agent instances with automatic tool validation.
   *
   * <p>Validates tools at build time to catch configuration errors early.
   */
  public static class AgentBuilder<TContext, TOutput extends AgentOutputType> {
    private String name;
    private String instructions = "";
    private String model = getDefaultModel();
    private ModelSettings modelSettings;
    private TOutput outputType;
    private String handoffDescription;
    private List<Agent<TContext, ?>> handoffs;
    private List<Tool<?>> tools;
    private List<MCPServer> mcpServers;
    private List<InputGuardrail<TContext>> inputGuardrails;
    private List<OutputGuardrail<TContext, TOutput>> outputGuardrails;
    private List<ToolInputGuardrail<TContext>> toolInputGuardrails;
    private List<ToolOutputGuardrail<TContext>> toolOutputGuardrails;
    private ToolUseBehavior toolUseBehavior;
    private Boolean resetToolChoice;

    public AgentBuilder<TContext, TOutput> name(String name) {
      this.name = name;
      return this;
    }

    public AgentBuilder<TContext, TOutput> instructions(String instructions) {
      this.instructions = instructions;
      return this;
    }

    public AgentBuilder<TContext, TOutput> model(String model) {
      this.model = model;
      return this;
    }

    public AgentBuilder<TContext, TOutput> modelSettings(ModelSettings modelSettings) {
      this.modelSettings = modelSettings;
      return this;
    }

    public AgentBuilder<TContext, TOutput> outputType(TOutput outputType) {
      this.outputType = outputType;
      return this;
    }

    public AgentBuilder<TContext, TOutput> handoffDescription(String handoffDescription) {
      this.handoffDescription = handoffDescription;
      return this;
    }

    public AgentBuilder<TContext, TOutput> handoffs(List<Agent<TContext, ?>> handoffs) {
      this.handoffs = handoffs;
      return this;
    }

    public AgentBuilder<TContext, TOutput> tools(List<Tool<?>> tools) {
      this.tools = tools;
      return this;
    }

    public AgentBuilder<TContext, TOutput> mcpServers(List<MCPServer> mcpServers) {
      this.mcpServers = mcpServers;
      return this;
    }

    public AgentBuilder<TContext, TOutput> inputGuardrails(
        List<InputGuardrail<TContext>> inputGuardrails) {
      this.inputGuardrails = inputGuardrails;
      return this;
    }

    public AgentBuilder<TContext, TOutput> outputGuardrails(
        List<OutputGuardrail<TContext, TOutput>> outputGuardrails) {
      this.outputGuardrails = outputGuardrails;
      return this;
    }

    public AgentBuilder<TContext, TOutput> toolInputGuardrails(
        List<ToolInputGuardrail<TContext>> toolInputGuardrails) {
      this.toolInputGuardrails = toolInputGuardrails;
      return this;
    }

    public AgentBuilder<TContext, TOutput> toolOutputGuardrails(
        List<ToolOutputGuardrail<TContext>> toolOutputGuardrails) {
      this.toolOutputGuardrails = toolOutputGuardrails;
      return this;
    }

    public AgentBuilder<TContext, TOutput> toolUseBehavior(ToolUseBehavior toolUseBehavior) {
      this.toolUseBehavior = toolUseBehavior;
      return this;
    }

    public AgentBuilder<TContext, TOutput> resetToolChoice(Boolean resetToolChoice) {
      this.resetToolChoice = resetToolChoice;
      return this;
    }

    /**
     * Builds the Agent instance with automatic tool validation.
     *
     * @return The constructed Agent
     * @throws IllegalArgumentException if tools are invalid or name is missing
     */
    public Agent<TContext, TOutput> build() {
      if (name == null) {
        throw new IllegalArgumentException("Agent name is required");
      }

      // Validate tools before building
      if (tools != null && !tools.isEmpty()) {
        ToolValidator.validateAll(tools);
      }

      Agent<TContext, TOutput> agent = new Agent<>();
      agent.name = name;
      agent.instructions = instructions;
      agent.model = model;
      agent.modelSettings = modelSettings;
      agent.outputType = outputType;
      agent.handoffDescription = handoffDescription;
      agent.handoffs = handoffs;
      agent.tools = tools;
      agent.mcpServers = mcpServers;
      agent.inputGuardrails = inputGuardrails;
      agent.outputGuardrails = outputGuardrails;
      agent.toolInputGuardrails = toolInputGuardrails;
      agent.toolOutputGuardrails = toolOutputGuardrails;
      agent.toolUseBehavior = toolUseBehavior;
      agent.resetToolChoice = resetToolChoice;

      return agent;
    }
  }

  /** Private constructor - use builder() */
  private Agent() {}
}
