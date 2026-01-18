package ai.acolite.agentsdk.core;

import java.util.concurrent.CompletableFuture;

/**
 * Shell
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/shell.ts">shell.ts</a>
 */
public interface Shell {

  /**
   * run
   *
   * @param action ShellAction
   * @return CompletableFuture<ShellResult>
   */
  CompletableFuture<ShellResult> run(ShellAction action);
}
