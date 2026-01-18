package com.acoliteai.agentsdk.core.runner;

import com.acoliteai.agentsdk.core.ModelResponse;
import com.acoliteai.agentsdk.core.RunItem;
import com.acoliteai.agentsdk.core.types.AgentInputItem;
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
