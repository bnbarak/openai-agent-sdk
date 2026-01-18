package ai.acolite.agentsdk.examples;

import ai.acolite.agentsdk.core.*;
import ai.acolite.agentsdk.core.tracing.*;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import java.util.List;
import java.util.Map;

/**
 * AgentWithTracingExample
 *
 * <p>Demonstrates tracing with real agent execution and handoff. Shows how traces and spans wrap
 * around agent operations.
 *
 * <p>This example: - Creates two agents (Triage -> Math Specialist) - Manually wraps execution with
 * traces/spans - Uses ConsoleTraceProcessor to print trace output - Requires OPENAI_API_KEY to run
 *
 * <p>Usage: OPENAI_API_KEY=sk-... java ai.acolite.agentsdk.examples.AgentWithTracingExample
 */
public class AgentWithTracingExample {

  public static void main(String[] args) {
    System.out.println("=== Agent with Tracing Example ===\n");
    System.out.println(
        "This example shows tracing output for a real agent execution with handoff.\n");

    // Check API key
    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("ERROR: OPENAI_API_KEY environment variable must be set");
      System.exit(1);
    }

    // Create console processor to see trace output
    TraceProcessor processor = new ConsoleTraceProcessor(true);

    // Create a trace for the workflow
    Trace trace =
        Trace.builder()
            .traceId(TracingUtils.generateTraceId())
            .name("Customer support workflow")
            .groupId(TracingUtils.generateGroupId())
            .metadata(
                Map.of(
                    "customer", "demo-user",
                    "session", "example-001"))
            .processor(processor)
            .build();

    trace.start();

    try {
      // Create specialist agent
      Agent<UnknownContext, TextOutput> mathAgent =
          Agent.<UnknownContext, TextOutput>builder()
              .name("Math_Specialist")
              .instructions("You are a math specialist. Solve math problems step by step.")
              .handoffDescription("Handles complex mathematical calculations")
              .model("gpt-4.1-mini")
              .build();

      // Create triage agent with handoff to specialist
      Agent<UnknownContext, TextOutput> triageAgent =
          Agent.<UnknownContext, TextOutput>builder()
              .name("Triage")
              .instructions(
                  "You are a triage agent. For math problems, transfer to Math_Specialist. "
                      + "For simple greetings, respond directly.")
              .handoffs(List.of(mathAgent))
              .model("gpt-4.1-mini")
              .build();

      String userInput = "Calculate 123 multiplied by 456";
      System.out.println("User: " + userInput + "\n");

      // Create agent span
      AgentSpanData agentData =
          AgentSpanData.builder().agentName("Triage").handoffs(List.of("Math_Specialist")).build();

      ai.acolite.agentsdk.core.tracing.Span<AgentSpanData> agentSpan =
          ai.acolite.agentsdk.core.tracing.Span.<AgentSpanData>builder()
              .spanId(TracingUtils.generateSpanId())
              .traceId(trace.getTraceId())
              .data(agentData)
              .processor(processor)
              .build();

      agentSpan.start();

      System.out.println(">>> Executing agent with handoff support...\n");

      // Run the agent (will handle handoff automatically)
      RunResult<UnknownContext, ?> result = Runner.run(triageAgent, userInput);

      agentSpan.end();

      // Show results
      System.out.println("\n=== Execution Result ===");
      System.out.println("Final Output: " + result.getFinalOutput());
      System.out.println("Total Turns: " + result.getNewItems().size());

      // Check if handoff occurred
      long handoffCount =
          result.getNewItems().stream()
              .filter(item -> item instanceof RunHandoffOutputItem)
              .count();

      if (handoffCount > 0) {
        System.out.println("Handoffs: " + handoffCount);

        // Create handoff span for the trace
        RunHandoffOutputItem handoff =
            (RunHandoffOutputItem)
                result.getNewItems().stream()
                    .filter(item -> item instanceof RunHandoffOutputItem)
                    .findFirst()
                    .orElse(null);

        if (handoff != null) {
          HandoffSpanData handoffData =
              HandoffSpanData.builder()
                  .fromAgent(handoff.getFromAgent())
                  .toAgent(handoff.getToAgent())
                  .reason("Math problem requires specialist")
                  .build();

          ai.acolite.agentsdk.core.tracing.Span<HandoffSpanData> handoffSpan =
              ai.acolite.agentsdk.core.tracing.Span.<HandoffSpanData>builder()
                  .spanId(TracingUtils.generateSpanId())
                  .traceId(trace.getTraceId())
                  .data(handoffData)
                  .processor(processor)
                  .build();

          handoffSpan.start();
          handoffSpan.end();

          System.out.println("Handoff: " + handoff.getFromAgent() + " -> " + handoff.getToAgent());
        }
      }

      System.out.println("\nUsage:");
      System.out.println("  Input tokens: " + result.getUsage().getInputTokens());
      System.out.println("  Output tokens: " + result.getUsage().getOutputTokens());

    } catch (Exception e) {
      System.err.println("Error during execution: " + e.getMessage());
      e.printStackTrace();
    } finally {
      trace.end();
    }

    System.out.println("\n=== Example Complete ===");
    System.out.println("\nNote: This example manually creates traces/spans.");
    System.out.println("In future versions, the Runner will automatically create traces for you.");
  }
}
