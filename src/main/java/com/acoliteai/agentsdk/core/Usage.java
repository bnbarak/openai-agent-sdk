package com.acoliteai.agentsdk.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Value;

/**
 * Usage
 *
 * <p>Tracks token usage statistics from API calls.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/usage.ts">usage.ts</a>
 */
@Value
@Builder
public class Usage {

  Double requests;
  Double inputTokens;
  Double outputTokens;
  Double totalTokens;
  List<Map<String, Double>> inputTokensDetails;
  List<Map<String, Double>> outputTokensDetails;
  Optional<List<RequestUsage>> requestUsageEntries;

  /** Creates an empty Usage instance with all counts set to 0 */
  public static Usage empty() {
    return Usage.builder()
        .requests(0.0)
        .inputTokens(0.0)
        .outputTokens(0.0)
        .totalTokens(0.0)
        .build();
  }

  /**
   * Adds the usage from another Usage instance to this one
   *
   * @param other The Usage instance to add
   * @return A new Usage instance with summed values
   */
  public Usage add(Usage other) {
    if (other == null) {
      return this;
    }

    return Usage.builder()
        .requests(safeAdd(this.requests, other.requests))
        .inputTokens(safeAdd(this.inputTokens, other.inputTokens))
        .outputTokens(safeAdd(this.outputTokens, other.outputTokens))
        .totalTokens(safeAdd(this.totalTokens, other.totalTokens))
        // TODO: Merge inputTokensDetails and outputTokensDetails
        .inputTokensDetails(this.inputTokensDetails)
        .outputTokensDetails(this.outputTokensDetails)
        .requestUsageEntries(this.requestUsageEntries)
        .build();
  }

  private static Double safeAdd(Double a, Double b) {
    double aVal = a != null ? a : 0.0;
    double bVal = b != null ? b : 0.0;
    return aVal + bVal;
  }
}
