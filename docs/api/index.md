# API Reference

Complete API documentation is available in the Javadoc:

**[📚 View Full Javadoc →](../javadoc/index.html)**

## Core Packages

- **[com.acoliteai.agentsdk.core](../javadoc/com/acoliteai/agentsdk/core/package-summary.html)** - Core agent framework classes
  - `Agent` - Main agent class
  - `Runner` - Execute agents
  - `RunContext` - Runtime context for tool execution
  - `FunctionTool` - Tool interface

- **[com.acoliteai.agentsdk.core.types](../javadoc/com/acoliteai/agentsdk/core/types/package-summary.html)** - Type definitions
  - `TextOutput` - Plain text output
  - `JsonSchemaOutput` - Structured JSON output
  - `UnknownContext` - Default context type

- **[com.acoliteai.agentsdk.core.tracing](../javadoc/com/acoliteai/agentsdk/core/tracing/package-summary.html)** - Tracing and observability
  - `TraceProvider` - Trace management
  - `Trace` - Trace context
  - `Span` - Span tracking

- **[com.acoliteai.agentsdk.core.sessions](../javadoc/com/acoliteai/agentsdk/core/sessions/package-summary.html)** - Session management
  - `Session` - Session interface
  - `MemorySession` - In-memory sessions
  - `SQLiteSession` - Persistent sessions

## Viewing Locally

Generate Javadocs locally:

```bash
mvn javadoc:javadoc
open target/site/apidocs/index.html
```

Or view directly in your IDE by hovering over classes and methods.
