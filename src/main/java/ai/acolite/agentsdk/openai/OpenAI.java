package ai.acolite.agentsdk.openai;

/**
 * OpenAI
 *
 * <p>OpenAI API client.
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-openai
 */
public class OpenAI {
  private String apiKey;

  public OpenAI(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getApiKey() {
    return apiKey;
  }
}
