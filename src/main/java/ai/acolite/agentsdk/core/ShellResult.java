package ai.acolite.agentsdk.core;

/**
 * ShellResult
 *
 * <p>Result from shell command execution.
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/shell.ts
 */
public class ShellResult {
  private String output;
  private int exitCode;
  private boolean success;

  public ShellResult() {}

  public String getOutput() {
    return output;
  }

  public void setOutput(String output) {
    this.output = output;
  }

  public int getExitCode() {
    return exitCode;
  }

  public void setExitCode(int exitCode) {
    this.exitCode = exitCode;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }
}
