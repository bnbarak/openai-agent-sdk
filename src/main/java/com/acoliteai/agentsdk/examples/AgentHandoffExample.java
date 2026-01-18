package com.acoliteai.agentsdk.examples;

import com.acoliteai.agentsdk.core.*;
import com.acoliteai.agentsdk.core.types.TextOutput;
import com.acoliteai.agentsdk.core.types.UnknownContext;

/**
 * AgentHandoffExample
 *
 * <p>Example demonstrating agent handoff for multi-agent conversations.
 *
 * <p>This example shows: - Creating specialized agents for different tasks - Configuring agent
 * handoffs - Triggering handoffs based on conversation context - Tracking agent switches during
 * execution
 *
 * <p>The example implements a simple customer service scenario: - A triage agent that directs users
 * to specialists - A billing agent that handles payment inquiries - A support agent that handles
 * technical issues
 *
 * <p>Usage: OPENAI_API_KEY=sk-... java com.acoliteai.agentsdk.examples.AgentHandoffExample
 */
public class AgentHandoffExample {

  public static void main(String[] args) {
    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Error: OPENAI_API_KEY environment variable not set");
      System.err.println(
          "Usage: OPENAI_API_KEY=sk-... java com.acoliteai.agentsdk.examples.AgentHandoffExample");
      System.exit(1);
    }

    System.out.println("=== Agent Handoff Example ===\n");

    simpleHandoff();
    System.out.println("\n" + "=".repeat(60) + "\n");
    multipleSpecialists();
  }

  /** Example 1: Simple handoff between two agents */
  private static void simpleHandoff() {
    System.out.println("Example 1: Simple Triage to Specialist Handoff");
    System.out.println("----------------------------------------------");

    // region create-specialist
    // Create a specialist agent for technical support
    Agent<UnknownContext, TextOutput> supportAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Technical Support")
            .instructions(
                "You are a technical support specialist. "
                    + "Help users troubleshoot technical issues, provide clear solutions, "
                    + "and explain technical concepts in simple terms.")
            .handoffDescription(
                "Hands off technical support and troubleshooting questions to this agent")
            .build();
    // endregion create-specialist

    // region create-triage
    // Create a triage agent that can hand off to the specialist
    Agent<UnknownContext, TextOutput> triageAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Triage Agent")
            .instructions(
                "You are a triage agent. Your ONLY job is to transfer users to specialists. "
                    + "For ANY technical problem, crash, or bug, you MUST call transfer_to_Technical_Support. "
                    + "DO NOT try to help directly. ALWAYS transfer.")
            .handoffs(java.util.List.of(supportAgent))
            .build();
    // endregion create-triage

    // region run-handoff
    // Run with a technical question (should trigger handoff)
    RunResult<UnknownContext, ?> result =
        Runner.run(
            triageAgent,
            "My application keeps crashing when I click the save button. How do I fix this?");
    // endregion run-handoff

    // Display results
    System.out.println("User question:");
    System.out.println("My application keeps crashing when I click the save button.");
    System.out.println();
    System.out.println("Final response:");
    System.out.println(result.getFinalOutput());
    System.out.println();

    // Analyze conversation for handoffs
    long handoffCount =
        result.getNewItems().stream().filter(item -> item instanceof RunHandoffOutputItem).count();

    System.out.println("Execution summary:");
    System.out.println("  Turns taken: " + result.getRawResponses().size());
    System.out.println("  Total items: " + result.getNewItems().size());
    System.out.println("  Handoffs executed: " + handoffCount);

    if (handoffCount > 0) {
      System.out.println("\nHandoff details:");
      result.getNewItems().stream()
          .filter(item -> item instanceof RunHandoffOutputItem)
          .map(item -> (RunHandoffOutputItem) item)
          .forEach(
              handoff -> {
                System.out.println("  From: " + handoff.getFromAgent());
                System.out.println("  To: " + handoff.getToAgent());
              });
    }
  }

  /** Example 2: Multiple specialist agents with handoff routing */
  private static void multipleSpecialists() {
    System.out.println("Example 2: Multiple Specialist Agents");
    System.out.println("-------------------------------------");

    // region create-specialists
    // Create specialized agents
    Agent<UnknownContext, TextOutput> billingAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Billing Specialist")
            .instructions(
                "You are a billing specialist. "
                    + "Help users with payment issues, invoices, refunds, and account charges. "
                    + "Be friendly and resolve billing concerns efficiently.")
            .handoffDescription("Handles billing, payments, refunds, and invoice questions")
            .build();

    Agent<UnknownContext, TextOutput> technicalAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Technical Support")
            .instructions(
                "You are a technical support specialist. "
                    + "Help users with bugs, crashes, performance issues, and feature questions. "
                    + "Provide detailed troubleshooting steps.")
            .handoffDescription("Handles technical issues, bugs, and troubleshooting")
            .build();

    Agent<UnknownContext, TextOutput> accountAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Account Manager")
            .instructions(
                "You are an account manager. "
                    + "Help users with account settings, profile updates, password resets, "
                    + "and subscription changes.")
            .handoffDescription("Handles account management and settings")
            .build();

    // Create triage agent with multiple handoff options
    Agent<UnknownContext, TextOutput> triageAgent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Customer Service Triage")
            .instructions(
                "You are a triage agent. Your ONLY job is to transfer to specialists. "
                    + "DO NOT help directly. You MUST call the appropriate transfer function:\n"
                    + "- For billing, payments, refunds: call transfer_to_Billing_Specialist\n"
                    + "- For technical issues, bugs, crashes: call transfer_to_Technical_Support\n"
                    + "- For account settings, profile: call transfer_to_Account_Manager\n"
                    + "ALWAYS transfer. DO NOT answer questions yourself.")
            .handoffs(java.util.List.of(billingAgent, technicalAgent, accountAgent))
            .build();
    // endregion create-specialists

    // Test multiple scenarios
    String[] testQuestions = {
      "I was charged twice for my subscription this month",
      "How do I change my email address?",
      "The app crashes every time I try to export data"
    };

    for (String question : testQuestions) {
      System.out.println("\nUser: " + question);
      System.out.println("─".repeat(60));

      RunResult<UnknownContext, ?> result = Runner.run(triageAgent, question);

      // Find handoff info
      result.getNewItems().stream()
          .filter(item -> item instanceof RunHandoffOutputItem)
          .map(item -> (RunHandoffOutputItem) item)
          .findFirst()
          .ifPresent(handoff -> System.out.println("→ Transferred to: " + handoff.getToAgent()));

      System.out.println("\nResponse: " + result.getFinalOutput());
    }
  }
}
