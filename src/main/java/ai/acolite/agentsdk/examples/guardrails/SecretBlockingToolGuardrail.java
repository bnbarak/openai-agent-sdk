package ai.acolite.agentsdk.examples.guardrails;

import ai.acolite.agentsdk.core.ToolGuardrailFunctionOutput;
import ai.acolite.agentsdk.core.ToolOutputGuardrail;
import ai.acolite.agentsdk.core.ToolOutputGuardrailFunctionArgs;
import ai.acolite.agentsdk.core.types.UnknownContext;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * SecretBlockingToolGuardrail
 *
 * <p>Example guardrail that redacts secrets from tool output.
 *
 * <p>This is a tool output guardrail that runs after tool execution and replaces secrets with
 * [REDACTED].
 */
public class SecretBlockingToolGuardrail implements ToolOutputGuardrail<UnknownContext> {

  private static final Pattern API_KEY_PATTERN =
      Pattern.compile("(api[_-]?key|apikey|token)[\":\\s=]+([a-zA-Z0-9_\\-]{20,})");
  private static final Pattern PASSWORD_PATTERN =
      Pattern.compile("(password|passwd|pwd)[\":\\s=]+([^\\s\"'}{,]+)");

  @Override
  public String getName() {
    return "secret_blocking";
  }

  @Override
  public CompletableFuture<ToolGuardrailFunctionOutput> execute(
      ToolOutputGuardrailFunctionArgs<UnknownContext> args) {

    String outputText = args.getToolOutput().toString();

    boolean hasSecrets =
        API_KEY_PATTERN.matcher(outputText).find() || PASSWORD_PATTERN.matcher(outputText).find();

    if (hasSecrets) {
      String redacted = redactSecrets(outputText);
      return CompletableFuture.completedFuture(
          ToolGuardrailFunctionOutput.rejectContent(
              redacted, "Tool output contained secrets that were redacted"));
    }

    return CompletableFuture.completedFuture(ToolGuardrailFunctionOutput.allow());
  }

  private String redactSecrets(String text) {
    String redacted = API_KEY_PATTERN.matcher(text).replaceAll("$1: [REDACTED]");
    redacted = PASSWORD_PATTERN.matcher(redacted).replaceAll("$1: [REDACTED]");
    return redacted;
  }
}
