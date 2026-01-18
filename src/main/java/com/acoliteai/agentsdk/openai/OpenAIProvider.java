package com.acoliteai.agentsdk.openai;

import com.acoliteai.agentsdk.core.Model;
import com.acoliteai.agentsdk.core.ModelProvider;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import java.util.concurrent.CompletableFuture;

/**
 * OpenAIProvider
 *
 * <p>Provider for OpenAI models using the official openai-java SDK.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-openai/src/openaiProvider.ts
 */
public class OpenAIProvider implements ModelProvider {
  private final OpenAIClient client;

  /**
   * Creates an OpenAI provider using the OPENAI_API_KEY environment variable.
   *
   * @throws IllegalArgumentException if OPENAI_API_KEY is not set
   */
  public OpenAIProvider() {
    this(System.getenv("OPENAI_API_KEY"));
  }

  /**
   * Creates an OpenAI provider with the specified API key.
   *
   * @param apiKey The OpenAI API key
   * @throws IllegalArgumentException if apiKey is null or empty
   */
  public OpenAIProvider(String apiKey) {
    if (apiKey == null || apiKey.isEmpty()) {
      throw new IllegalArgumentException("OPENAI_API_KEY is required");
    }
    this.client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();
  }

  @Override
  public CompletableFuture<Model> getModel(String modelName) {
    return CompletableFuture.completedFuture(new OpenAIResponsesModel(client, modelName));
  }
}
