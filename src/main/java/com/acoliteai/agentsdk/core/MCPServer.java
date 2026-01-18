package com.acoliteai.agentsdk.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * MCPServer
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/mcp.ts">mcp.ts</a>
 */
public interface MCPServer {

  /**
   * connect
   *
   * @return CompletableFuture<Void>
   */
  CompletableFuture<Void> connect();

  /**
   * close
   *
   * @return CompletableFuture<Void>
   */
  CompletableFuture<Void> close();

  /**
   * listTools
   *
   * @return CompletableFuture<List<MCPTool>>
   */
  CompletableFuture<List<MCPTool>> listTools();

  /**
   * invalidateToolsCache
   *
   * @return CompletableFuture<Void>
   */
  CompletableFuture<Void> invalidateToolsCache();
}
