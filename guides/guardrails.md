# Guardrails

Learn how to add safety constraints and validation to agent execution.

## Overview

Guardrails provide safety checks and validation at key points in agent execution. They can inspect, modify, or block inputs, outputs, and tool calls based on your security and safety requirements.

Three types of guardrails:

- **Input Guardrails**: Validate user inputs before processing
- **Output Guardrails**: Check agent responses before returning them
- **Tool Guardrails**: Monitor and control tool invocations

## Input Guardrails

Input guardrails run before the agent processes user messages. Use them to filter inappropriate content, detect sensitive information, or enforce policies.

### Creating an Input Guardrail

Implement `InputGuardrail` interface:

```java
public class ContentModerationGuardrail implements InputGuardrail {
    @Override
    public String getName() {
        return "content_moderation";
    }

    @Override
    public String getDescription() {
        return "Blocks inappropriate or harmful content in user inputs";
    }

    @Override
    public CompletableFuture<InputGuardrailResult> execute(
            RunContext<?> context,
            List<AgentInputItem> inputs
    ) {
        return CompletableFuture.supplyAsync(() -> {
            // Check for inappropriate content
            for (AgentInputItem item : inputs) {
                if (containsInappropriateContent(item)) {
                    // Block the input
                    return InputGuardrailResult.builder()
                        .tripwireTriggered(true)
                        .message("Input contains inappropriate content")
                        .build();
                }
            }

            // Allow the input
            return InputGuardrailResult.builder()
                .tripwireTriggered(false)
                .build();
        });
    }

    private boolean containsInappropriateContent(AgentInputItem item) {
        // Implement content moderation logic
        return false;
    }
}
```

### Using Input Guardrails

Add guardrails to your agent:

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Assistant")
        .instructions("You are a helpful assistant.")
        .inputGuardrails(List.of(new ContentModerationGuardrail()))
        .build();

// If input violates guardrail, execution stops with InputGuardrailTripwireTriggered
RunResult<UnknownContext, ?> result = Runner.run(agent, "User message");
```

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/ai/acolite/agentsdk/examples/guardrails/ContentModerationGuardrail.java)

## Output Guardrails

Output guardrails inspect agent responses before returning them to users. Use them to redact sensitive information, enforce formatting requirements, or filter inappropriate outputs.

### Creating an Output Guardrail

```java
public class PIIDetectionGuardrail implements OutputGuardrail {
    @Override
    public String getName() {
        return "pii_detection";
    }

    @Override
    public String getDescription() {
        return "Detects and redacts personally identifiable information";
    }

    @Override
    public CompletableFuture<OutputGuardrailResult> execute(
            RunContext<?> context,
            Object output
    ) {
        return CompletableFuture.supplyAsync(() -> {
            if (output instanceof String text) {
                // Check for PII (emails, phone numbers, SSNs, etc.)
                if (containsPII(text)) {
                    String redacted = redactPII(text);

                    // Modify the output
                    return OutputGuardrailResult.builder()
                        .modifiedOutput(redacted)
                        .tripwireTriggered(false)
                        .message("PII detected and redacted")
                        .build();
                }
            }

            // Output is safe
            return OutputGuardrailResult.builder()
                .tripwireTriggered(false)
                .build();
        });
    }

    private boolean containsPII(String text) {
        // Check for email, phone, SSN patterns
        return text.matches(".*\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b.*");
    }

    private String redactPII(String text) {
        // Redact detected PII
        return text.replaceAll("\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b", "[EMAIL REDACTED]");
    }
}
```

### Using Output Guardrails

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Assistant")
        .instructions("You are a helpful assistant.")
        .outputGuardrails(List.of(new PIIDetectionGuardrail()))
        .build();

// If output contains PII, it's automatically redacted
RunResult<UnknownContext, ?> result = Runner.run(
    agent,
    "What's John's email address?"
);
```

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/ai/acolite/agentsdk/examples/guardrails/PIIDetectionGuardrail.java)

## Tool Guardrails

Tool guardrails monitor tool inputs and outputs, providing fine-grained control over tool execution.

### Creating a Tool Guardrail

```java
public class SecretBlockingToolGuardrail implements ToolInputGuardrail {
    @Override
    public String getName() {
        return "secret_blocking";
    }

    @Override
    public String getDescription() {
        return "Prevents tools from accessing secrets or credentials";
    }

    @Override
    public CompletableFuture<ToolInputGuardrailResult> execute(
            RunContext<?> context,
            String toolName,
            Object toolInput
    ) {
        return CompletableFuture.supplyAsync(() -> {
            // Check if tool is trying to access secrets
            if (isAccessingSecrets(toolName, toolInput)) {
                // Block the tool call
                return ToolInputGuardrailResult.builder()
                    .tripwireTriggered(true)
                    .message("Tool attempted to access secrets")
                    .build();
            }

            // Allow the tool call
            return ToolInputGuardrailResult.builder()
                .tripwireTriggered(false)
                .build();
        });
    }

    private boolean isAccessingSecrets(String toolName, Object input) {
        // Check if input contains secret-related parameters
        return false;
    }
}
```

### Using Tool Guardrails

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Assistant")
        .instructions("You are a helpful assistant.")
        .tools(List.of(new DatabaseTool(), new FileTool()))
        .toolInputGuardrails(List.of(new SecretBlockingToolGuardrail()))
        .build();
```

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/ai/acolite/agentsdk/examples/guardrails/SecretBlockingToolGuardrail.java)

## Guardrail Behavior

### Blocking Execution

Set `tripwireTriggered(true)` to block execution:

```java
return InputGuardrailResult.builder()
    .tripwireTriggered(true)
    .message("Policy violation detected")
    .build();
```

This throws an exception and stops execution immediately.

### Modifying Content

Modify outputs without blocking:

```java
return OutputGuardrailResult.builder()
    .modifiedOutput(redactedText)
    .tripwireTriggered(false)
    .message("Content modified for safety")
    .build();
```

The modified output is used instead of the original.

### Logging Only

Run checks without affecting execution:

```java
return InputGuardrailResult.builder()
    .tripwireTriggered(false)
    .message("Content flagged for review")
    .build();
```

Log the message for auditing without blocking.

## Multiple Guardrails

Run multiple guardrails in sequence:

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("SecureAssistant")
        .instructions("You are a secure assistant.")
        .inputGuardrails(List.of(
            new ContentModerationGuardrail(),
            new SpamDetectionGuardrail(),
            new InjectionDetectionGuardrail()
        ))
        .outputGuardrails(List.of(
            new PIIDetectionGuardrail(),
            new SensitiveInfoGuardrail()
        ))
        .toolInputGuardrails(List.of(
            new SecretBlockingToolGuardrail(),
            new PathTraversalGuardrail()
        ))
        .build();
```

Guardrails run in order. If any guardrail triggers, execution stops.

## Common Guardrail Patterns

### PII Detection and Redaction

```java
public class PIIGuardrail implements OutputGuardrail {
    private static final Pattern EMAIL = Pattern.compile(
        "\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PHONE = Pattern.compile(
        "\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b"
    );

    @Override
    public CompletableFuture<OutputGuardrailResult> execute(
            RunContext<?> context,
            Object output
    ) {
        if (output instanceof String text) {
            String redacted = text;
            redacted = EMAIL.matcher(redacted).replaceAll("[EMAIL REDACTED]");
            redacted = PHONE.matcher(redacted).replaceAll("[PHONE REDACTED]");

            if (!redacted.equals(text)) {
                return CompletableFuture.completedFuture(
                    OutputGuardrailResult.builder()
                        .modifiedOutput(redacted)
                        .tripwireTriggered(false)
                        .message("PII redacted")
                        .build()
                );
            }
        }

        return CompletableFuture.completedFuture(
            OutputGuardrailResult.builder().tripwireTriggered(false).build()
        );
    }
}
```

### SQL Injection Prevention

```java
public class SQLInjectionGuardrail implements ToolInputGuardrail {
    private static final Pattern SQL_INJECTION = Pattern.compile(
        ".*(union|select|insert|update|delete|drop|create|alter)\\s+.*",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public CompletableFuture<ToolInputGuardrailResult> execute(
            RunContext<?> context,
            String toolName,
            Object toolInput
    ) {
        String inputStr = toolInput.toString();

        if (SQL_INJECTION.matcher(inputStr).matches()) {
            return CompletableFuture.completedFuture(
                ToolInputGuardrailResult.builder()
                    .tripwireTriggered(true)
                    .message("SQL injection attempt detected")
                    .build()
            );
        }

        return CompletableFuture.completedFuture(
            ToolInputGuardrailResult.builder().tripwireTriggered(false).build()
        );
    }
}
```

### Rate Limiting

```java
public class RateLimitGuardrail implements InputGuardrail {
    private final Map<String, Queue<Long>> requestTimestamps = new ConcurrentHashMap<>();
    private final int maxRequests = 10;
    private final long timeWindowMs = 60000; // 1 minute

    @Override
    public CompletableFuture<InputGuardrailResult> execute(
            RunContext<?> context,
            List<AgentInputItem> inputs
    ) {
        String userId = context.getContext().toString();
        long now = System.currentTimeMillis();

        Queue<Long> timestamps = requestTimestamps.computeIfAbsent(
            userId,
            k -> new ConcurrentLinkedQueue<>()
        );

        // Remove old timestamps
        timestamps.removeIf(ts -> now - ts > timeWindowMs);

        if (timestamps.size() >= maxRequests) {
            return CompletableFuture.completedFuture(
                InputGuardrailResult.builder()
                    .tripwireTriggered(true)
                    .message("Rate limit exceeded")
                    .build()
            );
        }

        timestamps.add(now);

        return CompletableFuture.completedFuture(
            InputGuardrailResult.builder().tripwireTriggered(false).build()
        );
    }
}
```

## Best Practices

!!! tip "Guardrail Design"
    - **Fast Execution**: Keep guardrails lightweight to avoid latency
    - **Clear Messages**: Provide informative messages when blocking
    - **Specific Checks**: Create focused guardrails for specific threats
    - **Fail Secure**: Default to blocking if uncertain
    - **Log Everything**: Record all guardrail decisions for auditing

!!! tip "Security"
    - **Defense in Depth**: Use multiple complementary guardrails
    - **Input Validation**: Always validate user inputs
    - **Output Sanitization**: Always check agent outputs
    - **Tool Restrictions**: Limit tool capabilities via guardrails
    - **Context Awareness**: Use RunContext for user-specific rules

!!! tip "Performance"
    - **Async Execution**: Use CompletableFuture for I/O operations
    - **Caching**: Cache expensive validation results
    - **Batch Processing**: Process multiple items together when possible
    - **Early Exit**: Check most likely violations first
    - **Resource Limits**: Set timeouts for guardrail execution

!!! warning "Common Pitfalls"
    - **False Positives**: Overly aggressive guardrails blocking legitimate use
    - **Incomplete Coverage**: Missing edge cases in validation logic
    - **Performance Issues**: Slow guardrails causing request timeouts
    - **Inconsistent Enforcement**: Different guardrails with conflicting rules
    - **Poor Error Messages**: Blocking without explaining why

## When to Use Guardrails

**Use guardrails for:**

- Content moderation and safety
- PII detection and redaction
- Compliance enforcement (GDPR, HIPAA, etc.)
- Security threat prevention (injection, XSS, etc.)
- Rate limiting and abuse prevention
- Policy enforcement

**Don't use guardrails for:**

- Complex business logic (use tools or context instead)
- Agent instruction modifications (use agent instructions)
- Performance optimization (use caching or async patterns)
- User preference handling (use context)

## Next Steps

- [Run Context](run-context.md) - Use context in guardrail logic
- [Tools](tools.md) - Combine guardrails with tools
- [Agents](agents.md) - Configure agents with guardrails
- [Tracing](tracing.md) - Monitor guardrail execution

## Additional Resources

- [PIIDetectionGuardrail.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/ai/acolite/agentsdk/examples/guardrails/PIIDetectionGuardrail.java) - Output guardrail example
- [ContentModerationGuardrail.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/ai/acolite/agentsdk/examples/guardrails/ContentModerationGuardrail.java) - Input guardrail example
- [SecretBlockingToolGuardrail.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/ai/acolite/agentsdk/examples/guardrails/SecretBlockingToolGuardrail.java) - Tool guardrail example
- [API Reference](../javadoc/index.html) - Complete Javadoc documentation
