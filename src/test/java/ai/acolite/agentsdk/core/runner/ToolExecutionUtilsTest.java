package ai.acolite.agentsdk.core.runner;

import static org.junit.jupiter.api.Assertions.*;

import ai.acolite.agentsdk.core.Agent;
import ai.acolite.agentsdk.core.FunctionTool;
import ai.acolite.agentsdk.core.RunContext;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import ai.acolite.agentsdk.examples.tools.CalculatorTool;
import ai.acolite.agentsdk.examples.tools.WeatherTool;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Data;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ToolExecutionUtils static methods.
 *
 * <p>Tests tool finding and parameter deserialization logic extracted from Runner.
 */
class ToolExecutionUtilsTest {

  @Data
  @com.fasterxml.jackson.annotation.JsonTypeName("simple_tool")
  @com.fasterxml.jackson.annotation.JsonClassDescription("Input for simple tool")
  static class SimpleInput {
    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Name parameter")
    private String name;

    @com.fasterxml.jackson.annotation.JsonPropertyDescription("Count parameter")
    private int count;
  }

  static class SimpleTool implements FunctionTool<Object, SimpleInput, String> {
    @Override
    public String getType() {
      return "function";
    }

    @Override
    public String getName() {
      return "simple_tool";
    }

    @Override
    public String getDescription() {
      return "A simple tool";
    }

    @Override
    public Object getParameters() {
      return SimpleInput.class;
    }

    @Override
    public boolean isStrict() {
      return true;
    }

    @Override
    public CompletableFuture<String> invoke(RunContext<Object> context, SimpleInput input) {
      return CompletableFuture.completedFuture("result");
    }

    @Override
    public boolean needsApproval(RunContext<Object> context, SimpleInput input) {
      return false;
    }

    @Override
    public boolean isEnabled(RunContext<Object> context) {
      return true;
    }
  }

  @Test
  void findToolByName_noTools_returnsNull() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("NoTools").build();

    FunctionTool<?, ?, ?> result = ToolExecutionUtils.findToolByName(agent, "calculator");

    assertNull(result);
  }

  @Test
  void findToolByName_toolExists_returnsIt() {
    CalculatorTool calculator = new CalculatorTool();
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MathAgent")
            .tools(List.of(calculator))
            .build();

    FunctionTool<?, ?, ?> result = ToolExecutionUtils.findToolByName(agent, "calculator");

    assertNotNull(result);
    assertSame(calculator, result);
  }

  @Test
  void findToolByName_toolDoesNotExist_returnsNull() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MathAgent")
            .tools(List.of(new CalculatorTool()))
            .build();

    FunctionTool<?, ?, ?> result = ToolExecutionUtils.findToolByName(agent, "nonexistent");

    assertNull(result);
  }

  @Test
  void findToolByName_multipleTools_findsCorrectOne() {
    CalculatorTool calculator = new CalculatorTool();
    WeatherTool weather = new WeatherTool();
    SimpleTool simple = new SimpleTool();

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MultiToolAgent")
            .tools(List.of(calculator, weather, simple))
            .build();

    FunctionTool<?, ?, ?> calc = ToolExecutionUtils.findToolByName(agent, "calculator");
    FunctionTool<?, ?, ?> weath = ToolExecutionUtils.findToolByName(agent, "get_weather");
    FunctionTool<?, ?, ?> simp = ToolExecutionUtils.findToolByName(agent, "simple_tool");

    assertSame(calculator, calc);
    assertSame(weather, weath);
    assertSame(simple, simp);
  }

  @Test
  void deserializeParameters_null_returnsNull() {
    Object result = ToolExecutionUtils.deserializeParameters(null, SimpleInput.class);

    assertNull(result);
  }

  @Test
  void deserializeParameters_alreadyCorrectType_returnsAsIs() {
    SimpleInput input = new SimpleInput();
    input.setName("test");
    input.setCount(42);

    Object result = ToolExecutionUtils.deserializeParameters(input, SimpleInput.class);

    assertSame(input, result);
  }

  @Test
  void deserializeParameters_mapToClass_deserializes() {
    Map<String, Object> params = Map.of("name", "test", "count", 42);

    Object result = ToolExecutionUtils.deserializeParameters(params, SimpleInput.class);

    assertInstanceOf(SimpleInput.class, result);
    SimpleInput input = (SimpleInput) result;
    assertEquals("test", input.getName());
    assertEquals(42, input.getCount());
  }

  @Test
  void deserializeParameters_extraFields_ignored() {
    Map<String, Object> params =
        Map.of(
            "name", "test",
            "count", 42,
            "extraField", "ignored");

    Object result = ToolExecutionUtils.deserializeParameters(params, SimpleInput.class);

    assertInstanceOf(SimpleInput.class, result);
    SimpleInput input = (SimpleInput) result;
    assertEquals("test", input.getName());
    assertEquals(42, input.getCount());
  }

  @Test
  void deserializeParameters_typeMismatch_throwsException() {
    Map<String, Object> params =
        Map.of(
            "name", "test",
            "count", "not-a-number");

    assertThrows(
        RuntimeException.class,
        () -> {
          ToolExecutionUtils.deserializeParameters(params, SimpleInput.class);
        });
  }

  @Test
  void deserializeParameters_calculatorToolInput_works() {
    Map<String, Object> params =
        Map.of(
            "operation", "multiply",
            "a", 123.0,
            "b", 456.0);

    Object result = ToolExecutionUtils.deserializeParameters(params, CalculatorTool.Input.class);

    assertInstanceOf(CalculatorTool.Input.class, result);
    CalculatorTool.Input input = (CalculatorTool.Input) result;
    assertEquals("multiply", input.getOperation());
    assertEquals(123.0, input.getA());
    assertEquals(456.0, input.getB());
  }

  @Test
  void deserializeParameters_weatherToolInput_works() {
    Map<String, Object> params =
        Map.of(
            "city", "San Francisco",
            "units", "celsius",
            "forecastDays", 3);

    Object result = ToolExecutionUtils.deserializeParameters(params, WeatherTool.Input.class);

    assertInstanceOf(WeatherTool.Input.class, result);
    WeatherTool.Input input = (WeatherTool.Input) result;
    assertEquals("San Francisco", input.getCity());
    assertEquals("celsius", input.getUnits());
    assertEquals(3, input.getForecastDays());
  }

  @Test
  void deserializeParameters_notAClass_returnsAsIs() {
    Map<String, Object> params = Map.of("key", "value");
    Map<String, Object> schema = Map.of("type", "object");

    Object result = ToolExecutionUtils.deserializeParameters(params, schema);

    assertSame(params, result);
  }

  @Test
  void deserializeParameters_errorMessage_includesClassName() {
    Map<String, Object> params =
        Map.of(
            "name", "test",
            "count", "not-a-number");

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              ToolExecutionUtils.deserializeParameters(params, SimpleInput.class);
            });

    boolean containsClassName = exception.getMessage().contains("SimpleInput");
    assertTrue(containsClassName);
  }
}
