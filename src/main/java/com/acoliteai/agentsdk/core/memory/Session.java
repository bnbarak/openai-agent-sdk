package com.acoliteai.agentsdk.core.memory;

import com.acoliteai.agentsdk.core.types.AgentInputItem;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Session
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/memory/session.ts">memory/session.ts</a>
 */
public interface Session {

  /**
   * getSessionId
   *
   * @return CompletableFuture<String>
   */
  CompletableFuture<String> getSessionId();

  /**
   * getItems
   *
   * @param limit Double
   * @return CompletableFuture<List<AgentInputItem>>
   */
  CompletableFuture<List<AgentInputItem>> getItems(Double limit);

  /**
   * addItems
   *
   * @param items List<AgentInputItem>
   * @return CompletableFuture<Void>
   */
  CompletableFuture<Void> addItems(List<AgentInputItem> items);

  /**
   * popItem
   *
   * @return CompletableFuture<Optional<AgentInputItem>>
   */
  CompletableFuture<Optional<AgentInputItem>> popItem();

  /**
   * clearSession
   *
   * @return CompletableFuture<Void>
   */
  CompletableFuture<Void> clearSession();
}
