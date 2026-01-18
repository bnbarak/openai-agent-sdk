package com.acoliteai.agentsdk.core;

import java.util.Map;
import java.util.Optional;

/**
 * ModelSettings
 *
 * <p>Configuration for model behavior.
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/model.ts
 */
public class ModelSettings {
  private Optional<Double> temperature;
  private Optional<Double> topP;
  private Optional<Double> frequencyPenalty;
  private Optional<Double> presencePenalty;
  private Optional<String> toolChoice; // ModelSettingsToolChoice
  private Optional<Boolean> parallelToolCalls;
  private Optional<String> truncation; // 'auto' | 'disabled'
  private Optional<Integer> maxTokens;
  private Optional<Boolean> store;
  private Optional<String> promptCacheRetention; // 'in-memory' | '24h' | null
  private Optional<Object> reasoning; // ModelSettingsReasoning
  private Optional<Object> text; // ModelSettingsText
  private Optional<Map<String, Object>> providerData;

  public ModelSettings() {
    this.temperature = Optional.empty();
    this.topP = Optional.empty();
    this.frequencyPenalty = Optional.empty();
    this.presencePenalty = Optional.empty();
    this.toolChoice = Optional.empty();
    this.parallelToolCalls = Optional.empty();
    this.truncation = Optional.empty();
    this.maxTokens = Optional.empty();
    this.store = Optional.empty();
    this.promptCacheRetention = Optional.empty();
    this.reasoning = Optional.empty();
    this.text = Optional.empty();
    this.providerData = Optional.empty();
  }

  // Getters and setters would go here
}
