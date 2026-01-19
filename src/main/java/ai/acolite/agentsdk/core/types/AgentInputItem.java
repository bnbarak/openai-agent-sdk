package ai.acolite.agentsdk.core.types;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * AgentInputItem
 *
 * <p>Union of all possible input item types for agents.
 *
 * <p>JsonTypeInfo enables polymorphic serialization/deserialization: - Type information stored in
 * "@type" property in JSON - Allows Jackson to deserialize to correct concrete class
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/types/aliases.ts
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({
  @JsonSubTypes.Type(
      value = ai.acolite.agentsdk.core.RunMessageInputItem.class,
      name = "message_input"),
  @JsonSubTypes.Type(
      value = ai.acolite.agentsdk.core.RunMessageOutputItem.class,
      name = "message_output"),
  @JsonSubTypes.Type(value = ai.acolite.agentsdk.core.RunToolCallItem.class, name = "tool_call"),
  @JsonSubTypes.Type(
      value = ai.acolite.agentsdk.core.RunToolCallOutputItem.class,
      name = "tool_output")
})
public interface AgentInputItem {
  // Marker interface for agent input items
  // Subtypes are registered via @JsonSubTypes for polymorphic deserialization
}
