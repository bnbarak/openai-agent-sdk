# OpenAI Agents SDK - Java Implementation Plan

## Project Goal
Create feature parity implementation of the OpenAI Agents SDK in Java, based on the TypeScript reference implementation at https://github.com/openai/openai-agents-js

## Source Reference
- **Repository**: https://github.com/openai/openai-agents-js
- **Local Clone**: `/tmp/openai-agents-js`
- **Documentation**: https://openai.github.io/openai-agents-js/
- **Base Commit**: main branch (as of January 2026)

### OpenAI Tracing API (Verified Jan 17, 2026)
Traced from actual JS SDK requests (`/tmp/openai-agents-js/test-trace-request.js`)

- **Endpoint**: `https://api.openai.com/v1/traces/ingest`
- **Method**: POST
- **Required Headers**: `Authorization: Bearer YOUR_API_KEY`, `OpenAI-Beta: traces=v1`
- **Response**: 204 No Content
- **Trace format**: `{"object": "trace", "id": "trace_...", "workflow_name": "...", "group_id": null, "metadata": {}}`
- **Span format**: `{"object": "trace.span", "id": "span_...", "trace_id": "...", "parent_id": null, "started_at": "ISO8601", "ended_at": "ISO8601", "span_data": {...}, "error": null}`

## Implementation Phases

### Phase 1: Foundation and Structure (Current Phase)
**Goal**: Generate complete Java skeleton with all classes, interfaces, and method signatures

#### 1.1 Setup Project Structure ✓
- [x] Create Maven multi-module project
- [x] Define module structure
- [ ] Create base exception classes
- [ ] Configure Maven dependencies

#### 1.2 Generate Core Skeletons (In Progress)
- [ ] Scan TypeScript source files
- [ ] Extract all classes and interfaces
- [ ] Generate Java equivalents with NotImplementedException
- [ ] Create proper package structure
- [ ] Map TypeScript types to Java types

**Packages to Generate**:
1. `agents-core` - Core abstractions and interfaces
2. `agents-openai` - OpenAI-specific implementations
3. `agents` - Main package combining core and OpenAI
4. `agents-realtime` - Stub package with README only

**Files to Generate** (from TypeScript):
```
agents-core/src/
├── agent.ts → Agent.java
├── computer.ts → Computer.java
├── config.ts → Config.java
├── defaultModel.ts → DefaultModel.java
├── editor.ts → Editor.java
├── errors.ts → [Multiple exception classes]
├── events.ts → Events.java
├── guardrail.ts → Guardrail.java
├── handoff.ts → Handoff.java
├── items.ts → Items.java
├── lifecycle.ts → Lifecycle.java
├── logger.ts → Logger.java
├── mcp.ts → Mcp.java
├── mcpUtil.ts → McpUtil.java
├── model.ts → Model.java
├── providers.ts → Providers.java
├── result.ts → Result.java, RunResult.java, StreamedRunResult.java
├── run.ts → Run.java
├── runContext.ts → RunContext.java
├── runState.ts → RunState.java
├── shell.ts → Shell.java
├── tool.ts → Tool.java, FunctionTool.java, etc.
├── toolGuardrail.ts → ToolGuardrail.java
├── usage.ts → Usage.java
└── [subdirectories]
    ├── extensions/ → com.openai.agents.core.extensions
    ├── memory/ → com.openai.agents.core.memory
    ├── runner/ → com.openai.agents.core.runner
    ├── tracing/ → com.openai.agents.core.tracing
    └── types/ → com.openai.agents.core.types
```

### Phase 2: Core Implementation
**Goal**: Implement core functionality without external dependencies

#### 2.1 Basic Data Structures
- [ ] Implement Item classes (AgentInputItem, AgentOutputItem, etc.)
- [ ] Implement RunState
- [ ] Implement RunContext
- [ ] Implement basic Result classes

#### 2.2 Agent Core
- [ ] Implement Agent class
- [ ] Implement Agent configuration
- [ ] Implement Agent lifecycle
- [ ] Implement system prompt generation

#### 2.3 Tool System
- [ ] Implement Tool interface
- [ ] Implement FunctionTool
- [ ] Implement tool schema validation
- [ ] Implement tool execution framework

#### 2.4 Handoff System
- [ ] Implement Handoff class
- [ ] Implement handoff execution
- [ ] Implement input filtering
- [ ] Implement agent transitions

#### 2.5 Session Management
- [ ] Implement Session interface
- [ ] Implement MemorySession
- [ ] Implement session persistence
- [ ] Implement history management

### Phase 3: Runner and Execution Loop
**Goal**: Implement the core agent execution logic

#### 3.1 Runner Implementation
- [ ] Implement Runner class
- [ ] Implement run() method
- [ ] Implement turn management
- [ ] Implement max turns enforcement

#### 3.2 Execution Loop
- [ ] Implement agent loop state machine
- [ ] Implement tool call execution
- [ ] Implement handoff execution
- [ ] Implement interruption handling

#### 3.3 State Management
- [ ] Implement state serialization
- [ ] Implement state deserialization
- [ ] Implement resumable runs
- [ ] Implement approval tracking

### Phase 4: OpenAI Integration
**Goal**: Integrate with OpenAI API

#### 4.1 Model Integration
- [ ] Implement OpenAI API client
- [ ] Implement ChatCompletions integration
- [ ] Implement model response parsing
- [ ] Implement streaming support

#### 4.2 OpenAI Sessions
- [ ] Implement OpenAIConversationsSession
- [ ] Implement conversation API integration
- [ ] Implement response compaction
- [ ] Implement server-managed history

#### 4.3 OpenAI Tools
- [ ] Implement hosted tools (web search, file search)
- [ ] Implement code interpreter integration
- [ ] Implement image generation tools
- [ ] Implement tool response handling

### Phase 5: Advanced Features
**Goal**: Implement advanced SDK features

#### 5.1 Guardrails
- [ ] Implement input guardrails
- [ ] Implement output guardrails
- [ ] Implement tool guardrails
- [ ] Implement parallel guardrail execution
- [ ] Implement tripwire error handling

#### 5.2 Streaming
- [ ] Implement StreamedRunResult
- [ ] Implement event streaming
- [ ] Implement text stream extraction
- [ ] Implement Node.js-compatible streams (if applicable)

#### 5.3 Tracing
- [ ] Implement Trace interface
- [ ] Implement Span interface
- [ ] Implement default trace processor
- [ ] Implement custom trace processors
- [ ] Implement observability integrations

#### 5.4 Context Management
- [ ] Implement context propagation
- [ ] Implement context dependency injection
- [ ] Implement dynamic instructions
- [ ] Implement lifecycle hooks

### Phase 6: Extended Features
**Goal**: Implement additional SDK capabilities

#### 6.1 MCP Integration
- [ ] Implement Model Context Protocol client
- [ ] Implement MCP server connections
- [ ] Implement MCP tool conversion
- [ ] Implement MCP utilities

#### 6.2 Structured Output
- [ ] Implement output type system
- [ ] Implement JSON schema output
- [ ] Implement validation
- [ ] Implement output parsing

#### 6.3 Computer/Shell Tools
- [ ] Implement Computer interface
- [ ] Implement Shell interface
- [ ] Implement Editor interface
- [ ] Implement local tool execution
- [ ] Implement approval workflows

### Phase 7: Testing
**Goal**: Ensure reliability and correctness

#### 7.1 Unit Tests
- [ ] Test Agent class
- [ ] Test Tool system
- [ ] Test Handoff system
- [ ] Test Session management
- [ ] Test RunState and RunContext

#### 7.2 Integration Tests
- [ ] Test full agent runs
- [ ] Test multi-agent handoffs
- [ ] Test streaming
- [ ] Test OpenAI API integration
- [ ] Test guardrails

#### 7.3 Example Applications
- [ ] Port basic examples from TypeScript
- [ ] Create Java-specific examples
- [ ] Create customer service example
- [ ] Create research bot example

### Phase 8: Documentation
**Goal**: Provide comprehensive documentation

#### 8.1 API Documentation
- [ ] Generate JavaDoc for all public APIs
- [ ] Document all interfaces
- [ ] Document all classes
- [ ] Document configuration options

#### 8.2 User Guides
- [ ] Getting started guide
- [ ] Agent creation guide
- [ ] Tool development guide
- [ ] Handoff patterns guide
- [ ] Session management guide
- [ ] Guardrails guide

#### 8.3 Migration Guide
- [ ] Create TypeScript-to-Java comparison
- [ ] Document API differences
- [ ] Provide code examples side-by-side
- [ ] Document limitations

### Phase 9: Realtime/Voice Features (Future)
**Goal**: Implement voice agent capabilities
- [ ] Assess Java WebRTC libraries
- [ ] Design realtime architecture for Java
- [ ] Implement RealtimeAgent
- [ ] Implement voice transport mechanisms
- [ ] Port realtime examples

## Technical Architecture

### Module Structure
```
openAIAgentSDK/
├── pom.xml (parent POM)
├── agents-core/
│   ├── pom.xml
│   └── src/main/java/com/openai/agents/core/
├── agents-openai/
│   ├── pom.xml
│   └── src/main/java/com/openai/agents/openai/
├── agents/
│   ├── pom.xml
│   └── src/main/java/com/openai/agents/
├── agents-realtime/
│   ├── pom.xml
│   ├── README.md (explains not implemented)
│   └── src/main/java/com/openai/agents/realtime/
└── examples/
    ├── pom.xml
    └── src/main/java/com/openai/agents/examples/
```

### Key Dependencies
- OpenAI Java Client (or HTTP client)
- Jackson for JSON processing
- SLF4J for logging
- CompletableFuture for async operations
- Java 21+ (for modern features)
- Optional: Reactor for reactive streams
- Optional: Apache Commons for utilities

### Design Patterns
- **Builder Pattern**: For agent and tool configuration
- **Strategy Pattern**: For model providers
- **Observer Pattern**: For lifecycle events
- **State Pattern**: For agent execution loop
- **Factory Pattern**: For tool creation
- **Decorator Pattern**: For guardrails

## Current Status

### Completed
- [x] Project structure analysis
- [x] TypeScript SDK documentation review
- [x] Clone guidelines documentation
- [x] Implementation plan creation
- [x] Maven project initialization

### In Progress
- [ ] Phase 1.2: Generate Core Skeletons

### Next Steps
1. Write Python script to scan TypeScript files
2. Generate Java class skeletons
3. Create exception classes
4. Validate generated structure
5. Begin Phase 2: Core Implementation

## Success Criteria

### Phase 1 Success (Current)
- All public classes from TypeScript have Java equivalents
- All public interfaces from TypeScript have Java equivalents
- All method signatures are present (with NotImplementedException)
- Project compiles without errors
- Package structure mirrors TypeScript

### Overall Success
- Feature parity with TypeScript SDK
- All examples from TypeScript work in Java
- Comprehensive test coverage (>80%)
- Complete API documentation
- Performance comparable to TypeScript implementation
- Production-ready code quality

## Timeline Estimate
- **Phase 1** (Current): 1-2 days
- **Phase 2-3**: 1-2 weeks
- **Phase 4**: 1 week
- **Phase 5-6**: 2-3 weeks
- **Phase 7-8**: 1-2 weeks
- **Total**: 6-8 weeks for feature parity (excluding realtime)

## Notes and Considerations

### Type System Differences
- TypeScript has union types; Java uses sealed interfaces or inheritance
- TypeScript has structural typing; Java has nominal typing
- Need careful mapping of TypeScript utility types to Java equivalents

### Async Programming Model
- TypeScript uses Promises; Java uses CompletableFuture
- TypeScript has async/await; Java has CompletableFuture chaining
- Need to handle backpressure in streaming scenarios

### Functional Programming
- TypeScript has extensive functional support; Java has limited lambda support
- Need to use Function, Consumer, Supplier interfaces appropriately
- Consider using Vavr library for advanced functional patterns

### JSON Handling
- TypeScript has native JSON support; Java needs library (Jackson/Gson)
- Need schema validation library (JSON Schema validator)
- Consider annotation-based serialization

### Testing Strategy
- Use JUnit 5 for unit tests
- Use Mockito for mocking
- Use Testcontainers for integration tests
- Port TypeScript test cases as reference

## Risk Mitigation

### Risks
1. **Type mapping complexity**: Some TypeScript patterns may not translate cleanly
2. **Async model mismatch**: Promise-based code may be complex in Java
3. **Library dependencies**: Need mature OpenAI Java client
4. **Streaming complexity**: Java streams vs TypeScript async iterables

### Mitigations
1. Create comprehensive type mapping guide
2. Use reactive libraries (Reactor) if needed
3. Evaluate/create OpenAI client wrapper
4. Design custom AsyncIterable interface for Java

## References
- [OpenAI Agents JS Repository](https://github.com/openai/openai-agents-js)
- [OpenAI Agents Documentation](https://openai.github.io/openai-agents-js/)
- [OpenAI API Documentation](https://platform.openai.com/docs)
- [Java CompletableFuture Guide](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/CompletableFuture.html)
