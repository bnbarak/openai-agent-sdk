package com.acoliteai.agentsdk.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * MCPTool
 *
 * <p>Tool from Model Context Protocol server.
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/mcp.ts
 */
public interface MCPTool {
  String getName();

  String getDescription();

  CompletableFuture<List<Object>> execute(Object params);
}
