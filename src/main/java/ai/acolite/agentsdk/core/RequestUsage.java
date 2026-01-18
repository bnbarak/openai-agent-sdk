package ai.acolite.agentsdk.core;

import java.util.Map;

/**
 * RequestUsage
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/usage.ts">usage.ts</a>
 */
public class RequestUsage {

  private Double inputTokens;
  private Double outputTokens;
  private Double totalTokens;
  private Map<String, Double> inputTokensDetails;
  private Map<String, Double> outputTokensDetails;
}
