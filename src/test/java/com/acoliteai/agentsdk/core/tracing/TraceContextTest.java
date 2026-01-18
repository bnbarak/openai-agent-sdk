package com.acoliteai.agentsdk.core.tracing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Unit tests for TraceContext */
class TraceContextTest {

  @AfterEach
  void cleanup() {
    TraceContext.reset();
  }

  @Test
  void getCurrentTrace_returnsEmptyWhenNoContext() {
    Optional<Trace> trace = TraceContext.getCurrentTrace();
    assertTrue(trace.isEmpty());
  }

  @Test
  void getCurrentSpan_returnsEmptyWhenNoContext() {
    Optional<Span<?>> span = TraceContext.getCurrentSpan();
    assertTrue(span.isEmpty());
  }

  @Test
  void withTrace_setsAndRestoresContext() {
    Trace trace =
        Trace.builder().traceId(TracingUtils.generateTraceId()).name("test-trace").build();

    String result =
        TraceContext.withTrace(
            trace,
            () -> {
              Optional<Trace> currentTrace = TraceContext.getCurrentTrace();
              assertTrue(currentTrace.isPresent());
              assertEquals(trace.getTraceId(), currentTrace.get().getTraceId());
              return "success";
            });

    assertEquals("success", result);

    // Context should be cleared after
    Optional<Trace> traceAfter = TraceContext.getCurrentTrace();
    assertTrue(traceAfter.isEmpty());
  }

  @Test
  void withTrace_startsAndEndsTrace() {
    Trace trace =
        Trace.builder().traceId(TracingUtils.generateTraceId()).name("test-trace").build();

    assertFalse(trace.isStarted());
    assertFalse(trace.isEnded());

    TraceContext.withTrace(
        trace,
        () -> {
          assertTrue(trace.isStarted());
          assertFalse(trace.isEnded());
          return null;
        });

    assertTrue(trace.isStarted());
    assertTrue(trace.isEnded());
  }

  @Test
  void withTraceAsync_setsAndRestoresContext() throws ExecutionException, InterruptedException {
    Trace trace =
        Trace.builder().traceId(TracingUtils.generateTraceId()).name("test-trace-async").build();

    CompletableFuture<String> future =
        TraceContext.withTraceAsync(
            trace, () -> CompletableFuture.completedFuture("async-success"));

    String result = future.get();
    assertEquals("async-success", result);

    // Trace should be ended
    assertTrue(trace.isStarted());
    assertTrue(trace.isEnded());
  }

  @Test
  void setCurrentSpan_updatesSpan() {
    Trace trace =
        Trace.builder().traceId(TracingUtils.generateTraceId()).name("test-trace").build();

    TraceContext.withTrace(
        trace,
        () -> {
          AgentSpanData data = AgentSpanData.builder().agentName("TestAgent").build();

          Span<AgentSpanData> span =
              Span.<AgentSpanData>builder()
                  .spanId(TracingUtils.generateSpanId())
                  .traceId(trace.getTraceId())
                  .data(data)
                  .build();

          TraceContext.setCurrentSpan(span);

          Optional<Span<?>> currentSpan = TraceContext.getCurrentSpan();
          assertTrue(currentSpan.isPresent());
          assertEquals(span.getSpanId(), currentSpan.get().getSpanId());

          return null;
        });
  }

  @Test
  void getOrCreateTrace_createsTraceWhenNoneExists() {
    Trace trace = TraceContext.getOrCreateTrace("auto-trace", NoopTraceProcessor.INSTANCE);

    assertNotNull(trace);
    assertEquals("auto-trace", trace.getName());

    // Should be set as current
    Optional<Trace> currentTrace = TraceContext.getCurrentTrace();
    assertTrue(currentTrace.isPresent());
    assertEquals(trace.getTraceId(), currentTrace.get().getTraceId());
  }

  @Test
  void getOrCreateTrace_returnsExistingTrace() {
    Trace existingTrace =
        Trace.builder().traceId(TracingUtils.generateTraceId()).name("existing-trace").build();

    TraceContext.withTrace(
        existingTrace,
        () -> {
          Trace trace = TraceContext.getOrCreateTrace("new-trace", NoopTraceProcessor.INSTANCE);

          // Should return existing, not create new
          assertEquals(existingTrace.getTraceId(), trace.getTraceId());
          assertEquals("existing-trace", trace.getName());

          return null;
        });
  }

  @Test
  void captureAndRestoreState_preservesContext() {
    Trace trace =
        Trace.builder().traceId(TracingUtils.generateTraceId()).name("test-trace").build();

    TraceContext.withTrace(
        trace,
        () -> {
          TraceContextState captured = TraceContext.captureState();
          assertNotNull(captured);
          assertEquals(trace.getTraceId(), captured.getTrace().getTraceId());

          // Clear context
          TraceContext.reset();
          assertTrue(TraceContext.getCurrentTrace().isEmpty());

          // Restore
          TraceContext.restoreState(captured);
          Optional<Trace> restoredTrace = TraceContext.getCurrentTrace();
          assertTrue(restoredTrace.isPresent());
          assertEquals(trace.getTraceId(), restoredTrace.get().getTraceId());

          return null;
        });
  }

  @Test
  void captureState_returnsNullWhenNoContext() {
    TraceContextState state = TraceContext.captureState();
    assertNull(state);
  }

  @Test
  void supplyAsync_propagatesContext() throws ExecutionException, InterruptedException {
    Trace trace =
        Trace.builder().traceId(TracingUtils.generateTraceId()).name("test-trace").build();

    AtomicReference<String> capturedTraceId = new AtomicReference<>();

    TraceContext.withTrace(
        trace,
        () -> {
          CompletableFuture<String> future =
              TraceContext.supplyAsync(
                  () -> {
                    // This runs on different thread, but should have context
                    Optional<Trace> currentTrace = TraceContext.getCurrentTrace();
                    currentTrace.ifPresent(t -> capturedTraceId.set(t.getTraceId()));
                    return "async-result";
                  });

          try {
            String result = future.get();
            assertEquals("async-result", result);
            assertEquals(trace.getTraceId(), capturedTraceId.get());
          } catch (Exception e) {
            throw new RuntimeException(e);
          }

          return null;
        });
  }

  @Test
  void runAsync_propagatesContext() throws ExecutionException, InterruptedException {
    Trace trace =
        Trace.builder().traceId(TracingUtils.generateTraceId()).name("test-trace").build();

    AtomicReference<String> capturedTraceId = new AtomicReference<>();

    TraceContext.withTrace(
        trace,
        () -> {
          CompletableFuture<Void> future =
              TraceContext.runAsync(
                  () -> {
                    // This runs on different thread, but should have context
                    Optional<Trace> currentTrace = TraceContext.getCurrentTrace();
                    currentTrace.ifPresent(t -> capturedTraceId.set(t.getTraceId()));
                  });

          try {
            future.get();
            assertEquals(trace.getTraceId(), capturedTraceId.get());
          } catch (Exception e) {
            throw new RuntimeException(e);
          }

          return null;
        });
  }

  @Test
  void contextIsolation_betweenThreads() throws InterruptedException {
    Trace trace1 = Trace.builder().traceId(TracingUtils.generateTraceId()).name("trace-1").build();

    Trace trace2 = Trace.builder().traceId(TracingUtils.generateTraceId()).name("trace-2").build();

    CountDownLatch latch = new CountDownLatch(2);
    AtomicReference<String> thread1TraceId = new AtomicReference<>();
    AtomicReference<String> thread2TraceId = new AtomicReference<>();

    // Thread 1
    new Thread(
            () -> {
              TraceContext.withTrace(
                  trace1,
                  () -> {
                    try {
                      Thread.sleep(50);
                      Optional<Trace> currentTrace = TraceContext.getCurrentTrace();
                      currentTrace.ifPresent(t -> thread1TraceId.set(t.getTraceId()));
                    } catch (InterruptedException e) {
                      Thread.currentThread().interrupt();
                    }
                    latch.countDown();
                    return null;
                  });
            })
        .start();

    // Thread 2
    new Thread(
            () -> {
              TraceContext.withTrace(
                  trace2,
                  () -> {
                    try {
                      Thread.sleep(50);
                      Optional<Trace> currentTrace = TraceContext.getCurrentTrace();
                      currentTrace.ifPresent(t -> thread2TraceId.set(t.getTraceId()));
                    } catch (InterruptedException e) {
                      Thread.currentThread().interrupt();
                    }
                    latch.countDown();
                    return null;
                  });
            })
        .start();

    latch.await();

    // Each thread should see its own trace
    assertEquals(trace1.getTraceId(), thread1TraceId.get());
    assertEquals(trace2.getTraceId(), thread2TraceId.get());
    assertNotEquals(thread1TraceId.get(), thread2TraceId.get());
  }

  @Test
  void reset_clearsContext() {
    Trace trace =
        Trace.builder().traceId(TracingUtils.generateTraceId()).name("test-trace").build();

    TraceContextState state = new TraceContextState(trace);
    TraceContext.restoreState(state);

    assertTrue(TraceContext.getCurrentTrace().isPresent());

    TraceContext.reset();

    assertTrue(TraceContext.getCurrentTrace().isEmpty());
  }
}
