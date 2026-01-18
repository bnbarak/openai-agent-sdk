package ai.acolite.agentsdk.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Value;

/**
 * ModelResponse
 *
 * <p>Immutable response from a language model.
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/model.ts
 */
@Value
@Builder
public class ModelResponse {
  Usage usage;
  List<Object> output; // AgentOutputItem[]
  Optional<String> responseId;
  Optional<Map<String, Object>> providerData;
}
