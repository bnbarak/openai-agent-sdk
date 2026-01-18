# Run Context

Learn how to use custom context for tool approval, usage tracking, and application data.

## Overview

`RunContext<TContext>` provides a way to pass custom data through agent execution and control tool behavior. Use it for:

- **Tool Approval**: Control which tools execute during a run
- **Usage Tracking**: Accumulate token costs across API calls
- **Application Data**: Pass user info, session data, or configuration to tools

Every tool invocation receives a `RunContext` instance, giving tools access to your custom data and approval logic.

## Creating a Run Context

### Simple Context

Use `UnknownContext` for basic scenarios:

```java
RunContext<UnknownContext> context = new RunContext<>();

RunResult<UnknownContext, ?> result = Runner.run(agent, "Your prompt", context);
```

### Custom Context

Define your own context class for application data:

```java
public class AppContext {
    private String userId;
    private String sessionId;
    private boolean isPremiumUser;
    private Set<String> allowedActions = new HashSet<>();

    public AppContext(String userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }

    // Getters and setters...
}

// Create and use custom context
AppContext appContext = new AppContext("user_123", "session_456");
appContext.setPremiumUser(true);

RunContext<AppContext> context = new RunContext<>(appContext);

Agent<AppContext, TextOutput> agent = /* ... */;
RunResult<AppContext, ?> result = Runner.run(agent, "Your prompt", context);
```

Tools can then access your custom data:

```java
public class DatabaseTool implements FunctionTool<AppContext, Input, Output> {
    @Override
    public CompletableFuture<Output> invoke(RunContext<AppContext> context, Input input) {
        String userId = context.getContext().userId;
        boolean isPremium = context.getContext().isPremiumUser();

        // Use application data in tool logic
        if (!isPremium) {
            return CompletableFuture.completedFuture(
                new Output("Upgrade to premium for database access")
            );
        }

        // Execute tool with user context
        return performDatabaseQuery(userId, input);
    }
}
```

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/RunContextExample.java)

## Tool Approval System

Control which tools execute using approval patterns:

### Per-Call Approval

Approve individual tool calls one at a time:

```java
RunContext<UnknownContext> context = new RunContext<>();

// Approve a specific tool call
RunToolApprovalItem approval = RunToolApprovalItem.builder()
    .toolName("send_email")
    .toolCallId("call_123")  // Specific call ID
    .build();

context.approveTool(approval);

// Check if approved
boolean isApproved = context.isToolApproved("send_email", "call_123");  // true
boolean otherCall = context.isToolApproved("send_email", "call_456");   // false
```

**Use when**: You want to approve each tool invocation explicitly.

### Permanent Approval

Approve all future calls to a tool:

```java
RunContext<UnknownContext> context = new RunContext<>();

// Approve all calls to this tool
RunToolApprovalItem approval = RunToolApprovalItem.builder()
    .toolName("send_email")
    .toolCallId(null)  // null = all future calls
    .build();

context.approveTool(approval);

// All calls are now approved
boolean call1 = context.isToolApproved("send_email", "call_123");  // true
boolean call2 = context.isToolApproved("send_email", "call_456");  // true
boolean call3 = context.isToolApproved("send_email", "call_789");  // true
```

**Use when**: You trust a tool and want to approve it once for the entire run.

### Tool Rejection

Explicitly reject tool calls:

```java
RunContext<UnknownContext> context = new RunContext<>();

// Reject a specific tool call
RunToolApprovalItem rejection = RunToolApprovalItem.builder()
    .toolName("delete_data")
    .toolCallId("call_123")
    .build();

context.rejectTool(rejection);

// Check if rejected
boolean isRejected = context.isToolRejected("delete_data", "call_123");  // true
```

**Use when**: You want to explicitly deny dangerous or inappropriate tool calls.

### Implementing Approval in Tools

Tools check approval status in `needsApproval()`:

```java
public class SensitiveTool implements FunctionTool<AppContext, Input, Output> {
    @Override
    public boolean needsApproval(RunContext<AppContext> context, Input input) {
        // Require approval for sensitive operations
        return input.getOperation().equals("delete") ||
               input.getOperation().equals("modify");
    }

    @Override
    public CompletableFuture<Output> invoke(RunContext<AppContext> context, Input input) {
        // This only runs if approved (or if needsApproval returned false)
        return CompletableFuture.supplyAsync(() -> {
            return performOperation(input);
        });
    }
}
```

When `needsApproval()` returns `true`, the SDK checks `context.isToolApproved()` before invoking the tool.

## Usage Tracking

Accumulate token usage across multiple API calls:

```java
RunContext<UnknownContext> context = new RunContext<>();

// First API call
RunResult<UnknownContext, ?> result1 = Runner.run(agent, "Question 1", context);
context.addUsage(result1.getUsage());  // Add usage from first call

// Second API call
RunResult<UnknownContext, ?> result2 = Runner.run(agent, "Question 2", context);
context.addUsage(result2.getUsage());  // Add usage from second call

// Third API call
RunResult<UnknownContext, ?> result3 = Runner.run(agent, "Question 3", context);
context.addUsage(result3.getUsage());  // Add usage from third call

// Get total accumulated usage
Usage totalUsage = context.getUsage();
System.out.println("Total tokens: " + totalUsage.getTotalTokens());
System.out.println("Input tokens: " + totalUsage.getInputTokens());
System.out.println("Output tokens: " + totalUsage.getOutputTokens());
```

Track costs across:

- Multi-turn conversations
- Tool-heavy workflows
- Multi-agent handoffs
- Session-based interactions

## Real-World Example: E-Commerce Agent

Combine context data, approvals, and usage tracking:

```java
public class ECommerceContext {
    private String userId;
    private String cartId;
    private double creditLimit;
    private Set<String> preApprovedActions = new HashSet<>();

    public boolean canAfford(double amount) {
        return amount <= creditLimit;
    }

    public void recordPurchase(double amount) {
        creditLimit -= amount;
    }
}

// Create context with user data
ECommerceContext appContext = new ECommerceContext("user_123", "cart_456", 500.0);
appContext.getPreApprovedActions().add("view_products");
appContext.getPreApprovedActions().add("add_to_cart");

RunContext<ECommerceContext> context = new RunContext<>(appContext);

// Pre-approve safe operations
context.approveTool(RunToolApprovalItem.builder()
    .toolName("view_products")
    .toolCallId(null)  // Approve all calls
    .build());

// Define tools that use context
public class CheckoutTool implements FunctionTool<ECommerceContext, Input, Output> {
    @Override
    public boolean needsApproval(RunContext<ECommerceContext> context, Input input) {
        // Require approval for purchases over $100
        return input.getTotalAmount() > 100.0;
    }

    @Override
    public boolean isEnabled(RunContext<ECommerceContext> context) {
        // Only enable if user can afford the purchase
        return context.getContext().canAfford(input.getTotalAmount());
    }

    @Override
    public CompletableFuture<Output> invoke(RunContext<ECommerceContext> context, Input input) {
        return CompletableFuture.supplyAsync(() -> {
            ECommerceContext appContext = context.getContext();

            // Check credit limit
            if (!appContext.canAfford(input.getTotalAmount())) {
                return new Output("Insufficient credit limit");
            }

            // Process purchase
            appContext.recordPurchase(input.getTotalAmount());

            return new Output("Purchase successful. New limit: $" + appContext.getCreditLimit());
        });
    }
}

// Create agent with e-commerce tools
Agent<ECommerceContext, TextOutput> agent =
    Agent.<ECommerceContext, TextOutput>builder()
        .name("ShoppingAssistant")
        .instructions("You are a shopping assistant. Help users browse and purchase products.")
        .tools(List.of(
            new ViewProductsTool(),
            new AddToCartTool(),
            new CheckoutTool()
        ))
        .build();

// Run agent
RunResult<ECommerceContext, ?> result = Runner.run(
    agent,
    "I want to buy the laptop for $1200",
    context
);

// Check final state
System.out.println("Response: " + result.getFinalOutput());
System.out.println("Remaining credit: $" + context.getContext().getCreditLimit());
System.out.println("Total tokens: " + context.getUsage().getTotalTokens());
```

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/RealWorldRunContextExample.java)

## Context Serialization

Contexts can be serialized for persistence or distribution:

```java
RunContext<AppContext> context = new RunContext<>(appContext);

// Serialize to JSON
String json = context.toJson();

// Deserialize from JSON
RunContext<AppContext> restored = RunContext.fromJson(json, AppContext.class);
```

Use serialization for:

- Saving context between requests
- Distributing context across services
- Debugging and logging
- Audit trails

## Context with Sessions

Combine context with sessions for stateful conversations:

```java
AppContext appContext = new AppContext("user_123", "session_456");
RunContext<AppContext> context = new RunContext<>(appContext);

Session session = new MemorySession("conversation_123");
RunConfig config = RunConfig.builder()
    .session(session)
    .build();

Agent<AppContext, TextOutput> agent = /* ... */;

// First turn
RunResult<AppContext, ?> result1 = Runner.run(
    agent,
    "My name is Alice",
    context,
    config
);

// Second turn - agent remembers Alice, context maintains approvals
RunResult<AppContext, ?> result2 = Runner.run(
    agent,
    "What's my name?",
    context,
    config
);
```

Context handles approvals and data; session handles conversation memory.

## Best Practices

!!! tip "Context Design"
    - **Keep Context Small**: Store only essential data
    - **Immutable Data**: Prefer immutable context fields when possible
    - **Clear Ownership**: Document which layer manages which context fields
    - **Type Safety**: Use custom context classes for type-safe data access
    - **Serializable**: Ensure context classes can be serialized if needed

!!! tip "Approval Strategy"
    - **Per-Call for Sensitive**: Use per-call approval for dangerous operations
    - **Permanent for Safe**: Permanently approve read-only tools
    - **Context-Based Logic**: Implement approval rules in `needsApproval()`
    - **User Prompting**: Prompt users before approving sensitive tools
    - **Audit Trail**: Log all approvals and rejections

!!! tip "Usage Tracking"
    - **Accumulate Properly**: Always call `context.addUsage()` after each run
    - **Monitor Costs**: Check accumulated usage before expensive operations
    - **Budget Limits**: Implement cost limits in context
    - **Per-User Tracking**: Track usage per user for billing
    - **Debug Aid**: Use usage data to optimize prompts and tools

!!! warning "Common Mistakes"
    - **Forgetting to Add Usage**: Not calling `context.addUsage()` after runs
    - **Approval Leaks**: Approving tools too broadly (security risk)
    - **Context Bloat**: Storing unnecessary data in context
    - **Missing Checks**: Not checking `isToolApproved()` in tools
    - **Shared Context**: Reusing context across unrelated operations

## Advanced Patterns

### Dynamic Approval Based on Input

```java
@Override
public boolean needsApproval(RunContext<AppContext> context, Input input) {
    // Approve automatically for small amounts
    if (input.getAmount() < 10.0) {
        return false;
    }

    // Require approval for large amounts
    if (input.getAmount() > 1000.0) {
        return true;
    }

    // Check context for mid-range amounts
    return !context.getContext().isPreApproved(input.getOperation());
}
```

### Cumulative Approval

```java
public class BudgetContext {
    private double budget;
    private double spent = 0.0;

    public boolean canSpend(double amount) {
        return (spent + amount) <= budget;
    }

    public void recordSpending(double amount) {
        spent += amount;
    }
}

@Override
public boolean needsApproval(RunContext<BudgetContext> context, Input input) {
    // Check if within budget
    return !context.getContext().canSpend(input.getAmount());
}
```

### Approval Chains

```java
@Override
public boolean needsApproval(RunContext<AppContext> context, Input input) {
    AppContext appContext = context.getContext();

    // Multiple approval conditions
    if (appContext.getUserRole().equals("admin")) {
        return false;  // Admins don't need approval
    }

    if (appContext.hasPermission(input.getOperation())) {
        return false;  // User has explicit permission
    }

    if (input.getAmount() < appContext.getApprovalThreshold()) {
        return false;  // Below threshold
    }

    return true;  // Require approval
}
```

## Next Steps

- [Tools](tools.md) - Implement tools that use context
- [Handoffs](handoffs.md) - Pass context across agent handoffs
- [Sessions](sessions.md) - Combine context with conversation memory
- [Guardrails](guardrails.md) - Add safety constraints using context

## Additional Resources

- [RunContextExample.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/RunContextExample.java) - Basic context patterns
- [RealWorldRunContextExample.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/RealWorldRunContextExample.java) - E-commerce scenario
- [API Reference](../javadoc/index.html) - Complete Javadoc documentation
