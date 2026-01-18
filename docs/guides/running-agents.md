# Running Agents

Learn how to execute agents and handle results.

## Overview

The `Runner` class provides static methods for executing agents synchronously, asynchronously, or with streaming. Each execution returns a `RunResult` containing the output, usage statistics, and execution metadata.

## Basic Execution

Execute an agent with a simple text prompt:

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Assistant")
        .instructions("You are a helpful assistant.")
        .build();

RunResult<UnknownContext, ?> result =
    Runner.run(agent, "Explain what an AI agent is in one sentence.");

System.out.println(result.getFinalOutput());
// Output: "An AI agent is a software system..."
```

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/BasicTextOutput.java)

## Runner Methods

### `Runner.run()`

Synchronous execution that blocks until completion:

```java
RunResult<UnknownContext, ?> result = Runner.run(agent, "Your prompt");
```

With configuration:

```java
RunConfig config = RunConfig.builder()
    .maxTurns(10)
    .build();

RunResult<UnknownContext, ?> result = Runner.run(agent, "Your prompt", config);
```

### `Runner.runAsync()`

Asynchronous execution returning a `CompletableFuture`:

```java
CompletableFuture<RunResult<UnknownContext, ?>> futureResult =
    Runner.runAsync(agent, "Your prompt");

// Process result when ready
futureResult.thenAccept(result -> {
    System.out.println(result.getFinalOutput());
});
```

### `Runner.runStreamed()`

Streaming execution for real-time updates:

```java
Flux<RunStreamEvent> stream = Runner.runStreamed(agent, "Your prompt");

stream.subscribe(event -> {
    if (event.getEventType().equals("output")) {
        System.out.print(event.getData());
    }
});
```

See the [Streaming guide](streaming.md) for detailed streaming patterns.

## Run Configuration

Configure execution behavior with `RunConfig`:

```java
RunConfig config = RunConfig.builder()
    .maxTurns(5)       // Maximum conversation turns (default: 20)
    .timeout(30000)     // Timeout in milliseconds
    .build();

RunResult<UnknownContext, ?> result = Runner.run(agent, "Your prompt", config);
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `maxTurns` | `int` | `20` | Maximum number of conversation turns before stopping |
| `timeout` | `long` | None | Maximum execution time in milliseconds |

!!! warning "Max Turns Limit"
    When the agent reaches `maxTurns`, execution stops and may throw `MaxTurnsExceededError`. Set this value based on your task complexity and cost tolerance.

## Understanding RunResult

`RunResult` contains comprehensive information about the agent execution:

```java
RunResult<UnknownContext, ?> result = Runner.run(agent, "Your question");

// Final output
Object output = result.getFinalOutput();

// Usage statistics
Usage usage = result.getUsage();
System.out.println("Total tokens: " + usage.getTotalTokens());
System.out.println("Input tokens: " + usage.getInputTokens());
System.out.println("Output tokens: " + usage.getOutputTokens());

// Execution metadata
List<ModelResponse> responses = result.getRawResponses();
System.out.println("Turns taken: " + responses.size());

List<RunOutputItem> items = result.getNewItems();
String lastId = result.getLastResponseId();
```

### RunResult Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getFinalOutput()` | `Object` | The agent's final output (text or structured) |
| `getUsage()` | `Usage` | Token usage statistics |
| `getRawResponses()` | `List<ModelResponse>` | All model responses (one per turn) |
| `getNewItems()` | `List<RunOutputItem>` | Generated conversation items |
| `getLastResponseId()` | `String` | ID of the last response |

## Multi-Turn Execution

Agents may require multiple turns to complete complex tasks, especially when using tools:

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("ThinkingAssistant")
        .instructions("Think step by step to provide thorough answers.")
        .build();

RunConfig config = RunConfig.builder()
    .maxTurns(5)  // Allow up to 5 turns
    .build();

RunResult<UnknownContext, ?> result = Runner.run(
    agent,
    "What are the key differences between OOP and functional programming?",
    config
);

// Track execution
System.out.println("Turns taken: " + result.getRawResponses().size());
System.out.println("Total tokens: " + result.getUsage().getTotalTokens());
```

Each turn represents one request-response cycle with the model. Multi-turn execution occurs when:

- The agent calls tools and processes their results
- The agent uses handoffs to delegate to other agents
- The agent performs multi-step reasoning

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/MultiTurnExecution.java)

## Per-Turn Usage Tracking

Track token usage for each individual turn:

```java
RunResult<UnknownContext, ?> result = Runner.run(agent, "Your question");

// Per-turn breakdown
int turnNumber = 1;
for (ModelResponse response : result.getRawResponses()) {
    System.out.printf(
        "Turn %d: %.0f tokens (in: %.0f, out: %.0f)%n",
        turnNumber,
        response.getUsage().getTotalTokens(),
        response.getUsage().getInputTokens(),
        response.getUsage().getOutputTokens()
    );
    turnNumber++;
}
```

## Error Handling

Handle common execution errors:

```java
try {
    RunResult<UnknownContext, ?> result = Runner.run(agent, "Your prompt");
    System.out.println(result.getFinalOutput());

} catch (MaxTurnsExceededError e) {
    // Agent hit the max turns limit
    System.err.println("Agent exceeded maximum turns: " + e.getMessage());

} catch (AuthenticationException e) {
    // Invalid or missing API key
    System.err.println("Authentication failed: " + e.getMessage());
    System.err.println("Check your OPENAI_API_KEY environment variable");

} catch (RateLimitException e) {
    // Hit OpenAI rate limits
    System.err.println("Rate limit exceeded: " + e.getMessage());
    System.err.println("Retry after: " + e.getRetryAfter());

} catch (Exception e) {
    // Other errors (network, model errors, etc.)
    System.err.println("Execution failed: " + e.getMessage());
}
```

### Common Error Types

| Exception | Cause | Resolution |
|-----------|-------|------------|
| `MaxTurnsExceededError` | Agent hit `maxTurns` limit | Increase `maxTurns` or simplify the task |
| `AuthenticationException` | Invalid/missing API key | Set `OPENAI_API_KEY` environment variable |
| `RateLimitException` | Hit OpenAI rate limits | Implement retry logic with backoff |
| `TimeoutException` | Execution exceeded timeout | Increase timeout or simplify task |

!!! tip "Production Error Handling"
    Always implement retry logic with exponential backoff for rate limit and network errors. Use structured logging to track execution failures.

## Running with Sessions

Add conversation memory using sessions:

```java
// Create a session for conversation memory
Session session = new MemorySession();

Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Assistant")
        .instructions("You are a helpful assistant.")
        .build();

// First message
RunResult<UnknownContext, ?> result1 = Runner.run(agent, "My name is Alice", session);

// Agent remembers previous context
RunResult<UnknownContext, ?> result2 = Runner.run(agent, "What's my name?", session);
System.out.println(result2.getFinalOutput());
// Output: "Your name is Alice."
```

See the [Sessions guide](sessions.md) for detailed session management.

## Running with Context

Pass custom context for tool approval or usage tracking:

```java
public class MyContext {
    private Set<String> approvedActions = new HashSet<>();

    public void approve(String action) {
        approvedActions.add(action);
    }

    public boolean isApproved(String action) {
        return approvedActions.contains(action);
    }
}

Agent<MyContext, TextOutput> agent = /* ... */;
MyContext context = new MyContext();

RunResult<MyContext, ?> result = Runner.run(agent, "Your prompt", context);
```

See the [Run Context guide](run-context.md) for advanced context patterns.

## Best Practices

!!! tip "Optimize Token Usage"
    - Monitor `result.getUsage()` to track costs
    - Use `gpt-4o-mini` for simple tasks to reduce costs
    - Set appropriate `maxTurns` to prevent runaway executions
    - Use sessions to maintain context without repeating information

!!! tip "Error Recovery"
    - Implement exponential backoff for rate limit errors
    - Log `lastResponseId` for debugging partial failures
    - Set reasonable timeouts for production workloads
    - Validate inputs before execution to fail fast

!!! tip "Performance"
    - Use `runAsync()` for non-blocking operations
    - Use streaming for real-time user feedback
    - Cache frequently used agents (they're immutable and thread-safe)
    - Pool session objects for concurrent executions

## Next Steps

- [Streaming](streaming.md) - Real-time output streaming
- [Tools](tools.md) - Add custom functions for agents to call
- [Sessions](sessions.md) - Add conversation memory
- [Run Context](run-context.md) - Custom context and tool approval

## Additional Resources

- [BasicTextOutput.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/BasicTextOutput.java) - Simple execution
- [MultiTurnExecution.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/MultiTurnExecution.java) - Multi-turn tracking
- [API Reference](../javadoc/index.html) - Complete Javadoc documentation
