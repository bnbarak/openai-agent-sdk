package ai.acolite.agentsdk.core;

import java.util.concurrent.CompletableFuture;

/**
 * ToolInputGuardrail
 *
 * <p>Validates tool input arguments before tool execution.
 *
 * <p>Tool guardrails execute sequentially (not in parallel) and can: - Allow the tool call to
 * proceed (ALLOW) - Reject and replace output with alternative content (REJECT_CONTENT) - Throw an
 * exception to halt execution (THROW_EXCEPTION)
 *
 * <p>Use tool input guardrails to: - Block tools with dangerous arguments - Validate parameters
 * before execution - Apply rate limits or quotas - Check for sensitive data in tool inputs
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/toolGuardrail.ts">toolGuardrail.ts</a>
 */
public interface ToolInputGuardrail<TContext> extends ToolGuardrailBase {
  /**
   * Unique name for this guardrail.
   *
   * @return The guardrail name
   */
  String getName();

  /**
   * Execute the guardrail validation on tool input.
   *
   * @param args The tool input arguments
   * @return A CompletableFuture with the guardrail result
   */
  CompletableFuture<ToolGuardrailFunctionOutput> execute(
      ToolInputGuardrailFunctionArgs<TContext> args);
}
