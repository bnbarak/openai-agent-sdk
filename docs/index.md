# OpenAI Agent SDK for Java

A modern Java SDK for building AI agents with OpenAI's API, featuring multi-agent handoffs, tool calling, memory management, and distributed tracing.

## Features

- **Agents**: Build conversational AI agents with OpenAI models
- **Tool Calling**: Define custom tools that agents can invoke
- **Multi-Agent Handoffs**: Transfer conversations between specialized agents
- **Memory Management**: Built-in session management with multiple backends
- **Distributed Tracing**: OpenTelemetry-compatible tracing with OpenAI platform integration
- **Guardrails**: Input/output validation and safety controls
- **Streaming**: Real-time streaming of agent responses

## Quick Example

```java
--8<-- "src/main/java/com/acoliteai/agentsdk/examples/HelloWorld.java"
```

[View complete example](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/HelloWorld.java)

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
    <groupId>com.acoliteai</groupId>
    <artifactId>openai-agent-sdk</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Support

- [GitHub Issues](https://github.com/bnbarak/openai-agent-sdk/issues)
- [API Reference](api/index.md)
- Email: support@acolite.ai
