package ai.acolite.agentsdk.core;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility for validating that tools are properly configured for use with OpenAI's function calling
 * framework.
 *
 * <p>OpenAI's function calling requires properly annotated parameter classes. This validator checks
 * that tools meet the requirements before runtime errors occur.
 *
 * <p>Example usage in tests:
 *
 * <pre>{@code
 * @Test
 * void myTool_isProperlyConfigured() {
 *     ToolValidator.validate(new MyTool());
 * }
 * }</pre>
 *
 * <p>Example usage at agent build time (automatic):
 *
 * <pre>{@code
 * Agent<UnknownContext, TextOutput> agent =
 *     Agent.<UnknownContext, TextOutput>builder()
 *         .name("MyAgent")
 *         .tools(List.of(new MyTool()))  // Automatically validated
 *         .build();
 * }</pre>
 *
 * @see <a href="https://platform.openai.com/docs/guides/function-calling">OpenAI Function
 *     Calling</a>
 */
public class ToolValidator {

  /**
   * Validates that a tool is properly configured for OpenAI function calling.
   *
   * @param tool The tool to validate
   * @throws IllegalArgumentException if validation fails with details about what's wrong
   */
  public static void validate(Object tool) {
    if (!(tool instanceof FunctionTool<?, ?, ?> functionTool)) {
      throw new IllegalArgumentException(
          "Tool must implement FunctionTool interface, got: " + tool.getClass().getName());
    }

    List<String> issues = new ArrayList<>();
    validateBasicProperties(functionTool, issues);
    validateParameters(functionTool, issues);
    if (!issues.isEmpty()) {
      throw new IllegalArgumentException(
          String.format(
              "Tool '%s' has validation errors:\n  - %s\n\n"
                  + "See ErrorReturningTool in BadToolExampleTest for a working example.",
              functionTool.getName(), String.join("\n  - ", issues)));
    }
  }

  private static void validateBasicProperties(FunctionTool<?, ?, ?> tool, List<String> issues) {
    if (tool.getName() == null || tool.getName().isEmpty()) {
      issues.add("getName() must return a non-empty string");
    }

    if (tool.getDescription() == null || tool.getDescription().isEmpty()) {
      issues.add("getDescription() must return a non-empty string");
    }

    if (!"function".equals(tool.getType())) {
      issues.add("getType() must return 'function'");
    }
  }

  private static void validateParameters(FunctionTool<?, ?, ?> tool, List<String> issues) {
    Object parameters = tool.getParameters();

    if (parameters == null) {
      issues.add("getParameters() must not return null");
      return;
    }

    if (!(parameters instanceof Class<?> paramClass)) {
      issues.add(
          String.format(
              "getParameters() must return a Class, got: %s", parameters.getClass().getName()));
      return;
    }

    boolean hasJsonTypeName = paramClass.isAnnotationPresent(JsonTypeName.class);
    boolean hasJsonClassDescription = paramClass.isAnnotationPresent(JsonClassDescription.class);
    if (!hasJsonTypeName && !hasJsonClassDescription) {
      issues.add(
          String.format(
              "Parameter class '%s' should have @JsonTypeName or @JsonClassDescription annotation "
                  + "for proper OpenAI schema generation",
              paramClass.getSimpleName()));
    }

    validateFieldDescriptions(paramClass, issues);
  }

  private static void validateFieldDescriptions(Class<?> paramClass, List<String> issues) {
    List<Field> allFields = new ArrayList<>();
    Class<?> current = paramClass;
    while (current != null && current != Object.class) {
      allFields.addAll(Arrays.asList(current.getDeclaredFields()));
      current = current.getSuperclass();
    }

    boolean hasFieldsWithoutDescriptions = false;
    for (Field field : allFields) {
      if (field.isSynthetic() || java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      if (!field.isAnnotationPresent(JsonPropertyDescription.class)) {
        hasFieldsWithoutDescriptions = true;
        break;
      }
    }

    if (hasFieldsWithoutDescriptions) {
      issues.add(
          String.format(
              "Parameter class '%s' has fields without @JsonPropertyDescription annotations. "
                  + "All fields should have descriptions for OpenAI function calling.",
              paramClass.getSimpleName()));
    }
  }

  /**
   * Validates a list of tools, collecting all validation errors.
   *
   * @param tools List of tools to validate
   * @throws IllegalArgumentException if any tool fails validation
   */
  public static void validateAll(List<?> tools) {
    if (tools == null || tools.isEmpty()) {
      return;
    }

    List<String> allErrors = new ArrayList<>();
    for (int i = 0; i < tools.size(); i++) {
      try {
        validate(tools.get(i));
      } catch (IllegalArgumentException e) {
        allErrors.add(String.format("Tool #%d: %s", i + 1, e.getMessage()));
      }
    }

    if (!allErrors.isEmpty()) {
      throw new IllegalArgumentException(
          "Tool validation failed:\n" + String.join("\n\n", allErrors));
    }
  }
}
