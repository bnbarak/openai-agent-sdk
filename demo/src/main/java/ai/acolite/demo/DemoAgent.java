package ai.acolite.demo;

import ai.acolite.agent.Agent;
import ai.acolite.agent.AgentConfig;
import ai.acolite.openai.OpenAIClientWrapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

public final class DemoAgent {
    private static final String SYSTEM_PROMPT = """
        You are a helpful AI assistant. Be concise and friendly.
        When writing code, use markdown code blocks with language tags.
        """;

    private DemoAgent() {}

    public static Agent create() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Error: OPENAI_API_KEY environment variable not set");
            System.err.println("Please set it with: export OPENAI_API_KEY='your-key-here'");
            System.exit(1);
        }

        OpenAIClient openAIClient = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
        OpenAIClientWrapper clientWrapper = new OpenAIClientWrapper(openAIClient);

        AgentConfig config = AgentConfig.builder()
                .name("ChatAssistant")
                .model("gpt-4o-mini")
                .instructions(SYSTEM_PROMPT)
                .build();

        return new Agent(clientWrapper, config);
    }
}
