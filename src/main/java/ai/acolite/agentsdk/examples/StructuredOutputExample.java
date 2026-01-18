package ai.acolite.agentsdk.examples;

import ai.acolite.agentsdk.core.Agent;
import ai.acolite.agentsdk.core.RunResult;
import ai.acolite.agentsdk.core.Runner;
import ai.acolite.agentsdk.core.types.JsonSchemaOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;

/**
 * StructuredOutputExample
 *
 * <p>Example demonstrating structured JSON output using JSON Schema.
 *
 * <p>This example shows: - Defining a Java class for structured output - Creating an agent with
 * JsonSchemaOutput type - Receiving type-safe structured data from the agent
 *
 * <p>Usage: OPENAI_API_KEY=sk-... java com.openai.agents.examples.StructuredOutputExample
 */
public class StructuredOutputExample {

  // region define-schema
  /** Data class for weather information. The agent will return an instance of this class. */
  public static class WeatherReport {
    public String location;
    public int temperature;
    public String conditions;
    public String recommendation;
  }

  // endregion define-schema

  public static void main(String[] args) {
    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Error: OPENAI_API_KEY environment variable not set");
      System.err.println(
          "Usage: OPENAI_API_KEY=sk-... java com.openai.agents.examples.StructuredOutputExample");
      System.exit(1);
    }

    System.out.println("=== Structured Output Example ===\n");

    // region create-agent
    // Define the output type using JSON Schema
    JsonSchemaOutput<WeatherReport> outputType = JsonSchemaOutput.of(WeatherReport.class);

    // Create an agent configured for structured output
    Agent<UnknownContext, JsonSchemaOutput<WeatherReport>> agent =
        Agent.<UnknownContext, JsonSchemaOutput<WeatherReport>>builder()
            .name("WeatherAgent")
            .instructions(
                "You are a weather assistant. Generate realistic weather data and recommendations.")
            .outputType(outputType)
            .build();
    // endregion create-agent

    // region run-agent
    // Run the agent
    RunResult<UnknownContext, ?> result =
        Runner.run(
            agent, "What's the weather like in San Francisco today? Include a recommendation.");
    // endregion run-agent

    // The result is automatically deserialized to our WeatherReport class
    if (result.getFinalOutput() instanceof WeatherReport weather) {
      System.out.println("Weather Report for Request:");
      System.out.println("\"What's the weather like in San Francisco today?\"");
      System.out.println();
      System.out.println("Structured Response:");
      System.out.println("  Location: " + weather.location);
      System.out.println("  Temperature: " + weather.temperature + "°F");
      System.out.println("  Conditions: " + weather.conditions);
      System.out.println("  Recommendation: " + weather.recommendation);
      System.out.println();
      System.out.println("Usage: " + result.getUsage().getTotalTokens() + " tokens");
    } else {
      System.err.println("Unexpected output type: " + result.getFinalOutput().getClass());
    }
  }
}
