package com.acoliteai.agentsdk.core;

import com.acoliteai.agentsdk.core.types.AgentOutputType;
import java.util.concurrent.CompletableFuture;

/**
 * OutputGuardrail
 *
 * <p>Validates agent output after the final model response.
 *
 * <p>Output guardrails always run in parallel (no blocking mode) and execute after the agent has
 * produced its final output.
 *
 * <p>Use output guardrails to: - Check for inappropriate content in responses - Validate output
 * format or structure - Log/audit agent responses - Apply business rules to outputs
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/guardrail.ts">guardrail.ts</a>
 */
public interface OutputGuardrail<TContext, TOutput extends AgentOutputType> {
  /**
   * Unique name for this guardrail.
   *
   * @return The guardrail name
   */
  String getName();

  /**
   * Execute the guardrail validation on agent output.
   *
   * @param args The output arguments containing final output and context
   * @return A CompletableFuture with the guardrail result
   */
  CompletableFuture<GuardrailFunctionOutput> execute(
      OutputGuardrailFunctionArgs<TContext, TOutput> args);
}
