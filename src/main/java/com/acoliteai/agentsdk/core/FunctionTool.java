package com.acoliteai.agentsdk.core;

import java.util.concurrent.CompletableFuture;

/**
 * FunctionTool
 *
 * <p>Function-based tool that can be called by the model.
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tool.ts
 */
public interface FunctionTool<TContext, TInput, TOutput> extends Tool<TContext> {
  Object getParameters(); // JSON schema or Zod schema

  boolean isStrict();

  CompletableFuture<TOutput> invoke(RunContext<TContext> context, TInput input);

  boolean needsApproval(RunContext<TContext> context, TInput input);

  boolean isEnabled(RunContext<TContext> context);

  /**
   * Transforms tool execution errors into user-visible error messages.
   *
   * <p>This method is called when invoke() throws an exception or returns a failed
   * CompletableFuture. Return a helpful error message string that will be sent back to the model.
   *
   * <p>Default implementation returns the exception message. Override to provide custom error
   * messages for specific failure cases.
   *
   * @param context The run context
   * @param error The exception that occurred
   * @return User-visible error message
   */
  default String errorFunction(RunContext<TContext> context, Throwable error) {
    return error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName();
  }
}
