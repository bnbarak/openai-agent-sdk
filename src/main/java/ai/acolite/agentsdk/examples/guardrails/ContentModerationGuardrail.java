package ai.acolite.agentsdk.examples.guardrails;

import ai.acolite.agentsdk.core.GuardrailFunctionOutput;
import ai.acolite.agentsdk.core.InputGuardrail;
import ai.acolite.agentsdk.core.InputGuardrailFunctionArgs;
import ai.acolite.agentsdk.core.types.UnknownContext;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ContentModerationGuardrail
 *
 * <p>Example guardrail that blocks inappropriate content in user input.
 *
 * <p>This is a blocking guardrail (runs before model call) that checks for banned words.
 */
public class ContentModerationGuardrail implements InputGuardrail<UnknownContext> {

  private static final List<String> BANNED_WORDS = List.of("inappropriate", "offensive", "harmful");

  @Override
  public String getName() {
    return "content_moderation";
  }

  @Override
  public CompletableFuture<GuardrailFunctionOutput> execute(
      InputGuardrailFunctionArgs<UnknownContext> args) {

    String inputText = extractText(args.getInput());

    for (String bannedWord : BANNED_WORDS) {
      if (inputText.toLowerCase().contains(bannedWord)) {
        return CompletableFuture.completedFuture(
            GuardrailFunctionOutput.tripwire(
                "Input contains inappropriate content: " + bannedWord));
      }
    }

    return CompletableFuture.completedFuture(GuardrailFunctionOutput.safe());
  }

  @Override
  public boolean isRunInParallel() {
    return false;
  }

  private String extractText(List<Object> input) {
    if (input == null || input.isEmpty()) {
      return "";
    }
    return input.stream().map(Object::toString).reduce("", (a, b) -> a + " " + b);
  }
}
