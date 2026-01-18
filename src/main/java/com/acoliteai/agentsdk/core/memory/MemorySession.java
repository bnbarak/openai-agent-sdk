package com.acoliteai.agentsdk.core.memory;

import com.acoliteai.agentsdk.core.types.AgentInputItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;

/**
 * MemorySession - In-memory session storage
 *
 * <p>Stores conversation history in RAM. Data is lost when the JVM exits. Suitable for: -
 * Development and testing - Stateless applications where persistence isn't needed - Short-lived
 * conversations
 *
 * <p>Thread-safe using CopyOnWriteArrayList.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/memory/memorySession.ts">memory/memorySession.ts</a>
 */
public class MemorySession implements Session, AutoCloseable {

  @Getter private final String sessionId;

  private final CopyOnWriteArrayList<AgentInputItem> items;

  /**
   * Create a new MemorySession
   *
   * @param sessionId Unique session identifier
   */
  public MemorySession(String sessionId) {
    this.sessionId = sessionId;
    this.items = new CopyOnWriteArrayList<>();
  }

  @Override
  public CompletableFuture<String> getSessionId() {
    return CompletableFuture.completedFuture(sessionId);
  }

  @Override
  public CompletableFuture<List<AgentInputItem>> getItems(Double limit) {
    return CompletableFuture.supplyAsync(
        () -> {
          if (limit == null || limit >= items.size()) {
            return new ArrayList<>(items);
          }

          int limitInt = limit.intValue();
          if (limitInt <= 0) {
            return new ArrayList<>();
          }

          int start = items.size() - limitInt;
          return new ArrayList<>(items.subList(start, items.size()));
        });
  }

  @Override
  public CompletableFuture<Void> addItems(List<AgentInputItem> newItems) {
    return CompletableFuture.runAsync(
        () -> {
          if (newItems != null && !newItems.isEmpty()) {
            items.addAll(newItems);
          }
        });
  }

  @Override
  public CompletableFuture<Optional<AgentInputItem>> popItem() {
    return CompletableFuture.supplyAsync(
        () -> {
          if (items.isEmpty()) {
            return Optional.empty();
          }

          AgentInputItem lastItem = items.remove(items.size() - 1);
          return Optional.of(lastItem);
        });
  }

  @Override
  public CompletableFuture<Void> clearSession() {
    return CompletableFuture.runAsync(
        () -> {
          items.clear();
        });
  }

  @Override
  public void close() {
    // No resources to clean up for in-memory storage
  }
}
