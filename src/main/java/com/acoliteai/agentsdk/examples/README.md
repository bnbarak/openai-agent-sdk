# OpenAI Agents SDK - Examples

This directory contains runnable examples demonstrating how to use the OpenAI Agents SDK.

## Prerequisites

1. **OpenAI API Key**: Set your API key as an environment variable:
   ```bash
   export OPENAI_API_KEY=sk-your-api-key-here
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

## Running Examples

### From Maven

Run examples directly using Maven:

```bash
# BasicTextOutput - Simple text interaction
OPENAI_API_KEY=sk-... mvn exec:java -Dexec.mainClass="examples.com.acoliteai.agentsdk.BasicTextOutput"

# StructuredOutputExample - JSON Schema outputs
OPENAI_API_KEY=sk-... mvn exec:java -Dexec.mainClass="examples.com.acoliteai.agentsdk.StructuredOutput"

# MultiTurnExecution - Multi-turn with tracking
OPENAI_API_KEY=sk-... mvn exec:java -Dexec.mainClass="examples.com.acoliteai.agentsdk.MultiTurnExecution"

# AgentConfiguration - Various config options
OPENAI_API_KEY=sk-... mvn exec:java -Dexec.mainClass="examples.com.acoliteai.agentsdk.AgentConfiguration"
```

### From Command Line

After building, run from the compiled JAR:

```bash
java -cp target/openAIAgentSDK-1.0-SNAPSHOT.jar:~/.m2/repository/... \
  examples.com.acoliteai.agentsdk.BasicTextOutput
```

### From IDE

Simply run the `main()` method in any example class. Make sure to set `OPENAI_API_KEY` in your run configuration.

## Examples Overview

### 1. BasicTextOutput.java ⭐ Start Here

The simplest possible example demonstrating text-based agent interaction.

**What you'll learn:**
- Creating an agent with `Agent.builder()`
- Running an agent with `Runner.run()`
- Accessing text responses
- Viewing usage statistics

**Key Code:**
```java
Agent<UnknownContext, TextOutput> agent = Agent.<UnknownContext, TextOutput>builder()
    .name("Assistant")
    .instructions("You are a helpful assistant.")
    .build();

RunResult<UnknownContext, ?> result = Runner.run(agent, "What is an AI agent?");
System.out.println(result.getFinalOutput());
```

**Output Example:**
```
Agent response:
An AI agent is a software program that can autonomously perform tasks using artificial intelligence.

Usage statistics:
  Total tokens: 42
  Input tokens: 15
  Output tokens: 27
```

---

### 2. StructuredOutput.java

Demonstrates how to receive structured JSON data from agents using JSON Schema.

**What you'll learn:**
- Defining data classes for structured output
- Using `JsonSchemaOutput<T>` type
- Type-safe data extraction
- Automatic JSON deserialization

**Key Code:**
```java
public static class WeatherReport {
    public String location;
    public int temperature;
    public String conditions;
}

JsonSchemaOutput<WeatherReport> outputType = JsonSchemaOutput.of(WeatherReport.class);

Agent<UnknownContext, JsonSchemaOutput<WeatherReport>> agent =
    Agent.<UnknownContext, JsonSchemaOutput<WeatherReport>>builder()
        .outputType(outputType)
        .build();

WeatherReport weather = (WeatherReport) Runner.run(agent, "Weather in SF?").getFinalOutput();
```

**Output Example:**
```
Weather Report:
  Location: San Francisco
  Temperature: 65°F
  Conditions: Partly cloudy
  Recommendation: Perfect weather for a walk!
```

---

### 3. MultiTurnExecution.java

Comprehensive example demonstrating multi-turn agent execution with detailed tracking.

**What you'll learn:**
- Configuring execution with `RunConfig`
- Setting `maxTurns` limit
- Tracking usage across multiple turns
- Monitoring conversation items
- Accessing per-turn metrics

**Key Code:**
```java
RunConfig config = RunConfig.builder()
    .maxTurns(10)
    .build();

RunResult<UnknownContext, ?> result = Runner.run(agent, prompt, config);

// Track execution details
System.out.println("Turns taken: " + result.getRawResponses().size());
System.out.println("Total tokens: " + result.getUsage().getTotalTokens());
System.out.println("Items generated: " + result.getNewItems().size());
```

**Output Example:**
```
Execution Metrics:
  Turns executed:                1
  Max turns allowed:             10
  Conversation items generated:  1

Token Usage:
  Input tokens:                  45
  Output tokens:                 320
  Total tokens:                  365

Per-Turn Token Usage:
  Turn 1: 365 tokens (in: 45, out: 320)
```

---

### 4. AgentConfiguration.java

Various agent configuration patterns and options.

**What you'll learn:**
- Writing effective agent instructions
- Selecting different models (gpt-4.1, gpt-4.1-mini)
- Using `RunConfig` for execution options
- Customizing agent behavior

**Key Code:**
```java
// Basic agent
Agent<UnknownContext, TextOutput> agent = Agent.<UnknownContext, TextOutput>builder()
    .name("BasicAssistant")
    .instructions("You are a helpful assistant")
    .build();

// Agent with custom model
Agent<UnknownContext, TextOutput> gpt4Agent = Agent.<UnknownContext, TextOutput>builder()
    .name("GPT4Assistant")
    .model("gpt-4.1-mini")
    .build();

// Agent with run configuration
RunConfig config = RunConfig.builder()
    .maxTurns(5)
    .build();
```

---

## Key Concepts

### Agent Creation

```java
Agent<UnknownContext, TextOutput> agent = Agent.<UnknownContext, TextOutput>builder()
    .name("MyAgent")
    .instructions("You are a helpful assistant")
    .model("gpt-4.1-mini")
    .build();
```

### Running an Agent

```java
// Simple run
RunResult<UnknownContext, ?> result = Runner.run(agent, "Hello!");

// With configuration
RunConfig config = RunConfig.builder().maxTurns(10).build();
RunResult<UnknownContext, ?> result = Runner.run(agent, "Hello!", config);
```

### Structured Outputs

```java
JsonSchemaOutput<MyClass> outputType = JsonSchemaOutput.of(MyClass.class);

Agent<UnknownContext, JsonSchemaOutput<MyClass>> agent =
    Agent.<UnknownContext, JsonSchemaOutput<MyClass>>builder()
        .outputType(outputType)
        .build();

MyClass result = (MyClass) Runner.run(agent, "Generate data").getFinalOutput();
```

### Multi-Turn Configuration

```java
RunConfig config = RunConfig.builder()
    .maxTurns(10)       // Maximum conversation turns
    .build();

RunResult<UnknownContext, ?> result = Runner.run(agent, input, config);

// Track execution
System.out.println("Turns: " + result.getRawResponses().size());
System.out.println("Tokens: " + result.getUsage().getTotalTokens());
```

## Example Output Formats

All examples produce clean, formatted output showing:
- The question or prompt
- The agent's response
- Usage statistics (tokens)
- Execution metrics (for multi-turn)

## Troubleshooting

### API Key Not Set
```
Error: OPENAI_API_KEY environment variable not set
```
**Solution**: Set the environment variable before running:
```bash
export OPENAI_API_KEY=sk-your-key
```

### Compilation Errors
```bash
mvn clean install
```

### Runtime Errors
Check that:
- API key is valid and has credits
- Internet connection is working
- Java 21 or higher is installed

## Next Steps

- Read the [Phase 3 Implementation Plan](../../../../../../../../instructions/phase3-plan.md) for advanced features
- Check the integration tests in `src/test/java/com/openai/agents/integration/`
- Explore the TypeScript SDK: https://github.com/openai/openai-agents-js

## Quick Reference

| Example | Complexity | What It Shows |
|---------|-----------|---------------|
| BasicTextOutput | ⭐ Beginner | Simple text interaction |
| StructuredOutput | ⭐⭐ Intermediate | JSON Schema outputs |
| MultiTurnExecution | ⭐⭐⭐ Advanced | Multi-turn tracking |
| AgentConfiguration | ⭐⭐ Intermediate | Configuration patterns |

Start with **BasicTextOutput** and progress through the examples as you learn!
