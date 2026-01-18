# OpenAI Agent SDK for Java

A modern Java SDK for building AI agents with OpenAI's API, featuring multi-agent handoffs, tool calling, memory management, and distributed tracing.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://www.oracle.com/java/)
[![Maven Central](https://img.shields.io/badge/maven--central-0.1.0--SNAPSHOT-blue.svg)](https://search.maven.org/)

## Features

- **🤖 Agent Framework**: Build conversational AI agents with OpenAI models
- **🔧 Tool Calling**: Define and execute custom tools with automatic validation
- **🤝 Multi-Agent Handoffs**: Seamlessly transfer conversations between specialized agents
- **💾 Memory Management**: Built-in session management with in-memory and SQLite backends
- **📊 Distributed Tracing**: OpenTelemetry-compatible tracing with OpenAI platform integration
- **🔒 Guardrails**: Input/output validation and safety controls
- **📝 Structured Output**: Type-safe responses with JSON schema validation
- **🌊 Streaming Support**: Real-time streaming of agent responses

## Quick Start

### Installation

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.acoliteai</groupId>
    <artifactId>openai-agent-sdk</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Hello World Example

```java
import com.acoliteai.agentsdk.core.Agent;
import com.acoliteai.agentsdk.core.RunResult;
import com.acoliteai.agentsdk.openai.OpenAI;

public class HelloWorld {
    public static void main(String[] args) {
        // Create an agent
        Agent agent = Agent.builder()
            .model(OpenAI.chatCompletionsModel("gpt-4o"))
            .instructions("You are a helpful assistant.")
            .build();

        // Run the agent
        RunResult result = agent.run("What is the capital of France?");

        // Get the response
        System.out.println(result.getTextOutput());
        // Output: "The capital of France is Paris."
    }
}
```

## Core Concepts

### 1. Agents

Agents are the core building blocks. They encapsulate a model, instructions, and optional tools:

```java
Agent agent = Agent.builder()
    .model(OpenAI.chatCompletionsModel("gpt-4o"))
    .instructions("You are a math tutor.")
    .tools(calculatorTool)
    .build();
```

### 2. Tool Calling

Define custom tools that agents can invoke:

```java
FunctionTool calculatorTool = FunctionTool.builder()
    .name("calculator")
    .description("Perform basic math operations")
    .inputSchema(CalculatorInput.class)
    .function(input -> {
        int result = switch(input.operation) {
            case "add" -> input.a + input.b;
            case "multiply" -> input.a * input.b;
            default -> 0;
        };
        return String.valueOf(result);
    })
    .build();
```

### 3. Multi-Agent Handoffs

Transfer conversations between specialized agents:

```java
Agent triage = Agent.builder()
    .model(OpenAI.chatCompletionsModel("gpt-4o"))
    .instructions("You triage customer requests.")
    .handoffs(billingAgent, technicalAgent)
    .build();

RunResult result = triage.run("I have a billing question");
// Automatically hands off to billingAgent
```

### 4. Memory & Sessions

Manage conversation history across turns:

```java
// In-memory session
Session session = new MemorySession();

// SQLite session (persistent)
Session session = new SQLiteSession("conversations.db");

Agent agent = Agent.builder()
    .model(OpenAI.chatCompletionsModel("gpt-4o"))
    .session(session)
    .build();

// Conversations are automatically persisted
agent.run("Remember: my name is Alice");
agent.run("What's my name?"); // "Your name is Alice"
```

### 5. Tracing

Monitor agent execution with distributed tracing:

```java
// Console tracing (development)
TraceProvider.configure(new ConsoleSpanExporter());

// OpenAI platform tracing (production)
TraceProvider.configure(
    new OpenAITracingExporter(System.getenv("OPENAI_API_KEY"))
);

// All agent operations are automatically traced
Agent agent = Agent.builder()
    .model(OpenAI.chatCompletionsModel("gpt-4o"))
    .tracing(TraceOptions.builder().enabled(true).build())
    .build();
```

## Examples

See the [`examples/`](src/main/java/com/acoliteai/agentsdk/examples/) directory for complete examples:

- [`HelloWorld.java`](src/main/java/com/acoliteai/agentsdk/examples/HelloWorld.java) - Basic agent usage
- [`ToolCallingExample.java`](src/main/java/com/acoliteai/agentsdk/examples/WellTypedToolsExample.java) - Custom tools
- [`AgentHandoffExample.java`](src/main/java/com/acoliteai/agentsdk/examples/AgentHandoffExample.java) - Multi-agent workflows
- [`MemorySessionExample.java`](src/main/java/com/acoliteai/agentsdk/examples/MemorySessionExample.java) - Session management
- [`StreamingExample.java`](src/main/java/com/acoliteai/agentsdk/examples/StreamingExample.java) - Streaming responses
- [`TracingExample.java`](src/main/java/com/acoliteai/agentsdk/examples/TracingExample.java) - Distributed tracing

## Requirements

- Java 21 or higher
- Maven 3.6+ or Gradle 7+
- OpenAI API key ([Get one here](https://platform.openai.com/api-keys))

## Configuration

Set your OpenAI API key as an environment variable:

```bash
export OPENAI_API_KEY='your-api-key-here'
```

Or configure programmatically:

```java
OpenAI openai = new OpenAI(apiKey);
Model model = openai.chatCompletionsModel("gpt-4o");
```

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

# All tests including integration tests
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

- [API Documentation](https://acolite.ai/docs/openai-agent-sdk)
- [Getting Started Guide](docs/getting-started.md)
- [API Reference](docs/api-surface.md)
- [Examples](src/main/java/com/acoliteai/agentsdk/examples/)

## Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) first.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Run tests (`mvn test`)
4. Format code (`mvn spotless:apply`)
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## Security

See [SECURITY.md](SECURITY.md) for details on reporting security vulnerabilities.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built on top of the official [OpenAI Java SDK](https://github.com/openai/openai-java)
- Inspired by the [OpenAI Agents TypeScript SDK](https://github.com/openai/openai-agents-js)

## Support

- 📧 Email: support@acolite.ai
- 💬 GitHub Issues: [Report a bug](https://github.com/bnbarak/openai-agent-sdk/issues)
- 📖 Documentation: [docs.acolite.ai](https://docs.acolite.ai)

---

Made with ❤️ by [Acolite AI](https://acolite.ai)
