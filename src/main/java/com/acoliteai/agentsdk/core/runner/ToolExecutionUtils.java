package com.acoliteai.agentsdk.core.runner;

import com.acoliteai.agentsdk.core.Agent;
import com.acoliteai.agentsdk.core.FunctionTool;
import com.acoliteai.agentsdk.openai.SerializationUtils;

/**
 * ToolExecutionUtils
 *
 * <p>Static utility methods for tool execution and parameter handling. Extracted from Runner for
 * better testability and reusability.
 */
public class ToolExecutionUtils {

  private ToolExecutionUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Find a tool by name in the agent's tool list.
   *
   * @param agent Agent containing tools
   * @param toolName Name of the tool to find
   * @return FunctionTool if found, null otherwise
   */
  public static FunctionTool<?, ?, ?> findToolByName(Agent<?, ?> agent, String toolName) {
    if (agent.getTools() == null) {
      return null;
    }

    return agent.getTools().stream()
        .filter(tool -> tool instanceof FunctionTool)
        .map(tool -> (FunctionTool<?, ?, ?>) tool)
        .filter(tool -> tool.getName().equals(toolName))
        .findFirst()
        .orElse(null);
  }

  /**
   * Deserialize parameters from Map/JSON to the tool's input type.
   *
   * <p>Uses Jackson with FAIL_ON_UNKNOWN_PROPERTIES=false to handle API responses that may include
   * extra fields.
   *
   * @param parameters Raw parameters (typically Map from JSON)
   * @param parameterSchema Schema defining the parameter type (should be a Class)
   * @return Deserialized parameters as the target type
   * @throws RuntimeException if deserialization fails
   */
  public static Object deserializeParameters(Object parameters, Object parameterSchema) {
    if (parameters == null) {
      return null;
    }

    if (parameterSchema instanceof Class<?> targetClass) {
      return SerializationUtils.convertViaJson(parameters, targetClass);
    }

    return parameters;
  }
}
