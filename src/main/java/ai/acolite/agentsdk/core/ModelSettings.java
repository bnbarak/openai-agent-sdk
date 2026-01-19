package ai.acolite.agentsdk.core;

import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ModelSettings
 *
 * <p>Configuration for model behavior.
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/model.ts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelSettings {
  @Builder.Default private Optional<Double> temperature = Optional.empty();
  @Builder.Default private Optional<Double> topP = Optional.empty();
  @Builder.Default private Optional<Double> frequencyPenalty = Optional.empty();
  @Builder.Default private Optional<Double> presencePenalty = Optional.empty();
  @Builder.Default private Optional<String> toolChoice = Optional.empty();
  @Builder.Default private Optional<Boolean> parallelToolCalls = Optional.empty();
  @Builder.Default private Optional<String> truncation = Optional.empty();
  @Builder.Default private Optional<Integer> maxTokens = Optional.empty();
  @Builder.Default private Optional<Integer> maxToolCalls = Optional.empty();
  @Builder.Default private Optional<Boolean> store = Optional.empty();
  @Builder.Default private Optional<String> promptCacheRetention = Optional.empty();
  @Builder.Default private Optional<Object> reasoning = Optional.empty();
  @Builder.Default private Optional<Object> text = Optional.empty();
  @Builder.Default private Optional<Map<String, Object>> providerData = Optional.empty();
}
