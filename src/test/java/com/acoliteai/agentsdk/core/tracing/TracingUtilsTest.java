package com.acoliteai.agentsdk.core.tracing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for TracingUtils */
class TracingUtilsTest {

  @Test
  void generateTraceId_hasCorrectFormat() {
    String traceId = TracingUtils.generateTraceId();

    assertTrue(traceId.startsWith("trace_"));
    // trace_ (6) + 32 hex chars = 38 total
    assertEquals(38, traceId.length());
    // Should be lowercase hex
    assertTrue(traceId.substring(6).matches("[0-9a-f]{32}"));
  }

  @Test
  void generateTraceId_generatesUniqueIds() {
    String id1 = TracingUtils.generateTraceId();
    String id2 = TracingUtils.generateTraceId();

    assertNotEquals(id1, id2);
  }

  @Test
  void generateSpanId_hasCorrectFormat() {
    String spanId = TracingUtils.generateSpanId();

    assertTrue(spanId.startsWith("span_"));
    // span_ (5) + 24 hex chars = 29 total
    assertEquals(29, spanId.length());
    // Should be lowercase hex
    assertTrue(spanId.substring(5).matches("[0-9a-f]{24}"));
  }

  @Test
  void generateSpanId_generatesUniqueIds() {
    String id1 = TracingUtils.generateSpanId();
    String id2 = TracingUtils.generateSpanId();

    assertNotEquals(id1, id2);
  }

  @Test
  void generateGroupId_hasCorrectFormat() {
    String groupId = TracingUtils.generateGroupId();

    assertTrue(groupId.startsWith("group_"));
    // group_ (6) + 24 hex chars = 30 total
    assertEquals(30, groupId.length());
    // Should be lowercase hex
    assertTrue(groupId.substring(6).matches("[0-9a-f]{24}"));
  }

  @Test
  void generateGroupId_generatesUniqueIds() {
    String id1 = TracingUtils.generateGroupId();
    String id2 = TracingUtils.generateGroupId();

    assertNotEquals(id1, id2);
  }
}
