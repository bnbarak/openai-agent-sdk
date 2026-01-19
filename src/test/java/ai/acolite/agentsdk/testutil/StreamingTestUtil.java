package ai.acolite.agentsdk.testutil;

import ai.acolite.agentsdk.core.Agent;
import ai.acolite.agentsdk.core.RunStreamEvent;
import ai.acolite.agentsdk.core.StreamedRunResult;
import ai.acolite.agentsdk.core.shims.ReadableStream;
import ai.acolite.agentsdk.core.shims.ReadableStreamAsyncIterator;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import ai.acolite.agentsdk.examples.tools.CalculatorTool;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Utility methods for streaming tests. */
public class StreamingTestUtil {

  public static Agent<UnknownContext, TextOutput> createMathAgent() {
    return Agent.<UnknownContext, TextOutput>builder()
        .name("MathAgent")
        .instructions("Use the calculator tool to perform calculations.")
        .tools(List.of(new CalculatorTool()))
        .build();
  }

  public static Agent<UnknownContext, TextOutput> createSimpleAgent() {
    return Agent.<UnknownContext, TextOutput>builder()
        .name("SimpleAgent")
        .instructions("You are a helpful assistant.")
        .build();
  }

  public static List<RunStreamEvent> collectAllEvents(
      StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result) {
    List<RunStreamEvent> events = new ArrayList<>();
    ReadableStream<RunStreamEvent> stream = result.toStream();
    ReadableStreamAsyncIterator<RunStreamEvent> iterator = stream.values();

    while (iterator.hasNext()) {
      events.add(iterator.next());
    }

    return events;
  }

  public static List<String> collectEventTypes(
      StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result) {
    return collectAllEvents(result).stream()
        .map(RunStreamEvent::getType)
        .collect(Collectors.toList());
  }

  public static List<String> collectTextChunks(
      StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result) {
    List<String> textChunks = new ArrayList<>();
    ReadableStream<String> textStream = result.toTextStream();
    ReadableStreamAsyncIterator<String> iterator = textStream.values();

    while (iterator.hasNext()) {
      textChunks.add(iterator.next());
    }

    return textChunks;
  }

  public static long countEventsByType(List<RunStreamEvent> events, String eventType) {
    return events.stream().filter(e -> e.getType().equals(eventType)).count();
  }
}
