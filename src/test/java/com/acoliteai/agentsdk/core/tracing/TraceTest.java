package com.acoliteai.agentsdk.core.tracing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for Trace class */
class TraceTest {

  @Test
  void constructor_createsTraceWithAllFields() {
    Map<String, Object> metadata = Map.of("key", "value");
    Trace trace =
        Trace.builder()
            .traceId("trace_123")
            .name("Test workflow")
            .groupId("group_456")
            .metadata(metadata)
            .tracingApiKey("sk-test")
            .processor(NoopTraceProcessor.INSTANCE)
            .build();

    assertEquals("trace_123", trace.getTraceId());
    assertEquals("Test workflow", trace.getName());
    assertEquals("group_456", trace.getGroupId());
    assertEquals(metadata, trace.getMetadata());
    assertEquals("sk-test", trace.getTracingApiKey());
    assertFalse(trace.isStarted());
    assertFalse(trace.isEnded());
  }

  @Test
  void constructor_usesDefaultName() {
    Trace trace = Trace.builder().traceId("trace_123").build();

    assertEquals("Agent workflow", trace.getName());
  }

  @Test
  void start_marksTraceAsStarted() {
    Trace trace = Trace.builder().traceId("trace_123").build();

    trace.start();

    assertTrue(trace.isStarted());
    assertNotNull(trace.getStartedAt());
    assertFalse(trace.isEnded());
  }

  @Test
  void start_isIdempotent() {
    Trace trace = Trace.builder().traceId("trace_123").build();

    trace.start();
    var firstStartTime = trace.getStartedAt();

    // Start again - should not change start time
    trace.start();

    assertEquals(firstStartTime, trace.getStartedAt());
  }

  @Test
  void end_marksTraceAsEnded() {
    Trace trace = Trace.builder().traceId("trace_123").build();

    trace.start();
    trace.end();

    assertTrue(trace.isStarted());
    assertTrue(trace.isEnded());
    assertNotNull(trace.getEndedAt());
  }

  @Test
  void end_isIdempotent() {
    Trace trace = Trace.builder().traceId("trace_123").build();

    trace.start();
    trace.end();
    var firstEndTime = trace.getEndedAt();

    // End again - should not change end time
    trace.end();

    assertEquals(firstEndTime, trace.getEndedAt());
  }

  @Test
  void clone_copiesAllProperties() {
    Map<String, Object> metadata = Map.of("key", "value");
    Trace original =
        Trace.builder()
            .traceId("trace_123")
            .name("Test workflow")
            .groupId("group_456")
            .metadata(metadata)
            .tracingApiKey("sk-test")
            .build();

    Trace cloned = original.clone();

    assertEquals(original.getTraceId(), cloned.getTraceId());
    assertEquals(original.getName(), cloned.getName());
    assertEquals(original.getGroupId(), cloned.getGroupId());
    assertEquals(original.getTracingApiKey(), cloned.getTracingApiKey());
    // Metadata should be a new map (deep copy)
    assertNotSame(original.getMetadata(), cloned.getMetadata());
    assertEquals(original.getMetadata(), cloned.getMetadata());
  }

  @Test
  void toJson_includesAllFields() {
    Map<String, Object> metadata = Map.of("key", "value");
    Trace trace =
        Trace.builder()
            .traceId("trace_123")
            .name("Test workflow")
            .groupId("group_456")
            .metadata(metadata)
            .tracingApiKey("sk-test")
            .build();

    trace.start();
    trace.end();

    Map<String, Object> json = trace.toJson(true);

    assertEquals("trace", json.get("object"));
    assertEquals("trace_123", json.get("id"));
    assertEquals("Test workflow", json.get("workflow_name"));
    assertEquals("group_456", json.get("group_id"));
    assertEquals(metadata, json.get("metadata"));
    assertEquals("sk-test", json.get("tracingApiKey"));
  }

  @Test
  void toJson_excludesApiKeyByDefault() {
    Trace trace = Trace.builder().traceId("trace_123").tracingApiKey("sk-test").build();

    Map<String, Object> json = trace.toJson();

    assertNull(json.get("tracingApiKey"));
  }

  @Test
  void toJson_includesNullFields() {
    Trace trace = Trace.builder().traceId("trace_123").name("Test workflow").build();

    Map<String, Object> json = trace.toJson();

    assertTrue(json.containsKey("group_id"));
    assertNull(json.get("group_id"));
    assertFalse(json.containsKey("tracingApiKey"));
  }

  @Test
  void noopTrace_doesNothing() {
    NoopTrace trace = NoopTrace.INSTANCE;

    trace.start();
    trace.end();

    assertEquals("noop", trace.getTraceId());
    assertEquals("noop", trace.getName());
    assertNull(trace.toJson());
  }

  @Test
  void noopTrace_isSingleton() {
    NoopTrace trace1 = NoopTrace.INSTANCE;
    NoopTrace trace2 = NoopTrace.INSTANCE;

    assertSame(trace1, trace2);
  }

  @Test
  void noopTrace_cloneReturnsSelf() {
    NoopTrace trace = NoopTrace.INSTANCE;
    NoopTrace cloned = trace.clone();

    assertSame(trace, cloned);
  }
}
