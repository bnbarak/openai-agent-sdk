# Handoffs

Learn how to build multi-agent systems where specialized agents collaborate on complex tasks.

## Overview

Handoffs enable conversations to transfer between specialized agents. A triage agent routes requests to experts, each handling specific domains like billing, technical support, or account management. This pattern improves response quality by leveraging specialized knowledge and clear task boundaries.

Key benefits:

- **Specialization**: Each agent focuses on its expertise
- **Maintainability**: Modify specialist agents independently
- **Scalability**: Add new specialists without changing existing agents
- **Clarity**: Clean separation of responsibilities

## Creating a Simple Handoff

Define a specialist agent with a `handoffDescription`, then configure the triage agent to hand off to it:

```java
// Create a specialist agent for technical support
Agent<UnknownContext, TextOutput> supportAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Technical Support")
        .instructions(
            "You are a technical support specialist. "
                + "Help users troubleshoot technical issues, provide clear solutions, "
                + "and explain technical concepts in simple terms.")
        .handoffDescription(
            "Hands off technical support and troubleshooting questions to this agent")
        .build();

// Create a triage agent that can hand off to the specialist
Agent<UnknownContext, TextOutput> triageAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Triage Agent")
        .instructions(
            "You are a triage agent. Your ONLY job is to transfer users to specialists. "
                + "For ANY technical problem, crash, or bug, you MUST call transfer_to_Technical_Support. "
                + "DO NOT try to help directly. ALWAYS transfer.")
        .handoffs(List.of(supportAgent))
        .build();

// Run with a technical question - triggers automatic handoff
RunResult<UnknownContext, ?> result = Runner.run(
    triageAgent,
    "My application keeps crashing when I click the save button. How do I fix this?"
);

System.out.println(result.getFinalOutput());
// Output from Technical Support specialist: "Let's troubleshoot this crash..."
```

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/ai/acolite/agentsdk/examples/AgentHandoffExample.java)

## How Handoffs Work

1. **Registration**: Triage agent registers specialists via `.handoffs(List.of(...))`
2. **Tool Generation**: SDK generates `transfer_to_<AgentName>` functions automatically
3. **Agent Decision**: Triage agent calls the appropriate transfer function
4. **Execution Transfer**: Specialist agent takes over and handles the request
5. **Final Response**: User receives the specialist's response

The SDK handles all transfer mechanics automatically. You define specialists and routing logic through instructions.

## Multiple Specialists

Configure a triage agent to route to multiple specialists:

```java
// Create specialized agents
Agent<UnknownContext, TextOutput> billingAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Billing Specialist")
        .instructions("You are a billing specialist. Help users with payment issues.")
        .handoffDescription("Handles billing, payments, refunds, and invoice questions")
        .build();

Agent<UnknownContext, TextOutput> technicalAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Technical Support")
        .instructions("You are a technical support specialist. Help with bugs and crashes.")
        .handoffDescription("Handles technical issues, bugs, and troubleshooting")
        .build();

Agent<UnknownContext, TextOutput> accountAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Account Manager")
        .instructions("You are an account manager. Help with account settings.")
        .handoffDescription("Handles account management and settings")
        .build();

// Create triage agent with multiple handoff options
Agent<UnknownContext, TextOutput> triageAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Customer Service Triage")
        .instructions("""
            You are a triage agent. Your ONLY job is to transfer to specialists.
            DO NOT help directly. You MUST call the appropriate transfer function:
            - For billing, payments, refunds: call transfer_to_Billing_Specialist
            - For technical issues, bugs, crashes: call transfer_to_Technical_Support
            - For account settings, profile: call transfer_to_Account_Manager
            ALWAYS transfer. DO NOT answer questions yourself.
            """)
        .handoffs(List.of(billingAgent, technicalAgent, accountAgent))
        .build();

// Test different question types
RunResult<UnknownContext, ?> result1 = Runner.run(
    triageAgent,
    "I was charged twice for my subscription this month"
);
// Routes to Billing Specialist

RunResult<UnknownContext, ?> result2 = Runner.run(
    triageAgent,
    "How do I change my email address?"
);
// Routes to Account Manager

RunResult<UnknownContext, ?> result3 = Runner.run(
    triageAgent,
    "The app crashes every time I try to export data"
);
// Routes to Technical Support
```

The triage agent analyzes each question and selects the appropriate specialist automatically.

## Handoff Description

The `handoffDescription` explains when to use each specialist. This description is presented to the triage agent as part of the transfer function documentation:

```java
Agent<UnknownContext, TextOutput> specialist =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Billing Specialist")
        .instructions("Handle billing questions...")
        .handoffDescription("Handles billing, payments, refunds, and invoice questions")
        .build();
```

!!! tip "Writing Good Handoff Descriptions"
    - Be specific about the specialist's domain
    - List key topics or question types
    - Use clear, action-oriented language
    - Avoid overlap between specialists
    - Keep descriptions concise (1-2 sentences)

## Tracking Handoffs

Monitor handoff execution using `RunResult`:

```java
RunResult<UnknownContext, ?> result = Runner.run(triageAgent, "My question...");

// Count handoffs
long handoffCount = result.getNewItems().stream()
    .filter(item -> item instanceof RunHandoffOutputItem)
    .count();

System.out.println("Handoffs executed: " + handoffCount);

// Get handoff details
result.getNewItems().stream()
    .filter(item -> item instanceof RunHandoffOutputItem)
    .map(item -> (RunHandoffOutputItem) item)
    .forEach(handoff -> {
        System.out.println("From: " + handoff.getFromAgent());
        System.out.println("To: " + handoff.getToAgent());
    });
```

Track handoffs for:

- Debugging routing logic
- Analyzing specialist utilization
- Monitoring conversation flow
- Improving triage instructions

## Triage Agent Patterns

### Direct Transfer Pattern

Triage agent immediately transfers without responding:

```java
Agent<UnknownContext, TextOutput> triageAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Triage")
        .instructions("""
            You are a triage agent. Your ONLY job is to transfer to specialists.
            DO NOT answer questions. DO NOT provide information.
            Immediately call the appropriate transfer function.
            """)
        .handoffs(List.of(specialist1, specialist2, specialist3))
        .build();
```

**Use when**: You want specialists to handle all interactions.

### Greeting + Transfer Pattern

Triage agent greets briefly, then transfers:

```java
Agent<UnknownContext, TextOutput> triageAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Triage")
        .instructions("""
            You are a triage agent. Greet the user briefly, then transfer:
            1. Acknowledge their question in one sentence
            2. Immediately call the appropriate transfer function
            DO NOT attempt to answer. Let specialists handle all questions.
            """)
        .handoffs(List.of(specialist1, specialist2, specialist3))
        .build();
```

**Use when**: You want a friendly greeting before specialist engagement.

### Smart Routing Pattern

Triage agent clarifies ambiguous requests before transferring:

```java
Agent<UnknownContext, TextOutput> triageAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Triage")
        .instructions("""
            You are a triage agent. For clear requests, transfer immediately.
            For ambiguous requests, ask ONE clarifying question, then transfer.
            Never provide answers - specialists handle all questions.
            """)
        .handoffs(List.of(specialist1, specialist2, specialist3))
        .build();
```

**Use when**: Questions might be ambiguous and benefit from clarification.

## Nested Handoffs

Specialists can themselves have handoffs to create hierarchical routing:

```java
// Level 2: Sub-specialists
Agent<UnknownContext, TextOutput> refundSpecialist = /* ... */;
Agent<UnknownContext, TextOutput> invoiceSpecialist = /* ... */;

// Level 1: Domain specialist with sub-specialists
Agent<UnknownContext, TextOutput> billingAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Billing Specialist")
        .instructions("""
            You are a billing specialist. For general billing questions, answer directly.
            For refunds, call transfer_to_Refund_Specialist.
            For invoice issues, call transfer_to_Invoice_Specialist.
            """)
        .handoffDescription("Handles billing, payments, and financial questions")
        .handoffs(List.of(refundSpecialist, invoiceSpecialist))
        .build();

// Level 0: Triage
Agent<UnknownContext, TextOutput> triageAgent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Triage")
        .instructions("Route to appropriate specialist...")
        .handoffs(List.of(billingAgent, /* other specialists */))
        .build();
```

This creates a three-level hierarchy: Triage → Billing → Refund/Invoice specialists.

## Session Management with Handoffs

Use sessions to maintain context across handoffs:

```java
Session session = new MemorySession("customer-123");
RunConfig config = RunConfig.builder().session(session).build();

// First interaction - routes to billing
RunResult<UnknownContext, ?> result1 = Runner.run(
    triageAgent,
    "I was charged incorrectly",
    config
);

// Follow-up question - specialist remembers context
RunResult<UnknownContext, ?> result2 = Runner.run(
    triageAgent,
    "Can you refund the extra charge?",
    config
);

// Context is preserved across handoffs
```

Specialists access full conversation history, including interactions before the handoff.

## Best Practices

!!! tip "Specialist Design"
    - **Narrow Scope**: Each specialist handles one clear domain
    - **Complete Instructions**: Provide comprehensive guidance for the specialist's area
    - **Avoid Overlaps**: Clearly distinguish between specialist domains
    - **Test Coverage**: Verify all question types route correctly
    - **Clear Boundaries**: Use handoff descriptions to define scope explicitly

!!! tip "Triage Instructions"
    - **Explicit Routing**: Clearly specify which specialist handles what
    - **Prevent Answering**: Instruct triage not to answer questions directly
    - **Function Names**: Reference exact transfer function names in instructions
    - **Edge Cases**: Address ambiguous scenarios explicitly
    - **Mandatory Transfers**: Use strong language like "MUST transfer" or "ALWAYS call"

!!! tip "Performance"
    - **Minimize Turns**: Design for single-handoff scenarios
    - **Session Reuse**: Use sessions to reduce context repetition
    - **Monitor Costs**: Track token usage across multi-agent conversations
    - **Test Routing**: Validate triage logic with representative questions
    - **Measure Accuracy**: Monitor misrouted questions

!!! warning "Common Pitfalls"
    - **Triage Answering**: Triage agents trying to help instead of transferring
    - **Unclear Boundaries**: Overlapping specialist domains causing confusion
    - **Missing Descriptions**: Handoff descriptions that don't guide routing
    - **Excessive Nesting**: Too many handoff levels increasing complexity
    - **Context Loss**: Not using sessions for multi-turn handoff conversations

## When to Use Handoffs

**Use handoffs when:**

- Different questions require different expertise
- You want modular, independent agent development
- Routing logic can be clearly defined
- You need to scale specialists independently

**Don't use handoffs when:**

- A single agent can handle all scenarios
- Questions require seamless context across all topics
- Handoff overhead outweighs specialization benefits
- Routing logic is too complex or ambiguous

## Next Steps

- [Run Context](run-context.md) - Custom context and tool approval across handoffs
- [Sessions](sessions.md) - Maintain conversation memory through handoffs
- [Tools](tools.md) - Add specialized tools to specialist agents
- [Tracing](tracing.md) - Monitor handoff execution in detail

## Additional Resources

- [AgentHandoffExample.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/ai/acolite/agentsdk/examples/AgentHandoffExample.java) - Complete handoff examples
- [API Reference](../javadoc/index.html) - Complete Javadoc documentation
