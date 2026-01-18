package com.acoliteai.agentsdk.examples.guardrails;

import com.acoliteai.agentsdk.core.GuardrailFunctionOutput;
import com.acoliteai.agentsdk.core.OutputGuardrail;
import com.acoliteai.agentsdk.core.OutputGuardrailFunctionArgs;
import com.acoliteai.agentsdk.core.types.AgentOutputType;
import com.acoliteai.agentsdk.core.types.UnknownContext;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * PIIDetectionGuardrail
 *
 * <p>Example guardrail that detects personally identifiable information in agent output.
 *
 * <p>This is a parallel guardrail (runs after model response) that checks for SSNs and credit card
 * numbers.
 */
public class PIIDetectionGuardrail implements OutputGuardrail<UnknownContext, AgentOutputType> {

  private static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
  private static final Pattern CREDIT_CARD_PATTERN =
      Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b");

  @Override
  public String getName() {
    return "pii_detection";
  }

  @Override
  public CompletableFuture<GuardrailFunctionOutput> execute(
      OutputGuardrailFunctionArgs<UnknownContext, AgentOutputType> args) {

    String outputText = args.getOutput().toString();

    if (SSN_PATTERN.matcher(outputText).find()) {
      return CompletableFuture.completedFuture(
          GuardrailFunctionOutput.tripwire("Output contains SSN"));
    }

    if (CREDIT_CARD_PATTERN.matcher(outputText).find()) {
      return CompletableFuture.completedFuture(
          GuardrailFunctionOutput.tripwire("Output contains credit card number"));
    }

    return CompletableFuture.completedFuture(GuardrailFunctionOutput.safe());
  }
}
