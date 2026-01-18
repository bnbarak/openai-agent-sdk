# API Reference

Complete API documentation is available in the Javadoc:

**[📚 View Full Javadoc →](../javadoc/index.html)**

## Core Packages

- **[ai.acolite.agentsdk.core](../javadoc/ai/acolite/agentsdk/core/package-summary.html)** - Core agent framework classes
  - `Agent` - Main agent class
  - `Runner` - Execute agents
  - `RunContext` - Runtime context for tool execution
  - `FunctionTool` - Tool interface

- **[ai.acolite.agentsdk.core.types](../javadoc/ai/acolite/agentsdk/core/types/package-summary.html)** - Type definitions
  - `TextOutput` - Plain text output
  - `JsonSchemaOutput` - Structured JSON output
  - `UnknownContext` - Default context type

- **[ai.acolite.agentsdk.core.tracing](../javadoc/ai/acolite/agentsdk/core/tracing/package-summary.html)** - Tracing and observability
  - `TraceProvider` - Trace management
  - `Trace` - Trace context
  - `Span` - Span tracking

- **[ai.acolite.agentsdk.core.sessions](../javadoc/ai/acolite/agentsdk/core/sessions/package-summary.html)** - Session management
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
