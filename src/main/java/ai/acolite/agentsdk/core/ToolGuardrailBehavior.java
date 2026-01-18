package ai.acolite.agentsdk.core;

/**
 * ToolGuardrailBehavior
 *
 * <p>Defines how a tool guardrail should behave when a violation is detected.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/toolGuardrail.ts">toolGuardrail.ts</a>
 */
public enum ToolGuardrailBehavior {
  /** Allow the tool execution to proceed normally */
  ALLOW,

  /** Reject the tool call and replace output with guardrail content */
  REJECT_CONTENT,

  /** Throw an exception to halt execution immediately */
  THROW_EXCEPTION
}
