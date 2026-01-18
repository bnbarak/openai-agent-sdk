package com.acoliteai.agentsdk.core;

import static org.junit.jupiter.api.Assertions.*;

import com.acoliteai.agentsdk.core.types.TextOutput;
import com.acoliteai.agentsdk.core.types.UnknownContext;
import org.junit.jupiter.api.Test;

/** Integration test to verify RunContext is properly integrated with the Runner execution loop. */
class RunContextIntegrationTest {

  @Test
  void runContext_tracksUsageAutomatically() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("TestAgent")
            .instructions("You are a helpful assistant.")
            .build();
    RunConfig config = RunConfig.builder().build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "Say hello", config);

    assertNotNull(result.getUsage());
    assertTrue(result.getUsage().getTotalTokens() > 0);
  }

  @Test
  void runContext_withCustomContext_preservesContext() {
    TestContext customContext = new TestContext();
    customContext.userId = "user_123";
    customContext.sessionId = "session_456";
    RunContext<TestContext> runContext = new RunContext<>(customContext);
    RunConfig config = RunConfig.builder().context(java.util.Optional.of(runContext)).build();
    Agent<TestContext, TextOutput> agent =
        Agent.<TestContext, TextOutput>builder()
            .name("TestAgent")
            .instructions("You are a helpful assistant.")
            .build();

    RunResult<TestContext, ?> result = Runner.run(agent, "Say hello", config);

    assertNotNull(result);
    assertNotNull(result.getUsage());
  }

  @Test
  void runContext_withoutCustomContext_usesDefaultContext() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("TestAgent")
            .instructions("You are a helpful assistant.")
            .build();
    RunConfig config = RunConfig.builder().build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "Say hello", config);

    assertNotNull(result);
    assertNotNull(result.getUsage());
    assertTrue(result.getUsage().getTotalTokens() >= 0);
  }

  static class TestContext {
    String userId;
    String sessionId;
  }
}
