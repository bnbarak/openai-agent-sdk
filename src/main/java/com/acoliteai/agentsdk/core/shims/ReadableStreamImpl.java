package com.acoliteai.agentsdk.core.shims;

import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ReadableStreamImpl
 *
 * <p>Simple implementation of ReadableStream using a BlockingQueue. Events are produced
 * asynchronously and consumed via iteration.
 */
public class ReadableStreamImpl<T> implements ReadableStream<T> {
  private final BlockingQueue<T> queue;
  private volatile boolean completed = false;
  private static final Object END_MARKER = new Object();

  public ReadableStreamImpl() {
    this.queue = new LinkedBlockingQueue<>();
  }

  /** Add an event to the stream */
  @SuppressWarnings("unchecked")
  public void emit(T event) {
    if (!completed) {
      queue.offer(event);
    }
  }

  /** Mark the stream as complete (no more events will be emitted) */
  @SuppressWarnings("unchecked")
  public void complete() {
    if (!completed) {
      completed = true;
      queue.offer((T) END_MARKER);
    }
  }

  /** Mark the stream as failed with an error */
  public void error(Throwable error) {
    completed = true;
    // In a real implementation, we'd propagate the error
    // For now, just complete the stream
    complete();
  }

  @Override
  public ReadableStreamAsyncIterator<T> values() {
    return new ReadableStreamAsyncIteratorImpl();
  }

  private class ReadableStreamAsyncIteratorImpl implements ReadableStreamAsyncIterator<T> {
    private T next = null;
    private boolean hasNext = false;
    private boolean streamEnded = false;

    @Override
    public boolean hasNext() {
      if (streamEnded) {
        return false;
      }

      if (!hasNext) {
        try {
          // Block indefinitely until an event arrives or stream ends
          next = queue.take();

          if (next == END_MARKER) {
            next = null;
            streamEnded = true;
            return false;
          }
          hasNext = true;
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          streamEnded = true;
          return false;
        }
      }
      return hasNext;
    }

    @Override
    public T next() {
      if (!hasNext && !hasNext()) {
        throw new NoSuchElementException();
      }
      hasNext = false;
      return next;
    }
  }
}
