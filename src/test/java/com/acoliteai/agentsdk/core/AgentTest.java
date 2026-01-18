package com.acoliteai.agentsdk.core;

import static org.junit.jupiter.api.Assertions.*;

import com.acoliteai.agentsdk.core.types.AgentOutputType;
import com.acoliteai.agentsdk.core.types.ResolvedAgentOutput;
import com.acoliteai.agentsdk.core.types.TextOutput;
import com.acoliteai.agentsdk.core.types.UnknownContext;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for Agent class */
class AgentTest {

  @Test
  void builder_withName_createsAgent() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("TestAgent").build();

    assertEquals("TestAgent", agent.getName());
    assertEquals("", agent.getInstructions());
    assertEquals("gpt-4.1", agent.getModel());
  }

  @Test
  void builder_withAllFields_createsAgent() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Assistant")
            .instructions("You are a helpful assistant")
            .model("gpt-3.5-turbo")
            .build();

    assertEquals("Assistant", agent.getName());
    assertEquals("You are a helpful assistant", agent.getInstructions());
    assertEquals("gpt-3.5-turbo", agent.getModel());
  }

  @Test
  void builder_withoutName_throwsException() {
    Exception exception =
        assertThrows(
            NullPointerException.class,
            () -> {
              Agent.<UnknownContext, TextOutput>builder().instructions("Test instructions").build();
            });

    assertTrue(exception.getMessage().contains("name") || exception.getMessage().contains("null"));
  }

  @Test
  void defaults_applyWhenNotSpecified() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("Agent").build();

    assertEquals("", agent.getInstructions());
    assertEquals("gpt-4.1", agent.getModel());
    assertNull(agent.getModelSettings());
    assertNull(agent.getOutputType());
  }

  @Test
  void processFinalOutput_withNoOutputType_returnsString() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("Agent").build();

    ResolvedAgentOutput<Object> result = agent.processFinalOutput("Hello, world!");

    assertNotNull(result);
    assertNotNull(result.getOutput());
    assertEquals("Hello, world!", result.getOutput());
  }

  @Test
  void processFinalOutput_withTextOutput_returnsString() {
    TextOutput outputType = TextOutput.INSTANCE;
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("Agent").outputType(outputType).build();

    ResolvedAgentOutput<Object> result = agent.processFinalOutput("Test output");

    assertNotNull(result);
    assertEquals("Test output", result.getOutput());
  }

  @Test
  void processFinalOutput_withJsonOutput_parsesCorrectly() {
    AgentOutputType jsonOutputType = new AgentOutputType() {};
    Agent<UnknownContext, AgentOutputType> agent =
        Agent.<UnknownContext, AgentOutputType>builder()
            .name("Agent")
            .outputType(jsonOutputType)
            .build();
    String jsonOutput = "{\"answer\":42,\"explanation\":\"The answer to life\"}";

    ResolvedAgentOutput<Object> result = agent.processFinalOutput(jsonOutput);

    @SuppressWarnings("unchecked")
    Map<String, Object> parsed = (Map<String, Object>) result.getOutput();
    assertEquals(42, parsed.get("answer"));
    assertEquals("The answer to life", parsed.get("explanation"));
  }

  @Test
  void processFinalOutput_withInvalidJson_throwsRuntimeException() {
    AgentOutputType jsonOutputType = new AgentOutputType() {};
    Agent<UnknownContext, AgentOutputType> agent =
        Agent.<UnknownContext, AgentOutputType>builder()
            .name("Agent")
            .outputType(jsonOutputType)
            .build();
    String invalidJson = "{invalid json}";

    RuntimeException error =
        assertThrows(RuntimeException.class, () -> agent.processFinalOutput(invalidJson));

    assertTrue(error.getMessage().contains("Failed to parse output as JSON"));
  }

  @Test
  void builder_withCustomModel_overridesDefault() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("Agent").model("gpt-4-turbo").build();

    assertEquals("gpt-4-turbo", agent.getModel());
  }

  @Test
  void builder_withModelSettings_setsModelSettings() {
    ModelSettings settings = new ModelSettings();

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder().name("Agent").modelSettings(settings).build();

    assertEquals(settings, agent.getModelSettings());
  }

  @Test
  void helloWorldExample_buildsCorrectly() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Assistant")
            .instructions("You are a helpful assistant")
            .build();

    assertEquals("Assistant", agent.getName());
    assertEquals("You are a helpful assistant", agent.getInstructions());
    assertEquals("gpt-4.1", agent.getModel());
  }

  @Test
  void getters_returnCorrectValues() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("TestAgent")
            .instructions("Test instructions")
            .model("gpt-4")
            .handoffDescription("Test handoff")
            .build();

    assertEquals("TestAgent", agent.getName());
    assertEquals("Test instructions", agent.getInstructions());
    assertEquals("gpt-4", agent.getModel());
    assertEquals("Test handoff", agent.getHandoffDescription());
  }
}
