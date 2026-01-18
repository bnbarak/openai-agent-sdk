package ai.acolite.agentsdk.core.tracing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for all SpanData types */
class SpanDataTest {

  @Test
  void agentSpanData_hasCorrectType() {
    AgentSpanData data =
        AgentSpanData.builder()
            .agentName("TestAgent")
            .handoffs(List.of("Agent1", "Agent2"))
            .tools(List.of("tool1", "tool2"))
            .build();

    assertEquals(SpanTypes.AGENT, data.getType());
    assertEquals("TestAgent", data.getAgentName());
    assertEquals(2, data.getHandoffs().size());
    assertEquals(2, data.getTools().size());
  }

  @Test
  void generationSpanData_hasCorrectType() {
    GenerationSpanData data =
        GenerationSpanData.builder()
            .model("gpt-4.1")
            .input(List.of())
            .output("Test output")
            .modelConfig(Map.of("temperature", 0.7))
            .build();

    assertEquals(SpanTypes.GENERATION, data.getType());
    assertEquals("gpt-4.1", data.getModel());
    assertEquals("Test output", data.getOutput());
  }

  @Test
  void functionSpanData_hasCorrectType() {
    FunctionSpanData data =
        FunctionSpanData.builder()
            .functionName("get_weather")
            .input(Map.of("location", "San Francisco"))
            .output(Map.of("temperature", 72))
            .mcpServer("weather-server")
            .build();

    assertEquals(SpanTypes.FUNCTION, data.getType());
    assertEquals("get_weather", data.getFunctionName());
    assertEquals("weather-server", data.getMcpServer());
  }

  @Test
  void handoffSpanData_hasCorrectType() {
    HandoffSpanData data =
        HandoffSpanData.builder()
            .fromAgent("Agent1")
            .toAgent("Agent2")
            .reason("Specialized knowledge required")
            .build();

    assertEquals(SpanTypes.HANDOFF, data.getType());
    assertEquals("Agent1", data.getFromAgent());
    assertEquals("Agent2", data.getToAgent());
    assertEquals("Specialized knowledge required", data.getReason());
  }

  @Test
  void customSpanData_hasCorrectType() {
    CustomSpanData data =
        CustomSpanData.builder()
            .name("custom-operation")
            .data(Map.of("key1", "value1", "key2", 123))
            .build();

    assertEquals(SpanTypes.CUSTOM, data.getType());
    assertEquals("custom-operation", data.getName());
    assertEquals(2, data.getData().size());
  }

  @Test
  void guardrailSpanData_hasCorrectType() {
    GuardrailSpanData data =
        GuardrailSpanData.builder()
            .guardrailName("content-filter")
            .triggered(true)
            .reason("Sensitive content detected")
            .build();

    assertEquals(SpanTypes.GUARDRAIL, data.getType());
    assertEquals("content-filter", data.getGuardrailName());
    assertTrue(data.isTriggered());
    assertEquals("Sensitive content detected", data.getReason());
  }

  @Test
  void spanError_fromThrowable() {
    RuntimeException exception = new RuntimeException("Test error message");
    SpanError error = SpanError.fromThrowable(exception);

    assertEquals("Test error message", error.getMessage());
    assertNotNull(error.getData());
    assertEquals("java.lang.RuntimeException", error.getData().get("type"));
    assertNotNull(error.getData().get("stackTrace"));
  }

  @Test
  void spanError_fromThrowableWithoutMessage() {
    NullPointerException exception = new NullPointerException();
    SpanError error = SpanError.fromThrowable(exception);

    // Should use class name when message is null
    assertEquals("java.lang.NullPointerException", error.getMessage());
  }

  @Test
  void spanError_builder() {
    SpanError error =
        SpanError.builder()
            .message("Custom error")
            .data(Map.of("code", "ERR_123", "details", "Something went wrong"))
            .build();

    assertEquals("Custom error", error.getMessage());
    assertEquals("ERR_123", error.getData().get("code"));
    assertEquals("Something went wrong", error.getData().get("details"));
  }
}
