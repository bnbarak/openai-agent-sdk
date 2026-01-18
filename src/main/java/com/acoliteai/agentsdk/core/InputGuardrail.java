package com.acoliteai.agentsdk.core;

import java.util.concurrent.CompletableFuture;

/**
 * InputGuardrail
 *
 * <p>Validates user input before the first model call in an agent execution.
 *
 * <p>Input guardrails support two execution modes:
 *
 * <ul>
 *   <li><b>Parallel (default)</b>: Runs concurrently with the model call for efficiency
 *   <li><b>Blocking</b>: Runs before the model call, halting execution if tripwire triggered
 * </ul>
 *
 * <p>Input guardrails only execute on the first turn of a conversation.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/guardrail.ts">guardrail.ts</a>
 */
public interface InputGuardrail<TContext> {
  /**
   * Unique name for this guardrail.
   *
   * @return The guardrail name
   */
  String getName();

  /**
   * Execute the guardrail validation on user input.
   *
   * @param args The input arguments containing user input and context
   * @return A CompletableFuture with the guardrail result
   */
  CompletableFuture<GuardrailFunctionOutput> execute(InputGuardrailFunctionArgs<TContext> args);

  /**
   * Whether this guardrail runs in parallel with the model call or blocks before it.
   *
   * <p>Default: true (run in parallel)
   *
   * @return true to run in parallel, false to block
   */
  default boolean isRunInParallel() {
    return true;
  }
}
