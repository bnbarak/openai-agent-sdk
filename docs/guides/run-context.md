# Run Context

RunContext lets you carry application data through a run, control tool approvals, and track usage.
It is the shared state object that tools, guardrails, and the runner can read and update.

## What RunContext Does

- **Application data**: user, session, org, feature flags, service clients, etc.
- **Tool approvals**: allow or block tool calls per-call or permanently.
- **Usage tracking**: accumulate token usage across turns and across runs.

If you do not supply a RunContext, the runner creates one with `UnknownContext`.

## Create and Pass a RunContext

### Default (UnknownContext)

```java
RunContext<UnknownContext> context = new RunContext<>();

RunConfig config =
    RunConfig.builder().context(java.util.Optional.of(context)).build();

RunResult<UnknownContext, ?> result = Runner.run(agent, "Your prompt", config);
```

### Custom Context Data

```java
public class AppContext {
  String userId;
  String sessionId;
  boolean premiumUser;
}

AppContext appContext = new AppContext();
appContext.userId = "user_123";
appContext.sessionId = "session_456";
appContext.premiumUser = true;

RunContext<AppContext> context = new RunContext<>(appContext);
RunConfig config =
    RunConfig.builder().context(java.util.Optional.of(context)).build();

RunResult<AppContext, ?> result = Runner.run(agent, "Your prompt", config);
```

### Access Context in Tools

Every tool receives the RunContext:

```java
public class DatabaseTool implements FunctionTool<AppContext, Input, Output> {
  @Override
  public CompletableFuture<Output> invoke(RunContext<AppContext> context, Input input) {
    AppContext appContext = context.getContext();
    if (!appContext.premiumUser) {
      return CompletableFuture.completedFuture(new Output("Upgrade required."));
    }
    return performDatabaseQuery(appContext.userId, input);
  }
}
```

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/RunContextExample.java)

## Tool Approval System

Approval is tri-state:

- `true` = approved
- `false` = rejected
- `null` = pending decision

### Per-Call Approval

```java
RunContext<UnknownContext> context = new RunContext<>();

RunToolApprovalItem approval =
    RunToolApprovalItem.builder()
        .toolName("send_email")
        .toolCallId("call_123")
        .build();

context.approveTool(approval);

Boolean approved = context.isToolApproved("send_email", "call_123"); // true
Boolean other = context.isToolApproved("send_email", "call_456");    // null
```

### Permanent Approval

```java
RunToolApprovalItem approval =
    RunToolApprovalItem.builder()
        .toolName("send_email")
        .toolCallId("call_123")
        .build();

context.approveTool(approval, true);

context.isToolApproved("send_email", "call_123"); // true
context.isToolApproved("send_email", "call_456"); // true
```

### Rejection (Per-Call or Permanent)

```java
RunToolApprovalItem rejection =
    RunToolApprovalItem.builder()
        .toolName("delete_data")
        .toolCallId("call_999")
        .build();

context.rejectTool(rejection, true);
context.isToolApproved("delete_data", "call_999"); // false
```

### Enforcing Approval in Tools

Tools declare approval needs with `needsApproval()`:

```java
public class SensitiveTool implements FunctionTool<AppContext, Input, Output> {
  @Override
  public boolean needsApproval(RunContext<AppContext> context, Input input) {
    return input.getOperation().equals("delete");
  }
}
```

When `needsApproval()` returns `true`, the runner checks
`context.isToolApproved(toolName, toolCallId)` before executing the tool.

## Usage Tracking

When you pass a RunContext via RunConfig, the runner accumulates usage automatically.
If you want to aggregate usage yourself (outside the runner), use `addUsage()`:

```java
RunContext<UnknownContext> context = new RunContext<>();

context.addUsage(Usage.builder().inputTokens(100.0).outputTokens(50.0).totalTokens(150.0).build());
context.addUsage(Usage.builder().inputTokens(200.0).outputTokens(75.0).totalTokens(275.0).build());

Usage total = context.getUsage();
System.out.println("Total tokens: " + total.getTotalTokens());
```

## Serialization and Restore

RunContext can serialize to a map for storage or debugging:

```java
RunContext<AppContext> context = new RunContext<>(appContext);
context.addUsage(Usage.builder().totalTokens(500.0).build());

Map<String, Object> json = context.toJSON();
```

To restore approvals from stored state, use `rebuildApprovals()`:

```java
RunContext<AppContext> restored = new RunContext<>(appContext);
restored.rebuildApprovals(savedApprovals);
```

## Combine Context with Sessions

Context and sessions solve different problems: context is app data and approvals, session is
conversation memory. Use both via RunConfig:

```java
RunContext<AppContext> context = new RunContext<>(appContext);
Session session = new MemorySession("conversation_123");

RunConfig config =
    RunConfig.builder()
        .context(java.util.Optional.of(context))
        .session(session)
        .build();

RunResult<AppContext, ?> result = Runner.run(agent, "My name is Alice", config);
```

## Real-World Example

[RealWorldRunContextExample.java →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/RealWorldRunContextExample.java)

## Best Practices

- Keep context small and focused on app data and approvals.
- Prefer immutable data where possible, or document ownership clearly.
- Use per-call approval for sensitive tools; permanent approval for safe tools.
- Avoid double-counting usage if the runner already tracks it.
- Make context serializable if you need persistence across requests.

## Next Steps

- [Tools](tools.md) - Implement tools that use context
- [Handoffs](handoffs.md) - Pass context across agent handoffs
- [Sessions](sessions.md) - Combine context with conversation memory
- [Guardrails](guardrails.md) - Add safety constraints using context
