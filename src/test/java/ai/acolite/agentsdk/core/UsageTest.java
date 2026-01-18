package ai.acolite.agentsdk.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for Usage class */
class UsageTest {

  @Test
  void empty_returnsZeroValues() {
    Usage usage = Usage.empty();

    assertNotNull(usage);
    assertEquals(0.0, usage.getRequests());
    assertEquals(0.0, usage.getInputTokens());
    assertEquals(0.0, usage.getOutputTokens());
    assertEquals(0.0, usage.getTotalTokens());
  }

  @Test
  void builder_createsUsageWithValues() {
    Usage usage =
        Usage.builder()
            .requests(1.0)
            .inputTokens(100.0)
            .outputTokens(50.0)
            .totalTokens(150.0)
            .build();

    assertEquals(1.0, usage.getRequests());
    assertEquals(100.0, usage.getInputTokens());
    assertEquals(50.0, usage.getOutputTokens());
    assertEquals(150.0, usage.getTotalTokens());
  }

  @Test
  void add_sumsTwoUsageInstances() {
    Usage usage1 =
        Usage.builder()
            .requests(1.0)
            .inputTokens(100.0)
            .outputTokens(50.0)
            .totalTokens(150.0)
            .build();
    Usage usage2 =
        Usage.builder()
            .requests(2.0)
            .inputTokens(200.0)
            .outputTokens(75.0)
            .totalTokens(275.0)
            .build();

    Usage result = usage1.add(usage2);

    assertEquals(3.0, result.getRequests());
    assertEquals(300.0, result.getInputTokens());
    assertEquals(125.0, result.getOutputTokens());
    assertEquals(425.0, result.getTotalTokens());
  }

  @Test
  void add_withNullValues_handlesGracefully() {
    Usage usage1 = Usage.builder().inputTokens(100.0).outputTokens(50.0).build();
    Usage usage2 = Usage.builder().requests(1.0).totalTokens(150.0).build();

    Usage result = usage1.add(usage2);

    assertEquals(1.0, result.getRequests());
    assertEquals(100.0, result.getInputTokens());
    assertEquals(50.0, result.getOutputTokens());
    assertEquals(150.0, result.getTotalTokens());
  }

  @Test
  void add_withNullOther_returnsOriginal() {
    Usage usage1 =
        Usage.builder()
            .requests(1.0)
            .inputTokens(100.0)
            .outputTokens(50.0)
            .totalTokens(150.0)
            .build();

    Usage result = usage1.add(null);

    assertEquals(usage1.getRequests(), result.getRequests());
    assertEquals(usage1.getInputTokens(), result.getInputTokens());
    assertEquals(usage1.getOutputTokens(), result.getOutputTokens());
    assertEquals(usage1.getTotalTokens(), result.getTotalTokens());
  }

  @Test
  void add_emptyPlusEmpty_returnsEmpty() {
    Usage empty1 = Usage.empty();
    Usage empty2 = Usage.empty();

    Usage result = empty1.add(empty2);

    assertEquals(0.0, result.getRequests());
    assertEquals(0.0, result.getInputTokens());
    assertEquals(0.0, result.getOutputTokens());
    assertEquals(0.0, result.getTotalTokens());
  }

  @Test
  void add_accumulation_worksAcrossMultipleCalls() {
    Usage total = Usage.empty();
    total =
        total.add(Usage.builder().inputTokens(10.0).outputTokens(5.0).totalTokens(15.0).build());
    total =
        total.add(Usage.builder().inputTokens(20.0).outputTokens(10.0).totalTokens(30.0).build());
    total =
        total.add(Usage.builder().inputTokens(15.0).outputTokens(8.0).totalTokens(23.0).build());

    assertEquals(45.0, total.getInputTokens());
    assertEquals(23.0, total.getOutputTokens());
    assertEquals(68.0, total.getTotalTokens());
  }

  @Test
  void value_providesEqualsAndHashCode() {
    Usage usage1 =
        Usage.builder()
            .requests(1.0)
            .inputTokens(100.0)
            .outputTokens(50.0)
            .totalTokens(150.0)
            .build();
    Usage usage2 =
        Usage.builder()
            .requests(1.0)
            .inputTokens(100.0)
            .outputTokens(50.0)
            .totalTokens(150.0)
            .build();

    assertEquals(usage1, usage2);
    assertEquals(usage1.hashCode(), usage2.hashCode());
  }

  @Test
  void value_providesToString() {
    Usage usage =
        Usage.builder()
            .requests(1.0)
            .inputTokens(100.0)
            .outputTokens(50.0)
            .totalTokens(150.0)
            .build();

    String toString = usage.toString();

    assertNotNull(toString);
    assertTrue(toString.contains("100.0"));
    assertTrue(toString.contains("50.0"));
    assertTrue(toString.contains("150.0"));
  }
}
