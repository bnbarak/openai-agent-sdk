package com.acoliteai.agentsdk.realworldapi;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.acoliteai.agentsdk.core.*;
import com.acoliteai.agentsdk.core.tracing.*;
import com.acoliteai.agentsdk.core.types.TextOutput;
import com.acoliteai.agentsdk.core.types.UnknownContext;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

/**
 * Real integration tests for cloud tracing with OpenAI platform.
 *
 * <p>These tests verify that traces and spans can be exported to the actual OpenAI tracing API and
 * receive successful responses.
 *
 * <p>Requirements: 1. OPENAI_API_KEY environment variable must be set 2. Internet connection to
 * api.openai.com
 *
 * <p>Usage: OPENAI_API_KEY=sk-... mvn test -Dtest=TracingRealAPITest
 */
@SetEnvironmentVariable(key = "OPENAI_MODEL", value = "gpt-4o-mini")
class TracingRealAPITest {

  @BeforeAll
  static void checkApiKey() {
    String apiKey = System.getenv("OPENAI_API_KEY");
    assumeTrue(
        apiKey != null && !apiKey.isEmpty(), "OPENAI_API_KEY environment variable must be set");
  }

  @Test
  void exportTraceToOpenAI_receives200() {
    // Create exporter with real API key
    OpenAITraceExporter exporter = new OpenAITraceExporter();

    // Create a simple trace
    Trace trace =
        Trace.builder()
            .traceId(TracingUtils.generateTraceId())
            .name("Test trace from Java SDK")
            .groupId(TracingUtils.generateGroupId())
            .build();

    trace.start();
    try {
      Thread.sleep(10); // Simulate some work
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    trace.end();

    // Export to OpenAI
    try {
      exporter.export(List.of(trace)).join();
      // If we get here without exception, export succeeded (200/20x response)
      assertTrue(true, "Export succeeded");
    } catch (Exception e) {
      fail("Export failed: " + e.getMessage());
    }
  }

  @Test
  void exportSpanToOpenAI_receives200() {
    OpenAITraceExporter exporter = new OpenAITraceExporter();

    // Create a span
    String traceId = TracingUtils.generateTraceId();
    AgentSpanData data = AgentSpanData.builder().agentName("TestAgent").build();

    com.acoliteai.agentsdk.core.tracing.Span<AgentSpanData> span =
        com.acoliteai.agentsdk.core.tracing.Span.<AgentSpanData>builder()
            .spanId(TracingUtils.generateSpanId())
            .traceId(traceId)
            .data(data)
            .build();

    span.start();
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    span.end();

    // Export to OpenAI
    try {
      exporter.export(List.of(span)).join();
      assertTrue(true, "Span export succeeded");
    } catch (Exception e) {
      fail("Span export failed: " + e.getMessage());
    }
  }

  @Test
  void batchProcessorWithOpenAI_exportsSuccessfully() throws InterruptedException {
    // Create real cloud processor
    OpenAITraceExporter exporter = new OpenAITraceExporter();
    BatchTraceProcessor processor =
        new BatchTraceProcessor(
            exporter,
            BatchTraceProcessor.Config.builder()
                .maxBatchSize(10)
                .scheduleDelayMs(1000) // 1 second
                .build());

    // Create trace and spans
    Trace trace =
        Trace.builder()
            .traceId(TracingUtils.generateTraceId())
            .name("Batch export test")
            .processor(processor)
            .build();

    trace.start();

    // Create some spans
    for (int i = 0; i < 3; i++) {
      AgentSpanData data = AgentSpanData.builder().agentName("Agent-" + i).build();

      com.acoliteai.agentsdk.core.tracing.Span<AgentSpanData> span =
          com.acoliteai.agentsdk.core.tracing.Span.<AgentSpanData>builder()
              .spanId(TracingUtils.generateSpanId())
              .traceId(trace.getTraceId())
              .data(data)
              .processor(processor)
              .build();

      span.start();
      Thread.sleep(10);
      span.end();
    }

    trace.end();

    // Flush and verify no errors
    try {
      processor.forceFlush().join();
      processor.shutdown().join();
      assertTrue(true, "Batch export succeeded");
    } catch (Exception e) {
      fail("Batch export failed: " + e.getMessage());
    }
  }

  @Test
  void agentExecutionWithTracing_exportsToCloud() {
    // Create cloud processor
    OpenAITraceExporter exporter = new OpenAITraceExporter();
    BatchTraceProcessor processor =
        new BatchTraceProcessor(
            exporter, BatchTraceProcessor.Config.builder().scheduleDelayMs(5000).build());

    // Create trace
    Trace trace =
        Trace.builder()
            .traceId(TracingUtils.generateTraceId())
            .name("Agent execution trace")
            .processor(processor)
            .build();

    trace.start();

    try {
      // Create simple agent
      Agent<UnknownContext, TextOutput> agent =
          Agent.<UnknownContext, TextOutput>builder()
              .name("TestAgent")
              .instructions("You are a test agent. Respond with 'Test successful'")
              .model("gpt-4o-mini")
              .build();

      // Create agent span
      AgentSpanData agentData = AgentSpanData.builder().agentName("TestAgent").build();

      com.acoliteai.agentsdk.core.tracing.Span<AgentSpanData> agentSpan =
          com.acoliteai.agentsdk.core.tracing.Span.<AgentSpanData>builder()
              .spanId(TracingUtils.generateSpanId())
              .traceId(trace.getTraceId())
              .data(agentData)
              .processor(processor)
              .build();

      agentSpan.start();

      // Run agent
      RunResult<UnknownContext, ?> result = Runner.run(agent, "Test");
      assertNotNull(result);
      assertNotNull(result.getFinalOutput());

      agentSpan.end();

    } finally {
      trace.end();
    }

    // Flush and verify
    try {
      processor.forceFlush().join();
      processor.shutdown().join();
      assertTrue(true, "Agent execution trace exported successfully");
    } catch (Exception e) {
      fail("Trace export failed: " + e.getMessage());
    }
  }
}
