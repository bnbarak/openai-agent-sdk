package com.acoliteai.agentsdk.core.tracing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class TraceProviderTest {

  private TraceProvider provider;

  @BeforeEach
  void setUp() {
    provider = new TraceProvider();
  }

  @AfterEach
  void tearDown() {
    if (provider != null) {
      provider.shutdown().join();
    }
  }

  @Test
  void registerProcessor_succeeds() {
    ConsoleTraceProcessor processor = new ConsoleTraceProcessor();

    assertDoesNotThrow(() -> provider.registerProcessor(processor));
  }

  @Test
  void setProcessors_succeeds() {
    ConsoleTraceProcessor processor1 = new ConsoleTraceProcessor();
    ConsoleTraceProcessor processor2 = new ConsoleTraceProcessor();

    assertDoesNotThrow(() -> provider.setProcessors(java.util.List.of(processor1, processor2)));
  }

  @Test
  void createTrace_whenEnabled_returnsRealTrace() {
    provider.setDisabled(false);

    Trace trace =
        provider.createTrace(
            Trace.builder().traceId(TracingUtils.generateTraceId()).name("Test trace"));

    assertNotNull(trace);
    assertFalse(trace instanceof NoopTrace);
    assertEquals("Test trace", trace.getName());
  }

  @Test
  void createTrace_whenDisabled_returnsNoopTrace() {
    provider.setDisabled(true);

    Trace trace =
        provider.createTrace(
            Trace.builder().traceId(TracingUtils.generateTraceId()).name("Test trace"));

    assertNotNull(trace);
    assertSame(NoopTrace.INSTANCE, trace);
  }

  @Test
  void createSpan_withActiveTrace_returnsRealSpan() {
    provider.setDisabled(false);
    Trace trace =
        provider.createTrace(
            Trace.builder().traceId(TracingUtils.generateTraceId()).name("Test trace"));

    TraceContext.withTrace(
        trace,
        () -> {
          Span<AgentSpanData> span =
              provider.createSpan(
                  Span.<AgentSpanData>builder()
                      .spanId(TracingUtils.generateSpanId())
                      .data(AgentSpanData.builder().agentName("TestAgent").build()));

          assertNotNull(span);
          assertFalse(span instanceof NoopSpan);
          assertEquals(trace.getTraceId(), span.getTraceId());
          return null;
        });
  }

  @Test
  void createSpan_withoutActiveTrace_returnsNoopSpan() {
    provider.setDisabled(false);

    Span<AgentSpanData> span =
        provider.createSpan(
            Span.<AgentSpanData>builder()
                .spanId(TracingUtils.generateSpanId())
                .data(AgentSpanData.builder().agentName("TestAgent").build()));

    assertNotNull(span);
    assertInstanceOf(NoopSpan.class, span);
  }

  @Test
  void createSpan_whenDisabled_returnsNoopSpan() {
    provider.setDisabled(true);
    Trace trace =
        provider.createTrace(
            Trace.builder().traceId(TracingUtils.generateTraceId()).name("Test trace"));

    TraceContext.withTrace(
        trace,
        () -> {
          Span<AgentSpanData> span =
              provider.createSpan(
                  Span.<AgentSpanData>builder()
                      .spanId(TracingUtils.generateSpanId())
                      .data(AgentSpanData.builder().agentName("TestAgent").build()));

          assertInstanceOf(NoopSpan.class, span);
          return null;
        });
  }

  @Test
  void getCurrentTrace_returnsContextTrace() {
    provider.setDisabled(false);
    Trace trace =
        provider.createTrace(
            Trace.builder().traceId(TracingUtils.generateTraceId()).name("Test trace"));

    TraceContext.withTrace(
        trace,
        () -> {
          var currentTrace = provider.getCurrentTrace();

          assertTrue(currentTrace.isPresent());
          assertEquals(trace.getTraceId(), currentTrace.get().getTraceId());
          return null;
        });
  }

  @Test
  void getCurrentSpan_whenNoSpan_isEmpty() {
    var currentSpan = provider.getCurrentSpan();

    assertTrue(currentSpan.isEmpty());
  }

  @Test
  void setDisabled_togglesState() {
    assertFalse(provider.isDisabled());

    provider.setDisabled(true);

    assertTrue(provider.isDisabled());

    provider.setDisabled(false);

    assertFalse(provider.isDisabled());
  }

  @Test
  void forceFlush_completesSuccessfully() {
    ConsoleTraceProcessor processor = new ConsoleTraceProcessor();
    provider.registerProcessor(processor);

    var future = provider.forceFlush();

    assertDoesNotThrow(() -> future.get());
  }

  @Test
  void shutdown_completesSuccessfully() {
    ConsoleTraceProcessor processor = new ConsoleTraceProcessor();
    provider.registerProcessor(processor);

    var future = provider.shutdown(1000);

    assertDoesNotThrow(() -> future.get());
  }

  @Test
  void getGlobalTraceProvider_returnsSameInstance() {
    TraceProvider provider1 = TraceProvider.getGlobalTraceProvider();
    TraceProvider provider2 = TraceProvider.getGlobalTraceProvider();

    assertSame(provider1, provider2);
  }

  @Test
  void initializeWithDefaultCloudTracing_succeeds() {
    TraceProvider.resetGlobalProvider();

    assertDoesNotThrow(() -> TraceProvider.initializeWithDefaultCloudTracing());

    TraceProvider provider = TraceProvider.getGlobalTraceProvider();

    assertNotNull(provider);
  }

  @Test
  void initializeWithDefaultCloudTracing_calledTwice_succeeds() {
    TraceProvider.resetGlobalProvider();

    assertDoesNotThrow(
        () -> {
          TraceProvider.initializeWithDefaultCloudTracing();
          TraceProvider.initializeWithDefaultCloudTracing();
        });
  }

  @AfterAll
  static void cleanupGlobal() {
    TraceProvider.resetGlobalProvider();
  }
}
