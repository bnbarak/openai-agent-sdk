package ai.acolite.agentsdk.core;

import java.util.concurrent.CompletableFuture;

/**
 * ToolOutputGuardrail
 *
 * <p>Validates tool output after tool execution.
 *
 * <p>Tool guardrails execute sequentially (not in parallel) and can: - Allow the tool output to be
 * used (ALLOW) - Reject and replace output with alternative content (REJECT_CONTENT) - Throw an
 * exception to halt execution (THROW_EXCEPTION)
 *
 * <p>Use tool output guardrails to: - Filter sensitive information from tool results - Validate
 * tool output format - Transform or sanitize tool responses - Log/audit tool outputs
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/toolGuardrail.ts">toolGuardrail.ts</a>
 */
public interface ToolOutputGuardrail<TContext> extends ToolGuardrailBase {
  /**
   * Unique name for this guardrail.
   *
   * @return The guardrail name
   */
  String getName();

  /**
   * Execute the guardrail validation on tool output.
   *
   * @param args The tool output arguments
   * @return A CompletableFuture with the guardrail result
   */
  CompletableFuture<ToolGuardrailFunctionOutput> execute(
      ToolOutputGuardrailFunctionArgs<TContext> args);
}
