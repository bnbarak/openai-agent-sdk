package com.acoliteai.agentsdk.openai;

import static org.junit.jupiter.api.Assertions.*;

import com.acoliteai.agentsdk.core.FunctionTool;
import com.acoliteai.agentsdk.core.RunContext;
import com.acoliteai.agentsdk.examples.tools.CalculatorTool;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Data;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for tool validation logic in OpenAIResponsesModel.
 *
 * <p>Tests the static validation methods that can be tested independently without requiring OpenAI
 * API client instances.
 */
class ToolValidationTest {

  @Data
  static class ValidInput {
    private String value;
  }

  static class ValidTool implements FunctionTool<Object, ValidInput, String> {
    @Override
    public String getType() {
      return "function";
    }

    @Override
    public String getName() {
      return "valid_tool";
    }

    @Override
    public String getDescription() {
      return "A valid tool";
    }

    @Override
    public Object getParameters() {
      return ValidInput.class;
    }

    @Override
    public boolean isStrict() {
      return true;
    }

    @Override
    public CompletableFuture<String> invoke(RunContext<Object> context, ValidInput input) {
      return CompletableFuture.completedFuture("result");
    }

    @Override
    public boolean needsApproval(RunContext<Object> context, ValidInput input) {
      return false;
    }

    @Override
    public boolean isEnabled(RunContext<Object> context) {
      return true;
    }
  }

  static class InvalidToolReturnsMap implements FunctionTool<Object, Map<String, Object>, String> {
    @Override
    public String getType() {
      return "function";
    }

    @Override
    public String getName() {
      return "invalid_tool";
    }

    @Override
    public String getDescription() {
      return "Tool that returns Map instead of Class";
    }

    @Override
    public Object getParameters() {
      return Map.of("type", "object", "properties", Map.of());
    }

    @Override
    public boolean isStrict() {
      return true;
    }

    @Override
    public CompletableFuture<String> invoke(RunContext<Object> context, Map<String, Object> input) {
      return CompletableFuture.completedFuture("result");
    }

    @Override
    public boolean needsApproval(RunContext<Object> context, Map<String, Object> input) {
      return false;
    }

    @Override
    public boolean isEnabled(RunContext<Object> context) {
      return true;
    }
  }

  @Test
  void extractParameterClass_validClass() {
    ValidTool tool = new ValidTool();

    Class<?> result = OpenAIResponsesModel.extractParameterClass(tool);

    assertEquals(ValidInput.class, result);
  }

  @Test
  void extractParameterClass_calculatorTool() {
    CalculatorTool tool = new CalculatorTool();

    Class<?> result = OpenAIResponsesModel.extractParameterClass(tool);

    assertEquals(CalculatorTool.Input.class, result);
  }

  @Test
  void extractParameterClass_returnsMap_throwsException() {
    InvalidToolReturnsMap tool = new InvalidToolReturnsMap();

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> OpenAIResponsesModel.extractParameterClass(tool));

    boolean containsToolName = exception.getMessage().contains("invalid_tool");
    assertTrue(containsToolName);
    boolean mentionsClass = exception.getMessage().contains("must return a Class");
    assertTrue(mentionsClass);
  }

  @Test
  void extractParameterClass_returnsNull_throwsException() {
    FunctionTool<Object, Void, String> toolReturningNull =
        new FunctionTool<>() {
          @Override
          public String getType() {
            return "function";
          }

          @Override
          public String getName() {
            return "null_tool";
          }

          @Override
          public String getDescription() {
            return "Returns null";
          }

          @Override
          public Object getParameters() {
            return null;
          }

          @Override
          public boolean isStrict() {
            return true;
          }

          @Override
          public CompletableFuture<String> invoke(RunContext<Object> context, Void input) {
            return CompletableFuture.completedFuture("result");
          }

          @Override
          public boolean needsApproval(RunContext<Object> context, Void input) {
            return false;
          }

          @Override
          public boolean isEnabled(RunContext<Object> context) {
            return true;
          }
        };

    assertThrows(
        NullPointerException.class,
        () -> OpenAIResponsesModel.extractParameterClass(toolReturningNull));
  }

  @Test
  void extractParameterClass_multipleToolsSameClass() {
    ValidTool tool1 = new ValidTool();
    ValidTool tool2 = new ValidTool();

    Class<?> class1 = OpenAIResponsesModel.extractParameterClass(tool1);
    Class<?> class2 = OpenAIResponsesModel.extractParameterClass(tool2);

    assertSame(class1, class2);
  }
}
