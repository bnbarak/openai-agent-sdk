package com.acoliteai.agentsdk.core;

/**
 * ShellAction
 *
 * <p>Represents a shell command action.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/types/protocol.ts
 */
public class ShellAction {
  private String command;
  private String workingDirectory;

  public ShellAction() {}

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getWorkingDirectory() {
    return workingDirectory;
  }

  public void setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
  }
}
