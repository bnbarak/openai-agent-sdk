/**
 * Type definitions for agent contexts and outputs.
 *
 * <p>This package provides type-safe wrappers for agent contexts and output formats:
 *
 * <ul>
 *   <li>{@link ai.acolite.agentsdk.core.types.TextOutput} - Plain text output from agents
 *   <li>{@link ai.acolite.agentsdk.core.types.JsonSchemaOutput} - Structured JSON output with type
 *       safety
 *   <li>{@link ai.acolite.agentsdk.core.types.UnknownContext} - Default context type when no custom
 *       context is needed
 * </ul>
 *
 * <h2>Example: Structured Output</h2>
 *
 * <pre>{@code
 * public static class WeatherReport {
 *     public String location;
 *     public int temperature;
 *     public String conditions;
 * }
 *
 * JsonSchemaOutput<WeatherReport> outputType = JsonSchemaOutput.of(WeatherReport.class);
 *
 * Agent<UnknownContext, JsonSchemaOutput<WeatherReport>> agent =
 *     Agent.<UnknownContext, JsonSchemaOutput<WeatherReport>>builder()
 *         .model("gpt-4.1")
 *         .outputType(outputType)
 *         .build();
 * }</pre>
 *
 * @see ai.acolite.agentsdk.core.Agent
 */
package ai.acolite.agentsdk.core.types;
