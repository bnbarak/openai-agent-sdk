package ai.acolite.agentsdk.realworldapi;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import java.util.List;

public class LLMJudge {

  private final OpenAIClient client;

  public LLMJudge() {
    this.client =
        OpenAIOkHttpClient.builder().apiKey(System.getenv("OPENAI_API_KEY")).build();
  }

  public boolean evaluateErrorHandling(String agentResponse, String expectedBehavior) {
    String prompt =
        """
            You are evaluating whether an AI agent properly handled a tool error.

            Expected behavior: %s

            Agent's actual response: "%s"

            Question: Did the agent's response appropriately handle the situation as described in the expected behavior?
            Be lenient - the agent doesn't need to use the exact words, just demonstrate understanding of the error.

            Answer with ONLY "YES" or "NO".
            """
            .formatted(expectedBehavior, agentResponse);

    Response response =
        client
            .responses()
            .create(
                ResponseCreateParams.builder()
                    .model("gpt-4o-mini")
                    .input(
                        ResponseCreateParams.Input.ofResponse(
                            List.of(
                                ResponseInputItem.ofMessage(
                                    ResponseInputItem.Message.builder()
                                        .addInputTextContent(prompt)
                                        .role(ResponseInputItem.Message.Role.USER)
                                        .build()))))
                    .build());

    String judgment = response.output().get(0).toString().trim().toUpperCase();
    return judgment.contains("YES");
  }
}
