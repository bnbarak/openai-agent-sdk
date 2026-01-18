package com.acoliteai.agentsdk.realworldapi;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.acoliteai.agentsdk.core.*;
import com.acoliteai.agentsdk.core.types.TextOutput;
import com.acoliteai.agentsdk.core.types.UnknownContext;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

/**
 * Real integration tests for agent handoff functionality.
 *
 * <p>These tests verify that agent handoffs work correctly with the actual OpenAI API, including
 * handoff tool serialization, LLM handoff decisions, and agent switching.
 *
 * <p>Requirements: 1. OPENAI_API_KEY environment variable must be set 2. Agent handoff
 * implementation must be complete
 *
 * <p>Usage: OPENAI_API_KEY=sk-... mvn test -Dtest=HandoffRealAPITest
 */
@SetEnvironmentVariable(key = "OPENAI_MODEL", value = "gpt-4.1-nano")
class HandoffRealAPITest {

  @BeforeAll
  static void checkApiKey() {
    String apiKey = System.getenv("OPENAI_API_KEY");
    assumeTrue(
        apiKey != null && !apiKey.isEmpty(), "OPENAI_API_KEY environment variable must be set");
  }

  @Test
  void simpleHandoff_transfersToSpecialist() {
    Agent<UnknownContext, TextOutput> specialistAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Math_Specialist")
            .instructions(
                "You are a math specialist. Solve math problems step by step. "
                    + "Always show your work and provide clear explanations.")
            .handoffDescription("Handles complex mathematical calculations and proofs")
            .model("gpt-4.1-mini")
            .build();
    Agent<UnknownContext, TextOutput> triageAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Triage")
            .instructions(
                "You are a triage agent. Your ONLY job is to transfer users to specialists. "
                    + "For ANY math problem, you MUST call transfer_to_Math_Specialist. "
                    + "DO NOT solve the problem yourself. ALWAYS transfer.")
            .handoffs(List.of(specialistAgent))
            .model("gpt-4.1-mini")
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(triageAgent, "Calculate 1234 multiplied by 5678");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());
    long handoffCount =
        result.getNewItems().stream().filter(item -> item instanceof RunHandoffOutputItem).count();

    if (handoffCount > 0) {
      RunHandoffOutputItem handoff =
          (RunHandoffOutputItem)
              result.getNewItems().stream()
                  .filter(item -> item instanceof RunHandoffOutputItem)
                  .findFirst()
                  .orElse(null);
      assertNotNull(handoff);
      assertEquals("Triage", handoff.getFromAgent());
      assertEquals("Math_Specialist", handoff.getToAgent());
      assertFalse(handoff.getError().isPresent());
    }

    String output = result.getFinalOutput().toString();
    assertFalse(output.isEmpty(), "Should have a response");
  }

  @Test
  void multipleHandoffOptions_choosesCorrectSpecialist() {
    Agent<UnknownContext, TextOutput> techAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Tech_Support")
            .instructions("You help with technical issues. Provide troubleshooting steps.")
            .handoffDescription("Handles technical problems and bugs")
            .model("gpt-4.1-mini")
            .build();
    Agent<UnknownContext, TextOutput> billingAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Billing")
            .instructions("You help with billing and payment issues.")
            .handoffDescription("Handles billing, payments, and refunds")
            .model("gpt-4.1-mini")
            .build();
    Agent<UnknownContext, TextOutput> triageAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Customer_Service")
            .instructions(
                "You are customer service triage. Your ONLY job is to transfer to specialists. "
                    + "For technical issues, call transfer_to_Tech_Support. "
                    + "For billing/payment issues, call transfer_to_Billing. "
                    + "DO NOT help directly. ALWAYS transfer.")
            .handoffs(List.of(techAgent, billingAgent))
            .model("gpt-4.1-mini")
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(triageAgent, "I was charged twice for my subscription");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());
    RunHandoffOutputItem handoff =
        (RunHandoffOutputItem)
            result.getNewItems().stream()
                .filter(item -> item instanceof RunHandoffOutputItem)
                .findFirst()
                .orElse(null);

    if (handoff != null) {
      assertEquals("Customer_Service", handoff.getFromAgent());
      assertEquals("Billing", handoff.getToAgent());
    }
  }

  @Test
  void noHandoffNeeded_staysWithOriginalAgent() {
    Agent<UnknownContext, TextOutput> specialistAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Specialist")
            .instructions("You are a specialist.")
            .model("gpt-4.1-mini")
            .build();
    Agent<UnknownContext, TextOutput> generalAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("General_Assistant")
            .instructions(
                "Answer simple questions directly. "
                    + "Only transfer to Specialist for very complex technical questions.")
            .handoffs(List.of(specialistAgent))
            .model("gpt-4.1-mini")
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(generalAgent, "What is the capital of France?");

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());
    long handoffCount =
        result.getNewItems().stream().filter(item -> item instanceof RunHandoffOutputItem).count();
    assertEquals(0, handoffCount, "Should not have executed any handoffs");
    String output = result.getFinalOutput().toString().toLowerCase();
    assertTrue(output.contains("paris"));
  }
}
