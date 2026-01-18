# Streaming

Streaming lets you consume run events as they happen. In this SDK, streaming emits run items
as they are produced (message outputs, tool calls, tool outputs, and handoffs).

## Overview

Streaming is event-based, not token-based. You receive structured events as the run progresses:

- Message outputs
- Tool calls and tool outputs
- Handoff calls and outputs

## Start a Streamed Run

Use `Runner.runStreamed()`:

```java
StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
    Runner.runStreamed(agent, "Tell me a short story.");

ReadableStream<RunStreamEvent> stream = result.toStream();
ReadableStreamAsyncIterator<RunStreamEvent> it = stream.values();

while (it.hasNext()) {
  RunStreamEvent event = it.next();
  System.out.println(event.getType());
}
```

## Text-Only Convenience Stream

If you only care about message text, use `toTextStream()`:

```java
ReadableStream<String> textStream = result.toTextStream();
ReadableStreamAsyncIterator<String> it = textStream.values();

while (it.hasNext()) {
  String chunk = it.next();
  System.out.print(chunk);
}
```

## Emitted Event Types

The stream currently emits `RunItemStreamEvent` for items produced during the run:

- `message_output_created` (assistant message output)
- `tool_called`
- `tool_output`
- `handoff_called`
- `handoff_output`

Each event also includes the `turnIndex` and the underlying `RunItem`.

## Limitations

- The OpenAI provider does not implement model-level token streaming yet.
  `OpenAIResponsesModel.getStreamedResponse()` throws `NotImplementedException`.
- As a result, `runStreamed()` emits items after each model response is received
  (not token-by-token).

## Tips

- Use `runStreamed()` for UI updates or progress reporting during tool-heavy flows.
- Prefer `toTextStream()` if you only need assistant text.
