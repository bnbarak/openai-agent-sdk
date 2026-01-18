package ai.acolite.agentsdk.core;

/**
 * Tool
 *
 * <p>Base interface for all tool types. In TypeScript this is a union: FunctionTool | ComputerTool
 * | ShellTool | ApplyPatchTool | HostedTool
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tool.ts
 */
public interface Tool<TContext> {
  String getType();

  String getName();

  String getDescription();
}
