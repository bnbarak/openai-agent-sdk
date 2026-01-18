package ai.acolite.agentsdk.core;

import lombok.Builder;
import lombok.Value;

/**
 * ToolGuardrailFunctionOutput
 *
 * <p>Result of a tool guardrail execution containing behavior and optional content.
 *
 * <p>Unlike input/output guardrails which use tripwireTriggered, tool guardrails use a behavior
 * enum to specify how to handle violations.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/toolGuardrail.ts">toolGuardrail.ts</a>
 */
@Value
@Builder
public class ToolGuardrailFunctionOutput {
  @Builder.Default ToolGuardrailBehavior behavior = ToolGuardrailBehavior.ALLOW;
  Object content;
  Object metadata;

  public static ToolGuardrailFunctionOutput allow() {
    return ToolGuardrailFunctionOutput.builder().behavior(ToolGuardrailBehavior.ALLOW).build();
  }

  public static ToolGuardrailFunctionOutput rejectContent(Object content, Object metadata) {
    return ToolGuardrailFunctionOutput.builder()
        .behavior(ToolGuardrailBehavior.REJECT_CONTENT)
        .content(content)
        .metadata(metadata)
        .build();
  }

  public static ToolGuardrailFunctionOutput throwException(Object metadata) {
    return ToolGuardrailFunctionOutput.builder()
        .behavior(ToolGuardrailBehavior.THROW_EXCEPTION)
        .metadata(metadata)
        .build();
  }
}
