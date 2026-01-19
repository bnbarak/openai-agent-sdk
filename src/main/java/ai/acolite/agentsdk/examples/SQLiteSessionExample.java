package ai.acolite.agentsdk.examples;

import ai.acolite.agentsdk.core.Agent;
import ai.acolite.agentsdk.core.RunConfig;
import ai.acolite.agentsdk.core.RunResult;
import ai.acolite.agentsdk.core.Runner;
import ai.acolite.agentsdk.core.memory.SQLiteSession;
import ai.acolite.agentsdk.core.types.AgentInputItem;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * SQLiteSessionExample
 *
 * <p>Demonstrates persistent session storage with SQLite database.
 *
 * <p>This example shows: - Creating a SQLiteSession for persistent conversation storage - Running
 * multiple conversations over multiple program executions - How conversation history persists
 * across restarts - Accessing and inspecting session history from database
 *
 * <p>SQLiteSession is ideal for: - Production applications requiring data persistence -
 * Multi-session management (multiple conversations in one database) - Conversation history that
 * survives application restarts - Audit trails and conversation analysis
 *
 * <p>Features: - WAL mode enabled for better concurrency - Session isolation (multiple sessions in
 * same database) - Transaction support for data integrity
 *
 * <p>Usage: OPENAI_API_KEY=sk-... java ai.acolite.agentsdk.examples.SQLiteSessionExample
 */
public class SQLiteSessionExample {

  public static void main(String[] args) throws Exception {
    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Error: OPENAI_API_KEY environment variable not set");
      System.err.println(
          "Usage: OPENAI_API_KEY=sk-... java ai.acolite.agentsdk.examples.SQLiteSessionExample");
      System.exit(1);
    }

    System.out.println("=== SQLite Session Example ===\n");

    // Database path (will be created if it doesn't exist)
    Path dbPath = Path.of("./example-conversations.db");
    String sessionId = "alice-conversation";

    // Check if this is a continuation of a previous conversation
    boolean isNewConversation = !Files.exists(dbPath);

    if (isNewConversation) {
      System.out.println("Starting a new conversation (database will be created)");
    } else {
      System.out.println("Continuing existing conversation (database found)");
    }
    System.out.println("Database: " + dbPath.toAbsolutePath());
    System.out.println("Session ID: " + sessionId);
    System.out.println();

    // region create-agent
    // Create agent
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("PersistentAssistant")
            .instructions(
                "You are a helpful assistant with persistent memory. "
                    + "Remember all details from previous conversations and refer back to them. "
                    + "If this is a continuation, acknowledge what you remember.")
            .build();
    // endregion create-agent

    // region create-session
    // Create or open SQLite session
    try (SQLiteSession session = SQLiteSession.fromFile(sessionId, dbPath)) {

      // Check existing history
      List<AgentInputItem> existingHistory = session.getItems(null).join();
      if (!existingHistory.isEmpty()) {
        System.out.println("Found " + existingHistory.size() + " items in conversation history");
        System.out.println("This is a continuation of a previous conversation.");
        System.out.println();
      }

      // Create RunConfig with the session
      RunConfig config = RunConfig.builder().session(session).build();
      // endregion create-session

      // region run-persistent
      if (isNewConversation) {
        // First conversation
        runFirstConversation(agent, config);
      } else {
        // Continuation - test memory
        runContinuation(agent, config);
      }
      // endregion run-persistent

      // Display session statistics
      System.out.println("\nSession Statistics:");
      System.out.println("  Session ID: " + session.getSessionId().join());
      System.out.println("  Total items in history: " + session.getItems(null).join().size());
      System.out.println("  Database file: " + dbPath.toAbsolutePath());
      System.out.println();

      if (isNewConversation) {
        System.out.println("Database has been created. Run this example again to see persistence!");
        System.out.println("The agent will remember the conversation from this run.");
      } else {
        System.out.println("The agent remembered details from the previous run!");
        System.out.println("You can delete '" + dbPath.getFileName() + "' to start fresh.");
      }
    }
  }

  private static void runFirstConversation(
      Agent<UnknownContext, TextOutput> agent, RunConfig config) {
    System.out.println("=== First Conversation ===\n");

    // Turn 1: Introduce yourself
    System.out.println("Turn 1:");
    System.out.println(
        "User: Hi! My name is Alice, I'm a software engineer, and I love rock climbing.");
    RunResult<UnknownContext, ?> result1 =
        Runner.run(
            agent,
            "Hi! My name is Alice, I'm a software engineer, and I love rock climbing.",
            config);
    System.out.println("Agent: " + result1.getFinalOutput());
    System.out.println();

    // Turn 2: Ask a question
    System.out.println("Turn 2:");
    System.out.println("User: What are some good stretches for climbers?");
    RunResult<UnknownContext, ?> result2 =
        Runner.run(agent, "What are some good stretches for climbers?", config);
    System.out.println("Agent: " + result2.getFinalOutput());
    System.out.println();

    // Turn 3: Share more info
    System.out.println("Turn 3:");
    System.out.println("User: Thanks! By the way, I work mostly with Java and Python.");
    RunResult<UnknownContext, ?> result3 =
        Runner.run(agent, "Thanks! By the way, I work mostly with Java and Python.", config);
    System.out.println("Agent: " + result3.getFinalOutput());
  }

  private static void runContinuation(Agent<UnknownContext, TextOutput> agent, RunConfig config) {
    System.out.println("=== Continuation Conversation ===\n");

    // Test memory from previous run
    System.out.println("Turn 1 (testing memory):");
    System.out.println("User: Hi again! Do you remember me?");
    RunResult<UnknownContext, ?> result1 =
        Runner.run(agent, "Hi again! Do you remember me?", config);
    System.out.println("Agent: " + result1.getFinalOutput());
    System.out.println();

    System.out.println("Turn 2 (testing memory):");
    System.out.println("User: What programming languages do I use?");
    RunResult<UnknownContext, ?> result2 =
        Runner.run(agent, "What programming languages do I use?", config);
    System.out.println("Agent: " + result2.getFinalOutput());
    System.out.println();

    System.out.println("Turn 3 (testing memory):");
    System.out.println("User: What's my hobby?");
    RunResult<UnknownContext, ?> result3 = Runner.run(agent, "What's my hobby?", config);
    System.out.println("Agent: " + result3.getFinalOutput());
  }
}
