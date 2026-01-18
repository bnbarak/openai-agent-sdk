# Tools

Learn how to define custom tools that agents can invoke.

## Overview

Tools allow agents to perform actions beyond text generation. You define tools as Java functions, and the agent decides when to call them.

## Creating a Simple Tool

```java
// TODO: Add region marker from WellTypedToolsExample.java
// --8<-- "src/main/java/com/acoliteai/agentsdk/examples/WellTypedToolsExample.java:define-tool"
```

[View complete example](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/WellTypedToolsExample.java)

## Tool Input Schema

Define the input structure for your tool:

```java
// TODO: Add example showing input schema definition
```

## Adding Tools to an Agent

```java
// TODO: Add example showing how to add tools to agent
```

## Best Practices

- Keep tools focused on a single task
- Provide clear descriptions so the agent knows when to use them
- Validate inputs
- Handle errors gracefully

## Next Steps

- [Multi-agent workflows](handoffs.md)
- [Guardrails](guardrails.md)

!!! note "Work in Progress"
    This guide is under development. See [WellTypedToolsExample.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/WellTypedToolsExample.java) for a complete working example.
