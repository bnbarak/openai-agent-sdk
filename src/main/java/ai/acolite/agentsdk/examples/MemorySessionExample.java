package ai.acolite.agentsdk.examples;

import ai.acolite.agentsdk.core.Agent;
import ai.acolite.agentsdk.core.RunConfig;
import ai.acolite.agentsdk.core.RunResult;
import ai.acolite.agentsdk.core.Runner;
import ai.acolite.agentsdk.core.memory.MemorySession;
import ai.acolite.agentsdk.core.memory.Session;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;

/**
 * MemorySessionExample
 *
 * <p>Demonstrates in-memory session storage for conversation memory.
 *
 * <p>This example shows: - Creating a MemorySession for in-memory conversation storage - Running
 * multiple turns with conversation context - How the agent remembers previous interactions -
 * Accessing session history
 *
 * <p>MemorySession is ideal for: - Development and testing - Short-lived conversations -
 * Applications where persistence isn't needed
 *
 * <p>Note: Data is lost when the JVM exits.
 *
 * <p>Usage: OPENAI_API_KEY=sk-... java ai.acolite.agentsdk.examples.MemorySessionExample
 */
public class MemorySessionExample {

  public static void main(String[] args) {
    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Error: OPENAI_API_KEY environment variable not set");
      System.err.println(
          "Usage: OPENAI_API_KEY=sk-... java ai.acolite.agentsdk.examples.MemorySessionExample");
      System.exit(1);
    }

    System.out.println("=== Memory Session Example ===\n");

    // region create-agent
    // Create a simple agent
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Assistant")
            .instructions(
                "You are a helpful assistant with a good memory. "
                    + "Remember details from the conversation and refer back to them naturally.")
            .build();
    // endregion create-agent

    // region create-session
    // Create an in-memory session
    Session session = new MemorySession("conversation-123");
    System.out.println("Created in-memory session: conversation-123");
    System.out.println();

    // Create RunConfig with the session
    RunConfig config = RunConfig.builder().session(session).build();
    // endregion create-session

    // region run-with-memory
    // Turn 1: Introduce yourself
    System.out.println("Turn 1: User introduces themselves");
    System.out.println("User: My name is Alice and I love hiking.");
    RunResult<UnknownContext, ?> result1 =
        Runner.run(agent, "My name is Alice and I love hiking.", config);
    System.out.println("Agent: " + result1.getFinalOutput());
    System.out.println();

    // Turn 2: Ask about something unrelated
    System.out.println("Turn 2: Ask about something unrelated");
    System.out.println("User: What's the capital of France?");
    RunResult<UnknownContext, ?> result2 =
        Runner.run(agent, "What's the capital of France?", config);
    System.out.println("Agent: " + result2.getFinalOutput());
    System.out.println();

    // Turn 3: Test if agent remembers
    System.out.println("Turn 3: Test memory - agent should remember your name");
    System.out.println("User: What's my name?");
    RunResult<UnknownContext, ?> result3 = Runner.run(agent, "What's my name?", config);
    System.out.println("Agent: " + result3.getFinalOutput());
    // endregion run-with-memory
    System.out.println();

    // Turn 4: Test if agent remembers hobby
    System.out.println("Turn 4: Test memory - agent should remember your hobby");
    System.out.println("User: What hobby did I mention earlier?");
    RunResult<UnknownContext, ?> result4 =
        Runner.run(agent, "What hobby did I mention earlier?", config);
    System.out.println("Agent: " + result4.getFinalOutput());
    System.out.println();

    // Display session stats
    System.out.println("Session Statistics:");
    System.out.println("  Session ID: " + session.getSessionId().join());
    System.out.println("  Total items in history: " + session.getItems(null).join().size());
    System.out.println();

    // Cumulative usage
    double totalTokens =
        result1.getUsage().getTotalTokens()
            + result2.getUsage().getTotalTokens()
            + result3.getUsage().getTotalTokens()
            + result4.getUsage().getTotalTokens();
    System.out.println("Total tokens used: " + (int) totalTokens);
    System.out.println();
    System.out.println("Note: Data will be lost when the program exits (in-memory only)");
  }
}
