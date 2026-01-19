package ai.acolite.agentsdk.core.shims.mcp.server;

import ai.acolite.agentsdk.core.BaseMCPServerStreamableHttp;
import ai.acolite.agentsdk.core.Client;
import ai.acolite.agentsdk.core.InitializeResult;
import ai.acolite.agentsdk.core.MCPServerStreamableHttpOptions;
import ai.acolite.agentsdk.core.MCPTool;
import ai.acolite.agentsdk.exceptions.NotImplementedException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * NodeMCPServerStreamableHttp
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/shims/mcp-server/node.ts">shims/mcp-server/node.ts</a>
 */
public class NodeMCPServerStreamableHttp extends BaseMCPServerStreamableHttp {

  private Optional<Client> session;
  private List<Object> _toolsList;
  private Optional<InitializeResult> serverInitializeResult;
  private Double timeout;
  private MCPServerStreamableHttpOptions params;
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
