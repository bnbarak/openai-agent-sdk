package com.acoliteai.agentsdk.openai;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for SerializationUtils.
 *
 * <p>Tests JSON serialization utilities used across OpenAI API integration.
 */
class SerializationUtilsTest {

  @Test
  void serializeToJson_null_returnsNullString() {
    String result = SerializationUtils.serializeToJson(null);

    assertEquals("null", result);
  }

  @Test
  void serializeToJson_string_returnsAsIs() {
    String result = SerializationUtils.serializeToJson("already a string");

    assertEquals("already a string", result);
  }

  @Test
  void serializeToJson_number_returnsJsonNumber() {
    String result = SerializationUtils.serializeToJson(42);

    assertEquals("42", result);
  }

  @Test
  void serializeToJson_boolean_returnsJsonBoolean() {
    String trueResult = SerializationUtils.serializeToJson(true);
    String falseResult = SerializationUtils.serializeToJson(false);

    assertEquals("true", trueResult);
    assertEquals("false", falseResult);
  }

  @Test
  void serializeToJson_map_returnsJsonObject() {
    Map<String, Object> map = Map.of("name", "test", "count", 42, "active", true);

    String result = SerializationUtils.serializeToJson(map);

    assertTrue(result.contains("\"name\""));
    assertTrue(result.contains("\"test\""));
    assertTrue(result.contains("\"count\""));
    assertTrue(result.contains("42"));
  }

  @Test
  void serializeToJson_list_returnsJsonArray() {
    List<String> list = List.of("apple", "banana", "cherry");

    String result = SerializationUtils.serializeToJson(list);

    assertTrue(result.startsWith("["));
    assertTrue(result.endsWith("]"));
    assertTrue(result.contains("apple"));
    assertTrue(result.contains("banana"));
    assertTrue(result.contains("cherry"));
  }

  @Test
  void serializeToJson_nestedStructure_handlesCorrectly() {
    Map<String, Object> nested = Map.of("user", "john", "data", Map.of("score", 100, "level", 5));

    String result = SerializationUtils.serializeToJson(nested);

    assertTrue(result.contains("user"));
    assertTrue(result.contains("data"));
    assertTrue(result.contains("score"));
    assertTrue(result.contains("100"));
  }

  @Test
  void serializeToJson_emptyMap_returnsEmptyJsonObject() {
    String result = SerializationUtils.serializeToJson(Map.of());

    assertEquals("{}", result);
  }

  @Test
  void serializeToJson_emptyList_returnsEmptyJsonArray() {
    String result = SerializationUtils.serializeToJson(List.of());

    assertEquals("[]", result);
  }
}
