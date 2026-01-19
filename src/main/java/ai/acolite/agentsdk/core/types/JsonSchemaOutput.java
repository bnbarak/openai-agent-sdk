package ai.acolite.agentsdk.core.types;

import lombok.Getter;

@Getter
public class JsonSchemaOutput<T> implements AgentOutputType {
  private final Class<T> targetClass;

  public JsonSchemaOutput(Class<T> targetClass) {
    this.targetClass = targetClass;
  }

  public static <T> JsonSchemaOutput<T> of(Class<T> targetClass) {
    return new JsonSchemaOutput<>(targetClass);
  }
}
