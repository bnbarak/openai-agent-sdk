package ai.acolite.agentsdk.core.tracing;

import lombok.Builder;
import lombok.Value;

/**
 * Span data for function/tool calls.
 *
 * <p>Captures function name, input parameters, output result, and optional MCP (Model Context
 * Protocol) server information.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/spans.ts">tracing/spans.ts</a>
 */
@Value
@Builder
public final class FunctionSpanData implements SpanData {

  /** Function/tool name */
  String functionName;

  /** Function input parameters (JSON serializable) */
  Object input;

  /** Function output result (JSON serializable) */
  Object output;

  /** Optional MCP server name (if function is from MCP server) */
  String mcpServer;

  @Override
  public String getType() {
    return SpanTypes.FUNCTION;
  }
}
