package ai.acolite.agentsdk.core;

import ai.acolite.agentsdk.core.shims.ReadableStream;
import ai.acolite.agentsdk.core.shims.ReadableStreamAsyncIterator;
import ai.acolite.agentsdk.core.shims.ReadableStreamImpl;
import ai.acolite.agentsdk.core.types.AgentOutputType;
import ai.acolite.agentsdk.exceptions.NotImplementedException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.Getter;

/**
 * StreamedRunResult
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/result.ts">result.ts</a>
 */
@Getter
@Builder
public class StreamedRunResult<TContext, TAgent extends Agent<TContext, ? extends AgentOutputType>>
    implements AsyncIterable<RunStreamEvent> {
  List<Object> input;
  List<Object> newItems;
  List<ModelResponse> rawResponses;
  String lastResponseId;
  TAgent lastAgent;
  Usage usage;

  private Double currentTurn;
  private Optional<Double> maxTurns;
  private RunState<TContext, TAgent> state;
  private ReadableStreamImpl<RunStreamEvent> stream; // The event stream
  private CompletableFuture<RunState<TContext, TAgent>> executionFuture; // Execution in progress

  /**
   * Get the event stream for this run. Events are emitted in real-time as execution progresses.
   *
   * @return ReadableStream that emits RunStreamEvent objects
   */
  public ReadableStream<RunStreamEvent> toStream() {
    if (stream == null) {
      throw new IllegalStateException(
          "Stream not available - was this run started with runStreamed()?");
    }
    return stream;
  }

  /**
   * Get a text-only stream that emits content from message output events. Convenience method for
   * applications that only care about text responses.
   *
   * @return ReadableStream that emits String content
   */
  public ReadableStream<String> toTextStream() {
    if (stream == null) {
      throw new IllegalStateException(
          "Stream not available - was this run started with runStreamed()?");
    }
    ReadableStreamImpl<String> textStream = new ReadableStreamImpl<>();
    CompletableFuture.runAsync(
        () -> {
          try {
            ReadableStreamAsyncIterator<RunStreamEvent> iterator = stream.values();
            while (iterator.hasNext()) {
              RunStreamEvent event = iterator.next();

              // Only emit text from message output events.
              if (event instanceof RunItemStreamEvent itemEvent) {
                RunItem item = itemEvent.item();
                if (item instanceof RunMessageOutputItem messageItem) {
                  Object content = messageItem.getContent();
                  String textContent =
                      content instanceof String ? (String) content : content.toString();
                  textStream.emit(textContent);
                }
              }
            }
            textStream.complete();
          } catch (Exception e) {
            textStream.error(e);
          }
        });

    return textStream;
  }

  /**
   * _getStreamLoopPromise
   *
   * @return Optional<CompletableFuture<Void>>
   * @throws NotImplementedException Not yet implemented
   */
  public Optional<CompletableFuture<Void>> _getStreamLoopPromise() {
    throw new NotImplementedException("Not yet implemented");
  }

  /**
   * _getAbortSignal
   *
   * @return Optional<AbortSignal>
   * @throws NotImplementedException Not yet implemented
   */
  public Optional<AbortSignal> _getAbortSignal() {
    throw new NotImplementedException("Not yet implemented");
  }

  @Override
  public Iterator<RunStreamEvent> iterator() {
    return null;
  }
}
