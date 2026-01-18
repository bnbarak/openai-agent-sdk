# Agents

Learn how to create and configure agents for AI-powered interactions.

## Overview

Agents are the core building blocks of the SDK. Each agent encapsulates a model, instructions, and optional tools or output schemas. Agents are immutable once built, ensuring predictable behavior and thread safety.

An agent is defined by two type parameters:

- `TContext`: Custom data passed during execution (use `UnknownContext` for simple cases)
- `TOutput`: The output type (e.g., `TextOutput` or `JsonSchemaOutput<T>`)

## Creating a Basic Agent

Create a simple text-based agent using the builder pattern:

```java
// Configures using OPENAI_API_KEY environment variable
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Assistant")
        .instructions("You are a helpful assistant. Keep responses concise and clear.")
        .build();
```

Run the agent with a prompt:

```java
RunResult<UnknownContext, ?> result =
    Runner.run(agent, "Explain what an AI agent is in one sentence.");

System.out.println(result.getFinalOutput());
// Output: "An AI agent is a software system that uses AI to perceive its environment..."
```

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/BasicTextOutput.java)

!!! note "API Key Configuration"
    The SDK automatically reads the `OPENAI_API_KEY` environment variable. System properties take precedence over environment variables if both are set.

## Configuration Options

### Model Selection

Specify which OpenAI model to use. Defaults to `"gpt-4.1"` if not specified:

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("FastAssistant")
        .model("gpt-4.1-mini")  // Faster, more cost-effective model
        .instructions("You are a helpful assistant.")
        .build();
```

Common model options:

- `"gpt-4.1"` - Latest GPT-4.1 model (default)
- `"gpt-4.1-mini"` - Faster, more cost-effective
- `"gpt-4-turbo"` - Previous generation
- `"gpt-3.5-turbo"` - Legacy model

See the [Models guide](models.md) for the complete list and selection criteria.

### Instructions

Instructions define your agent's behavior, personality, and response style:

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("PythonExpert")
        .instructions("""
            You are an expert Python developer.

            When answering questions:
            - Provide working code examples
            - Explain your reasoning
            - Follow PEP 8 style guidelines
            - Suggest best practices
            """)
        .build();
```

!!! tip "Instruction Design"
    Clear, specific instructions improve agent performance. Include response format requirements, constraints, and any domain-specific guidelines.

### Agent Name

The agent name appears in traces and logs. Use descriptive names for multi-agent systems:

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("SupportAgent")  // Descriptive name for logging/tracing
        .instructions("You are a customer support agent.")
        .build();
```

## Structured Output

Use `JsonSchemaOutput<T>` to receive type-safe structured data instead of plain text:

```java
// Define your output structure
public static class WeatherReport {
    public String location;
    public int temperature;
    public String conditions;
    public String recommendation;
}

// Create agent with structured output
JsonSchemaOutput<WeatherReport> outputType = JsonSchemaOutput.of(WeatherReport.class);

Agent<UnknownContext, JsonSchemaOutput<WeatherReport>> agent =
    Agent.<UnknownContext, JsonSchemaOutput<WeatherReport>>builder()
        .name("WeatherAgent")
        .instructions("You are a weather assistant. Generate realistic weather data and recommendations.")
        .outputType(outputType)
        .build();

// Run and access structured data
RunResult<UnknownContext, ?> result =
    Runner.run(agent, "What's the weather like in San Francisco today?");

if (result.getFinalOutput() instanceof WeatherReport weather) {
    System.out.println("Location: " + weather.location);
    System.out.println("Temperature: " + weather.temperature + "°F");
    System.out.println("Conditions: " + weather.conditions);
}
```

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/StructuredOutput.java)

!!! tip "When to Use Structured Output"
    Use structured output when you need:

    - Parsed data for further processing
    - Type-safe access to response fields
    - Integration with databases or APIs
    - Consistent response formats

## Type Parameters

Agents are parameterized by two types:

### TContext

Custom context data available during agent execution. Use `UnknownContext` for simple agents:

```java
// Simple agent without custom context
Agent<UnknownContext, TextOutput> agent = Agent.<UnknownContext, TextOutput>builder()
    .name("Assistant")
    .instructions("You are helpful.")
    .build();
```

For custom context with tool approval or usage tracking, define your own type:

```java
// Custom context with approval tracking
public class MyContext {
    private Set<String> approvedActions = new HashSet<>();

    public boolean canApprove(String action) {
        return !approvedActions.contains(action);
    }
}

Agent<MyContext, TextOutput> agent = Agent.<MyContext, TextOutput>builder()
    .name("ControlledAgent")
    .instructions("You are a controlled assistant.")
    .build();
```

See the [Run Context guide](run-context.md) for advanced context patterns.

### TOutput

The agent's output type. Two primary options:

- `TextOutput` - Plain text responses
- `JsonSchemaOutput<T>` - Structured data conforming to a schema

```java
// Text output
Agent<UnknownContext, TextOutput> textAgent = /* ... */;

// Structured output
Agent<UnknownContext, JsonSchemaOutput<MyData>> structuredAgent = /* ... */;
```

## Immutability and Thread Safety

!!! note "Immutability Guarantee"
    Each Agent instance is immutable once built. You can safely share agents across threads and reuse them for multiple runs without side effects.

```java
// Create an agent once
Agent<UnknownContext, TextOutput> agent = Agent.<UnknownContext, TextOutput>builder()
    .name("SharedAgent")
    .instructions("You are helpful.")
    .build();

// Reuse safely across multiple executions
RunResult<UnknownContext, ?> result1 = Runner.run(agent, "Question 1");
RunResult<UnknownContext, ?> result2 = Runner.run(agent, "Question 2");

// Agent state never changes
```

## Next Steps

- [Running Agents](running-agents.md) - Execute agents and handle results
- [Tools](tools.md) - Add custom functions for agents to call
- [Sessions](sessions.md) - Add conversation memory
- [Handoffs](handoffs.md) - Build multi-agent systems

## Additional Resources

- [BasicTextOutput.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/BasicTextOutput.java) - Simple text agent
- [StructuredOutput.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/StructuredOutput.java) - Structured output example
- [API Reference](../javadoc/index.html) - Complete Javadoc documentation
