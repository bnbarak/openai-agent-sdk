package com.acoliteai.agentsdk.core;

import java.util.concurrent.CompletableFuture;

/**
 * InputGuardrailDefinition
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/guardrail.ts">guardrail.ts</a>
 */
public interface InputGuardrailDefinition extends InputGuardrailMetadata {

  /**
   * run
   *
   * @param args InputGuardrailFunctionArgs
   * @return CompletableFuture<InputGuardrailResult>
   */
  CompletableFuture<InputGuardrailResult> run(InputGuardrailFunctionArgs args);
}
