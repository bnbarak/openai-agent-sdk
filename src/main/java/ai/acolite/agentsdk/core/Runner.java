package ai.acolite.agentsdk.core;

import ai.acolite.agentsdk.core.runner.AsyncExecutor;
import ai.acolite.agentsdk.core.runner.ResponseParser;
import ai.acolite.agentsdk.core.runner.ToolExecutionUtils;
import ai.acolite.agentsdk.core.shims.ReadableStreamImpl;
import ai.acolite.agentsdk.core.types.AgentOutputType;
import ai.acolite.agentsdk.core.types.ResolvedAgentOutput;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.exceptions.MaxTurnsExceededError;
import ai.acolite.agentsdk.exceptions.ModelBehaviorError;
import ai.acolite.agentsdk.exceptions.SystemError;
import ai.acolite.agentsdk.exceptions.TimeoutError;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runner
 *
 * <p>Executes agents and manages their interactions with models.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/run.ts">run.ts</a>
 */
public class Runner extends RunHooks<Object, TextOutput> {
  private static final Logger log = LoggerFactory.getLogger(Runner.class);

  private Map<String, Object> options;
  private Model model;
  private Boolean explictlyModelSet;

  /**
   * Static convenience method to run an agent with a single text input (blocking).
   *
   * <p>This is the primary entry point for simple hello-world usage.
   *
   * @param agent The agent to run
   * @param input The input text
   * @param <TContext> The context type
   * @param <TOutput> The output type
   * @return The run result
   */
  public static <TContext, TOutput extends AgentOutputType> RunResult<TContext, ?> run(
      Agent<TContext, TOutput> agent, String input) {
    return runAsync(agent, input).join();
  }

  /**
   * Static method to run an agent asynchronously with a single text input.
   *
   * @param agent The agent to run
   * @param input The input text
   * @param <TContext> The context type
   * @param <TOutput> The output type
   * @return CompletableFuture that resolves to the run result
   */
  public static <TContext, TOutput extends AgentOutputType>
      CompletableFuture<RunResult<TContext, ?>> runAsync(
          Agent<TContext, TOutput> agent, String input) {
    RunConfig config = RunConfig.builder().build();
    return new Runner().executeRun(agent, List.of(input), config);
  }

  /**
   * Static method to run an agent with custom configuration (blocking).
   *
   * @param agent The agent to run
   * @param input The input text
   * @param config Custom run configuration
   * @param <TContext> The context type
   * @param <TOutput> The output type
   * @return The run result
   */
  public static <TContext, TOutput extends AgentOutputType> RunResult<TContext, ?> run(
      Agent<TContext, TOutput> agent, String input, RunConfig config) {
    try {
      return runAsync(agent, input, config).join();
    } catch (java.util.concurrent.CompletionException e) {
      // Unwrap CompletionException to expose the actual error
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      if (cause instanceof Error) {
        throw (Error) cause;
      }
      throw e;
    }
  }

  /**
   * Static method to run an agent asynchronously with custom configuration.
   *
   * @param agent The agent to run
   * @param input The input text
   * @param config Custom run configuration
   * @param <TContext> The context type
   * @param <TOutput> The output type
   * @return CompletableFuture that resolves to the run result
   */
  public static <TContext, TOutput extends AgentOutputType>
      CompletableFuture<RunResult<TContext, ?>> runAsync(
          Agent<TContext, TOutput> agent, String input, RunConfig config) {
    return new Runner().executeRun(agent, List.of(input), config);
  }

  /**
   * Static method to run an agent with streaming event emission.
   *
   * <p>Returns immediately with a StreamedRunResult. Events are emitted in real-time as execution
   * progresses. Use toStream() to iterate over events.
   *
   * @param agent The agent to run
   * @param input The input text
   * @param <TContext> The context type
   * @param <TOutput> The output type
   * @return StreamedRunResult with event stream
   */
  public static <TContext, TOutput extends AgentOutputType>
      StreamedRunResult<TContext, Agent<TContext, TOutput>> runStreamed(
          Agent<TContext, TOutput> agent, String input) {

    return runStreamed(agent, input, RunConfig.builder().build());
  }

  /**
   * Static method to run an agent with streaming and custom configuration.
   *
   * @param agent The agent to run
   * @param input The input text
   * @param config Custom run configuration
   * @param <TContext> The context type
   * @param <TOutput> The output type
   * @return StreamedRunResult with event stream
   */
  public static <TContext, TOutput extends AgentOutputType>
      StreamedRunResult<TContext, Agent<TContext, TOutput>> runStreamed(
          Agent<TContext, TOutput> agent, String input, RunConfig config) {
    ReadableStreamImpl<RunStreamEvent> eventStream = new ReadableStreamImpl<>();
    CompletableFuture<RunState<TContext, Agent<TContext, TOutput>>> executionFuture =
        new Runner().executeRunStreamed(agent, List.of(input), config, eventStream);
    executionFuture.whenComplete(
        (finalState, error) -> {
          if (error != null) {
            eventStream.error(error);
          } else {
            eventStream.complete();
          }
        });

    RunState<TContext, Agent<TContext, TOutput>> initialState =
        new RunState<>(agent, List.of(input), config);
    return StreamedRunResult.<TContext, Agent<TContext, TOutput>>builder()
        .state(initialState)
        .stream(eventStream)
        .executionFuture(executionFuture)
        .input(List.of(input))
        .build();
  }

  /**
   * Executes a run with the given agent, input, and configuration.
   *
   * <p>Uses non-blocking multi-turn execution loop via AsyncExecutor. Iterates until the agent
   * produces final output or reaches max turns.
   *
   * @param agent The agent to run
   * @param input The input items
   * @param config The run configuration
   * @param <TContext> The context type
   * @param <TOutput> The output type
   * @return CompletableFuture that resolves to the run result
   */
  private <TContext, TOutput extends AgentOutputType>
      CompletableFuture<RunResult<TContext, ?>> executeRun(
          Agent<TContext, TOutput> agent, List<Object> input, RunConfig config) {
    List<Object> effectiveInput;
    if (config.getSession() != null) {
      effectiveInput =
          config
              .getSession()
              .getItems(null)
              .thenApply(
                  history -> {
                    List<Object> combined = new ArrayList<>(history);
                    combined.addAll(input);
                    return combined;
                  })
              .join();
    } else {
      effectiveInput = input;
    }

    RunState<TContext, Agent<TContext, TOutput>> state =
        new RunState<>(agent, effectiveInput, config);
    ModelProvider provider = config.getEffectiveModelProvider();
    String modelName = config.getModel() != null ? config.getModel() : agent.getModel();
    Supplier<Model> modelSupplier =
        AsyncExecutor.memoize(() -> provider.getModel(modelName).join());
    CompletableFuture<RunState<TContext, Agent<TContext, TOutput>>> stateFuture =
        AsyncExecutor.iterateUntilWithSideEffect(
            state,
            s -> !s.hasFinalOutput() && !s.hasReachedMaxTurns(),
            s -> executeTurn(s, modelSupplier.get(), modelName, config),
            s -> {
              s.incrementTurn();
              return s;
            });

    // Apply overall timeout if specified
    Long overallTimeout = config.getEffectiveTimeoutMs();
    if (overallTimeout != null) {
      stateFuture =
          stateFuture
              .orTimeout(overallTimeout, java.util.concurrent.TimeUnit.MILLISECONDS)
              .exceptionally(
                  error -> {
                    if (error.getCause() instanceof java.util.concurrent.TimeoutException) {
                      throw new TimeoutError("Run execution", overallTimeout, error.getCause());
                    }
                    if (error instanceof java.util.concurrent.TimeoutException) {
                      throw new TimeoutError("Run execution", overallTimeout, error);
                    }
                    throw new SystemError("Run execution failed", error);
                  });
    }

    // Save new items to session after execution
    return stateFuture.thenCompose(
        finalState -> {
          if (config.getSession() != null) {
            List<ai.acolite.agentsdk.core.types.AgentInputItem> itemsToSave =
                buildItemsToSave(input, finalState);
            return config
                .getSession()
                .addItems(itemsToSave)
                .thenCompose(v -> buildRunResultAsync(finalState, agent));
          } else {
            return buildRunResultAsync(finalState, agent);
          }
        });
  }

  /**
   * Executes a run with streaming event emission.
   *
   * <p>Similar to executeRun() but emits events in real-time as execution progresses. Events are
   * emitted to the provided stream for consumption.
   *
   * @param agent The agent to run
   * @param input The input items
   * @param config The run configuration
   * @param eventEmitter Stream to emit events to
   * @param <TContext> The context type
   * @param <TOutput> The output type
   * @return CompletableFuture that resolves to the final state
   */
  private <TContext, TOutput extends AgentOutputType>
      CompletableFuture<RunState<TContext, Agent<TContext, TOutput>>> executeRunStreamed(
          Agent<TContext, TOutput> agent,
          List<Object> input,
          RunConfig config,
          ReadableStreamImpl<RunStreamEvent> eventEmitter) {

    // Load session history if session is provided
    List<Object> effectiveInput;
    if (config.getSession() != null) {
      effectiveInput =
          config
              .getSession()
              .getItems(null)
              .thenApply(
                  history -> {
                    List<Object> combined = new ArrayList<>(history);
                    combined.addAll(input);
                    return combined;
                  })
              .join();
    } else {
      effectiveInput = input;
    }

    RunState<TContext, Agent<TContext, TOutput>> state =
        new RunState<>(agent, effectiveInput, config);

    ModelProvider provider = config.getEffectiveModelProvider();
    String modelName = config.getModel() != null ? config.getModel() : agent.getModel();
    Supplier<Model> modelSupplier =
        AsyncExecutor.memoize(() -> provider.getModel(modelName).join());

    CompletableFuture<RunState<TContext, Agent<TContext, TOutput>>> stateFuture =
        AsyncExecutor.iterateUntilWithSideEffect(
            state,
            s -> !s.hasFinalOutput() && !s.hasReachedMaxTurns(),
            s -> executeTurnStreamed(s, modelSupplier.get(), modelName, config, eventEmitter),
            s -> {
              s.incrementTurn();
              return s;
            });

    // Save new items to session after execution
    if (config.getSession() != null) {
      stateFuture =
          stateFuture.thenCompose(
              finalState -> {
                List<ai.acolite.agentsdk.core.types.AgentInputItem> itemsToSave =
                    buildItemsToSave(input, finalState);
                return config.getSession().addItems(itemsToSave).thenApply(v -> finalState);
              });
    }

    return stateFuture;
  }

  /**
   * Execute one turn of the conversation.
   *
   * <p>Builds request from current state, calls model, parses response, updates state.
   *
   * @param state Current conversation state
   * @param model Model to use
   * @param modelName Name of the model
   * @param config Run configuration (for timeout)
   * @return CompletableFuture resolving to updated state
   */
  private <TContext, TAgent> CompletableFuture<RunState<TContext, TAgent>> executeTurn(
      RunState<TContext, TAgent> state, Model model, String modelName, RunConfig config) {
    @SuppressWarnings("unchecked")
    Agent<TContext, ?> agent = (Agent<TContext, ?>) state.getCurrentAgent();

    // Execute input guardrails on first turn only
    if (state.getCurrentTurn() == 0
        && agent.getInputGuardrails() != null
        && !agent.getInputGuardrails().isEmpty()) {

      InputGuardrailFunctionArgs<TContext> guardrailArgs =
          InputGuardrailFunctionArgs.<TContext>builder()
              .input(state.getOriginalInput())
              .context(state.getContext())
              .build();

      CompletableFuture<List<InputGuardrailResult>> guardrailsFuture =
          GuardrailExecutor.executeInputGuardrails(agent.getInputGuardrails(), guardrailArgs);

      return guardrailsFuture.thenCompose(
          results -> continueExecuteTurn(state, model, modelName, config));
    }

    return continueExecuteTurn(state, model, modelName, config);
  }

  private <TContext, TAgent> CompletableFuture<RunState<TContext, TAgent>> continueExecuteTurn(
      RunState<TContext, TAgent> state, Model model, String modelName, RunConfig config) {
    Agent<?, ?> agent = (Agent<?, ?>) state.getCurrentAgent();
    List<Object> allTools = new ArrayList<>();
    if (agent.getTools() != null) {
      allTools.addAll(agent.getTools());
    }

    if (agent.getHandoffs() != null && !agent.getHandoffs().isEmpty()) {
      List<Object> handoffTools = convertHandoffsToTools(agent.getHandoffs());
      allTools.addAll(handoffTools);
    }

    ModelRequest request =
        ModelRequest.builder()
            .input(state.getAllItems())
            .instructions(agent.getInstructions())
            .model(modelName)
            .settings(agent.getModelSettings())
            .outputType(agent.getOutputType())
            .tools(allTools.isEmpty() ? null : allTools)
            .build();

    long modelTimeout = config.getEffectiveModelTimeoutMs();
    return model
        .getResponse(request)
        .orTimeout(modelTimeout, java.util.concurrent.TimeUnit.MILLISECONDS)
        .exceptionally(
            error -> {
              if (error.getCause() instanceof java.util.concurrent.TimeoutException) {
                throw new TimeoutError("Model API call", modelTimeout, error.getCause());
              }
              throw new SystemError("Model API call failed", error);
            })
        .thenCompose(
            response -> {
              state.addModelResponse(response);
              List<RunItem> items;
              try {
                items = ResponseParser.parseResponseItems(response);
              } catch (Exception e) {
                throw new ModelBehaviorError("Failed to parse model response", e);
              }
              for (RunItem item : items) {
                state.addGeneratedItem(item);
              }

              // Separate tool calls into handoffs and regular tools
              List<RunToolCallItem> toolCalls =
                  items.stream()
                      .filter(item -> item instanceof RunToolCallItem)
                      .map(item -> (RunToolCallItem) item)
                      .toList();
              List<RunToolCallItem> handoffCalls =
                  toolCalls.stream().filter(this::isHandoffToolCall).toList();
              List<RunToolCallItem> regularToolCalls =
                  toolCalls.stream().filter(call -> !isHandoffToolCall(call)).toList();

              if (!handoffCalls.isEmpty()) {
                // Only process the first handoff (following OAI TS SDK TypeScript SDK pattern).
                RunToolCallItem handoffCall = handoffCalls.get(0);
                RunHandoffCallItem handoffItem =
                    RunHandoffCallItem.builder().toolCall(handoffCall).sourceAgent(agent).build();
                state.addGeneratedItem(handoffItem);
                RunHandoffOutputItem handoffOutput = executeHandoff(state, handoffItem, agent);

                // Add tool output so LLM knows the handoff tool was executed
                String toolResult =
                    handoffOutput.getError().isPresent()
                        ? "{\"error\": \"" + handoffOutput.getError().get() + "\"}"
                        : "{\"assistant\": \"" + handoffOutput.getToAgent() + "\"}";
                RunToolCallOutputItem toolOutput =
                    RunToolCallOutputItem.builder()
                        .toolCallId(handoffCall.getId())
                        .result(toolResult)
                        .error(handoffOutput.getError())
                        .build();
                state.addGeneratedItem(toolOutput);
                return CompletableFuture.completedFuture(state);
              }

              if (!regularToolCalls.isEmpty()) {
                return executeTools(state, regularToolCalls, agent).thenApply(s -> state);
              }

              return CompletableFuture.completedFuture(state);
            });
  }

  /**
   * Execute one turn of the conversation with streaming event emission.
   *
   * <p>Same as executeTurn() but emits RunItemStreamEvent for each item generated.
   *
   * @param state Current conversation state
   * @param model Model to use
   * @param modelName Name of the model
   * @param config Run configuration (for timeout)
   * @param eventEmitter Stream to emit events to
   * @return CompletableFuture resolving to updated state
   */
  private <TContext, TAgent> CompletableFuture<RunState<TContext, TAgent>> executeTurnStreamed(
      RunState<TContext, TAgent> state,
      Model model,
      String modelName,
      RunConfig config,
      ReadableStreamImpl<RunStreamEvent> eventEmitter) {

    @SuppressWarnings("unchecked")
    Agent<TContext, ?> agent = (Agent<TContext, ?>) state.getCurrentAgent();

    // Execute input guardrails on first turn only
    if (state.getCurrentTurn() == 0
        && agent.getInputGuardrails() != null
        && !agent.getInputGuardrails().isEmpty()) {

      InputGuardrailFunctionArgs<TContext> guardrailArgs =
          InputGuardrailFunctionArgs.<TContext>builder()
              .input(state.getOriginalInput())
              .context(state.getContext())
              .build();

      CompletableFuture<List<InputGuardrailResult>> guardrailsFuture =
          GuardrailExecutor.executeInputGuardrails(agent.getInputGuardrails(), guardrailArgs);

      return guardrailsFuture.thenCompose(
          results -> continueExecuteTurnStreamed(state, model, modelName, config, eventEmitter));
    }

    return continueExecuteTurnStreamed(state, model, modelName, config, eventEmitter);
  }

  private <TContext, TAgent>
      CompletableFuture<RunState<TContext, TAgent>> continueExecuteTurnStreamed(
          RunState<TContext, TAgent> state,
          Model model,
          String modelName,
          RunConfig config,
          ReadableStreamImpl<RunStreamEvent> eventEmitter) {

    Agent<?, ?> agent = (Agent<?, ?>) state.getCurrentAgent();

    // Build list of tools including agent handoffs
    List<Object> allTools = new ArrayList<>();
    if (agent.getTools() != null) {
      allTools.addAll(agent.getTools());
    }
    // Add handoff agents as tools
    if (agent.getHandoffs() != null && !agent.getHandoffs().isEmpty()) {
      allTools.addAll(convertHandoffsToTools(agent.getHandoffs()));
    }

    ModelRequest request =
        ModelRequest.builder()
            .input(state.getAllItems())
            .instructions(agent.getInstructions())
            .model(modelName)
            .settings(agent.getModelSettings())
            .outputType(agent.getOutputType())
            .tools(allTools.isEmpty() ? null : allTools)
            .build();

    long modelTimeout = config.getEffectiveModelTimeoutMs();
    return model
        .getResponse(request)
        .orTimeout(modelTimeout, java.util.concurrent.TimeUnit.MILLISECONDS)
        .exceptionally(
            error -> {
              if (error.getCause() instanceof java.util.concurrent.TimeoutException) {
                throw new TimeoutError("Model API call", modelTimeout, error.getCause());
              }
              throw new SystemError("Model API call failed", error);
            })
        .thenCompose(
            response -> {
              state.addModelResponse(response);

              List<RunItem> items;
              try {
                items = ResponseParser.parseResponseItems(response);
              } catch (Exception e) {
                throw new ModelBehaviorError("Failed to parse model response", e);
              }
              for (RunItem item : items) {
                state.addGeneratedItem(item);

                // EMIT EVENT (thread-safe!)
                eventEmitter.emit(
                    RunItemStreamEvent.builder()
                        .item(item)
                        .turnIndex(state.getCurrentTurn())
                        .build());
              }

              // Separate tool calls into handoffs and regular tools
              List<RunToolCallItem> toolCalls =
                  items.stream()
                      .filter(item -> item instanceof RunToolCallItem)
                      .map(item -> (RunToolCallItem) item)
                      .toList();

              log.debug("Detected {} tool calls from model response", toolCalls.size());
              if (!toolCalls.isEmpty()) {
                toolCalls.forEach(call -> log.debug("  Tool call: {}", call.getName()));
              }

              List<RunToolCallItem> handoffCalls =
                  toolCalls.stream().filter(this::isHandoffToolCall).toList();

              List<RunToolCallItem> regularToolCalls =
                  toolCalls.stream().filter(call -> !isHandoffToolCall(call)).toList();

              log.debug(
                  "Classified: {} handoff calls, {} regular tool calls",
                  handoffCalls.size(),
                  regularToolCalls.size());

              // Process handoffs
              if (!handoffCalls.isEmpty()) {
                // Only process the first handoff (as per TypeScript SDK pattern)
                RunToolCallItem handoffCall = handoffCalls.get(0);

                RunHandoffCallItem handoffItem =
                    RunHandoffCallItem.builder().toolCall(handoffCall).sourceAgent(agent).build();
                state.addGeneratedItem(handoffItem);

                // EMIT EVENT for handoff call
                eventEmitter.emit(
                    RunItemStreamEvent.builder()
                        .item(handoffItem)
                        .turnIndex(state.getCurrentTurn())
                        .build());

                // Execute the handoff (find target agent and switch)
                RunHandoffOutputItem outputItem = executeHandoff(state, handoffItem, agent);

                // EMIT EVENT for handoff output
                eventEmitter.emit(
                    RunItemStreamEvent.builder()
                        .item(outputItem)
                        .turnIndex(state.getCurrentTurn())
                        .build());

                // Add tool output so LLM knows the handoff tool was executed
                String toolResult =
                    outputItem.getError().isPresent()
                        ? "{\"error\": \"" + outputItem.getError().get() + "\"}"
                        : "{\"assistant\": \"" + outputItem.getToAgent() + "\"}";

                RunToolCallOutputItem toolOutput =
                    RunToolCallOutputItem.builder()
                        .toolCallId(handoffCall.getId())
                        .result(toolResult)
                        .error(outputItem.getError())
                        .build();
                state.addGeneratedItem(toolOutput);

                // EMIT EVENT for tool output
                eventEmitter.emit(
                    RunItemStreamEvent.builder()
                        .item(toolOutput)
                        .turnIndex(state.getCurrentTurn())
                        .build());

                // Handoff detected - don't execute regular tools, return for agent switch
                return CompletableFuture.completedFuture(state);
              }

              // Execute regular tools
              if (!regularToolCalls.isEmpty()) {
                return executeToolsStreamed(state, regularToolCalls, agent, eventEmitter)
                    .thenApply(s -> state);
              }

              return CompletableFuture.completedFuture(state);
            });
  }

  /**
   * Execute tool calls and add results to state.
   *
   * @param state Current conversation state
   * @param toolCalls List of tool calls to execute
   * @param agent Current agent with tool definitions
   * @return CompletableFuture resolving when all tools complete
   */
  private <TContext, TAgent> CompletableFuture<Void> executeTools(
      RunState<TContext, TAgent> state, List<RunToolCallItem> toolCalls, Agent<?, ?> agent) {

    List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (RunToolCallItem toolCall : toolCalls) {
      CompletableFuture<Void> future = executeSingleTool(state, toolCall, agent);
      futures.add(future);
    }

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
  }

  /** Execute a single tool call. */
  private <TContext, TAgent> CompletableFuture<Void> executeSingleTool(
      RunState<TContext, TAgent> state, RunToolCallItem toolCall, Agent<?, ?> agent) {

    FunctionTool<?, ?, ?> tool = ToolExecutionUtils.findToolByName(agent, toolCall.getName());

    if (tool == null) {
      RunToolCallOutputItem errorOutput =
          RunToolCallOutputItem.builder()
              .toolCallId(toolCall.getId())
              .result(null)
              .error(Optional.of("Tool not found: " + toolCall.getName()))
              .build();
      state.addGeneratedItem(errorOutput);
      return CompletableFuture.completedFuture(null);
    }

    @SuppressWarnings("unchecked")
    Agent<TContext, ?> typedAgent = (Agent<TContext, ?>) agent;

    // Execute tool input guardrails
    if (typedAgent.getToolInputGuardrails() != null
        && !typedAgent.getToolInputGuardrails().isEmpty()) {
      ToolInputGuardrailFunctionArgs<TContext> guardrailArgs =
          ToolInputGuardrailFunctionArgs.<TContext>builder()
              .toolName(toolCall.getName())
              .toolInput(toolCall.getParameters())
              .context(state.getContext())
              .build();

      return GuardrailExecutor.executeToolInputGuardrails(
              typedAgent.getToolInputGuardrails(), guardrailArgs)
          .thenCompose(
              guardrailResult -> {
                if (!guardrailResult.isAllowed()) {
                  // Guardrail rejected - use replacement content
                  RunToolCallOutputItem output =
                      RunToolCallOutputItem.builder()
                          .toolCallId(toolCall.getId())
                          .result(guardrailResult.getReplacementContent())
                          .error(Optional.empty())
                          .build();
                  state.addGeneratedItem(output);
                  return CompletableFuture.completedFuture(null);
                }

                // Guardrail allowed - proceed with tool execution
                return executeToolWithOutputGuardrails(state, toolCall, tool, typedAgent);
              });
    }

    // No input guardrails - proceed directly
    return executeToolWithOutputGuardrails(state, toolCall, tool, typedAgent);
  }

  private <TContext, TAgent> CompletableFuture<Void> executeToolWithOutputGuardrails(
      RunState<TContext, TAgent> state,
      RunToolCallItem toolCall,
      FunctionTool<?, ?, ?> tool,
      Agent<TContext, ?> typedAgent) {

    return invokeTool(tool, toolCall.getParameters())
        .thenCompose(
            result -> {
              // Execute tool output guardrails
              if (typedAgent.getToolOutputGuardrails() != null
                  && !typedAgent.getToolOutputGuardrails().isEmpty()) {
                ToolOutputGuardrailFunctionArgs<TContext> guardrailArgs =
                    ToolOutputGuardrailFunctionArgs.<TContext>builder()
                        .toolName(toolCall.getName())
                        .toolInput(toolCall.getParameters())
                        .toolOutput(result)
                        .context(state.getContext())
                        .build();

                return GuardrailExecutor.executeToolOutputGuardrails(
                        typedAgent.getToolOutputGuardrails(), guardrailArgs)
                    .thenApply(
                        guardrailResult -> {
                          Object finalResult =
                              guardrailResult.isAllowed()
                                  ? result
                                  : guardrailResult.getReplacementContent();

                          RunToolCallOutputItem output =
                              RunToolCallOutputItem.builder()
                                  .toolCallId(toolCall.getId())
                                  .result(finalResult)
                                  .error(Optional.empty())
                                  .build();
                          state.addGeneratedItem(output);
                          return null;
                        });
              }

              // No output guardrails - add result directly
              RunToolCallOutputItem output =
                  RunToolCallOutputItem.builder()
                      .toolCallId(toolCall.getId())
                      .result(result)
                      .error(Optional.empty())
                      .build();
              state.addGeneratedItem(output);
              return null;
            })
        .thenApply(v -> (Void) v)
        .exceptionally(
            error -> {
              // Use tool's errorFunction to generate user-visible error message
              @SuppressWarnings("unchecked")
              FunctionTool<Object, Object, Object> uncheckedTool =
                  (FunctionTool<Object, Object, Object>) tool;
              @SuppressWarnings("unchecked")
              RunContext<Object> uncheckedContext = (RunContext<Object>) state.getContext();
              String errorMessage = uncheckedTool.errorFunction(uncheckedContext, error);
              RunToolCallOutputItem errorOutput =
                  RunToolCallOutputItem.builder()
                      .toolCallId(toolCall.getId())
                      .result(null)
                      .error(Optional.of(errorMessage))
                      .build();
              state.addGeneratedItem(errorOutput);
              return null;
            });
  }

  /**
   * Execute tool calls with streaming event emission.
   *
   * <p>Same as executeTools() but emits RunItemStreamEvent for each tool output.
   *
   * @param state Current conversation state
   * @param toolCalls List of tool calls to execute
   * @param agent Current agent with tool definitions
   * @param eventEmitter Stream to emit events to
   * @return CompletableFuture resolving when all tools complete
   */
  private <TContext, TAgent> CompletableFuture<Void> executeToolsStreamed(
      RunState<TContext, TAgent> state,
      List<RunToolCallItem> toolCalls,
      Agent<?, ?> agent,
      ReadableStreamImpl<RunStreamEvent> eventEmitter) {

    List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (RunToolCallItem toolCall : toolCalls) {
      CompletableFuture<Void> future =
          executeSingleToolStreamed(state, toolCall, agent, eventEmitter);
      futures.add(future);
    }

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
  }

  /**
   * Execute a single tool call with streaming event emission.
   *
   * <p>Same as executeSingleTool() but emits RunItemStreamEvent for tool output.
   */
  private <TContext, TAgent> CompletableFuture<Void> executeSingleToolStreamed(
      RunState<TContext, TAgent> state,
      RunToolCallItem toolCall,
      Agent<?, ?> agent,
      ReadableStreamImpl<RunStreamEvent> eventEmitter) {

    FunctionTool<?, ?, ?> tool = ToolExecutionUtils.findToolByName(agent, toolCall.getName());

    if (tool == null) {
      RunToolCallOutputItem errorOutput =
          RunToolCallOutputItem.builder()
              .toolCallId(toolCall.getId())
              .result(null)
              .error(Optional.of("Tool not found: " + toolCall.getName()))
              .build();
      state.addGeneratedItem(errorOutput);

      // EMIT EVENT (thread-safe!)
      eventEmitter.emit(
          RunItemStreamEvent.builder().item(errorOutput).turnIndex(state.getCurrentTurn()).build());

      return CompletableFuture.completedFuture(null);
    }

    return invokeTool(tool, toolCall.getParameters())
        .thenAccept(
            result -> {
              RunToolCallOutputItem output =
                  RunToolCallOutputItem.builder()
                      .toolCallId(toolCall.getId())
                      .result(result)
                      .error(Optional.empty())
                      .build();
              state.addGeneratedItem(output);

              // EMIT EVENT (thread-safe, can be parallel!)
              eventEmitter.emit(
                  RunItemStreamEvent.builder()
                      .item(output)
                      .turnIndex(state.getCurrentTurn())
                      .build());
            })
        .exceptionally(
            error -> {
              // Use tool's errorFunction to generate user-visible error message
              @SuppressWarnings("unchecked")
              FunctionTool<Object, Object, Object> uncheckedTool =
                  (FunctionTool<Object, Object, Object>) tool;
              @SuppressWarnings("unchecked")
              RunContext<Object> uncheckedContext = (RunContext<Object>) state.getContext();
              String errorMessage = uncheckedTool.errorFunction(uncheckedContext, error);
              RunToolCallOutputItem errorOutput =
                  RunToolCallOutputItem.builder()
                      .toolCallId(toolCall.getId())
                      .result(null)
                      .error(Optional.of(errorMessage))
                      .build();
              state.addGeneratedItem(errorOutput);

              // EMIT EVENT (thread-safe!)
              eventEmitter.emit(
                  RunItemStreamEvent.builder()
                      .item(errorOutput)
                      .turnIndex(state.getCurrentTurn())
                      .build());

              return null;
            });
  }

  /** Invoke a tool with type-safe parameter deserialization. */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private CompletableFuture<Object> invokeTool(FunctionTool<?, ?, ?> tool, Object parameters) {
    try {
      Object typedParams =
          ToolExecutionUtils.deserializeParameters(parameters, tool.getParameters());
      RunContext context = new RunContext();
      return ((FunctionTool) tool).invoke(context, typedParams).thenApply(result -> result);
    } catch (Exception e) {
      return CompletableFuture.failedFuture(e);
    }
  }

  /**
   * Build final RunResult from completed state.
   *
   * @param state Final conversation state
   * @param agent Original agent
   * @return RunResult with final output and metadata
   */
  private <TContext, TOutput extends AgentOutputType>
      CompletableFuture<RunResult<TContext, ?>> buildRunResultAsync(
          RunState<TContext, Agent<TContext, TOutput>> state, Agent<TContext, TOutput> agent) {

    // Check if we exited due to maxTurns limit
    if (state.hasReachedMaxTurns() && !state.hasFinalOutput()) {
      throw new MaxTurnsExceededError(state.getMaxTurns(), state.getCurrentTurn());
    }

    Object outputData = extractFinalOutput(state);
    ResolvedAgentOutput<?> resolved = agent.processFinalOutput(outputData);

    // Execute output guardrails if configured
    if (agent.getOutputGuardrails() != null && !agent.getOutputGuardrails().isEmpty()) {
      @SuppressWarnings("unchecked")
      TOutput typedOutput = (TOutput) resolved.getOutput();

      OutputGuardrailFunctionArgs<TContext, TOutput> guardrailArgs =
          OutputGuardrailFunctionArgs.<TContext, TOutput>builder()
              .output(typedOutput)
              .input(state.getOriginalInput())
              .context(state.getContext())
              .build();

      return GuardrailExecutor.executeOutputGuardrails(agent.getOutputGuardrails(), guardrailArgs)
          .thenApply(
              results ->
                  RunResult.<TContext, Agent<TContext, TOutput>>builder()
                      .finalOutput(resolved.getOutput())
                      .usage(state.getContext().getUsage())
                      .rawResponses(state.getModelResponses())
                      .input(state.getOriginalInput())
                      .newItems(List.copyOf(state.getGeneratedItems()))
                      .lastAgent(agent)
                      .lastResponseId(
                          state
                              .getLastTurnResponse()
                              .flatMap(ModelResponse::getResponseId)
                              .orElse(null))
                      .build());
    }

    return CompletableFuture.completedFuture(
        RunResult.<TContext, Agent<TContext, TOutput>>builder()
            .finalOutput(resolved.getOutput())
            .usage(state.getContext().getUsage())
            .rawResponses(state.getModelResponses())
            .input(state.getOriginalInput())
            .newItems(List.copyOf(state.getGeneratedItems()))
            .lastAgent(agent)
            .lastResponseId(
                state.getLastTurnResponse().flatMap(ModelResponse::getResponseId).orElse(null))
            .build());
  }

  @Deprecated
  private <TContext, TOutput extends AgentOutputType> RunResult<TContext, ?> buildRunResult(
      RunState<TContext, Agent<TContext, TOutput>> state, Agent<TContext, TOutput> agent) {
    return buildRunResultAsync(state, agent).join();
  }

  /**
   * Extract final output from state.
   *
   * <p>Gets the last output from the last model response. Following TypeScript SDK pattern from
   * packages/agents-core/src/utils/messages.ts:getOutputText()
   *
   * @param state Final conversation state
   * @return The final output object
   */
  private <TContext, TAgent> Object extractFinalOutput(RunState<TContext, TAgent> state) {
    if (state.getLastTurnResponse().isEmpty()) {
      return "";
    }

    ModelResponse lastResponse = state.getLastTurnResponse().get();
    if (lastResponse.getOutput() == null || lastResponse.getOutput().isEmpty()) {
      return "";
    }

    return lastResponse.getOutput().get(lastResponse.getOutput().size() - 1);
  }

  /**
   * Check if a tool call is a handoff request (tool name starts with transfer_to_)
   *
   * @param toolCall The tool call to check
   * @return true if this is a handoff tool call
   */
  private boolean isHandoffToolCall(RunToolCallItem toolCall) {
    return toolCall.getName() != null && toolCall.getName().startsWith("transfer_to_");
  }

  /**
   * Execute a handoff by finding the target agent and switching to it.
   *
   * @param state Current conversation state
   * @param handoffCallItem The handoff call item
   * @param sourceAgent The agent initiating the handoff
   * @return RunHandoffOutputItem representing the handoff result
   */
  private <TContext, TAgent> RunHandoffOutputItem executeHandoff(
      RunState<TContext, TAgent> state,
      RunHandoffCallItem handoffCallItem,
      Agent<?, ?> sourceAgent) {
    String targetAgentName = handoffCallItem.getTargetAgentName();
    if (targetAgentName == null || targetAgentName.isEmpty()) {
      log.debug("Handoff failed: could not extract target agent name from tool call");
      RunHandoffOutputItem errorItem =
          RunHandoffOutputItem.builder()
              .toolCallId(handoffCallItem.getToolCall().getId())
              .sourceAgent(sourceAgent)
              .targetAgent(null)
              .error(Optional.of("Invalid handoff: could not extract target agent name"))
              .build();
      state.addGeneratedItem(errorItem);
      return errorItem;
    }

    // Find target agent in source agent's handoffs list. Try multiple matching strategies since we
    // can't distinguish between underscores in original names vs underscores that replaced spaces.
    Agent<?, ?> targetAgent = null;
    if (sourceAgent.getHandoffs() != null) {
      final String extractedName = targetAgentName;
      final String nameWithSpaces = targetAgentName.replace("_", " ");
      final String nameWithUnderscores = targetAgentName.replace(" ", "_");
      targetAgent =
          sourceAgent.getHandoffs().stream()
              .filter(
                  agent -> {
                    String agentName = agent.getName();
                    return agentName.equals(extractedName)
                        || agentName.equals(nameWithSpaces)
                        || agentName.equals(nameWithUnderscores);
                  })
              .findFirst()
              .orElse(null);
    }

    if (targetAgent == null) {
      log.debug("Handoff failed: agent '{}' not found in handoffs list", targetAgentName);
      RunHandoffOutputItem errorItem =
          RunHandoffOutputItem.builder()
              .toolCallId(handoffCallItem.getToolCall().getId())
              .sourceAgent(sourceAgent)
              .targetAgent(null)
              .error(Optional.of("Agent not found: " + targetAgentName))
              .build();
      state.addGeneratedItem(errorItem);
      return errorItem;
    }

    log.debug(
        "Handoff successful: '{}' -> '{}' (tool_call_id: {})",
        sourceAgent.getName(),
        targetAgent.getName(),
        handoffCallItem.getToolCall().getId());
    RunHandoffOutputItem outputItem =
        RunHandoffOutputItem.builder()
            .toolCallId(handoffCallItem.getToolCall().getId())
            .sourceAgent(sourceAgent)
            .targetAgent(targetAgent)
            .error(Optional.empty())
            .build();
    state.addGeneratedItem(outputItem);
    @SuppressWarnings("unchecked")
    TAgent newAgent = (TAgent) targetAgent;
    state.setCurrentAgent(newAgent);
    return outputItem;
  }

  /**
   * Converts handoff agents to function tools for the LLM. Creates transfer_to_<AgentName> function
   * tools that the LLM can call to hand off.
   *
   * @param handoffs List of agents this agent can hand off to
   * @return List of FunctionTool wrappers for handoffs
   */
  private List<Object> convertHandoffsToTools(List<? extends Agent<?, ?>> handoffs) {
    return handoffs.stream()
        .map(this::createHandoffTool)
        .collect(java.util.stream.Collectors.toList());
  }

  /**
   * Creates a function tool for handing off to a specific agent. Tool name: transfer_to_<AgentName>
   * Tool description: Uses handoffDescription if available
   *
   * @param targetAgent The agent to hand off to
   * @return FunctionTool that represents the handoff
   */
  private FunctionTool<?, HandoffInput, String> createHandoffTool(Agent<?, ?> targetAgent) {
    String toolName = "transfer_to_" + targetAgent.getName().replace(" ", "_");
    String description =
        targetAgent.getHandoffDescription() != null
            ? targetAgent.getHandoffDescription()
            : "Transfer the conversation to the " + targetAgent.getName() + " agent.";

    return new FunctionTool<Object, HandoffInput, String>() {
      @Override
      public String getType() {
        return "function";
      }

      @Override
      public String getName() {
        return toolName;
      }

      @Override
      public String getDescription() {
        return description;
      }

      @Override
      public Object getParameters() {
        return createHandoffInputClassForAgent(targetAgent, toolName);
      }

      @Override
      public boolean isStrict() {
        return false;
      }

      @Override
      public CompletableFuture<String> invoke(RunContext context, HandoffInput input) {
        // Return confirmation message
        // The actual handoff logic happens in the execution loop
        return CompletableFuture.completedFuture(
            "{\"assistant\": \"" + targetAgent.getName() + "\"}");
      }

      @Override
      public boolean needsApproval(RunContext context, HandoffInput input) {
        // Handoffs don't need approval by default
        return false;
      }

      @Override
      public boolean isEnabled(RunContext context) {
        // Handoffs are always enabled (conditional handoffs can be added later)
        return true;
      }
    };
  }

  /** Input parameters for handoff tool calls. Simple structure with optional reason field. */
  public static class HandoffInput {
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Optional reason for the handoff")
    public String reason;
  }

  private static final java.util.concurrent.ConcurrentHashMap<String, Class<?>> HANDOFF_CLASSES =
      new java.util.concurrent.ConcurrentHashMap<>();

  private static Class<?> createHandoffInputClassForAgent(
      Agent<?, ?> targetAgent, String toolName) {
    return HANDOFF_CLASSES.computeIfAbsent(
        toolName,
        name -> {
          try {
            String className = "HandoffTo" + targetAgent.getName().replaceAll("[^A-Za-z0-9]", "");

            net.bytebuddy.description.annotation.AnnotationDescription annotation =
                net.bytebuddy.description.annotation.AnnotationDescription.Builder.ofType(
                        com.fasterxml.jackson.annotation.JsonTypeName.class)
                    .define("value", name)
                    .build();

            return new net.bytebuddy.ByteBuddy()
                .subclass(HandoffInput.class)
                .name("ai.acolite.agentsdk.generated." + className)
                .annotateType(annotation)
                .make()
                .load(Runner.class.getClassLoader())
                .getLoaded();
          } catch (Exception e) {
            log.warn(
                "Failed to create dynamic class for handoff '{}', using base HandoffInput class",
                name);
            return HandoffInput.class;
          }
        });
  }

  /**
   * Build list of items to save to session. Includes the user input message and all generated items
   * from the run.
   *
   * @param input The user input
   * @param finalState The final run state
   * @return List of items to save to session
   */
  private <TContext, TAgent> List<ai.acolite.agentsdk.core.types.AgentInputItem> buildItemsToSave(
      List<Object> input, RunState<TContext, TAgent> finalState) {

    List<ai.acolite.agentsdk.core.types.AgentInputItem> itemsToSave = new ArrayList<>();

    // Add user input message(s)
    for (Object inputItem : input) {
      if (inputItem instanceof String) {
        itemsToSave.add(
            RunMessageInputItem.builder().content((String) inputItem).role("user").build());
      } else if (inputItem instanceof ai.acolite.agentsdk.core.types.AgentInputItem) {
        // If already an AgentInputItem (from history), skip it
        // We only want to save NEW items from this run
        continue;
      }
    }

    // Add all generated items (assistant messages, tool calls, tool outputs)
    itemsToSave.addAll(finalState.getGeneratedItems());

    return itemsToSave;
  }
}
