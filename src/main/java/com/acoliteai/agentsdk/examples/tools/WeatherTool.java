package com.acoliteai.agentsdk.examples.tools;

import com.acoliteai.agentsdk.core.FunctionTool;
import com.acoliteai.agentsdk.core.RunContext;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * WeatherTool
 *
 * <p>Example tool demonstrating complex typed parameters with nested objects. Shows how Lombok +
 * Jackson create a clean, type-safe API.
 */
public class WeatherTool implements FunctionTool<Object, WeatherTool.Input, WeatherTool.Output> {

  // region define-input
  /** Input parameters with validation and descriptions */
  @Data
  @JsonClassDescription("Parameters for getting weather information")
  public static class Input {
    @JsonPropertyDescription("The city name (e.g., 'San Francisco', 'New York')")
    private String city;

    @JsonPropertyDescription("Optional: Units for temperature (celsius or fahrenheit)")
    private String units = "fahrenheit";

    @JsonPropertyDescription("Optional: Include forecast for next N days (0-7)")
    private int forecastDays = 0;
  }

  // endregion define-input

  // region define-output
  /** Structured output with nested data */
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

      public Current(double temp, String conditions, int humidity, String units) {
        this.temperature = temp;
        this.conditions = conditions;
        this.humidity = humidity;
        this.units = units;
      }
    }

    @Data
    @AllArgsConstructor
    public static class Forecast {
      private String date;
      private double highTemp;
      private double lowTemp;
      private String conditions;
    }

    public Output(String city, Current current, List<Forecast> forecast) {
      this.city = city;
      this.current = current;
      this.forecast = forecast;
    }
  }

  // endregion define-output

  @Override
  public String getType() {
    return "function";
  }

  @Override
  public String getName() {
    return "get_weather";
  }

  @Override
  public String getDescription() {
    return "Get current weather and optional forecast for a given city. Returns temperature, conditions, and humidity.";
  }

  @Override
  public Object getParameters() {
    // Return the Input class - schema auto-generated from Jackson annotations.
    return Input.class;
  }

  @Override
  public boolean isStrict() {
    return true;
  }

  @Override
  public CompletableFuture<Output> invoke(RunContext<Object> context, Input input) {
    return CompletableFuture.supplyAsync(
        () -> {
          // Mock weather data (in reality, would call weather API).
          double baseTemp = input.getUnits().equals("celsius") ? 18.0 : 65.0;
          String units = input.getUnits();
          Output.Current current = new Output.Current(baseTemp, "Partly cloudy", 65, units);
          List<Output.Forecast> forecast =
              input.getForecastDays() > 0
                  ? generateForecast(input.getForecastDays(), baseTemp, units)
                  : List.of();

          return new Output(input.getCity(), current, forecast);
        });
  }

  @Override
  public boolean needsApproval(RunContext<Object> context, Input input) {
    return false;
  }

  @Override
  public boolean isEnabled(RunContext<Object> context) {
    return true;
  }

  private List<Output.Forecast> generateForecast(int days, double baseTemp, String units) {
    return java.util.stream.IntStream.range(1, days + 1)
        .mapToObj(
            i ->
                new Output.Forecast(
                    "Day " + i,
                    baseTemp + (i * 2),
                    baseTemp - (i),
                    i % 2 == 0 ? "Sunny" : "Cloudy"))
        .toList();
  }
}
