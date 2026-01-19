package ai.acolite.agentsdk.core.tracing;

import java.util.Map;
import lombok.Builder;
import lombok.Value;

/**
 * Span data for custom user-defined events.
 *
 * <p>Allows users to add custom spans to traces with arbitrary metadata. Useful for tracking
 * application-specific operations and workflows.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/spans.ts">tracing/spans.ts</a>
 */
@Value
@Builder
public final class CustomSpanData implements SpanData {

  /** Custom span name */
  String name;

  /** Arbitrary metadata (JSON serializable) */
  Map<String, Object> data;

  @Override
  public String getType() {
    return SpanTypes.CUSTOM;
  }
}
