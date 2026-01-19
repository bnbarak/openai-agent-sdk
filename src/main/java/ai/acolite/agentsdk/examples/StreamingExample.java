package ai.acolite.agentsdk.examples;

import ai.acolite.agentsdk.core.*;
import ai.acolite.agentsdk.core.shims.ReadableStream;
import ai.acolite.agentsdk.core.shims.ReadableStreamAsyncIterator;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import ai.acolite.agentsdk.examples.tools.CalculatorTool;
import java.util.List;

/**
 * StreamingExample
 *
 * <p>Demonstrates real-time streaming of agent execution events. Events are emitted as they happen
 * during multi-turn execution.
 *
 * <p>Usage: OPENAI_API_KEY=sk-... java ai.acolite.agentsdk.examples.StreamingExample
 */
public class StreamingExample {

  public static void main(String[] args) {
    String apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Error: OPENAI_API_KEY environment variable not set");
      System.err.println(
          "Usage: OPENAI_API_KEY=sk-... java com.openai.agents.examples.StreamingExample");
      System.exit(1);
    }

    System.out.println("=== Streaming Example ===\n");

    basicStreamingExample();
    System.out.println("\n" + "=".repeat(60) + "\n");
    textStreamExample();
  }

  /** Example 1: Stream all events */
  private static void basicStreamingExample() {
    System.out.println("Example 1: Stream All Events");
    System.out.println("-".repeat(60));

    // Create agent with calculator tool
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MathAssistant")
            .instructions(
                "You are a math assistant. Use the calculator tool to perform calculations.")
            .tools(List.of(new CalculatorTool()))
            .build();

    String question = "What is 12345 multiplied by 67890?";
    System.out.println("Question: " + question);
    System.out.println();

    // Start streaming execution
    StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
        Runner.runStreamed(agent, question);

    // Consume events as they arrive (real-time!)
    System.out.println("Events (streaming in real-time):");
    System.out.println();

    ReadableStream<RunStreamEvent> stream = result.toStream();
    ReadableStreamAsyncIterator<RunStreamEvent> iterator = stream.values();

    int eventCount = 0;
    while (iterator.hasNext()) {
      RunStreamEvent event = iterator.next();
      eventCount++;

      System.out.println("Event #" + eventCount + ": " + event.getType());

      if (event instanceof RunItemStreamEvent itemEvent) {
        RunItem item = itemEvent.item();
        System.out.println("  Turn: " + itemEvent.turnIndex());
        System.out.println("  Item: " + item.getClass().getSimpleName());

        if (item instanceof RunToolCallItem toolCall) {
          System.out.println("  Tool: " + toolCall.getName());
          System.out.println("  Parameters: " + toolCall.getParameters());
        } else if (item instanceof RunToolCallOutputItem toolOutput) {
          System.out.println("  Tool Result: " + toolOutput.getResult());
        } else if (item instanceof RunMessageOutputItem message) {
          System.out.println("  Content: " + message.getContent());
        }
      }

      System.out.println();
    }

    System.out.println("Total events received: " + eventCount);
  }

  /** Example 2: Text-only stream (convenience method) */
  private static void textStreamExample() {
    System.out.println("Example 2: Text-Only Stream");
    System.out.println("-".repeat(60));

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("MathAssistant")
            .instructions(
                "You are a math assistant. Use the calculator tool to perform calculations.")
            .tools(List.of(new CalculatorTool()))
            .build();

    String question = "What is 999 plus 1?";
    System.out.println("Question: " + question);
    System.out.println();

    // Start streaming execution
    StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
        Runner.runStreamed(agent, question);

    // Get text-only stream (filters out tool calls/outputs)
    System.out.println("Text Output (streaming):");
    ReadableStream<String> textStream = result.toTextStream();
    ReadableStreamAsyncIterator<String> iterator = textStream.values();

    while (iterator.hasNext()) {
      String text = iterator.next();
      System.out.print(text); // Print as it arrives (no newline)
    }

    System.out.println("\n");
  }
}
