# Tools

Learn how to define custom tools that agents can invoke.

## Overview

Tools allow agents to perform actions beyond text generation. Tools are Java functions that agents call autonomously to access data, perform calculations, or interact with external systems. The agent decides when to invoke tools based on the conversation context.

!!! info "OpenAI Function Calling"
    This SDK implements [OpenAI's function calling framework](https://platform.openai.com/docs/guides/function-calling), which enables agents to autonomously invoke tools during conversations. OpenAI models decide when and how to call functions based on the conversation context and available tool definitions.

Each tool is defined using the `FunctionTool` interface with:

- Type-safe input parameters
- Type-safe output values
- Automatic JSON schema generation
- Optional approval requirements
- Enable/disable logic
- Automatic validation at agent build time

## Required Annotations

For OpenAI's function calling to work correctly, input parameter classes **must** have proper Jackson annotations:

```java
@Data
@JsonTypeName("calculator")  // Required: Identifies the tool parameter type
@JsonClassDescription("Input parameters for arithmetic operations")  // Required: Describes the parameters
public static class Input {
    @JsonPropertyDescription("The arithmetic operation to perform")  // Required: Describes each field
    private String operation;

    @JsonPropertyDescription("The first number")
    private double a;

    @JsonPropertyDescription("The second number")
    private double b;
}
```

!!! warning "Validation at Build Time"
    The SDK automatically validates tools when you build an agent. If your tool is missing required annotations, you'll get a clear error message pointing to the issue. This prevents runtime failures with the OpenAI API.

All tools are automatically validated for:
- `@JsonTypeName` or `@JsonClassDescription` on the Input class
- `@JsonPropertyDescription` on all input fields
- Valid getName(), getDescription(), and getParameters() implementations

## Creating a Simple Tool

Implement `FunctionTool` with typed input and output classes:

```java
public class CalculatorTool
    implements FunctionTool<Object, CalculatorTool.Input, CalculatorTool.Output> {

  @Data
  @JsonClassDescription("Input parameters for arithmetic operations")
  public static class Input {
    @JsonPropertyDescription("The arithmetic operation: add, subtract, multiply, or divide")
    private String operation;

    @JsonPropertyDescription("The first number")
    private double a;

    @JsonPropertyDescription("The second number")
    private double b;
  }

  @Data
  @AllArgsConstructor
  public static class Output {
    private double result;
    private String operation;
    private String expression;
  }

  @Override
  public String getName() {
    return "calculator";
  }

  @Override
  public String getDescription() {
    return "Performs basic arithmetic operations: add, subtract, multiply, divide.";
  }

  @Override
  public Object getParameters() {
    return Input.class;  // Jackson auto-generates JSON schema
  }

  @Override
  public CompletableFuture<Output> invoke(RunContext<Object> context, Input input) {
    return CompletableFuture.supplyAsync(() -> {
      double result = switch (input.getOperation()) {
        case "add" -> input.getA() + input.getB();
        case "subtract" -> input.getA() - input.getB();
        case "multiply" -> input.getA() * input.getB();
        case "divide" -> {
          if (input.getB() == 0) throw new IllegalArgumentException("Cannot divide by zero");
          yield input.getA() / input.getB();
        }
        default -> throw new IllegalArgumentException("Unknown operation: " + input.getOperation());
      };

      String expression = String.format("%.2f %s %.2f = %.2f",
          input.getA(), getOperatorSymbol(input.getOperation()), input.getB(), result);

      return new Output(result, input.getOperation(), expression);
    });
  }

  @Override
  public boolean needsApproval(RunContext<Object> context, Input input) {
    return false;  // Calculator doesn't need approval
  }

  @Override
  public boolean isEnabled(RunContext<Object> context) {
    return true;  // Always enabled
  }
}
```

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/ai/acolite/agentsdk/examples/tools/CalculatorTool.java)

## Adding Tools to an Agent

Pass tools to the agent builder:

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("MathAssistant")
        .instructions("You are a math assistant. Use the calculator tool to perform calculations.")
        .tools(List.of(new CalculatorTool()))
        .build();

RunResult<UnknownContext, ?> result = Runner.run(
    agent,
    "What is 123 multiplied by 456? Please use the calculator."
);

System.out.println(result.getFinalOutput());
// Output: "123 multiplied by 456 equals 56,088. I used the calculator to compute this: 123.00 × 456.00 = 56088.00"
```

The agent automatically calls the tool when needed and incorporates the result into its response.

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/ai/acolite/agentsdk/examples/WellTypedToolsExample.java)

## Type-Safe Input Parameters

Use Lombok `@Data` and Jackson annotations for clean, type-safe parameter definitions:

```java
@Data
@JsonClassDescription("Parameters for getting weather information")
public static class Input {
    @JsonPropertyDescription("The city name (e.g., 'San Francisco', 'New York')")
    private String city;

    @JsonPropertyDescription("Optional: Units for temperature (celsius or fahrenheit)")
    private String units = "fahrenheit";  // Default value

    @JsonPropertyDescription("Optional: Include forecast for next N days (0-7)")
    private int forecastDays = 0;
}
```

Benefits:

- **Type Safety**: Compile-time checking of all parameters
- **Auto-complete**: IDE support for parameter names and types
- **Documentation**: Annotations describe parameters for the model
- **Schema Generation**: Jackson automatically generates JSON schema
- **Validation**: Type system prevents invalid inputs

[View complex example with nested types →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/ai/acolite/agentsdk/examples/tools/WeatherTool.java)

## Type-Safe Output Values

Define structured output using POJOs:

```java
@Data
public static class Output {
    private String city;
    private Current current;
    private List<Forecast> forecast;

    @Data
    public static class Current {
        private double temperature;
        private String conditions;
        private int humidity;
        private String units;
    }

    @Data
    @AllArgsConstructor
    public static class Forecast {
        private String date;
        private double highTemp;
        private double lowTemp;
        private String conditions;
    }
}
```

The agent receives the structured output and can reference specific fields in its response.

## FunctionTool Interface

All tools implement the `FunctionTool<TContext, TInput, TOutput>` interface:

```java
public interface FunctionTool<TContext, TInput, TOutput> {
    // Required: Tool identification
    String getName();
    String getDescription();
    Object getParameters();  // Usually returns Input.class

    // Required: Tool execution
    CompletableFuture<TOutput> invoke(RunContext<TContext> context, TInput input);

    // Optional: Control flow
    boolean needsApproval(RunContext<TContext> context, TInput input);
    boolean isEnabled(RunContext<TContext> context);

    // Optional: Configuration
    String getType();  // Default: "function"
    boolean isStrict();  // Default: false
}
```

### Type Parameters

| Parameter | Description | Example |
|-----------|-------------|---------|
| `TContext` | Custom context type for approval/tracking | `Object`, `MyContext` |
| `TInput` | Tool input parameter type | `CalculatorTool.Input` |
| `TOutput` | Tool return value type | `CalculatorTool.Output` |

## JSON Schema Generation

The SDK automatically generates JSON schemas from your input classes using Jackson annotations:

```java
@Data
@JsonClassDescription("Input parameters for arithmetic operations")
public static class Input {
    @JsonPropertyDescription("The arithmetic operation to perform")
    private String operation;

    @JsonPropertyDescription("The first number")
    private double a;

    @JsonPropertyDescription("The second number")
    private double b;
}
```

Generated schema:

```json
{
  "type": "object",
  "description": "Input parameters for arithmetic operations",
  "properties": {
    "operation": {
      "type": "string",
      "description": "The arithmetic operation to perform"
    },
    "a": {
      "type": "number",
      "description": "The first number"
    },
    "b": {
      "type": "number",
      "description": "The second number"
    }
  },
  "required": ["operation", "a", "b"]
}
```

The model uses this schema to generate valid tool calls.

## Tool Approval System

Control tool execution with the `needsApproval()` method:

```java
@Override
public boolean needsApproval(RunContext<Object> context, Input input) {
    // Require approval for delete operations
    return input.getOperation().equals("delete");
}
```

When a tool needs approval:

1. Execution pauses before invoking the tool
2. Your context can implement approval logic
3. The tool runs only if approved

See the [Run Context guide](run-context.md) for implementing approval workflows.

### Example: Approval for Sensitive Operations

```java
public class FileOperationsTool implements FunctionTool<MyContext, Input, Output> {
    @Override
    public boolean needsApproval(RunContext<MyContext> context, Input input) {
        // Require approval for writes and deletes
        return input.getOperation().equals("write") || input.getOperation().equals("delete");
    }

    @Override
    public CompletableFuture<Output> invoke(RunContext<MyContext> context, Input input) {
        // Tool only runs if approved by context
        return CompletableFuture.supplyAsync(() -> {
            // Perform file operation
            return new Output(/* ... */);
        });
    }
}
```

## Conditional Tool Enabling

Control tool availability with `isEnabled()`:

```java
@Override
public boolean isEnabled(RunContext<MyContext> context) {
    // Only enable if user has premium access
    return context.getContextData().hasPremiumAccess();
}
```

Disabled tools are not presented to the model as available functions.

### Example: Feature Flags

```java
public class AdvancedSearchTool implements FunctionTool<MyContext, Input, Output> {
    @Override
    public boolean isEnabled(RunContext<MyContext> context) {
        // Check feature flag
        return context.getContextData().isFeatureEnabled("advanced_search");
    }
}
```

## Multiple Tools

Agents can use multiple tools simultaneously:

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("GeneralAssistant")
        .instructions("Use available tools to answer questions accurately.")
        .tools(List.of(
            new CalculatorTool(),
            new WeatherTool(),
            new SearchTool()
        ))
        .build();

RunResult<UnknownContext, ?> result = Runner.run(
    agent,
    "What's the weather in NYC? Also, what's 65°F in Celsius? Use (F - 32) * 5/9"
);

// Agent will call WeatherTool, then CalculatorTool to answer both questions
```

The agent selects the appropriate tool(s) based on the task and available functions.

## Hosted Tools

Hosted tools execute on OpenAI's infrastructure rather than in your application. These tools are provided and maintained by OpenAI, so you configure them but don't implement their logic.

### Currently Supported Hosted Tools

This SDK currently supports:

- **`web_search`** - Search the web for current information
- **`image_generation`** - Generate images using DALL-E

!!! warning "Limited Support"
    Other OpenAI hosted tools like `file_search`, `code_interpreter`, and `computer_use` are not yet supported by this SDK. Attempting to use them will throw an `UnsupportedOperationException`.

### Web Search Example

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("SearchAssistant")
        .instructions("You can search the web for current information.")
        .tools(List.of(HostedTool.webSearch()))
        .build();

RunResult<UnknownContext, ?> result = Runner.run(
    agent,
    "What is the current weather in Tokyo?"
);

System.out.println(result.getFinalOutput());
```

### Image Generation Example

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("Artist")
        .instructions("You can generate images using DALL-E.")
        .tools(List.of(HostedTool.imageGeneration()))
        .build();

RunResult<UnknownContext, ?> result = Runner.run(
    agent,
    "Generate an image of a serene mountain landscape"
);

System.out.println(result.getFinalOutput());
```

### Combining Hosted and Function Tools

You can use hosted tools alongside your custom function tools:

```java
Agent<UnknownContext, TextOutput> agent =
    Agent.<UnknownContext, TextOutput>builder()
        .name("MultiToolAssistant")
        .instructions("Use available tools to answer questions.")
        .tools(List.of(
            new CalculatorTool(),           // Custom function tool
            HostedTool.webSearch(),         // Hosted tool
            HostedTool.imageGeneration()    // Hosted tool
        ))
        .build();
```

[View complete example →](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/ai/acolite/agentsdk/examples/HostedToolsExample.java)

## Error Handling in Tools

When a tool encounters an error (missing credentials, invalid input, external API failure), return the error information in the output structure. The agent will read the error and communicate it to the user appropriately.

```java
@Override
public CompletableFuture<Output> invoke(RunContext<Object> context, Input input) {
    return CompletableFuture.completedFuture(() -> {
        // Check for missing configuration
        if (apiKey == null) {
            return new Output(false, "API credentials not configured. Please set up credentials.");
        }

        try {
            // Perform operation
            Result result = performOperation(input);
            return new Output(true, "Operation completed successfully", result);

        } catch (Exception e) {
            // Return error in output for the agent to communicate
            return new Output(false, "Error: " + e.getMessage(), null);
        }
    });
}

public record Output(
    @JsonProperty boolean success,
    @JsonProperty String message,
    @JsonProperty Result data
) {}
```

!!! tip "Error Patterns"
    See `ErrorReturningTool` in [BadToolExampleTest.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/test/java/ai/acolite/agentsdk/realworldapi/BadToolExampleTest.java) for a complete example of proper error handling. The agent successfully reads error responses and communicates them to users.

!!! warning "Throwing Exceptions"
    If your tool throws an uncaught exception, the SDK catches it and converts it to an error message for the agent. However, it's better to handle errors gracefully and return structured error information in your Output type.

## Testing Tools with ToolValidator

Use `ToolValidator` in your tests to ensure tools are properly configured before deploying:

```java
@Test
void myTool_isProperlyConfigured() {
    ToolValidator.validate(new MyTool());
}
```

The validator checks for:
- Required Jackson annotations (`@JsonTypeName`, `@JsonClassDescription`, `@JsonPropertyDescription`)
- Valid getName(), getDescription(), and getParameters() implementations
- Proper parameter class structure

If validation fails, you'll get a detailed error message:

```
Tool 'my_tool' has validation errors:
  - Parameter class 'Input' should have @JsonTypeName or @JsonClassDescription annotation for proper OpenAI schema generation
  - Parameter class 'Input' has fields without @JsonPropertyDescription annotations

See ErrorReturningTool in BadToolExampleTest for a working example.
```

!!! tip "Validate During Development"
    Add `ToolValidator.validate(new MyTool())` to your test suite to catch configuration issues early. This prevents runtime failures when the agent tries to use your tool.

See [ToolValidatorTest.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/test/java/ai/acolite/agentsdk/core/ToolValidatorTest.java) for comprehensive validation examples.

## Best Practices

!!! tip "Tool Design"
    - **Single Responsibility**: Each tool should do one thing well
    - **Clear Naming**: Use descriptive names like `get_weather`, not `tool1`
    - **Rich Descriptions**: Help the agent understand when to use the tool
    - **Validate Inputs**: Check parameters before performing operations
    - **Meaningful Errors**: Return clear error messages in the output

!!! tip "Type Safety"
    - Use Lombok `@Data` to eliminate boilerplate
    - Add Jackson `@JsonPropertyDescription` for all fields
    - Use primitive types for required parameters
    - Use wrapper types or defaults for optional parameters
    - Leverage Java's type system for compile-time safety

!!! tip "Performance"
    - Return `CompletableFuture` for async operations
    - Use connection pools for database/API tools
    - Cache frequently accessed data
    - Set reasonable timeouts for external calls
    - Log tool execution for monitoring

!!! tip "Security"
    - Use `needsApproval()` for dangerous operations
    - Validate and sanitize all inputs
    - Use `isEnabled()` for access control
    - Never expose sensitive data in error messages
    - Log security-relevant tool calls

## Common Tool Patterns

### External API Tool

```java
public class WeatherTool implements FunctionTool<Object, Input, Output> {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public CompletableFuture<Output> invoke(RunContext<Object> context, Input input) {
        return httpClient
            .sendAsync(buildRequest(input), HttpResponse.BodyHandlers.ofString())
            .thenApply(this::parseResponse);
    }
}
```

### Database Query Tool

```java
public class DatabaseTool implements FunctionTool<Object, Input, Output> {
    private final DataSource dataSource;

    @Override
    public CompletableFuture<Output> invoke(RunContext<Object> context, Input input) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                return executeQuery(conn, input);
            }
        });
    }
}
```

### File System Tool

```java
public class FileReadTool implements FunctionTool<MyContext, Input, Output> {
    @Override
    public boolean needsApproval(RunContext<MyContext> context, Input input) {
        // Require approval for files outside allowed directories
        return !isAllowedPath(input.getPath());
    }

    @Override
    public CompletableFuture<Output> invoke(RunContext<MyContext> context, Input input) {
        return CompletableFuture.supplyAsync(() -> {
            String content = Files.readString(Path.of(input.getPath()));
            return new Output(content);
        });
    }
}
```

## Next Steps

- [Run Context](run-context.md) - Implement tool approval workflows
- [Handoffs](handoffs.md) - Multi-agent systems with specialized tools
- [Guardrails](guardrails.md) - Add safety constraints to tool usage
- [Sessions](sessions.md) - Maintain conversation context across tool calls

## Additional Resources

- [WellTypedToolsExample.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/ai/acolite/agentsdk/examples/WellTypedToolsExample.java) - Multiple tool examples
- [CalculatorTool.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/ai/acolite/agentsdk/examples/tools/CalculatorTool.java) - Simple tool
- [WeatherTool.java](https://github.com/bnbarak/openai-agent-sdk/blob/main/src/main/java/ai/acolite/agentsdk/examples/tools/WeatherTool.java) - Complex nested types
- [API Reference](../javadoc/index.html) - Complete Javadoc documentation
