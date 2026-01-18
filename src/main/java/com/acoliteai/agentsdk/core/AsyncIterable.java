package com.acoliteai.agentsdk.core;

import java.util.Iterator;

/**
 * AsyncIterable
 *
 * <p>Interface for async iteration.
 *
 * <p>Similar to JavaScript's AsyncIterable.
 */
public interface AsyncIterable<T> extends Iterable<T> {
  @Override
  Iterator<T> iterator();
}
