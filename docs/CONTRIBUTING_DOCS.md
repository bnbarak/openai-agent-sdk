# Documentation Contribution Guidelines

This guide explains how to write and update documentation in the `docs/` directory.

## Table of Contents

- [Philosophy](#philosophy)
- [Documentation Structure](#documentation-structure)
- [Writing Style](#writing-style)
- [Including Code Examples](#including-code-examples)
- [Linking to API Documentation](#linking-to-api-documentation)
- [File Naming Conventions](#file-naming-conventions)
- [MkDocs Syntax](#mkdocs-syntax)
- [Testing Documentation](#testing-documentation)
- [AI-Generated Documentation](#ai-generated-documentation)

---

## Philosophy

### One Source of Truth

**Never copy-paste code into documentation.** Instead:

1. Write real, runnable Java examples in `src/main/java/com/acoliteai/agentsdk/examples/`
2. Test them in CI (they run in E2E workflow)
3. Reference them in docs via links or includes

**Why?** Code examples can go stale. If they're real files that compile and run, they can't lie.

### Docs People Actually Read

Write documentation like you're explaining to a colleague, not writing a textbook:

- **Start with the outcome**: "By the end of this guide, you'll have a working agent."
- **Show, don't tell**: Code example first, explanation second
- **Keep it scannable**: Use headings, bullets, code blocks
- **No fluff**: Get to the point

---

## Documentation Structure

Our documentation structure is inspired by the [OpenAI Agents TypeScript SDK](https://openai.github.io/openai-agents-js/) but adapted for Java:

```
docs/
├── CONTRIBUTING_DOCS.md     # This file (guideline for contributors)
├── quickstart.md           # First stop for new users - get started in 5 minutes
├── guides/                 # Core guides (task-oriented, with inline examples)
│   ├── agents.md          # Creating and configuring agents
│   ├── running-agents.md  # How to run agents and handle results
│   ├── tools.md           # Defining custom tools with FunctionTool
│   ├── handoffs.md        # Agent-to-agent handoffs
│   ├── sessions.md        # Session management (MemorySession, SQLiteSession)
│   ├── models.md          # Working with different OpenAI models
│   ├── guardrails.md      # Input/output validation and safety
│   ├── streaming.md       # Streaming responses
│   ├── tracing.md         # Distributed tracing and observability
│   └── troubleshooting.md # Common issues and solutions
├── examples/              # Real-world examples (outcome-oriented)
└── api-reference/         # Javadoc-generated API docs
    └── (auto-generated)
```

### When to Create a New Doc

- **Guide**: Task-oriented, "How do I X?" - Always include inline code examples
- **Example**: Outcome-oriented, "Build a customer service bot" - Show complete working code
- **Reference**: Information-oriented, "What are all the config options?" - Link to Javadoc

---

## Writing Style

### Tone

- **Conversational but professional**: "Let's build an agent" not "The instantiation of an agent object shall..."
- **Active voice**: "Create an agent" not "An agent is created"
- **Present tense**: "The agent calls tools" not "The agent will call tools"
- **Direct**: Use "you" and "we"

### Structure

Every guide should have:

```markdown
# Guide Title

Brief 1-2 sentence overview of what this guide covers.

## Prerequisites

- List required knowledge
- Link to other docs if needed

## Quick Example

```java
// Show the working code first
Agent agent = Agent.builder()
    .model(OpenAI.chatCompletionsModel("gpt-4o"))
    .build();
```

[Link to complete example](https://github.com/...)

## Concepts

Now explain what's happening...

## Common Patterns

Show 2-3 common use cases...

## Troubleshooting

Common errors and how to fix them...

## Next Steps

- Link to related guides
- Link to examples
```

### Formatting

- **Code**: Always use syntax-highlighted code blocks with language
- **Commands**: Use `bash` code blocks for shell commands
- **File paths**: Use `code` formatting: `src/main/java/Agent.java`
- **Emphasis**: Use **bold** for important terms, _italic_ sparingly

---

## Including Code Examples

**We love inline examples!** Every guide should show code directly in the page, not just link to files.

### Preferred: Snippet Regions (Inline + One Source of Truth)

This is the **best approach** - it shows code inline while keeping examples testable and current.

Mark regions in your example files:

**In `HelloWorld.java`:**
```java
public class HelloWorld {
    public static void main(String[] args) {
        // region:create-agent
        Agent agent = Agent.builder()
            .model(OpenAI.chatCompletionsModel("gpt-4o"))
            .instructions("You are a helpful assistant.")
            .build();
        // endregion:create-agent

        RunResult result = agent.run("Hello!");
    }
}
```

**In docs:**
```markdown
## Creating an Agent

Here's how to create a basic agent:

```java
--8<-- "src/main/java/.../HelloWorld.java:create-agent"
```

[View complete example](https://github.com/.../HelloWorld.java)
```

**Why this is best:**
- Code appears inline in the guide (readers don't have to click away)
- Code is real, tested, and runs in CI
- Updates to examples automatically appear in docs
- Readers can still click to see the complete file

### Alternative: Include Entire File (Use for Small Examples Only)

For very small examples (< 30 lines), include the entire file:

```markdown
## Example: Creating an Agent

```java
--8<-- "src/main/java/com/acoliteai/agentsdk/examples/HelloWorld.java"
```
```

**Warning**: Only use for small files. Large files make docs hard to scan.

### Last Resort: Link Only (Use Rarely)

Only link without showing code if the example is extremely long or complex:

```markdown
## Example: Creating an Agent

See [HelloWorld.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/HelloWorld.java) for a complete working example.
```

**When to use**: Advanced examples, complete applications, or reference implementations.

### What NOT to Do

❌ **Don't write example code directly in markdown:**

```markdown
<!-- BAD: This code will go stale -->
```java
Agent agent = new Agent(...); // This might not even compile!
```
```

✅ **Do link to real, tested code:**

```markdown
<!-- GOOD: Links to code that compiles and runs -->
See [complete example](https://github.com/.../HelloWorld.java)
```

---

## Linking to API Documentation

### Link to Javadoc

Point users to Javadoc for detailed API reference:

```markdown
For details on all available options, see the [`Agent.Builder`](/api/com/acoliteai/agentsdk/core/Agent.Builder.html) Javadoc.
```

### Link Between Docs

Use relative links:

```markdown
See the [Tools Guide](guides/tools.md) for more information on custom tools.
```

### External Links

Always use full URLs:

```markdown
Learn more about [OpenAI's Chat Completions API](https://platform.openai.com/docs/api-reference/chat).
```

---

## File Naming Conventions

- **Use kebab-case**: `multi-agent-handoffs.md` not `MultiAgentHandoffs.md`
- **Be descriptive**: `memory-session-management.md` not `memory.md`
- **Avoid redundancy**: `tools.md` not `tools-guide.md` (it's already in `guides/`)

---

## MkDocs Syntax

### Headers

```markdown
# Top Level (Page Title) - Only one per page
## Section
### Subsection
#### Rarely needed
```

### Code Blocks

Always specify language for syntax highlighting:

````markdown
```java
// Java code
Agent agent = Agent.builder().build();
```

```bash
# Shell commands
mvn clean install
```

```xml
<!-- XML/Maven -->
<dependency>
  <groupId>com.acoliteai</groupId>
  <artifactId>openai-agent-sdk</artifactId>
</dependency>
```
````

### Admonitions (Callout Boxes)

Use for warnings, tips, notes:

```markdown
!!! note
    This is a note. Use for additional context.

!!! tip
    This is a tip. Use for helpful suggestions.

!!! warning
    This is a warning. Use for important caveats.

!!! danger
    This is danger. Use for critical security warnings.
```

### Tables

```markdown
| Feature | Description |
|---------|-------------|
| Tools   | Custom functions agents can call |
| Handoffs| Transfer between agents |
```

### Lists

```markdown
- Unordered list
- Another item
  - Nested item

1. Ordered list
2. Another item
```

---

## Testing Documentation

### Before Committing

1. **Check links**: All links to code must point to real files
2. **Verify examples**: All referenced examples must exist and work
3. **Test build**: Run `mkdocs build` locally
4. **Preview**: Run `mkdocs serve` and view at http://localhost:8000

### Commands

```bash
# Install MkDocs
pip install mkdocs mkdocs-material mkdocs-include-markdown-plugin

# Preview locally
mkdocs serve

# Build static site
mkdocs build

# Validate links (if link checker installed)
mkdocs build --strict
```

### CI Checks

Documentation is automatically checked in CI:
- All code examples are executed in E2E workflow
- Broken links fail the build (once configured)
- MkDocs build errors fail the build

---

## AI-Generated Documentation

### Policy

**AI-generated documentation must be reviewed by a human before merging.**

From the launch plan (Stage 1.2):
> AI generated docs must be reviewed before merge

### If You Use AI to Draft Docs

1. ✅ **Do**: Use AI to draft initial structure
2. ✅ **Do**: Use AI to improve clarity
3. ✅ **Do**: Have a human review and edit
4. ❌ **Don't**: Merge AI-generated docs without review
5. ❌ **Don't**: Let AI write code examples (write real code instead)

### Why?

AI can hallucinate APIs that don't exist or generate outdated code. Always verify:
- Code examples compile
- APIs actually exist
- Links point to real files

---

## Examples

### Good Example

```markdown
# Creating Your First Agent

This guide shows you how to create a basic agent and run your first conversation.

## Prerequisites

- Java 21+
- OpenAI API key set in environment: `OPENAI_API_KEY`

## Quick Start

```java
--8<-- "src/main/java/com/acoliteai/agentsdk/examples/HelloWorld.java:create-agent"
```

[View complete example](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/com/acoliteai/agentsdk/examples/HelloWorld.java)

## Running the Agent

Once you have an agent, run it with a message:

```java
--8<-- "src/main/java/com/acoliteai/agentsdk/examples/HelloWorld.java:run-agent"
```

## How It Works

The `Agent.builder()` creates a new agent with:
- **model**: The OpenAI model to use (gpt-4o, gpt-3.5-turbo, etc.)
- **instructions**: The system prompt that defines agent behavior

When you call `agent.run()`, the agent:
1. Sends your message to OpenAI
2. Receives the response
3. Returns a `RunResult` with the output

## Next Steps

- [Add tools to your agent](guides/tools.md)
- [Enable conversation memory](guides/sessions.md)
```

**Why this is good:**
- Shows real, tested code inline (pulled from HelloWorld.java)
- Links to complete example for context
- Multiple code snippets with region markers
- Clear explanations after each code block
- No copy-pasted code that could go stale

### Bad Example

```markdown
# Agent Documentation

The Agent class is the main class for creating agents in the OpenAI Agent SDK for Java.

## Constructor

The Agent class has a constructor that takes parameters.

## Methods

### run

The run method runs the agent.

```java
// This code might not work
agent.run();
```

## See Also

- Other documentation
```

**Problems**:
- No clear outcome
- No working example
- Code snippet not tested
- Vague descriptions
- No links to real code

---

## Quick Checklist

Before submitting documentation:

- [ ] Links point to real files (not fictional paths)
- [ ] Code examples are in `src/main/java/.../examples/` and tested
- [ ] Writing is clear and concise
- [ ] Tested with `mkdocs serve` locally
- [ ] No copy-pasted code (only links or includes)
- [ ] Follows file naming conventions
- [ ] Reviewed by a human (if AI-assisted)

---

## Questions?

- Check existing docs in `docs/` for examples
- Open a GitHub issue with the `documentation` label
- Ask in pull request reviews

---

**Last Updated**: January 2026
