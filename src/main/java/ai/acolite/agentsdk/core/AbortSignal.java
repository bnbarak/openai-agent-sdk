package ai.acolite.agentsdk.core;

/**
 * AbortSignal
 *
 * <p>Signal for aborting/canceling operations.
 *
 * <p>Similar to JavaScript's AbortSignal.
 */
public class AbortSignal {
  private boolean aborted = false;

  public boolean isAborted() {
    return aborted;
  }

  public void abort() {
    this.aborted = true;
  }
}
