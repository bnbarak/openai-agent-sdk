package ai.acolite.agentsdk.realworldapi;

import static org.junit.jupiter.api.Assertions.*;

import ai.acolite.agentsdk.core.*;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

@SetEnvironmentVariable(key = "OPENAI_MODEL", value = "gpt-4o-mini")
public class BadToolExampleTest {

  private final LLMJudge judge = new LLMJudge();

  @Test
  void toolThatThrows_agentRespondsGracefully() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("TestAgent")
            .instructions("You can verify service status using the tools you have.")
            .tools(List.of(new ThrowingTool()))
            .build();
    RunConfig config = RunConfig.builder().maxTurns(5).build();

    RunResult<UnknownContext, ?> result =
        Runner.run(agent, "Use the broken tool to say hello", config);

    assertTrue(result.getNewItems().size() < 12, "Should not loop trying failed tool");
    assertTrue(
        judge.evaluateErrorHandling(
            result.getFinalOutput().toString(),
            "The agent should acknowledge the tool failed and continue gracefully"),
        "Agent should handle tool failure appropriately");
  }

  @Test
  void disabledTool_notOfferedToLLM() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("TestAgent")
            .instructions("Use tools when available.")
            .tools(List.of(new DisabledTool()))
            .build();
    RunConfig config = RunConfig.builder().maxTurns(3).build();

    RunResult<UnknownContext, ?> result = Runner.run(agent, "Use the disabled tool", config);

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());
  }

  @Test
  void toolReturningError_agentAcknowledges() {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("TestAgent")
            .instructions("You are a helpful assistant. Do not use a tool that failed again")
            .tools(List.of(new ErrorReturningTool()))
            .build();
    RunConfig config = RunConfig.builder().maxTurns(5).build();

    RunResult<UnknownContext, ?> result =
        Runner.run(agent, "Check if the service XYZ is configured", config);

    assertNotNull(result);
    assertNotNull(result.getFinalOutput());
    assertTrue(result.getNewItems().size() < 12, "Should not loop after tool error");
    assertTrue(
        judge.evaluateErrorHandling(
            result.getFinalOutput().toString(),
            "The agent should acknowledge that the service is not configured"),
        "Agent should acknowledge configuration error from tool response");
  }

  public static class ThrowingTool
      implements FunctionTool<UnknownContext, ThrowingTool.Input, String> {

    @Override
    public String getType() {
      return "function";
    }

    @Override
    public String getName() {
      return "broken_tool";
    }

    @Override
    public String getDescription() {
      return "A tool that always fails";
    }

    @Override
    public Object getParameters() {
      return Input.class;
    }

    @Override
    public boolean isStrict() {
      return true;
    }

    @Override
    public CompletableFuture<String> invoke(RunContext<UnknownContext> context, Input input) {
      return CompletableFuture.failedFuture(new RuntimeException("Tool failed with exception"));
    }

    @Override
    public boolean needsApproval(RunContext<UnknownContext> context, Input input) {
      return false;
    }

    @Override
    public boolean isEnabled(RunContext<UnknownContext> context) {
      return true;
    }

    @Data
    @JsonTypeName("broken_tool")
    @JsonClassDescription("Input for broken tool")
    public static class Input {
      @JsonPropertyDescription("A test message")
      private String message;
    }
  }

  public static class DisabledTool
      implements FunctionTool<UnknownContext, DisabledTool.Input, String> {

    @Override
    public String getType() {
      return "function";
    }

    @Override
    public String getName() {
      return "disabled_tool";
    }

    @Override
    public String getDescription() {
      return "A disabled tool";
    }

    @Override
    public Object getParameters() {
      return Input.class;
    }

    @Override
    public boolean isStrict() {
      return true;
    }

    @Override
    public CompletableFuture<String> invoke(RunContext<UnknownContext> context, Input input) {
      throw new RuntimeException("Disabled tool should not be invoked");
    }

    @Override
    public boolean needsApproval(RunContext<UnknownContext> context, Input input) {
      return false;
    }

    @Override
    public boolean isEnabled(RunContext<UnknownContext> context) {
      return false;
    }

    @Data
    @JsonTypeName("disabled_tool")
    @JsonClassDescription("Input for disabled tool")
    public static class Input {
      @JsonPropertyDescription("A test message")
      private String message;
    }
  }

  public static class ErrorReturningTool
      implements FunctionTool<UnknownContext, ErrorReturningTool.Input, ErrorReturningTool.Output> {

    @Override
    public String getType() {
      return "function";
    }

    @Override
    public String getName() {
      return "check_service";
    }

    @Override
    public String getDescription() {
      return "Checks if a service is configured";
    }

    @Override
    public Object getParameters() {
      return Input.class;
    }

    @Override
    public boolean isStrict() {
      return true;
    }

    @Override
    public CompletableFuture<Output> invoke(RunContext<UnknownContext> context, Input input) {
      return CompletableFuture.completedFuture(
          new Output(false, "Error!. Please set up credentials."));
    }

    @Override
    public boolean needsApproval(RunContext<UnknownContext> context, Input input) {
      return false;
    }

    @Override
    public boolean isEnabled(RunContext<UnknownContext> context) {
      return true;
    }

    @Data
    @JsonTypeName("check_service")
    @JsonClassDescription("Input for checking service configuration")
    public static class Input {
      @JsonPropertyDescription("Name of the service")
      private String serviceName;
    }

    public record Output(@JsonProperty boolean configured, @JsonProperty String message) {}
  }
}
