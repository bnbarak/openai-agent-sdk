package ai.acolite.agentsdk.core;

import ai.acolite.agentsdk.core.types.AgentOutputType;
import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * ModelRequest
 *
 * <p>Immutable request to a language model.
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/model.ts
 */
@Value
@Builder
public class ModelRequest {
  List<Object> input;
  String instructions;
  String model;
  ModelSettings settings;
  List<Object> tools;
  AgentOutputType outputType;
}
