package ai.acolite.agentsdk.core.runner;

import ai.acolite.agentsdk.core.ModelResponse;
import ai.acolite.agentsdk.core.RunItem;
import ai.acolite.agentsdk.core.types.AgentInputItem;
import java.util.List;
import java.util.Optional;

/**
 * ServerConversationTracker
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/runner/conversation.ts">...</a>
 */
public class ServerConversationTracker {

  private Optional<List<AgentInputItem>> remainingInitialInput;
  private List<Object> originalInput;
  private List<RunItem> generatedItems;
  private List<ModelResponse> modelResponses;
}
