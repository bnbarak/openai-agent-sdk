package ai.acolite.agentsdk.realworldapi;

import static org.junit.jupiter.api.Assertions.*;

import ai.acolite.agentsdk.core.Agent;
import ai.acolite.agentsdk.core.RunConfig;
import ai.acolite.agentsdk.core.RunResult;
import ai.acolite.agentsdk.core.Runner;
import ai.acolite.agentsdk.core.memory.MemorySession;
import ai.acolite.agentsdk.core.memory.Session;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import org.junit.jupiter.api.Test;

class ConversationMemoryRealAPITest {

  @Test
  void multiTurnConversation_remembersUserName() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MemoryTestAgent")
            .instructions("You are a helpful assistant with a good memory.")
            .model("gpt-4o-mini")
            .build();

    Session session = new MemorySession("test-conversation");
    RunConfig config = RunConfig.builder().session(session).build();

    RunResult<UnknownContext, ?> result1 =
        Runner.run(agent, "My name is Barak and I love programming.", config);
    String response1 = result1.getFinalOutput().toString();
    assertNotNull(response1);

    RunResult<UnknownContext, ?> result2 = Runner.run(agent, "What is my name?", config);
    String response2 = result2.getFinalOutput().toString().toLowerCase();

    assertTrue(
        response2.contains("barak"),
        "Agent should remember the user's name. Response was: " + response2);
  }

  @Test
  void multiTurnConversation_remembersPreviousContext() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("ContextTestAgent")
            .instructions("You are a helpful assistant. Remember what users tell you.")
            .model("gpt-4o-mini")
            .build();

    Session session = new MemorySession("context-test");
    RunConfig config = RunConfig.builder().session(session).build();

    Runner.run(agent, "I have a dog named Max who is 3 years old.", config);

    RunResult<UnknownContext, ?> result = Runner.run(agent, "How old is my dog?", config);
    String response = result.getFinalOutput().toString().toLowerCase();

    assertTrue(
        response.contains("3") || response.contains("three"),
        "Agent should remember dog's age. Response was: " + response);
  }
}
