package com.acoliteai.agentsdk.core;

/**
 * SpanData
 *
 * <p>Data for tracing spans.
 *
 * <p>Source: https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing
 */
public class SpanData {
  private String spanType;
  private Object data;

  public SpanData() {}

  public String getSpanType() {
    return spanType;
  }

  public void setSpanType(String spanType) {
    this.spanType = spanType;
  }

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }
}
