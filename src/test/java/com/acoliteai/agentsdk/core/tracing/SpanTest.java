package com.acoliteai.agentsdk.core.tracing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for Span class */
class SpanTest {

  @Test
  void constructor_createsSpanWithAllFields() {
    CustomSpanData data =
        CustomSpanData.builder().name("test-span").data(Map.of("key", "value")).build();

    Span<CustomSpanData> span =
        Span.<CustomSpanData>builder()
            .spanId("span_123")
            .traceId("trace_456")
            .parentId("span_parent")
            .data(data)
            .tracingApiKey("sk-test")
            .processor(NoopTraceProcessor.INSTANCE)
            .build();

    assertEquals("span_123", span.getSpanId());
    assertEquals("trace_456", span.getTraceId());
    assertEquals("span_parent", span.getParentId());
    assertEquals(data, span.getData());
    assertEquals("sk-test", span.getTracingApiKey());
    assertFalse(span.isStarted());
    assertFalse(span.isEnded());
    assertNull(span.getError());
  }

  @Test
  void start_marksSpanAsStarted() {
    CustomSpanData data = CustomSpanData.builder().name("test-span").data(Map.of()).build();

    Span<CustomSpanData> span =
        Span.<CustomSpanData>builder().spanId("span_123").traceId("trace_456").data(data).build();

    span.start();

    assertTrue(span.isStarted());
    assertNotNull(span.getStartedAt());
    assertFalse(span.isEnded());
  }

  @Test
  void start_isIdempotent() {
    CustomSpanData data = CustomSpanData.builder().name("test-span").data(Map.of()).build();

    Span<CustomSpanData> span =
        Span.<CustomSpanData>builder().spanId("span_123").traceId("trace_456").data(data).build();

    span.start();
    var firstStartTime = span.getStartedAt();

    // Start again - should not change start time
    span.start();

    assertEquals(firstStartTime, span.getStartedAt());
  }

  @Test
  void end_marksSpanAsEnded() {
    CustomSpanData data = CustomSpanData.builder().name("test-span").data(Map.of()).build();

    Span<CustomSpanData> span =
        Span.<CustomSpanData>builder().spanId("span_123").traceId("trace_456").data(data).build();

    span.start();
    span.end();

    assertTrue(span.isStarted());
    assertTrue(span.isEnded());
    assertNotNull(span.getEndedAt());
  }

  @Test
  void end_isIdempotent() {
    CustomSpanData data = CustomSpanData.builder().name("test-span").data(Map.of()).build();

    Span<CustomSpanData> span =
        Span.<CustomSpanData>builder().spanId("span_123").traceId("trace_456").data(data).build();

    span.start();
    span.end();
    var firstEndTime = span.getEndedAt();

    // End again - should not change end time
    span.end();

    assertEquals(firstEndTime, span.getEndedAt());
  }

  @Test
  void setError_storesErrorInformation() {
    CustomSpanData data = CustomSpanData.builder().name("test-span").data(Map.of()).build();

    Span<CustomSpanData> span =
        Span.<CustomSpanData>builder().spanId("span_123").traceId("trace_456").data(data).build();

    SpanError error =
        SpanError.builder().message("Something went wrong").data(Map.of("code", "500")).build();

    span.setError(error);

    assertEquals(error, span.getError());
  }

  @Test
  void setError_fromThrowable() {
    CustomSpanData data = CustomSpanData.builder().name("test-span").data(Map.of()).build();

    Span<CustomSpanData> span =
        Span.<CustomSpanData>builder().spanId("span_123").traceId("trace_456").data(data).build();

    RuntimeException exception = new RuntimeException("Test error");
    span.setError(exception);

    assertNotNull(span.getError());
    assertEquals("Test error", span.getError().getMessage());
    assertNotNull(span.getError().getData());
  }

  @Test
  void clone_copiesAllProperties() {
    CustomSpanData data =
        CustomSpanData.builder().name("test-span").data(Map.of("key", "value")).build();

    Span<CustomSpanData> original =
        Span.<CustomSpanData>builder()
            .spanId("span_123")
            .traceId("trace_456")
            .parentId("span_parent")
            .data(data)
            .tracingApiKey("sk-test")
            .build();

    Span<CustomSpanData> cloned = original.clone();

    assertEquals(original.getSpanId(), cloned.getSpanId());
    assertEquals(original.getTraceId(), cloned.getTraceId());
    assertEquals(original.getParentId(), cloned.getParentId());
    assertEquals(original.getData(), cloned.getData());
    assertEquals(original.getTracingApiKey(), cloned.getTracingApiKey());
  }

  @Test
  void toJson_includesAllFields() {
    CustomSpanData data =
        CustomSpanData.builder().name("test-span").data(Map.of("key", "value")).build();

    Span<CustomSpanData> span =
        Span.<CustomSpanData>builder()
            .spanId("span_123")
            .traceId("trace_456")
            .parentId("span_parent")
            .data(data)
            .build();

    span.start();
    span.end();

    SpanError error = SpanError.builder().message("Test error").build();
    span.setError(error);

    Map<String, Object> json = span.toJson();

    assertEquals("trace.span", json.get("object"));
    assertEquals("span_123", json.get("id"));
    assertEquals("trace_456", json.get("trace_id"));
    assertEquals("span_parent", json.get("parent_id"));
    assertNotNull(json.get("span_data"));
    assertNotNull(json.get("started_at"));
    assertNotNull(json.get("ended_at"));
    assertNotNull(json.get("error"));
  }

  @Test
  void toJson_includesNullFields() {
    CustomSpanData data = CustomSpanData.builder().name("test-span").data(Map.of()).build();

    Span<CustomSpanData> span =
        Span.<CustomSpanData>builder().spanId("span_123").traceId("trace_456").data(data).build();

    Map<String, Object> json = span.toJson();

    assertTrue(json.containsKey("parent_id"));
    assertTrue(json.containsKey("error"));
    assertNull(json.get("parent_id"));
    assertNull(json.get("error"));
  }

  @Test
  void noopSpan_doesNothing() {
    CustomSpanData data = CustomSpanData.builder().name("test-span").data(Map.of()).build();

    NoopSpan<CustomSpanData> span = new NoopSpan<>(data);

    span.start();
    span.end();
    span.setError(SpanError.builder().message("error").build());
    span.setError(new RuntimeException("error"));

    assertEquals("noop", span.getSpanId());
    assertEquals("noop", span.getTraceId());
    assertNull(span.toJson());
  }

  @Test
  void noopSpan_cloneReturnsSelf() {
    CustomSpanData data = CustomSpanData.builder().name("test-span").data(Map.of()).build();

    NoopSpan<CustomSpanData> span = new NoopSpan<>(data);
    NoopSpan<CustomSpanData> cloned = span.clone();

    assertSame(span, cloned);
  }
}
