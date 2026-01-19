# Quickstart

Get started with the OpenAI Agent SDK in 5 minutes. By the end of this guide, you'll have a working agent.

A modern Java SDK for building AI agents with OpenAI's API, similar to the [TypeScript OpenAI Agents SDK](https://openai.github.io/openai-agents-js/), following its public API and implementation patterns where possible.

## Prerequisites

- Java 21+
- Maven or Gradle
- OpenAI API key

## 1. Install the SDK

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>ai.acolite</groupId>
    <artifactId>openai-agent-sdk</artifactId>
    <version>{{ config.extra.sdk_version }}</version>
</dependency>
```

Or for Gradle (`build.gradle`):

```groovy
implementation 'ai.acolite:openai-agent-sdk:{{ config.extra.sdk_version }}'
```

## 2. Set Your API Key

Export your OpenAI API key as an environment variable:

```bash
export OPENAI_API_KEY='your-api-key-here'
```

## 3. Create Your First Agent

Create a simple agent that answers a question:

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

## 4. Run It

Compile and run:

```bash
mvn exec:java -Dexec.mainClass="ai.acolite.agentsdk.examples.HelloWorld"
```

You should see the agent's response printed to the console.

## What's Next?

Now that you have a basic agent, explore more features:

- [Add custom tools](guides/tools.md) - Let agents call functions
- [Enable memory](guides/sessions.md) - Maintain conversation history
- [Multi-agent workflows](guides/handoffs.md) - Coordinate multiple agents
- [Streaming responses](guides/streaming.md) - Real-time output

## Common Issues

### API Key Not Found

If you see "API key not found", make sure you've exported `OPENAI_API_KEY`:

```bash
echo $OPENAI_API_KEY  # Should print your key
```

### Java Version Error

The SDK requires Java 21+. Check your version:

```bash
java -version  # Should show 21 or higher
```

## Need Help?

- [Troubleshooting Guide](guides/troubleshooting.md)
- [GitHub Issues](https://github.com/bnbarak/openai-agent-sdk/issues)
- [API Reference](api/index.md)
