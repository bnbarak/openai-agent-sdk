package ai.acolite.agentsdk.core.runner;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * AsyncExecutor
 *
 * <p>Utility for non-blocking iterative execution using CompletableFuture composition. Replaces
 * blocking while loops with recursive async composition.
 *
 * <p>This separates Java concurrency concepts from agent execution logic.
 */
public class AsyncExecutor {

  /**
   * Execute an async operation iteratively with a side effect on each iteration.
   *
   * @param state The current state
   * @param shouldContinue Predicate to check if iteration should continue
   * @param operation Function to execute on each iteration
   * @param sideEffect Side effect to run after each iteration (e.g., increment counter)
   * @param <T> The state type
   * @return CompletableFuture that resolves when iteration completes
   */
  public static <T> CompletableFuture<T> iterateUntilWithSideEffect(
      T state,
      Predicate<T> shouldContinue,
      Function<T, CompletableFuture<T>> operation,
      Function<T, T> sideEffect) {

    if (!shouldContinue.test(state)) {
      return CompletableFuture.completedFuture(state);
    }

    return operation
        .apply(state)
        .thenApply(sideEffect)
        .thenCompose(
            newState ->
                iterateUntilWithSideEffect(newState, shouldContinue, operation, sideEffect));
  }

  /**
   * Create a supplier that executes once and caches the result. Useful for expensive operations
   * that should only run once.
   *
   * @param supplier The supplier to memoize
   * @param <T> The result type
   * @return Memoized supplier
   */
  public static <T> Supplier<T> memoize(Supplier<T> supplier) {
    return new Supplier<T>() {
      private T value;
      private boolean computed = false;

      @Override
      public synchronized T get() {
        if (!computed) {
          value = supplier.get();
          computed = true;
        }
        return value;
      }
    };
  }
}
