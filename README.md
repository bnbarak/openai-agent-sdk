# OpenAI Agent SDK for Java

A modern Java SDK for building AI agents with OpenAI's API, inspired by the [OpenAI Agents TypeScript SDK](https://openai.github.io/openai-agents-js/). Built on the [OpenAI Java SDK](https://github.com/openai/openai-java).

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://www.oracle.com/java/)
[![Maven Central](https://img.shields.io/badge/maven--central-0.3.0-SNAPSHOTblue.svg)](https://search.maven.org/)
[![Documentation](https://img.shields.io/badge/docs-latest-blue.svg)](https://bnbarak.github.io/openai-agent-sdk/)

**📖 [Full Documentation](https://bnbarak.github.io/openai-agent-sdk/)** | **🚀 [Quickstart Guide](https://bnbarak.github.io/openai-agent-sdk/quickstart/)** | **📚 [API Reference](https://bnbarak.github.io/openai-agent-sdk/javadoc/)**

## Features

| Feature | Description | Docs | Examples |
|---------|-------------|------|----------|
| **Agent Loop** | Built-in agent loop that handles tool invocation, sends results back to the LLM, and continues until the task is complete | [Guide](https://bnbarak.github.io/openai-agent-sdk/guides/running-agents/) | [Code](src/main/java/ai/acolite/agentsdk/examples/BasicTextOutput.java) |
| **Java-First** | Orchestrate and chain agents using idiomatic Java language features, without needing to learn new abstractions | [Guide](https://bnbarak.github.io/openai-agent-sdk/guides/agents/) | [Code](src/main/java/ai/acolite/agentsdk/examples/AgentConfiguration.java) |
| **Handoffs** | Powerful mechanism for coordinating and delegating work across multiple agents | [Guide](https://bnbarak.github.io/openai-agent-sdk/guides/handoffs/) | [Code](src/main/java/ai/acolite/agentsdk/examples/AgentHandoffExample.java) |
| **Guardrails** | Run input validation and safety checks in parallel with agent execution, and fail fast when checks do not pass | [Guide](https://bnbarak.github.io/openai-agent-sdk/guides/guardrails/) | [Code](src/main/java/ai/acolite/agentsdk/examples/GuardrailsExample.java) |
| **Function Tools** | Turn any Java function into a tool with automatic schema generation and validation | [Guide](https://bnbarak.github.io/openai-agent-sdk/guides/tools/) | [Code](src/main/java/ai/acolite/agentsdk/examples/WellTypedToolsExample.java) |
| **Hosted Tools** | Built-in support for OpenAI hosted tools like web search and image generation | [Guide](https://bnbarak.github.io/openai-agent-sdk/guides/tools/) | [Code](src/main/java/ai/acolite/agentsdk/examples/HostedToolsExample.java) |
| **Sessions** | Persistent memory layer for maintaining working context within an agent loop | [Guide](https://bnbarak.github.io/openai-agent-sdk/guides/sessions/) | [Code](src/main/java/ai/acolite/agentsdk/examples/MemorySessionExample.java) |
| **Human in the Loop** | Built-in mechanisms for involving humans across agent runs | [Guide](https://bnbarak.github.io/openai-agent-sdk/guides/run-context/) | [Code](src/main/java/ai/acolite/agentsdk/examples/RunContextExample.java) |
| **Tracing** | Built-in tracing for visualizing, debugging, and monitoring workflows | [Guide](https://bnbarak.github.io/openai-agent-sdk/guides/tracing/) | [Code](src/main/java/ai/acolite/agentsdk/examples/AgentWithTracingExample.java) |
| **MCP Server Tools** | Built-in MCP server tool integration that works the same way as function tools | TBD | TBD |
| **Realtime API** | Build powerful voice agents with features such as automatic interruption detection, context management, and guardrails | TBD | TBD |

## Requirements

- Java 21 or higher
- Maven 3.6+ or Gradle 7+
- OpenAI API key ([Get one here](https://platform.openai.com/api-keys))

## Quick Start

### Installation

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>ai.acolite</groupId>
    <artifactId>openai-agent-sdk</artifactId>
    <version>0.3.0-SNAPSHOT</version>
</dependency>
```

### Configuration

Set your OpenAI API key as an environment variable:

```bash
export OPENAI_API_KEY='your-api-key-here'
```

### Basic Usage

```java
import ai.acolite.agentsdk.core.Agent;
import ai.acolite.agentsdk.core.RunResult;
import ai.acolite.agentsdk.core.Runner;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;

public class HelloWorld {
    public static void main(String[] args) {
        // Create a simple agent
        Agent<UnknownContext, TextOutput> agent =
            Agent.<UnknownContext, TextOutput>builder()
                .name("Assistant")
                .instructions("You are a helpful assistant.")
                .build();

        // Run the agent
        RunResult<UnknownContext, ?> result =
            Runner.run(agent, "What is the capital of France?");

        // Print the response
        System.out.println(result.getFinalOutput());
    }
}
```

## Interactive Demo

Try the SDK with our interactive command-line demos:

```bash
cd demo
mvn clean package

# Run async chat demo (responses appear all at once)
java -jar target/async-chat-demo.jar

# Or run streaming chat demo (real-time character-by-character)
java -jar target/streaming-chat-demo.jar
```

The demos include:
- 💬 **Interactive chat interface** with conversation memory
- 🏠 **Address validation tool** using the Smarty US Street API
- 🔄 **Up to 20 turns** for complex multi-step workflows
- 📝 **Both async and streaming modes** to compare execution patterns

[View demo source code →](demo/) | [Demo README →](demo/README.md)

## Core Concepts

### Agents

Agents are the core building blocks. They encapsulate instructions and optional tools:

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("MathAssistant")
        .instructions("You are a math tutor. Use the calculator tool to perform calculations.")
        .tools(List.of(new CalculatorTool()))
        .build();
```

[Learn more about Agents →](https://bnbarak.github.io/openai-agent-sdk/guides/agents/)

### Tool Calling

Define custom tools that agents can invoke. Tools use type-safe input/output with automatic JSON schema generation:

```java
public class CalculatorTool
    implements FunctionTool<Object, CalculatorTool.Input, CalculatorTool.Output> {

  @Data
  @JsonClassDescription("Input parameters for arithmetic operations")
  public static class Input {
    @JsonPropertyDescription("The arithmetic operation: add, subtract, multiply, or divide")
    private String operation;
    @JsonPropertyDescription("The first number")
    private double a;
    @JsonPropertyDescription("The second number")
    private double b;
  }

  @Data
  @AllArgsConstructor
  public static class Output {
    private double result;
    private String operation;
    private String expression;
  }

  @Override
  public String getName() {
    return "calculator";
  }

  @Override
  public String getDescription() {
    return "Performs basic arithmetic operations.";
  }

  @Override
  public Object getParameters() {
    return Input.class;
  }

  @Override
  public CompletableFuture<Output> invoke(RunContext<Object> context, Input input) {
    return CompletableFuture.supplyAsync(() -> {
      double result = switch (input.getOperation()) {
        case "add" -> input.getA() + input.getB();
        case "subtract" -> input.getA() - input.getB();
        case "multiply" -> input.getA() * input.getB();
        case "divide" -> input.getA() / input.getB();
        default -> throw new IllegalArgumentException("Unknown operation");
      };
      return new Output(result, input.getOperation(),
          String.format("%.2f %s %.2f = %.2f", input.getA(),
              input.getOperation(), input.getB(), result));
    });
  }
}
```

[View complete tool example →](src/main/java/ai/acolite/agentsdk/examples/tools/CalculatorTool.java)

[Learn more about Tools →](https://bnbarak.github.io/openai-agent-sdk/guides/tools/)

### Hosted Tools

Use OpenAI's hosted tools for web search and image generation:

```java
import ai.acolite.agentsdk.core.HostedTool;

// Web search
Agent<UnknownContext, TextOutput> searchAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("SearchAssistant")
        .instructions("You can search the web for current information.")
        .tools(List.of(HostedTool.webSearch()))
        .build();

// Image generation
Agent<UnknownContext, TextOutput> artistAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Artist")
        .instructions("You can generate images using DALL-E.")
        .tools(List.of(HostedTool.imageGeneration()))
        .build();
```

[View hosted tools example →](src/main/java/ai/acolite/agentsdk/examples/HostedToolsExample.java)

[Learn more about Tools →](https://bnbarak.github.io/openai-agent-sdk/guides/tools/)

### Multi-Agent Handoffs

Transfer conversations between specialized agents:

```java
// Create specialist agents
Agent<UnknownContext, TextOutput> supportAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Technical Support")
        .instructions("You are a technical support specialist.")
        .handoffDescription("Handles technical support questions")
        .build();

Agent<UnknownContext, TextOutput> billingAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Billing Support")
        .instructions("You handle billing and payment questions.")
        .handoffDescription("Handles billing and payment questions")
        .build();

// Create triage agent with handoffs
Agent<UnknownContext, TextOutput> triageAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Triage")
        .instructions("You route customer requests to the right specialist.")
        .handoffs(List.of(supportAgent, billingAgent))
        .build();

// Run - automatically hands off to appropriate agent
RunResult<UnknownContext, ?> result =
    Runner.run(triageAgent, "My app keeps crashing, can you help?");
```

[View complete handoff example →](src/main/java/ai/acolite/agentsdk/examples/AgentHandoffExample.java)

[Learn more about Handoffs →](https://bnbarak.github.io/openai-agent-sdk/guides/handoffs/)

### Memory & Sessions

Manage conversation history across turns:

```java
import ai.acolite.agentsdk.core.Session;
import ai.acolite.agentsdk.core.memory.MemorySession;
import ai.acolite.agentsdk.core.RunConfig;

// Create an in-memory session
Session session = new MemorySession("conversation-123");

// Create agent
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Assistant")
        .instructions("You are a helpful assistant with a good memory.")
        .build();

// Use session across multiple turns
RunConfig config = RunConfig.builder().session(session).build();

// Turn 1
Runner.run(agent, "My name is Alice and I love hiking.", config);

// Turn 2
Runner.run(agent, "What's the capital of France?", config);

// Turn 3 - agent remembers your name!
RunResult<UnknownContext, ?> result =
    Runner.run(agent, "What's my name?", config);
// Response: "Your name is Alice!"
```

For persistent storage, use SQLiteSession:

```java
import ai.acolite.agentsdk.core.memory.SQLiteSession;

Session session = new SQLiteSession("conversations.db", "user-123");
```

[View memory example →](src/main/java/ai/acolite/agentsdk/examples/MemorySessionExample.java) | [View SQLite example →](src/main/java/ai/acolite/agentsdk/examples/SQLiteSessionExample.java)

[Learn more about Sessions →](https://bnbarak.github.io/openai-agent-sdk/guides/sessions/)

### Tracing

Monitor agent execution with distributed tracing:

```java
import ai.acolite.agentsdk.core.tracing.TraceProvider;
import ai.acolite.agentsdk.core.tracing.ConsoleTraceProcessor;

// Enable console tracing (development)
TraceProvider.configure(new ConsoleTraceProcessor());

// All agent operations are automatically traced
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("TracedAgent")
        .instructions("You are a helpful assistant.")
        .build();

Runner.run(agent, "Hello!");
// Traces will be printed to console showing execution flow
```

[View complete tracing example →](src/main/java/ai/acolite/agentsdk/examples/AgentWithTracingExample.java)

[Learn more about Tracing →](https://bnbarak.github.io/openai-agent-sdk/guides/tracing/)

## Development

### Building from Source

```bash
git clone https://github.com/bnbarak/openai-agent-sdk.git
cd openai-agent-sdk
mvn clean install
```

### Running Tests

```bash
# Unit tests only (fast)
mvn test

# All tests including integration tests (requires a real API key and may incur costs)
mvn verify -Pe2e
```

### Code Formatting

This project uses [Spotless](https://github.com/diffplug/spotless) with Google Java Format:

```bash
# Check formatting
mvn spotless:check

# Apply formatting
mvn spotless:apply
```

## Documentation

**📖 [Full Documentation](https://bnbarak.github.io/openai-agent-sdk/)** - Complete guides and tutorials

**Guides:**
- [Agents](https://bnbarak.github.io/openai-agent-sdk/guides/agents/) - Creating and configuring agents
- [Tools](https://bnbarak.github.io/openai-agent-sdk/guides/tools/) - Building custom function tools
- [Handoffs](https://bnbarak.github.io/openai-agent-sdk/guides/handoffs/) - Multi-agent coordination
- [Sessions](https://bnbarak.github.io/openai-agent-sdk/guides/sessions/) - Memory and conversation history
- [Tracing](https://bnbarak.github.io/openai-agent-sdk/guides/tracing/) - Monitoring and debugging
- [Running Agents](https://bnbarak.github.io/openai-agent-sdk/guides/running-agents/) - Execution patterns

**Reference:**
- [Javadoc API Reference](https://bnbarak.github.io/openai-agent-sdk/javadoc/)
- [Examples (source code)](src/main/java/ai/acolite/agentsdk/examples/)

## Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) first.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Run tests (`mvn test`)
4. Format code (`mvn spotless:apply`)
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- We are appreciative of the great work Stainless is doing on the [OpenAI Java SDK](https://github.com/openai/openai-java), which enabled this SDK.
- Built on top of the official [OpenAI Java SDK](https://github.com/openai/openai-java)
- Inspired by the [OpenAI Agents TypeScript SDK](https://github.com/openai/openai-agents-js)

## Support

- 📧 Email: support@acolite.ai
- 💬 GitHub Issues: [Report a bug](https://github.com/bnbarak/openai-agent-sdk/issues)
- 📖 Documentation: [bnbarak.github.io/openai-agent-sdk](https://bnbarak.github.io/openai-agent-sdk/)

---

Made with ❤️ by [Acolite AI](https://acolite.ai)
