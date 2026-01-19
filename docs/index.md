# OpenAI Agent SDK for Java

A modern Java SDK for building AI agents with OpenAI's API, similar to the [TypeScript OpenAI Agents SDK](https://openai.github.io/openai-agents-js/), following its public API and implementation patterns where possible.

Built on the [OpenAI Java SDK](https://github.com/openai/openai-java).

## Features

- **Agents**: Build conversational AI agents with OpenAI models
- **Tool Calling**: Define custom tools that agents can invoke
- **Multi-Agent Handoffs**: Transfer conversations between specialized agents
- **Memory Management**: Built-in session management with multiple backends
- **Distributed Tracing**: OpenTelemetry-compatible tracing with OpenAI platform integration
- **Guardrails**: Input/output validation and safety controls
- **Streaming**: Real-time streaming of agent responses

## Example

```java
import ai.acolite.agentsdk.core.Agent;
import ai.acolite.agentsdk.core.RunResult;
import ai.acolite.agentsdk.core.Runner;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;

public class Example {
  public static void main(String[] args) {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Assistant")
            .instructions("You are a helpful assistant.")
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(agent, "Write a haiku about recursion in programming.");

    System.out.println(result.getFinalOutput());
  }
}
```

## Getting Started

New to the SDK? Start with the [Quickstart guide](quickstart.md) to build your first agent.

## Guides

Learn how to use specific features:

- [Agents](guides/agents.md) - Creating and configuring agents
- [Tools](guides/tools.md) - Defining custom tools
- [Handoffs](guides/handoffs.md) - Multi-agent workflows
- [Sessions](guides/sessions.md) - Memory and conversation history
- [Tracing](guides/tracing.md) - Observability and debugging

## Requirements

- Java 21 or higher
- Maven 3.6+ or Gradle 7+
- OpenAI API key ([Get one here](https://platform.openai.com/api-keys))

## Installation

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>ai.acolite</groupId>
    <artifactId>openai-agent-sdk</artifactId>
    <version>0.2.0</version>
</dependency>
```

## Support

- [GitHub Issues](https://github.com/bnbarak/openai-agent-sdk/issues)
- [API Reference](api/index.md)
- Email: support@acolite.ai
