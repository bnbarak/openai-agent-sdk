package com.acoliteai.agentsdk.openai;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * SerializationUtils
 *
 * <p>Static utility methods for JSON serialization/deserialization. Shared across all SDK
 * components for consistent JSON handling.
 *
 * <p>Uses a single configured ObjectMapper with: - FAIL_ON_UNKNOWN_PROPERTIES=false for API
 * resilience
 */
public class SerializationUtils {

  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private SerializationUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Serialize an object to JSON string.
   *
   * @param obj Object to serialize (can be null)
   * @return JSON string representation
   */
  public static String serializeToJson(Object obj) {
    if (obj == null) {
      return "null";
    }

    if (obj instanceof String) {
      return (String) obj;
    }

    try {
      return OBJECT_MAPPER.writeValueAsString(obj);
    } catch (Exception e) {
      return obj.toString();
    }
  }

  /**
   * Deserialize JSON string to an object of specified type.
   *
   * @param json JSON string to parse
   * @param targetClass Target class to deserialize into
   * @param <T> Type of target class
   * @return Deserialized object
   * @throws RuntimeException if deserialization fails
   */
  public static <T> T deserializeFromJson(String json, Class<T> targetClass) {
    if (json == null) {
      return null;
    }

    try {
      return OBJECT_MAPPER.readValue(json, targetClass);
    } catch (Exception e) {
      throw new RuntimeException(
          String.format(
              "Failed to deserialize JSON to %s: %s", targetClass.getSimpleName(), e.getMessage()),
          e);
    }
  }

  /**
   * Deserialize JSON string to untyped Object (Map/List/primitive).
   *
   * @param json JSON string to parse
   * @return Deserialized object (Map, List, String, Number, Boolean, or null)
   */
  public static Object deserializeFromJson(String json) {
    if (json == null || json.isEmpty()) {
      return null;
    }

    try {
      return OBJECT_MAPPER.readValue(json, Object.class);
    } catch (Exception e) {
      return json;
    }
  }

  /**
   * Round-trip serialize-deserialize for type conversion.
   *
   * <p>Useful for converting Map to typed object, or converting between compatible types. Uses JSON
   * as intermediate format.
   *
   * @param obj Source object
   * @param targetClass Target class to convert to
   * @param <T> Type of target class
   * @return Converted object
   * @throws RuntimeException if conversion fails
   */
  public static <T> T convertViaJson(Object obj, Class<T> targetClass) {
    if (obj == null) {
      return null;
    }

    if (targetClass.isInstance(obj)) {
      return targetClass.cast(obj);
    }

    try {
      String json = OBJECT_MAPPER.writeValueAsString(obj);
      return OBJECT_MAPPER.readValue(json, targetClass);
    } catch (Exception e) {
      throw new RuntimeException(
          String.format(
              "Failed to convert %s to %s: %s",
              obj.getClass().getSimpleName(), targetClass.getSimpleName(), e.getMessage()),
          e);
    }
  }
}
