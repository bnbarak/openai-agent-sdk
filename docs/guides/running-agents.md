# Running Agents

Learn how to run agents and handle results.

## Overview

Once you've created an agent, you can run it with messages and get responses.

## Basic Usage

```java
// TODO: Add region marker and include example
RunResult result = agent.run("Your message here");
String output = result.getTextOutput();
```

## Understanding RunResult

The `RunResult` object contains:

- Text output
- Message history
- Tool calls (if any)
- Token usage
- Metadata

```java
// TODO: Add example showing all RunResult methods
```

## Handling Errors

```java
// TODO: Add example showing error handling
```

## Next Steps

- [Understanding results](../api/index.md)
- [Streaming responses](streaming.md)
- [Adding tools](tools.md)

!!! note "Work in Progress"
    This guide is under development. See the [examples directory](https://github.com/bnbarak/openai-agent-sdk/tree/main/src/main/java/com/acoliteai/agentsdk/examples) for complete working examples.
