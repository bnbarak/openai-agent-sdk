package com.acoliteai.agentsdk.core;

import java.util.concurrent.CompletableFuture;

/**
 * ModelProvider
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/model.ts">model.ts</a>
 */
public interface ModelProvider {

  /**
   * Get a model by name
   *
   * <p>TypeScript: getModel(modelName?: string): Promise<Model> | Model
   *
   * @param modelName The name of the model to get (optional)
   * @return CompletableFuture that resolves to a Model
   */
  CompletableFuture<Model> getModel(String modelName);
}
