package ai.acolite.agentsdk.core;

import java.util.List;
import java.util.Map;

/**
 * JsonObjectSchema
 *
 * <p>JSON schema for objects.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/types/helpers.ts
 */
public class JsonObjectSchema<T> {
  private String type = "object";
  private Map<String, Object> properties;
  private List<String> required;
  private boolean additionalProperties;

  public JsonObjectSchema() {}

  public String getType() {
    return type;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  public List<String> getRequired() {
    return required;
  }

  public void setRequired(List<String> required) {
    this.required = required;
  }

  public boolean isAdditionalProperties() {
    return additionalProperties;
  }

  public void setAdditionalProperties(boolean additionalProperties) {
    this.additionalProperties = additionalProperties;
  }
}
