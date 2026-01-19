package ai.acolite.agentsdk.core;

import ai.acolite.agentsdk.core.errors.InputGuardrailTripwireTriggered;
import ai.acolite.agentsdk.core.errors.OutputGuardrailTripwireTriggered;
import ai.acolite.agentsdk.core.errors.ToolInputGuardrailTripwireTriggered;
import ai.acolite.agentsdk.core.errors.ToolOutputGuardrailTripwireTriggered;
import ai.acolite.agentsdk.core.types.AgentOutputType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

/**
 * GuardrailExecutor
 *
 * <p>Utility class for executing guardrails with proper parallel/sequential execution patterns.
 *
 * <p>Execution patterns:
 *
 * <ul>
 *   <li><b>Input guardrails</b>: Split into blocking (sequential) and parallel groups
 *   <li><b>Output guardrails</b>: All execute in parallel
 *   <li><b>Tool guardrails</b>: All execute sequentially
 * </ul>
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/guardrail.ts">guardrail.ts</a>
 */
public final class GuardrailExecutor {

  private GuardrailExecutor() {}

  /**
   * Execute input guardrails with blocking and parallel groups.
   *
   * @param guardrails The input guardrails to execute
   * @param args The input arguments
   * @return A future with the list of results
   */
  public static <TContext> CompletableFuture<List<InputGuardrailResult>> executeInputGuardrails(
      List<? extends InputGuardrail<TContext>> guardrails,
      InputGuardrailFunctionArgs<TContext> args) {

    if (guardrails == null || guardrails.isEmpty()) {
      return CompletableFuture.completedFuture(List.of());
    }

    List<InputGuardrail<TContext>> blocking = new ArrayList<>();
    List<InputGuardrail<TContext>> parallel = new ArrayList<>();

    for (InputGuardrail<TContext> guardrail : guardrails) {
      if (guardrail.isRunInParallel()) {
        parallel.add(guardrail);
      } else {
        blocking.add(guardrail);
      }
    }

    return executeBlockingInputGuardrails(blocking, args)
        .thenCompose(
            blockingResults ->
                executeParallelInputGuardrails(parallel, args)
                    .thenApply(
                        parallelResults -> {
                          List<InputGuardrailResult> allResults = new ArrayList<>();
                          allResults.addAll(blockingResults);
                          allResults.addAll(parallelResults);
                          return allResults;
                        }));
  }

  private static <TContext>
      CompletableFuture<List<InputGuardrailResult>> executeBlockingInputGuardrails(
          List<InputGuardrail<TContext>> guardrails, InputGuardrailFunctionArgs<TContext> args) {

    if (guardrails.isEmpty()) {
      return CompletableFuture.completedFuture(List.of());
    }

    CompletableFuture<List<InputGuardrailResult>> future =
        CompletableFuture.completedFuture(new ArrayList<>());

    for (InputGuardrail<TContext> guardrail : guardrails) {
      future =
          future.thenCompose(
              results -> {
                return guardrail
                    .execute(args)
                    .thenApply(
                        output -> {
                          InputGuardrailResult result =
                              InputGuardrailResult.builder()
                                  .guardrailName(guardrail.getName())
                                  .output(output)
                                  .build();

                          if (output.isTripwireTriggered()) {
                            throw new InputGuardrailTripwireTriggered(
                                guardrail.getName(), output.getMetadata());
                          }

                          results.add(result);
                          return results;
                        });
              });
    }

    return future;
  }

  private static <TContext>
      CompletableFuture<List<InputGuardrailResult>> executeParallelInputGuardrails(
          List<InputGuardrail<TContext>> guardrails, InputGuardrailFunctionArgs<TContext> args) {

    if (guardrails.isEmpty()) {
      return CompletableFuture.completedFuture(List.of());
    }

    List<CompletableFuture<InputGuardrailResult>> futures = new ArrayList<>();

    for (InputGuardrail<TContext> guardrail : guardrails) {
      CompletableFuture<InputGuardrailResult> future =
          guardrail
              .execute(args)
              .thenApply(
                  output -> {
                    if (output.isTripwireTriggered()) {
                      throw new InputGuardrailTripwireTriggered(
                          guardrail.getName(), output.getMetadata());
                    }

                    return InputGuardrailResult.builder()
                        .guardrailName(guardrail.getName())
                        .output(output)
                        .build();
                  });

      futures.add(future);
    }

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());
  }

  /**
   * Execute output guardrails in parallel.
   *
   * @param guardrails The output guardrails to execute
   * @param args The output arguments
   * @return A future with the list of results
   */
  public static <TContext, TOutput extends AgentOutputType>
      CompletableFuture<List<OutputGuardrailResult>> executeOutputGuardrails(
          List<? extends OutputGuardrail<TContext, TOutput>> guardrails,
          OutputGuardrailFunctionArgs<TContext, TOutput> args) {

    if (guardrails == null || guardrails.isEmpty()) {
      return CompletableFuture.completedFuture(List.of());
    }

    List<CompletableFuture<OutputGuardrailResult>> futures = new ArrayList<>();

    for (OutputGuardrail<TContext, TOutput> guardrail : guardrails) {
      CompletableFuture<OutputGuardrailResult> future =
          guardrail
              .execute(args)
              .thenApply(
                  output -> {
                    if (output.isTripwireTriggered()) {
                      throw new OutputGuardrailTripwireTriggered(
                          guardrail.getName(), output.getMetadata());
                    }

                    return OutputGuardrailResult.builder()
                        .guardrailName(guardrail.getName())
                        .output(output)
                        .build();
                  });

      futures.add(future);
    }

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());
  }

  /**
   * Execute tool input guardrails sequentially.
   *
   * @param guardrails The tool input guardrails to execute
   * @param args The tool input arguments
   * @return A future with the guardrail result
   */
  public static <TContext>
      CompletableFuture<ToolGuardrailExecutionResult> executeToolInputGuardrails(
          List<? extends ToolInputGuardrail<TContext>> guardrails,
          ToolInputGuardrailFunctionArgs<TContext> args) {

    if (guardrails == null || guardrails.isEmpty()) {
      return CompletableFuture.completedFuture(ToolGuardrailExecutionResult.allowed(List.of()));
    }

    CompletableFuture<ToolGuardrailExecutionResult> future =
        CompletableFuture.completedFuture(ToolGuardrailExecutionResult.allowed(new ArrayList<>()));

    for (ToolInputGuardrail<TContext> guardrail : guardrails) {
      future =
          future.thenCompose(
              result -> {
                return guardrail
                    .execute(args)
                    .thenApply(
                        output -> {
                          result
                              .getResults()
                              .add(
                                  ToolGuardrailResult.builder()
                                      .guardrailName(guardrail.getName())
                                      .output(output)
                                      .build());

                          if (output.getBehavior() == ToolGuardrailBehavior.THROW_EXCEPTION) {
                            throw new ToolInputGuardrailTripwireTriggered(
                                guardrail.getName(), output.getMetadata());
                          }

                          if (output.getBehavior() == ToolGuardrailBehavior.REJECT_CONTENT) {
                            return ToolGuardrailExecutionResult.rejected(
                                result.getResults(), output.getContent(), output.getMetadata());
                          }

                          return result;
                        });
              });
    }

    return future;
  }

  /**
   * Execute tool output guardrails sequentially.
   *
   * @param guardrails The tool output guardrails to execute
   * @param args The tool output arguments
   * @return A future with the guardrail result
   */
  public static <TContext>
      CompletableFuture<ToolGuardrailExecutionResult> executeToolOutputGuardrails(
          List<? extends ToolOutputGuardrail<TContext>> guardrails,
          ToolOutputGuardrailFunctionArgs<TContext> args) {

    if (guardrails == null || guardrails.isEmpty()) {
      return CompletableFuture.completedFuture(ToolGuardrailExecutionResult.allowed(List.of()));
    }

    CompletableFuture<ToolGuardrailExecutionResult> future =
        CompletableFuture.completedFuture(ToolGuardrailExecutionResult.allowed(new ArrayList<>()));

    for (ToolOutputGuardrail<TContext> guardrail : guardrails) {
      future =
          future.thenCompose(
              result -> {
                return guardrail
                    .execute(args)
                    .thenApply(
                        output -> {
                          result
                              .getResults()
                              .add(
                                  ToolGuardrailResult.builder()
                                      .guardrailName(guardrail.getName())
                                      .output(output)
                                      .build());

                          if (output.getBehavior() == ToolGuardrailBehavior.THROW_EXCEPTION) {
                            throw new ToolOutputGuardrailTripwireTriggered(
                                guardrail.getName(), output.getMetadata());
                          }

                          if (output.getBehavior() == ToolGuardrailBehavior.REJECT_CONTENT) {
                            return ToolGuardrailExecutionResult.rejected(
                                result.getResults(), output.getContent(), output.getMetadata());
                          }

                          return result;
                        });
              });
    }

    return future;
  }

  @Value
  @lombok.Builder
  public static class ToolGuardrailExecutionResult {
    @lombok.Builder.Default boolean allowed = true;
    List<ToolGuardrailResult> results;
    Object replacementContent;
    Object metadata;

    static ToolGuardrailExecutionResult allowed(List<ToolGuardrailResult> results) {
      return ToolGuardrailExecutionResult.builder().allowed(true).results(results).build();
    }

    static ToolGuardrailExecutionResult rejected(
        List<ToolGuardrailResult> results, Object replacementContent, Object metadata) {
      return ToolGuardrailExecutionResult.builder()
          .allowed(false)
          .results(results)
          .replacementContent(replacementContent)
          .metadata(metadata)
          .build();
    }
  }

  @Value
  @lombok.Builder
  public static class ToolGuardrailResult {
    String guardrailName;
    ToolGuardrailFunctionOutput output;
  }
}
