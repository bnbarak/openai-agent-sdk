package ai.acolite.agentsdk.core.tracing;

import lombok.Data;

/**
 * TraceContextState
 *
 * <p>Holds the current trace and span state for a thread. Used by TraceContext for ThreadLocal
 * storage.
 *
 * <p>This class is mutable to allow updating the current span as execution progresses through
 * nested spans.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/context.ts
 */
@Data
public class TraceContextState {
  private final Trace trace;
  private Span<?> currentSpan;
  private boolean active = true;

  public TraceContextState(Trace trace) {
    this.trace = trace;
    this.currentSpan = null;
  }

  /** Create a copy of this state for propagation to another thread */
  public TraceContextState copy() {
    TraceContextState copy = new TraceContextState(this.trace);
    copy.setCurrentSpan(this.currentSpan);
    copy.setActive(this.active);
    return copy;
  }
}
