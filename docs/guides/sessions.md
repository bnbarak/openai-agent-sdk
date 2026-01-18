# Sessions

Add conversation memory so agents remember previous interactions.

## Overview

Sessions store conversation history across multiple agent runs. Without sessions, each run starts with no context. With sessions, agents remember previous messages, creating coherent multi-turn conversations.

Two session implementations:

- **MemorySession**: In-memory storage for development and testing
- **SQLiteSession**: Persistent SQLite storage for production

## Basic Session Usage

Create a session and pass it via `RunConfig`:

```java
// Create a simple agent
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Assistant")
        .instructions("You are a helpful assistant with a good memory.")
        .build();

// Create an in-memory session
Session session = new MemorySession("conversation-123");

// Create RunConfig with the session
RunConfig config = RunConfig.builder().session(session).build();

// Turn 1
RunResult<UnknownContext, ?> result1 = Runner.run(
    agent,
    "My name is Alice and I love hiking.",
    config
);

// Turn 2 - agent remembers Alice
RunResult<UnknownContext, ?> result2 = Runner.run(
    agent,
    "What's my name?",
    config
);

System.out.println(result2.getFinalOutput());
// Output: "Your name is Alice."
```

The session automatically stores all messages and tool calls, maintaining full conversation context.

## MemorySession

In-memory session for development and short-lived conversations.

### Creating a MemorySession

```java
// Create with explicit session ID
Session session = new MemorySession("conversation-123");

// Or generate random ID
Session session = new MemorySession();
```

### When to Use MemorySession

**Use for:**

- Development and testing
- Short-lived conversations (single request lifecycle)
- Prototyping and experimentation
- Applications where persistence isn't needed

**Don't use for:**

- Production applications requiring persistence
- Long-running conversations
- Multi-instance deployments
- Conversations that must survive restarts

### Example: Multi-Turn Conversation

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Assistant")
        .instructions("Remember details from the conversation naturally.")
        .build();

Session session = new MemorySession("user-session");
RunConfig config = RunConfig.builder().session(session).build();

// Turn 1: User introduces themselves
RunResult<UnknownContext, ?> result1 = Runner.run(
    agent,
    "My name is Alice and I love hiking.",
    config
);
System.out.println("Agent: " + result1.getFinalOutput());

// Turn 2: Ask unrelated question
RunResult<UnknownContext, ?> result2 = Runner.run(
    agent,
    "What's the capital of France?",
    config
);
System.out.println("Agent: " + result2.getFinalOutput());

// Turn 3: Agent remembers name
RunResult<UnknownContext, ?> result3 = Runner.run(
    agent,
    "What's my name?",
    config
);
System.out.println("Agent: " + result3.getFinalOutput());
// Output: "Your name is Alice."

// Turn 4: Agent remembers hobby
RunResult<UnknownContext, ?> result4 = Runner.run(
    agent,
    "What hobby did I mention?",
    config
);
System.out.println("Agent: " + result4.getFinalOutput());
// Output: "You mentioned that you love hiking."

// Session statistics
System.out.println("Total items in history: " + session.getItems(null).join().size());
```

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/MemorySessionExample.java)

!!! warning "Data Loss"
    MemorySession data is lost when the JVM exits. Use SQLiteSession for persistence.

## SQLiteSession

Persistent session storage using SQLite database.

### Creating a SQLiteSession

```java
Path dbPath = Path.of("./conversations.db");
String sessionId = "alice-conversation";

// Create or open SQLite session
try (SQLiteSession session = SQLiteSession.fromFile(sessionId, dbPath)) {
    // Use session...
}
```

The database file is created automatically if it doesn't exist.

### When to Use SQLiteSession

**Use for:**

- Production applications requiring persistence
- Long-running conversations
- Multi-session management (multiple users/conversations)
- Conversation history across application restarts
- Audit trails and conversation analysis

**Features:**

- WAL mode for better concurrency
- Session isolation (multiple sessions in one database)
- Transaction support for data integrity
- Automatic schema management

### Example: Persistent Conversations

```java
Path dbPath = Path.of("./example-conversations.db");
String sessionId = "alice-conversation";

Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("PersistentAssistant")
        .instructions("Remember all details from previous conversations.")
        .build();

try (SQLiteSession session = SQLiteSession.fromFile(sessionId, dbPath)) {
    // Check existing history
    List<AgentInputItem> existingHistory = session.getItems(null).join();
    if (!existingHistory.isEmpty()) {
        System.out.println("Found " + existingHistory.size() + " items in conversation history");
        System.out.println("This is a continuation of a previous conversation.");
    }

    // Create RunConfig with the session
    RunConfig config = RunConfig.builder().session(session).build();

    if (existingHistory.isEmpty()) {
        // First conversation
        System.out.println("Turn 1:");
        RunResult<UnknownContext, ?> result1 = Runner.run(
            agent,
            "Hi! My name is Alice, I'm a software engineer, and I love rock climbing.",
            config
        );
        System.out.println("Agent: " + result1.getFinalOutput());

        System.out.println("\nTurn 2:");
        RunResult<UnknownContext, ?> result2 = Runner.run(
            agent,
            "I work mostly with Java and Python.",
            config
        );
        System.out.println("Agent: " + result2.getFinalOutput());

        System.out.println("\nRun this example again to see persistence!");

    } else {
        // Continuation - test memory
        System.out.println("Turn 1 (testing memory):");
        RunResult<UnknownContext, ?> result1 = Runner.run(
            agent,
            "Hi again! Do you remember me?",
            config
        );
        System.out.println("Agent: " + result1.getFinalOutput());

        System.out.println("\nTurn 2:");
        RunResult<UnknownContext, ?> result2 = Runner.run(
            agent,
            "What programming languages do I use?",
            config
        );
        System.out.println("Agent: " + result2.getFinalOutput());
        // Output: "You mentioned that you work with Java and Python."
    }

    System.out.println("\nTotal items in history: " + session.getItems(null).join().size());
}
```

Run this example multiple times - the agent remembers details across program executions.

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/SQLiteSessionExample.java)

## Session Operations

Both session types implement the `Session` interface:

### Get Session ID

```java
CompletableFuture<String> sessionIdFuture = session.getSessionId();
String sessionId = sessionIdFuture.join();
```

### Get Conversation History

```java
// Get all items
CompletableFuture<List<AgentInputItem>> itemsFuture = session.getItems(null);
List<AgentInputItem> items = itemsFuture.join();

System.out.println("Total items: " + items.size());
```

### Add Items Manually

```java
// Create a message item
AgentInputItem message = /* ... */;

// Add to session
session.addItems(List.of(message)).join();
```

### Remove Last Item

```java
// Remove the most recent item
session.popItem().join();
```

### Clear Session

```java
// Clear all conversation history
session.clearSession().join();
```

## Sessions with Tools

Sessions store tool calls and results, maintaining full execution context:

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Assistant")
        .instructions("Use tools to help answer questions.")
        .tools(List.of(new CalculatorTool(), new WeatherTool()))
        .build();

Session session = new MemorySession("tool-conversation");
RunConfig config = RunConfig.builder().session(session).build();

// Turn 1: Agent uses calculator
RunResult<UnknownContext, ?> result1 = Runner.run(
    agent,
    "What is 25 * 17?",
    config
);
System.out.println(result1.getFinalOutput());

// Turn 2: Agent references previous calculation
RunResult<UnknownContext, ?> result2 = Runner.run(
    agent,
    "Double that number",
    config
);
System.out.println(result2.getFinalOutput());
// Agent remembers the result (425) and calculates 425 * 2 = 850
```

Tool calls, inputs, and outputs are all stored in the session.

## Sessions with Handoffs

Sessions preserve context across agent handoffs:

```java
Agent<UnknownContext, TextOutput> billingAgent = /* ... */;
Agent<UnknownContext, TextOutput> supportAgent = /* ... */;

Agent<UnknownContext, TextOutput> triageAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Triage")
        .instructions("Route to appropriate specialist.")
        .handoffs(List.of(billingAgent, supportAgent))
        .build();

Session session = new MemorySession("customer-conversation");
RunConfig config = RunConfig.builder().session(session).build();

// Turn 1: Routes to billing specialist
RunResult<UnknownContext, ?> result1 = Runner.run(
    triageAgent,
    "I was charged incorrectly for my subscription.",
    config
);

// Turn 2: Routes to support, but specialist sees full history
RunResult<UnknownContext, ?> result2 = Runner.run(
    triageAgent,
    "Also, the app is crashing when I try to view my invoices.",
    config
);

// Both specialists have access to full conversation history
```

Specialists can reference earlier parts of the conversation, even from before the handoff.

## Managing Multiple Sessions

### Single Database, Multiple Sessions

```java
Path dbPath = Path.of("./all-conversations.db");

// User 1's conversation
try (SQLiteSession session1 = SQLiteSession.fromFile("user-alice", dbPath)) {
    RunConfig config1 = RunConfig.builder().session(session1).build();
    Runner.run(agent, "Alice's question", config1);
}

// User 2's conversation (same database, different session)
try (SQLiteSession session2 = SQLiteSession.fromFile("user-bob", dbPath)) {
    RunConfig config2 = RunConfig.builder().session(session2).build();
    Runner.run(agent, "Bob's question", config2);
}

// Sessions are isolated - Alice and Bob have separate conversation histories
```

### Session Cleanup

Delete old sessions to manage database size:

```java
try (SQLiteSession session = SQLiteSession.fromFile(sessionId, dbPath)) {
    // Clear conversation history
    session.clearSession().join();
}

// Or delete the entire database file
Files.deleteIfExists(dbPath);
```

## Thread Safety

Both `MemorySession` and `SQLiteSession` are thread-safe:

```java
Session session = new MemorySession("shared-session");
RunConfig config = RunConfig.builder().session(session).build();

// Safe to use from multiple threads
ExecutorService executor = Executors.newFixedThreadPool(5);

for (int i = 0; i < 10; i++) {
    int turnNumber = i;
    executor.submit(() -> {
        RunResult<UnknownContext, ?> result = Runner.run(
            agent,
            "Question " + turnNumber,
            config
        );
        System.out.println("Turn " + turnNumber + ": " + result.getFinalOutput());
    });
}

executor.shutdown();
```

SQLiteSession uses connection pooling and WAL mode for concurrent access.

## Best Practices

!!! tip "Development vs Production"
    - **Development**: Use MemorySession for simplicity and speed
    - **Production**: Use SQLiteSession for data persistence
    - **Testing**: Use MemorySession for isolated test cases
    - **Long-Running**: Use SQLiteSession for multi-day conversations

!!! tip "Session Management"
    - **Unique IDs**: Use meaningful session IDs (user ID, conversation ID)
    - **Lifecycle**: Match session lifecycle to conversation lifecycle
    - **Cleanup**: Periodically clear old or abandoned sessions
    - **Size Monitoring**: Monitor database size for long conversations
    - **Isolation**: One session per conversation/user for clean separation

!!! tip "Performance"
    - **Connection Reuse**: Reuse SQLiteSession instances when possible
    - **Batch Operations**: Add multiple items at once when applicable
    - **Index Strategy**: SQLiteSession automatically indexes session_id
    - **WAL Mode**: SQLiteSession uses WAL mode for better concurrency
    - **History Limits**: Consider truncating very long conversation histories

!!! warning "Common Mistakes"
    - **Sharing Sessions**: Don't share sessions across unrelated conversations
    - **Missing try-with-resources**: Always close SQLiteSession (use try-with-resources)
    - **Lost Context**: Forgetting to pass session via RunConfig
    - **Memory Leaks**: Not cleaning up MemorySession references
    - **File Permissions**: Ensure write permissions for SQLiteSession database path

## Comparing Session Types

| Feature | MemorySession | SQLiteSession |
|---------|---------------|---------------|
| **Persistence** | No (lost on exit) | Yes (survives restarts) |
| **Performance** | Fast (in-memory) | Good (disk I/O) |
| **Concurrency** | Thread-safe | Thread-safe + WAL |
| **Storage Limit** | JVM memory | Disk space |
| **Setup** | None required | Database file |
| **Use Case** | Dev/testing | Production |
| **Cleanup** | Automatic (GC) | Manual |
| **Multi-session** | Separate instances | Single database |

## Next Steps

- [Running Agents](running-agents.md) - Use RunConfig to pass sessions
- [Handoffs](handoffs.md) - Maintain context across agent handoffs
- [Run Context](run-context.md) - Combine sessions with custom context
- [Tools](tools.md) - Store tool interactions in sessions

## Additional Resources

- [MemorySessionExample.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/MemorySessionExample.java) - In-memory session usage
- [SQLiteSessionExample.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/SQLiteSessionExample.java) - Persistent storage
- [API Reference](../javadoc/index.html) - Complete Javadoc documentation
