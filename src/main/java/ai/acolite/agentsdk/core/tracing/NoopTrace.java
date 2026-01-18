package ai.acolite.agentsdk.core.tracing;

import java.util.HashMap;

/**
 * No-op trace that does nothing.
 *
 * <p>Used when tracing is disabled. All lifecycle methods are no-ops. Extends Trace to maintain
 * type compatibility. Singleton pattern for efficiency.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/traces.ts">tracing/traces.ts</a>
 */
public class NoopTrace extends Trace {

  /** Singleton instance */
  public static final NoopTrace INSTANCE = new NoopTrace();

  private NoopTrace() {
    super(
        "noop", // traceId
        "noop", // name
        null, // groupId
        new HashMap<>(), // metadata
        null, // tracingApiKey
        NoopTraceProcessor.INSTANCE, // processor
        null, // startedAt
        null, // endedAt
        false, // started
        false // ended
        );
  }

  @Override
  public synchronized void start() {
    // No-op - don't call super
  }

  @Override
  public synchronized void end() {
    // No-op - don't call super
  }

  @Override
  public NoopTrace clone() {
    return this;
  }

  @Override
  public java.util.Map<String, Object> toJson(boolean includeTracingApiKey) {
    return null; // No-op trace doesn't export
  }

  @Override
  public java.util.Map<String, Object> toJson() {
    return null; // No-op trace doesn't export
  }
}
