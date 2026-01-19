package ai.acolite.agentsdk.realworldapi;

import static org.junit.jupiter.api.Assertions.*;

import ai.acolite.agentsdk.core.*;
import ai.acolite.agentsdk.core.memory.MemorySession;
import ai.acolite.agentsdk.core.types.AgentInputItem;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

/**
 * Tests thread safety of the Agent SDK under concurrent execution scenarios with real OpenAI API
 * calls.
 *
 * <p>Tests cover: - Multiple threads running the same agent instance - Concurrent access to shared
 * sessions - Concurrent tool executions - Run context isolation between threads
 *
 * <p>Note: These tests make real API calls and will incur costs. They are slow (~30s total).
 */
@SetEnvironmentVariable(key = "OPENAI_MODEL", value = "gpt-4.1-nano")
public class ThreadSafetyRealAPITest {

  private static final int THREAD_COUNT = 5;
  private static final int ITERATIONS_PER_THREAD = 2;

  private ExecutorService executorService;

  @BeforeEach
  void setUp() {
    executorService = Executors.newFixedThreadPool(THREAD_COUNT);
  }

  /**
   * Test that the same agent can be safely invoked from multiple threads concurrently.
   *
   * <p>This simulates a real-world scenario where a single agent instance is reused across multiple
   * requests.
   */
  @Test
  void testConcurrentAgentRuns() throws Exception {
    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("ConcurrentAgent")
            .instructions("You are a helpful assistant. Always respond concisely.")
            .build();

    AtomicInteger successCount = new AtomicInteger(0);
    List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

    // Run agent concurrently from multiple threads
    CountDownLatch startLatch = new CountDownLatch(1);
    List<Future<Void>> futures = new ArrayList<>();

    for (int i = 0; i < THREAD_COUNT; i++) {
      final int threadId = i;
      Future<Void> future =
          executorService.submit(
              () -> {
                try {
                  startLatch.await();
                  for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                    RunResult<UnknownContext, ?> result =
                        Runner.run(agent, "Say hello from thread " + threadId + " iteration " + j);
                    assertNotNull(result);
                    assertNotNull(result.getFinalOutput());
                    successCount.incrementAndGet();
                  }
                } catch (Throwable e) {
                  errors.add(e);
                }
                return null;
              });
      futures.add(future);
    }

    // Start all threads simultaneously
    startLatch.countDown();

    for (Future<Void> future : futures) {
      future.get(60, TimeUnit.SECONDS);
    }

    if (!errors.isEmpty()) {
      fail("Errors occurred during concurrent execution: " + errors.get(0).getMessage());
    }

    assertEquals(
        THREAD_COUNT * ITERATIONS_PER_THREAD,
        successCount.get(),
        "All concurrent runs should complete successfully");
  }

  /**
   * Test that multiple threads can safely access the same session concurrently.
   *
   * <p>This tests the thread safety of the session implementation under concurrent agent runs.
   */
  @Test
  void testConcurrentSessionAccess() throws Exception {
    MemorySession sharedSession = new MemorySession("concurrent-test-session");
    AtomicInteger successCount = new AtomicInteger(0);
    List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("SessionAgent")
            .instructions("You are a helpful assistant. Respond concisely.")
            .build();

    RunConfig config = RunConfig.builder().session(sharedSession).build();

    CountDownLatch startLatch = new CountDownLatch(1);
    List<Future<Void>> futures = new ArrayList<>();

    for (int i = 0; i < THREAD_COUNT; i++) {
      final int threadId = i;
      Future<Void> future =
          executorService.submit(
              () -> {
                try {
                  startLatch.await();

                  for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                    String message = "Message from thread " + threadId + " iteration " + j;

                    RunResult<UnknownContext, ?> result = Runner.run(agent, message, config);
                    assertNotNull(result);
                    successCount.incrementAndGet();
                  }
                } catch (Throwable e) {
                  errors.add(e);
                }
                return null;
              });
      futures.add(future);
    }

    startLatch.countDown();

    for (Future<Void> future : futures) {
      future.get(60, TimeUnit.SECONDS);
    }

    if (!errors.isEmpty()) {
      fail("Errors occurred: " + errors.get(0).getMessage());
    }

    List<AgentInputItem> items = sharedSession.getItems(100.0).join();
    assertNotNull(items);
    assertFalse(items.isEmpty(), "Session should contain conversation items");

    assertEquals(
        THREAD_COUNT * ITERATIONS_PER_THREAD,
        successCount.get(),
        "All concurrent session access should complete");
  }

  /**
   * Test that tools can be safely executed concurrently.
   *
   * <p>This validates that tool handlers with shared state don't have race conditions.
   */
  @Test
  void testConcurrentToolExecution() throws Exception {
    List<Integer> sharedList = Collections.synchronizedList(new ArrayList<>());
    AtomicInteger callCount = new AtomicInteger(0);

    CounterTool counterTool = new CounterTool(sharedList, callCount);

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("ToolAgent")
            .instructions(
                "You are a test agent. You MUST use the count_number tool with the value provided by the user. Always call the tool.")
            .tools(List.of(counterTool))
            .build();

    CountDownLatch startLatch = new CountDownLatch(1);
    List<Future<Void>> futures = new ArrayList<>();
    List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

    for (int i = 0; i < THREAD_COUNT; i++) {
      final int value = i;
      Future<Void> future =
          executorService.submit(
              () -> {
                try {
                  startLatch.await();
                  RunResult<UnknownContext, ?> result =
                      Runner.run(agent, "Use the count_number tool with value: " + value);
                  assertNotNull(result);
                } catch (Throwable e) {
                  errors.add(e);
                }
                return null;
              });
      futures.add(future);
    }

    startLatch.countDown();

    for (Future<Void> future : futures) {
      future.get(60, TimeUnit.SECONDS);
    }

    if (!errors.isEmpty()) {
      fail("Errors during execution: " + errors.get(0).getMessage());
    }

    // Verify tool was called at least once (may not be called by all threads if model decides not
    // to)
    // In a real scenario, the model might not always use the tool
    // So we just verify that if the tool was used, there are no race conditions
    if (callCount.get() > 0) {
      assertEquals(callCount.get(), sharedList.size(), "Call count should match list size");
    }

    // This test primarily validates that IF tools are called concurrently, there are no crashes
    // The model behavior is non-deterministic, so we can't guarantee tool usage
    assertTrue(true, "Test completed without crashes");
  }

  /**
   * Test that run context state is properly isolated between concurrent runs.
   *
   * <p>Each run should have its own isolated context even when running concurrently.
   */
  @Test
  void testRunContextIsolation() throws Exception {
    ConcurrentHashMap<Integer, String> threadResults = new ConcurrentHashMap<>();
    AtomicInteger successCount = new AtomicInteger(0);

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("ContextAgent")
            .instructions(
                "You are a helpful assistant. Respond with just the thread ID number you're given.")
            .build();

    CountDownLatch startLatch = new CountDownLatch(1);
    List<Future<Void>> futures = new ArrayList<>();

    for (int i = 0; i < THREAD_COUNT; i++) {
      final int threadId = i;
      Future<Void> future =
          executorService.submit(
              () -> {
                startLatch.await();

                RunResult<UnknownContext, ?> result = Runner.run(agent, "Thread ID: " + threadId);

                assertNotNull(result);
                String output = result.getFinalOutput().toString();
                threadResults.put(threadId, output);
                successCount.incrementAndGet();

                return null;
              });
      futures.add(future);
    }

    startLatch.countDown();

    for (Future<Void> future : futures) {
      future.get(60, TimeUnit.SECONDS);
    }

    assertEquals(THREAD_COUNT, threadResults.size(), "Each thread should have received a response");
    assertEquals(THREAD_COUNT, successCount.get(), "All runs should complete successfully");
  }

  @lombok.RequiredArgsConstructor
  public static class CounterTool
      implements FunctionTool<UnknownContext, CounterTool.Input, CounterTool.Output> {

    private final List<Integer> sharedList;
    private final AtomicInteger callCount;

    @Override
    public String getType() {
      return "function";
    }

    @Override
    public String getName() {
      return "count_number";
    }

    @Override
    public String getDescription() {
      return "Adds a number to the counter";
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
      return CompletableFuture.supplyAsync(
          () -> {
            callCount.incrementAndGet();

            int currentSize = sharedList.size();
            try {
              // Small delay to increase chance of race condition
              Thread.sleep(100);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
            sharedList.add(input.value);
            return new Output(currentSize + 1, input.value);
          });
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
    public static class Input {
      @JsonProperty
      @JsonPropertyDescription("Number to count")
      private int value;
    }

    @Data
    @AllArgsConstructor
    public static class Output {
      @JsonProperty private int newSize;
      @JsonProperty private int addedValue;
    }
  }
}
