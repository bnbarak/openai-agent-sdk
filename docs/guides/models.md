# Models

This guide covers model selection, defaults, and how models are resolved at runtime.

## Defaults and Overrides

Model selection follows this order:

1. `RunConfig.model` if provided.
2. `Agent.model` if provided.
3. `OPENAI_MODEL` environment variable.
4. Fallback to `gpt-4.1`.

Example:

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Assistant")
        .instructions("You are a helpful assistant.")
        .model("gpt-4o-mini")
        .build();

RunConfig config = RunConfig.builder().model("gpt-4o").build();
RunResult<UnknownContext, ?> result = Runner.run(agent, "Hello", config);
```

## Model Providers

The SDK uses a `ModelProvider` to fetch a `Model` by name:

- Default provider: `OpenAIProvider`
- Default provider API key source: `OPENAI_API_KEY`
- Default model implementation: `OpenAIResponsesModel` (OpenAI Responses API)

You can provide a custom `ModelProvider` in `RunConfig`:

```java
public class MyProvider implements ModelProvider {
  @Override
  public CompletableFuture<Model> getModel(String modelName) {
    return CompletableFuture.completedFuture(new MyModel(modelName));
  }
}

RunConfig config =
    RunConfig.builder().modelProvider(new MyProvider()).model("my-model").build();
```

## Model Timeouts

- `RunConfig.modelTimeoutMs` sets per-call timeout (default: 60000ms).
- `RunConfig.timeoutMs` sets overall run timeout.

```java
RunConfig config =
    RunConfig.builder()
        .modelTimeoutMs(30000L)
        .timeoutMs(120000L)
        .build();
```

## Structured Outputs

When you use `JsonSchemaOutput`, the OpenAI provider uses structured responses under the hood.
This lets you get typed outputs instead of raw text.

## Model Settings (Advanced)

`ModelSettings` exists and is plumbed through `Agent`, but it is not fully exposed yet (no public
setters at the moment). Expect this surface to expand as more model configuration is wired up.

## Notes

- OpenAI model IDs are account-specific and may change over time.
- If a model name is rejected, verify the ID in your OpenAI account and ensure your provider
  supports it.
