package ai.acolite.agentsdk.core;

/**
 * InitializeResult
 *
 * <p>Result from MCP initialization.
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/mcp.ts
 */
public class InitializeResult {
  private String protocolVersion;
  private Object capabilities;

  public InitializeResult() {}

  public String getProtocolVersion() {
    return protocolVersion;
  }

  public void setProtocolVersion(String protocolVersion) {
    this.protocolVersion = protocolVersion;
  }

  public Object getCapabilities() {
    return capabilities;
  }

  public void setCapabilities(Object capabilities) {
    this.capabilities = capabilities;
  }
}
