package com.acoliteai.agentsdk.core.tracing;

/**
 * Constant span type identifiers.
 *
 * <p>Used for serialization and type discrimination in trace export.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/spans.ts">tracing/spans.ts</a>
 */
public final class SpanTypes {

  private SpanTypes() {
    // Utility class - prevent instantiation
  }

  /** Agent execution span */
  public static final String AGENT = "agent";

  /** LLM generation span */
  public static final String GENERATION = "generation";

  /** Function/tool call span */
  public static final String FUNCTION = "function";

  /** Agent handoff span */
  public static final String HANDOFF = "handoff";

  /** Custom user-defined span */
  public static final String CUSTOM = "custom";

  /** Guardrail check span */
  public static final String GUARDRAIL = "guardrail";

  /** Response span */
  public static final String RESPONSE = "response";

  /** Transcription span (audio to text) */
  public static final String TRANSCRIPTION = "transcription";

  /** Speech synthesis span (text to audio) */
  public static final String SPEECH = "speech";

  /** Speech group span */
  public static final String SPEECH_GROUP = "speech_group";

  /** MCP list tools span */
  public static final String MCP_TOOLS = "mcp_tools";
}
