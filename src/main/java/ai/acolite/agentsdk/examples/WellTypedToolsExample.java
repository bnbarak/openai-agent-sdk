package ai.acolite.agentsdk.examples;

import ai.acolite.agentsdk.core.Agent;
import ai.acolite.agentsdk.core.RunResult;
import ai.acolite.agentsdk.core.Runner;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import ai.acolite.agentsdk.examples.tools.CalculatorTool;
import ai.acolite.agentsdk.examples.tools.WeatherTool;
import java.util.List;

/**
 * WellTypedToolsExample
 *
 * <p>Demonstrates the benefits of well-typed tools using Lombok + Jackson.
 *
 * <p>Benefits: - Full type safety for inputs and outputs - Jackson annotations auto-generate JSON
 * schemas - Lombok eliminates boilerplate (getters, setters, constructors) - IDE autocomplete for
 * all tool parameters - Compile-time checking of parameter types - Clear, self-documenting code
 *
 * <p>This pattern matches the OpenAI SDK style while maintaining flexibility.
 *
 * <p>Usage: OPENAI_API_KEY=sk-... java com.openai.agents.examples.WellTypedToolsExample
 */
public class WellTypedToolsExample {

  public static void main(String[] args) {
    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Error: OPENAI_API_KEY environment variable not set");
      System.err.println(
          "Usage: OPENAI_API_KEY=sk-... java com.openai.agents.examples.WellTypedToolsExample");
      System.exit(1);
    }

    System.out.println("=== Well-Typed Tools Example ===\n");

    calculatorExample();
    System.out.println("\n" + "=".repeat(60) + "\n");
    weatherExample();
    System.out.println("\n" + "=".repeat(60) + "\n");
    multiToolExample();
  }

  /** Example 1: Calculator tool with simple typed parameters */
  private static void calculatorExample() {
    System.out.println("Example 1: Calculator Tool (Simple Types)");
    System.out.println("-".repeat(60));

    // region create-agent
    // Create agent with calculator tool
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MathAssistant")
            .instructions(
                "You are a math assistant. Use the calculator tool to perform calculations. Always show the calculation you performed.")
            .tools(List.of(new CalculatorTool()))
            .build();
    // endregion create-agent

    // region run-with-tools
    // Ask a math question - tool will be called automatically
    String question = "What is 123 multiplied by 456? Please use the calculator.";
    System.out.println("Question: " + question);
    System.out.println();

    RunResult<UnknownContext, ?> result = Runner.run(agent, question);
    // endregion run-with-tools

    System.out.println("Agent response:");
    System.out.println(result.getFinalOutput());
    System.out.println();

    System.out.println("✓ Tool called with type-safe parameters");
    System.out.println("  Input:  CalculatorTool.Input  { operation, a, b }");
    System.out.println("  Output: CalculatorTool.Output { result, operation, expression }");
    System.out.println("  Turns: " + result.getRawResponses().size());
  }

  /** Example 2: Weather tool with complex nested types */
  private static void weatherExample() {
    System.out.println("Example 2: Weather Tool (Complex Nested Types)");
    System.out.println("-".repeat(60));

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("WeatherAssistant")
            .instructions(
                "You are a weather assistant. Use the get_weather tool to retrieve weather information.")
            .tools(List.of(new WeatherTool()))
            .build();

    String question = "What's the weather in San Francisco with a 3-day forecast?";
    System.out.println("Question: " + question);
    System.out.println();

    RunResult<UnknownContext, ?> result = Runner.run(agent, question);

    System.out.println("Agent response:");
    System.out.println(result.getFinalOutput());
    System.out.println();

    System.out.println("✓ Tool called with nested type-safe parameters");
    System.out.println("  Input:  WeatherTool.Input  { city, units?, forecastDays? }");
    System.out.println(
        "  Output: WeatherTool.Output { city, current: Current, forecast: List<Forecast> }");
    System.out.println("    Current  { temperature, conditions, humidity, units }");
    System.out.println("    Forecast { date, highTemp, lowTemp, conditions }");
  }

  /** Example 3: Multiple tools working together */
  private static void multiToolExample() {
    System.out.println("Example 3: Multiple Tools");
    System.out.println("-".repeat(60));

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("GeneralAssistant")
            .instructions(
                "You are a helpful assistant with access to calculator and weather tools. Use them when appropriate.")
            .tools(List.of(new CalculatorTool(), new WeatherTool()))
            .build();

    String question =
        "What's the weather in NYC? Also, if it's 65°F, what's that in Celsius? (Use formula: C = (F - 32) * 5/9)";
    System.out.println("Question: " + question);
    System.out.println();

    RunResult<UnknownContext, ?> result = Runner.run(agent, question);

    System.out.println("Agent response:");
    System.out.println(result.getFinalOutput());
    System.out.println();

    System.out.println("✓ Agent called multiple typed tools");
    System.out.println("✓ Can call multiple tools in sequence");
    System.out.println("✓ All parameters and returns are type-safe");
  }

  /** Shows the benefits of this approach */
  private static void showBenefits() {
    System.out.println("\n" + "=".repeat(60));
    System.out.println("Benefits of Well-Typed Tools");
    System.out.println("=".repeat(60));
    System.out.println();

    System.out.println("1. Type Safety:");
    System.out.println("   ✓ Compile-time checking of parameter types");
    System.out.println("   ✓ No runtime type errors from wrong parameters");
    System.out.println("   ✓ IDE autocomplete for all fields");
    System.out.println();

    System.out.println("2. Clean Code (Lombok):");
    System.out.println("   ✓ @Data generates getters, setters, equals, hashCode");
    System.out.println("   ✓ No boilerplate code to maintain");
    System.out.println("   ✓ Focus on business logic");
    System.out.println();

    System.out.println("3. Self-Documenting (Jackson):");
    System.out.println("   ✓ @JsonClassDescription documents the tool");
    System.out.println("   ✓ @JsonPropertyDescription documents each parameter");
    System.out.println("   ✓ JSON schema auto-generated from annotations");
    System.out.println();

    System.out.println("4. Flexibility:");
    System.out.println("   ✓ Simple types: String, int, double");
    System.out.println("   ✓ Complex types: nested objects, lists");
    System.out.println("   ✓ Optional parameters with defaults");
    System.out.println("   ✓ Validation via Jackson/Bean Validation");
    System.out.println();

    System.out.println("5. Matches OpenAI SDK Pattern:");
    System.out.println("   ✓ Same annotation style as OpenAI examples");
    System.out.println("   ✓ Familiar to users of OpenAI Java SDK");
    System.out.println("   ✓ Easy to adapt OpenAI SDK tool examples");
  }
}
