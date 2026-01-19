package ai.acolite.agentsdk.examples.tools;

import ai.acolite.agentsdk.core.FunctionTool;
import ai.acolite.agentsdk.core.RunContext;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * CalculatorTool
 *
 * <p>Example tool demonstrating well-typed parameters using Lombok and Jackson. This pattern
 * matches the OpenAI SDK style with full type safety.
 */
public class CalculatorTool
    implements FunctionTool<Object, CalculatorTool.Input, CalculatorTool.Output> {

  // region define-input
  /**
   * Input parameters for the calculator tool. Uses Jackson annotations for JSON schema generation
   * and Lombok for boilerplate.
   */
  @Data
  @JsonTypeName("calculator")
  @JsonClassDescription("Input parameters for arithmetic operations")
  public static class Input {
    @JsonPropertyDescription(
        "The arithmetic operation to perform: add, subtract, multiply, or divide")
    private String operation;

    @JsonPropertyDescription("The first number")
    private double a;

    @JsonPropertyDescription("The second number")
    private double b;
  }

  // endregion define-input

  // region define-output
  /** Output from the calculator tool. Fully typed return value. */
  @Data
  @AllArgsConstructor
  public static class Output {
    private double result;
    private String operation;
    private String expression;
  }

  // endregion define-output

  @Override
  public String getType() {
    return "function";
  }

  @Override
  public String getName() {
    return "calculator";
  }

  @Override
  public String getDescription() {
    return "Performs basic arithmetic operations: add, subtract, multiply, divide. Returns the result along with the operation performed.";
  }

  @Override
  public Object getParameters() {
    // Return the Input class - Jackson will auto-generate schema from annotations.
    return Input.class;
  }

  @Override
  public boolean isStrict() {
    return true;
  }

  // region implement-invoke
  @Override
  public CompletableFuture<Output> invoke(RunContext<Object> context, Input input) {
    return CompletableFuture.supplyAsync(
        () -> {
          double result =
              switch (input.getOperation()) {
                case "add" -> input.getA() + input.getB();
                case "subtract" -> input.getA() - input.getB();
                case "multiply" -> input.getA() * input.getB();
                case "divide" -> {
                  if (input.getB() == 0) {
                    throw new IllegalArgumentException("Cannot divide by zero");
                  }
                  yield input.getA() / input.getB();
                }
                default ->
                    throw new IllegalArgumentException(
                        "Unknown operation: " + input.getOperation());
              };
          String expression =
              String.format(
                  "%.2f %s %.2f = %.2f",
                  input.getA(), getOperatorSymbol(input.getOperation()), input.getB(), result);
          return new Output(result, input.getOperation(), expression);
        });
  }

  // endregion implement-invoke

  @Override
  public boolean needsApproval(RunContext<Object> context, Input input) {
    // Calculator operations don't need approval
    return false;
  }

  @Override
  public boolean isEnabled(RunContext<Object> context) {
    // Calculator is always enabled
    return true;
  }

  private String getOperatorSymbol(String operation) {
    return switch (operation) {
      case "add" -> "+";
      case "subtract" -> "-";
      case "multiply" -> "×";
      case "divide" -> "÷";
      default -> "?";
    };
  }
}
