package com.acoliteai.agentsdk.exceptions;

import lombok.Getter;

/**
 * MaxTurnsExceededError
 *
 * <p>Thrown when the agent execution exceeds the maxTurns safety limit. Default maxTurns is 10,
 * configurable via RunConfig.
 *
 * <p>Follows TypeScript SDK pattern from @openai/agents-core Source:
 * https://openai.github.io/openai-agents-js/guides/running-agents/
 */
@Getter
public class MaxTurnsExceededError extends AgentsError {
  private final int maxTurns;
  private final int currentTurn;

  public MaxTurnsExceededError(int maxTurns, int currentTurn) {
    super(
        String.format(
            "Agent execution exceeded maxTurns limit of %d (reached turn %d)",
            maxTurns, currentTurn));
    this.maxTurns = maxTurns;
    this.currentTurn = currentTurn;
  }
}
