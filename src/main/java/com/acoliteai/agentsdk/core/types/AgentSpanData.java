package com.acoliteai.agentsdk.core.types;

/**
 * AgentSpanData
 *
 * <p>Data for tracing spans related to agents.
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing
 */
public class AgentSpanData {
  private String agentName;
  private Object data;

  public AgentSpanData() {}

  public String getAgentName() {
    return agentName;
  }

  public void setAgentName(String agentName) {
    this.agentName = agentName;
  }

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }
}
