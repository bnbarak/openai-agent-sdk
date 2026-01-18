package com.acoliteai.agentsdk.core;

import static org.junit.jupiter.api.Assertions.*;

import com.acoliteai.agentsdk.openai.OpenAIProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Unit tests for RunConfig class */
class RunConfigTest {

  @Test
  void builder_createsRunConfigWithValues() {
    ModelProvider mockProvider = Mockito.mock(ModelProvider.class);

    RunConfig config =
        RunConfig.builder().maxTurns(10).modelProvider(mockProvider).model("gpt-4.1").build();

    assertEquals(10, config.getMaxTurns());
    assertEquals(mockProvider, config.getModelProvider());
    assertEquals("gpt-4.1", config.getModel());
  }

  @Test
  void getEffectiveMaxTurns_withValue_returnsValue() {
    RunConfig config = RunConfig.builder().maxTurns(15).build();

    assertEquals(15, config.getEffectiveMaxTurns());
  }

  @Test
  void getEffectiveMaxTurns_withNull_returnsDefault() {
    RunConfig config = RunConfig.builder().build();

    assertEquals(10, config.getEffectiveMaxTurns());
  }

  @Test
  void getEffectiveMaxTurns_withZero_returnsZero() {
    RunConfig config = RunConfig.builder().maxTurns(0).build();

    assertEquals(0, config.getEffectiveMaxTurns());
  }

  @Test
  void getEffectiveModelProvider_withValue_returnsValue() {
    ModelProvider mockProvider = Mockito.mock(ModelProvider.class);
    RunConfig config = RunConfig.builder().modelProvider(mockProvider).build();

    assertEquals(mockProvider, config.getEffectiveModelProvider());
  }

  @Test
  void getEffectiveModelProvider_withNull_returnsDefault() {
    RunConfig config = RunConfig.builder().build();

    ModelProvider provider = config.getEffectiveModelProvider();

    assertNotNull(provider);
    assertInstanceOf(OpenAIProvider.class, provider);
  }

  @Test
  void builderDefaults_allowsPartialConfiguration() {
    RunConfig config = RunConfig.builder().model("gpt-3.5-turbo").build();

    assertNull(config.getMaxTurns());
    assertEquals(10, config.getEffectiveMaxTurns());
    assertNull(config.getModelProvider());
    assertNotNull(config.getEffectiveModelProvider());
    assertEquals("gpt-3.5-turbo", config.getModel());
  }

  @Test
  void value_providesEqualsAndHashCode() {
    RunConfig config1 = RunConfig.builder().maxTurns(10).model("gpt-4.1").build();
    RunConfig config2 = RunConfig.builder().maxTurns(10).model("gpt-4.1").build();

    assertEquals(config1, config2);
    assertEquals(config1.hashCode(), config2.hashCode());
  }

  @Test
  void value_providesToString() {
    RunConfig config = RunConfig.builder().maxTurns(10).model("gpt-4.1").build();

    String toString = config.toString();

    assertNotNull(toString);
    assertTrue(toString.contains("10"));
    assertTrue(toString.contains("gpt-4.1"));
  }

  @Test
  void fullConfiguration_setsAllFields() {
    ModelProvider mockProvider = Mockito.mock(ModelProvider.class);

    RunConfig config =
        RunConfig.builder().maxTurns(20).modelProvider(mockProvider).model("gpt-4-turbo").build();

    assertEquals(20, config.getEffectiveMaxTurns());
    assertEquals(mockProvider, config.getEffectiveModelProvider());
    assertEquals("gpt-4-turbo", config.getModel());
  }
}
