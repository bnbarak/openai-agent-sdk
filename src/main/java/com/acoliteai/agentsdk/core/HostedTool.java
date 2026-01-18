package com.acoliteai.agentsdk.core;

/**
 * HostedTool
 *
 * <p>Tool that runs on hosted services (e.g., web search, file search).
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tool.ts
 */
public class HostedTool implements Tool<Object> {
  private String type;
  private String name;
  private String description;

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }
}
