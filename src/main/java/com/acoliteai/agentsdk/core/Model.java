package com.acoliteai.agentsdk.core;

import java.util.concurrent.CompletableFuture;

/**
 * Model
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/model.ts">model.ts</a>
 */
public interface Model {

  /**
   * getResponse
   *
   * @param request ModelRequest
   * @return CompletableFuture<ModelResponse>
   */
  CompletableFuture<ModelResponse> getResponse(ModelRequest request);

  /**
   * getStreamedResponse
   *
   * @param request ModelRequest
   * @return AsyncIterable<StreamEvent>
   */
  AsyncIterable<StreamEvent> getStreamedResponse(ModelRequest request);
}
