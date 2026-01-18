package ai.acolite.agentsdk.examples;

import ai.acolite.agentsdk.core.tracing.*;
import java.util.Map;

/**
 * TracingExample
 *
 * <p>Demonstrates the tracing system with console output. Shows how to create traces, spans, and
 * use the ConsoleTraceProcessor.
 *
 * <p>This example doesn't require an OpenAI API key - it just demonstrates the tracing
 * infrastructure.
 *
 * <p>Usage: java ai.acolite.agentsdk.examples.TracingExample
 */
public class TracingExample {

  public static void main(String[] args) {
    System.out.println("=== Tracing Example ===\n");
    System.out.println("This example demonstrates the tracing system with console output.\n");

    // Create a console processor to see trace output
    TraceProcessor processor = new ConsoleTraceProcessor(true); // true = pretty print

    // Create a trace for the workflow
    Trace trace =
        Trace.builder()
            .traceId(TracingUtils.generateTraceId())
            .name("Example workflow")
            .groupId(TracingUtils.generateGroupId())
            .metadata(
                Map.of(
                    "user", "demo-user",
                    "environment", "example"))
            .processor(processor)
            .build();

    // Start the trace
    trace.start();

    try {
      // Simulate some work with spans
      simulateAgentExecution(trace, processor);
      simulateToolCall(trace, processor);
      simulateCustomOperation(trace, processor);

    } finally {
      // End the trace
      trace.end();
    }

    System.out.println("Example complete!");
    System.out.println("\nIn a real application:");
    System.out.println("- Traces would be created automatically by Runner");
    System.out.println("- Spans would be created for each agent, LLM call, and tool call");
    System.out.println("- Traces would be exported to OpenAI platform (when configured)");
  }

  /** Simulate an agent execution span */
  private static void simulateAgentExecution(Trace trace, TraceProcessor processor) {
    AgentSpanData data =
        AgentSpanData.builder()
            .agentName("ExampleAgent")
            .handoffs(java.util.List.of("SupportAgent", "EscalationAgent"))
            .tools(java.util.List.of("get_weather", "search_web"))
            .build();

    Span<AgentSpanData> span =
        Span.<AgentSpanData>builder()
            .spanId(TracingUtils.generateSpanId())
            .traceId(trace.getTraceId())
            .data(data)
            .processor(processor)
            .build();

    span.start();

    // Simulate work
    sleep(100);

    span.end();
  }

  /** Simulate a tool call span */
  private static void simulateToolCall(Trace trace, TraceProcessor processor) {
    FunctionSpanData data =
        FunctionSpanData.builder()
            .functionName("get_weather")
            .input(Map.of("location", "San Francisco", "units", "fahrenheit"))
            .output(Map.of("temperature", 72, "condition", "sunny"))
            .build();

    Span<FunctionSpanData> span =
        Span.<FunctionSpanData>builder()
            .spanId(TracingUtils.generateSpanId())
            .traceId(trace.getTraceId())
            .data(data)
            .processor(processor)
            .build();

    span.start();

    // Simulate work
    sleep(50);

    span.end();
  }

  /** Simulate a custom operation with error handling */
  private static void simulateCustomOperation(Trace trace, TraceProcessor processor) {
    CustomSpanData data =
        CustomSpanData.builder()
            .name("data-processing")
            .data(Map.of("records_processed", 1000, "validation_passed", true))
            .build();

    Span<CustomSpanData> span =
        Span.<CustomSpanData>builder()
            .spanId(TracingUtils.generateSpanId())
            .traceId(trace.getTraceId())
            .data(data)
            .processor(processor)
            .build();

    span.start();

    try {
      // Simulate work
      sleep(75);

      // Simulate an error (commented out by default)
      // throw new RuntimeException("Simulated error");

    } catch (Exception e) {
      span.setError(e);
    } finally {
      span.end();
    }
  }

  private static void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
