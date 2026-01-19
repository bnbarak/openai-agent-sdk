package ai.acolite.demo;

import ai.acolite.agentsdk.core.Agent;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;

import java.util.List;

public final class DemoAgent {
    private static final String SYSTEM_PROMPT = """
        You are a helpful AI assistant. Be concise and friendly.
        When writing code, use markdown code blocks with language tags.

        You have access to an address validation tool that can verify and standardize US addresses.
        Use it when users ask about addresses or need to validate them.
        """;

    private DemoAgent() {}

    public static Agent<UnknownContext, TextOutput> create() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Error: OPENAI_API_KEY environment variable not set");
            System.err.println("Please set it with: export OPENAI_API_KEY='your-key-here'");
            System.exit(1);
        }

        return Agent.<UnknownContext, TextOutput>builder()
                .name("ChatAssistant")
                .model("gpt-4o-mini")
                .instructions(SYSTEM_PROMPT)
                .tools(List.of(new AddressValidationTool()))
                .build();
    }
}
