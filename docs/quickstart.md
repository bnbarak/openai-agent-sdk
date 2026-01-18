# Quickstart

Get started with the OpenAI Agent SDK in 5 minutes. By the end of this guide, you'll have a working agent.

## Prerequisites

- Java 21+
- Maven or Gradle
- OpenAI API key

## 1. Install the SDK

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.acoliteai</groupId>
    <artifactId>openai-agent-sdk</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Or for Gradle (`build.gradle`):

```groovy
implementation 'com.acoliteai:openai-agent-sdk:0.1.0-SNAPSHOT'
```

## 2. Set Your API Key

Export your OpenAI API key as an environment variable:

```bash
export OPENAI_API_KEY='your-api-key-here'
```

## 3. Create Your First Agent

Create a simple agent that answers questions:

```java
--8<-- "src/main/java/com/acoliteai/agentsdk/examples/HelloWorld.java"
```

[View complete example](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/HelloWorld.java)

## 4. Run It

Compile and run:

```bash
mvn exec:java -Dexec.mainClass="com.acoliteai.agentsdk.examples.HelloWorld"
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
- [API Reference](api/)
