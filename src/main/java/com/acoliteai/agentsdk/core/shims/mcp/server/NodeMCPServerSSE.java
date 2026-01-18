package com.acoliteai.agentsdk.core.shims.mcp.server;

import com.acoliteai.agentsdk.core.BaseMCPServerSSE;
import com.acoliteai.agentsdk.core.Client;
import com.acoliteai.agentsdk.core.InitializeResult;
import com.acoliteai.agentsdk.core.MCPServerSSEOptions;
import com.acoliteai.agentsdk.core.MCPTool;
import com.acoliteai.agentsdk.exceptions.NotImplementedException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * NodeMCPServerSSE
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/shims/mcp-server/node.ts">shims/mcp-server/node.ts</a>
 */
public class NodeMCPServerSSE extends BaseMCPServerSSE {

  private Optional<Client> session;
  private List<Object> _toolsList;
  private Optional<InitializeResult> serverInitializeResult;
  private Double timeout;
  private MCPServerSSEOptions params;
  private String _name;
  private Object transport;

  /**
   * connect
   *
   * @return CompletableFuture<Void>
   * @throws NotImplementedException Not yet implemented
   */
  public CompletableFuture<Void> connect() {
    throw new NotImplementedException("Not yet implemented");
  }

  /**
   * invalidateToolsCache
   *
   * @return CompletableFuture<Void>
   * @throws NotImplementedException Not yet implemented
   */
  public CompletableFuture<Void> invalidateToolsCache() {
    throw new NotImplementedException("Not yet implemented");
  }

  /**
   * listTools
   *
   * @return CompletableFuture<List<MCPTool>>
   * @throws NotImplementedException Not yet implemented
   */
  public CompletableFuture<List<MCPTool>> listTools() {
    throw new NotImplementedException("Not yet implemented");
  }

  /**
   * close
   *
   * @return CompletableFuture<Void>
   * @throws NotImplementedException Not yet implemented
   */
  public CompletableFuture<Void> close() {
    throw new NotImplementedException("Not yet implemented");
  }
}
