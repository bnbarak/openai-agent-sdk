package ai.acolite.agentsdk.core.shims;

import java.util.Iterator;

/**
 * ReadableStreamAsyncIterator
 *
 * <p>Async iterator for readable streams.
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/shims
 */
public interface ReadableStreamAsyncIterator<T> extends Iterator<T> {
  // Async iterator interface
}
