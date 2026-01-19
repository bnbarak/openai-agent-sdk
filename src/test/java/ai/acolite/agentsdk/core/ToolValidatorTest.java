package ai.acolite.agentsdk.core;

import static org.junit.jupiter.api.Assertions.*;

import ai.acolite.agentsdk.core.types.UnknownContext;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Data;
import org.junit.jupiter.api.Test;

class ToolValidatorTest {

  @Test
  void validTool_passesValidation() {
    ToolValidator.validate(new ValidTool());
  }

  @Test
  void toolWithoutName_failsValidation() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> ToolValidator.validate(new NoNameTool()));

    assertTrue(exception.getMessage().contains("getName() must return a non-empty string"));
  }

  @Test
  void toolWithoutDescription_failsValidation() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> ToolValidator.validate(new NoDescriptionTool()));

    assertTrue(exception.getMessage().contains("getDescription() must return a non-empty string"));
  }

  @Test
  void toolWithWrongType_failsValidation() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> ToolValidator.validate(new WrongTypeTool()));

    assertTrue(exception.getMessage().contains("getType() must return 'function'"));
  }

  @Test
  void toolWithNullParameters_failsValidation() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> ToolValidator.validate(new NullParametersTool()));

    assertTrue(exception.getMessage().contains("getParameters() must not return null"));
  }

  @Test
  void toolWithNonClassParameters_failsValidation() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> ToolValidator.validate(new NonClassParametersTool()));

    assertTrue(exception.getMessage().contains("getParameters() must return a Class"));
  }

  @Test
  void toolWithoutAnnotations_failsValidation() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> ToolValidator.validate(new NoAnnotationsTool()));

    assertTrue(
        exception
            .getMessage()
            .contains("should have @JsonTypeName or @JsonClassDescription annotation"));
  }

  @Test
  void toolWithoutFieldDescriptions_failsValidation() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> ToolValidator.validate(new NoFieldDescriptionsTool()));

    assertTrue(
        exception.getMessage().contains("fields without @JsonPropertyDescription annotations"));
  }

  @Test
  void validateAll_withValidTools_passes() {
    ToolValidator.validateAll(List.of(new ValidTool(), new ValidTool()));
  }

  @Test
  void validateAll_withInvalidTool_collectsAllErrors() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> ToolValidator.validateAll(List.of(new ValidTool(), new NoNameTool())));

    assertTrue(exception.getMessage().contains("Tool #2"));
    assertTrue(exception.getMessage().contains("getName() must return a non-empty string"));
  }

  @Test
  void validateAll_withEmptyList_passes() {
    ToolValidator.validateAll(List.of());
  }

  @Test
  void validateAll_withNull_passes() {
    ToolValidator.validateAll(null);
  }

  public static class ValidTool implements FunctionTool<UnknownContext, ValidTool.Input, String> {

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
      return Input.class;
    }

    @Override
    public boolean isStrict() {
      return true;
    }

    @Override
    public CompletableFuture<String> invoke(RunContext<UnknownContext> context, Input input) {
      return CompletableFuture.completedFuture("ok");
    }

    @Override
    public boolean needsApproval(RunContext<UnknownContext> context, Input input) {
      return false;
    }

    @Override
    public boolean isEnabled(RunContext<UnknownContext> context) {
      return true;
    }

    @Data
    @JsonTypeName("valid_tool")
    @JsonClassDescription("Input for valid tool")
    public static class Input {
      @JsonPropertyDescription("A message")
      private String message;
    }
  }

  public static class NoNameTool implements FunctionTool<UnknownContext, ValidTool.Input, String> {

    @Override
    public String getType() {
      return "function";
    }

    @Override
    public String getName() {
      return "";
    }

    @Override
    public String getDescription() {
      return "A tool without name";
    }

    @Override
    public Object getParameters() {
      return ValidTool.Input.class;
    }

    @Override
    public boolean isStrict() {
      return true;
    }

    @Override
    public CompletableFuture<String> invoke(
        RunContext<UnknownContext> context, ValidTool.Input input) {
      return CompletableFuture.completedFuture("ok");
    }

    @Override
    public boolean needsApproval(RunContext<UnknownContext> context, ValidTool.Input input) {
      return false;
    }

    @Override
    public boolean isEnabled(RunContext<UnknownContext> context) {
      return true;
    }
  }

  public static class NoDescriptionTool
      implements FunctionTool<UnknownContext, ValidTool.Input, String> {

    @Override
    public String getType() {
      return "function";
    }

    @Override
    public String getName() {
      return "no_description";
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Object getParameters() {
      return ValidTool.Input.class;
    }

    @Override
    public boolean isStrict() {
      return true;
    }

    @Override
    public CompletableFuture<String> invoke(
        RunContext<UnknownContext> context, ValidTool.Input input) {
      return CompletableFuture.completedFuture("ok");
    }

    @Override
    public boolean needsApproval(RunContext<UnknownContext> context, ValidTool.Input input) {
      return false;
    }

    @Override
    public boolean isEnabled(RunContext<UnknownContext> context) {
      return true;
    }
  }

  public static class WrongTypeTool
      implements FunctionTool<UnknownContext, ValidTool.Input, String> {

    @Override
    public String getType() {
      return "wrong";
    }

    @Override
    public String getName() {
      return "wrong_type";
    }

    @Override
    public String getDescription() {
      return "A tool with wrong type";
    }

    @Override
    public Object getParameters() {
      return ValidTool.Input.class;
    }

    @Override
    public boolean isStrict() {
      return true;
    }

    @Override
    public CompletableFuture<String> invoke(
        RunContext<UnknownContext> context, ValidTool.Input input) {
      return CompletableFuture.completedFuture("ok");
    }

    @Override
    public boolean needsApproval(RunContext<UnknownContext> context, ValidTool.Input input) {
      return false;
    }

    @Override
    public boolean isEnabled(RunContext<UnknownContext> context) {
      return true;
    }
  }

  public static class NullParametersTool
      implements FunctionTool<UnknownContext, ValidTool.Input, String> {

    @Override
    public String getType() {
      return "function";
    }

    @Override
    public String getName() {
      return "null_params";
    }

    @Override
    public String getDescription() {
      return "A tool with null parameters";
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
    public CompletableFuture<String> invoke(
        RunContext<UnknownContext> context, ValidTool.Input input) {
      return CompletableFuture.completedFuture("ok");
    }

    @Override
    public boolean needsApproval(RunContext<UnknownContext> context, ValidTool.Input input) {
      return false;
    }

    @Override
    public boolean isEnabled(RunContext<UnknownContext> context) {
      return true;
    }
  }

  public static class NonClassParametersTool
      implements FunctionTool<UnknownContext, ValidTool.Input, String> {

    @Override
    public String getType() {
      return "function";
    }

    @Override
    public String getName() {
      return "non_class_params";
    }

    @Override
    public String getDescription() {
      return "A tool with non-class parameters";
    }

    @Override
    public Object getParameters() {
      return "not a class";
    }

    @Override
    public boolean isStrict() {
      return true;
    }

    @Override
    public CompletableFuture<String> invoke(
        RunContext<UnknownContext> context, ValidTool.Input input) {
      return CompletableFuture.completedFuture("ok");
    }

    @Override
    public boolean needsApproval(RunContext<UnknownContext> context, ValidTool.Input input) {
      return false;
    }

    @Override
    public boolean isEnabled(RunContext<UnknownContext> context) {
      return true;
    }
  }

  public static class NoAnnotationsTool
      implements FunctionTool<UnknownContext, NoAnnotationsTool.BadInput, String> {

    @Override
    public String getType() {
      return "function";
    }

    @Override
    public String getName() {
      return "no_annotations";
    }

    @Override
    public String getDescription() {
      return "A tool without annotations";
    }

    @Override
    public Object getParameters() {
      return BadInput.class;
    }

    @Override
    public boolean isStrict() {
      return true;
    }

    @Override
    public CompletableFuture<String> invoke(RunContext<UnknownContext> context, BadInput input) {
      return CompletableFuture.completedFuture("ok");
    }

    @Override
    public boolean needsApproval(RunContext<UnknownContext> context, BadInput input) {
      return false;
    }

    @Override
    public boolean isEnabled(RunContext<UnknownContext> context) {
      return true;
    }

    @Data
    public static class BadInput {
      @JsonPropertyDescription("A message")
      private String message;
    }
  }

  public static class NoFieldDescriptionsTool
      implements FunctionTool<UnknownContext, NoFieldDescriptionsTool.BadInput, String> {

    @Override
    public String getType() {
      return "function";
    }

    @Override
    public String getName() {
      return "no_field_descriptions";
    }

    @Override
    public String getDescription() {
      return "A tool without field descriptions";
    }

    @Override
    public Object getParameters() {
      return BadInput.class;
    }

    @Override
    public boolean isStrict() {
      return true;
    }

    @Override
    public CompletableFuture<String> invoke(RunContext<UnknownContext> context, BadInput input) {
      return CompletableFuture.completedFuture("ok");
    }

    @Override
    public boolean needsApproval(RunContext<UnknownContext> context, BadInput input) {
      return false;
    }

    @Override
    public boolean isEnabled(RunContext<UnknownContext> context) {
      return true;
    }

    @Data
    @JsonTypeName("bad_input")
    @JsonClassDescription("Input without field descriptions")
    public static class BadInput {
      private String message;
    }
  }
}
