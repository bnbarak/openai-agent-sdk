# Tracing

Tracing captures workflows and spans for observability. The tracing system is available today,
but it is not auto-wired into the Runner yet, so you create traces and spans explicitly.

## Overview

Tracing is built from:

- `TraceProvider` for global configuration and processor registration
- `TraceContext` for context propagation
- `Trace` and `Span` for the actual trace data
- Processors/exporters for output (console or OpenAI)

## Quick Start (Console Tracing)

```java
TraceProcessor processor = new ConsoleTraceProcessor(true);
TraceProvider provider = TraceProvider.getGlobalTraceProvider();
provider.registerProcessor(processor);

Trace trace =
    provider.createTrace(
        Trace.builder()
            .traceId(TracingUtils.generateTraceId())
            .name("Example workflow")
            .build());

TraceContext.withTrace(
    trace,
    () -> {
      Span<CustomSpanData> span =
          provider.createSpan(
              Span.<CustomSpanData>builder()
                  .spanId(TracingUtils.generateSpanId())
                  .data(CustomSpanData.builder().name("custom-op").build())
                  .build());
      span.start();
      try {
        // Do work
        return null;
      } finally {
        span.end();
      }
    });
```

## Export to OpenAI Tracing

The SDK includes an OpenAI exporter and a batch processor:

```java
TraceProvider.initializeWithDefaultCloudTracing();
```

This configures:

- `OpenAITraceExporter` (uses `OPENAI_API_KEY`)
- `BatchTraceProcessor` (batching and retries)

## Disabling Tracing

Tracing can be disabled globally:

- Environment variable: `OPENAI_AGENTS_DISABLE_TRACING=1` or `true`
- Runtime: `TraceProvider.getGlobalTraceProvider().setDisabled(true)`

When disabled, `NoopTrace` and `NoopSpan` are returned with near-zero overhead.

## Context Propagation

Tracing uses `ThreadLocal`. For async code, use helpers to preserve context:

```java
Trace trace = Trace.builder().traceId(TracingUtils.generateTraceId()).name("async").build();

TraceContext.withTrace(
    trace,
    () ->
        TraceContext.supplyAsync(
            () -> {
              // Trace context is preserved here
              return "ok";
            }));
```

## Processor Options

Built-in processors and exporters:

- `ConsoleTraceProcessor`: prints trace/span JSON to stdout (good for local dev).
- `OpenAITraceExporter`: HTTP export to OpenAI tracing endpoint.
- `BatchTraceProcessor`: buffers and sends traces/spans in batches.

You can also register multiple processors with the global provider.

## Examples

- `TracingExample.java` - end-to-end trace and span creation (console only).
- `AgentWithTracingExample.java` - manual tracing around an agent run.
