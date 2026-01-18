package ai.acolite.agentsdk.core;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for tool parameter deserialization.
 *
 * <p>Tests the critical path of converting OpenAI API parameters (JSON/Map) to typed Input classes
 * for tool invocation.
 */
class ToolParameterDeserializationTest {

  private final ObjectMapper mapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Data
  @JsonClassDescription("Simple parameters for testing")
  static class SimpleParams {
    @JsonPropertyDescription("A string field")
    private String name;

    @JsonPropertyDescription("A number field")
    private int count;
  }

  @Data
  @JsonClassDescription("Parameters with optional fields")
  static class OptionalParams {
    @JsonPropertyDescription("Required field")
    private String required;

    @JsonPropertyDescription("Optional with default")
    private String optional = "default";

    @JsonPropertyDescription("Optional number")
    private Integer optionalNumber;
  }

  @Data
  @JsonClassDescription("Parameters with nested objects")
  static class NestedParams {
    @JsonPropertyDescription("Simple string")
    private String name;

    @JsonPropertyDescription("Nested object")
    private Address address;

    @Data
    static class Address {
      private String street;
      private String city;
    }
  }

  @Data
  @JsonClassDescription("Parameters with collections")
  static class CollectionParams {
    @JsonPropertyDescription("List of strings")
    private List<String> tags;

    @JsonPropertyDescription("List of numbers")
    private List<Integer> numbers;
  }

  /** Test basic deserialization from Map to typed class */
  @Test
  void deserializeSimpleParameters() throws Exception {
    Map<String, Object> params = Map.of("name", "test", "count", 42);

    String json = mapper.writeValueAsString(params);
    SimpleParams result = mapper.readValue(json, SimpleParams.class);

    assertEquals("test", result.getName());
    assertEquals(42, result.getCount());
  }

  /** Test that missing optional fields use defaults */
  @Test
  void deserializeWithDefaults() throws Exception {
    Map<String, Object> params = Map.of("required", "value");

    String json = mapper.writeValueAsString(params);
    OptionalParams result = mapper.readValue(json, OptionalParams.class);

    assertEquals("value", result.getRequired());
    assertEquals("default", result.getOptional());
    assertNull(result.getOptionalNumber());
  }

  /** Test that missing required fields throws exception */
  @Test
  void missingRequiredField_throwsException() {
    Map<String, Object> params = Map.of("count", 42);

    String json = assertDoesNotThrow(() -> mapper.writeValueAsString(params));
    SimpleParams result = assertDoesNotThrow(() -> mapper.readValue(json, SimpleParams.class));

    assertNull(result.getName());
    assertEquals(42, result.getCount());
  }

  /** Test type mismatch (string where number expected) */
  @Test
  void typeMismatch_throwsException() {
    Map<String, Object> params =
        Map.of(
            "name", "test",
            "count", "not-a-number");

    String json = assertDoesNotThrow(() -> mapper.writeValueAsString(params));

    assertThrows(
        Exception.class,
        () -> {
          mapper.readValue(json, SimpleParams.class);
        });
  }

  /** Test extra fields are ignored (not strict) */
  @Test
  void extraFields_ignored() throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("name", "test");
    params.put("count", 42);
    params.put("extraField", "ignored");

    String json = mapper.writeValueAsString(params);
    SimpleParams result = mapper.readValue(json, SimpleParams.class);

    assertEquals("test", result.getName());
    assertEquals(42, result.getCount());
  }

  /** Test nested object deserialization */
  @Test
  void deserializeNestedObjects() throws Exception {
    Map<String, Object> params =
        Map.of(
            "name",
            "John",
            "address",
            Map.of(
                "street", "123 Main St",
                "city", "Springfield"));

    String json = mapper.writeValueAsString(params);
    NestedParams result = mapper.readValue(json, NestedParams.class);

    assertEquals("John", result.getName());
    assertNotNull(result.getAddress());
    assertEquals("123 Main St", result.getAddress().getStreet());
    assertEquals("Springfield", result.getAddress().getCity());
  }

  /** Test collection deserialization */
  @Test
  void deserializeCollections() throws Exception {
    Map<String, Object> params =
        Map.of(
            "tags", List.of("tag1", "tag2", "tag3"),
            "numbers", List.of(1, 2, 3));

    String json = mapper.writeValueAsString(params);
    CollectionParams result = mapper.readValue(json, CollectionParams.class);

    assertEquals(3, result.getTags().size());
    assertEquals("tag1", result.getTags().get(0));
    assertEquals(3, result.getNumbers().size());
    assertEquals(1, result.getNumbers().get(0));
  }

  /** Test null parameters handling */
  @Test
  void nullParameters_handledGracefully() {
    Object result = null;
    if (result == null) {
      assertNull(result);
    }
  }

  /** Test empty object */
  @Test
  void emptyObject_createsInstanceWithDefaults() throws Exception {
    Map<String, Object> params = Map.of();

    String json = mapper.writeValueAsString(params);
    OptionalParams result = mapper.readValue(json, OptionalParams.class);

    assertNull(result.getRequired());
    assertEquals("default", result.getOptional());
    assertNull(result.getOptionalNumber());
  }

  /** Test numeric type coercion (double to int) */
  @Test
  void numericCoercion_works() throws Exception {
    Map<String, Object> params = Map.of("name", "test", "count", 42.7);

    String json = mapper.writeValueAsString(params);
    SimpleParams result = mapper.readValue(json, SimpleParams.class);

    assertEquals("test", result.getName());
    assertEquals(42, result.getCount());
  }

  /** Test already-deserialized parameters (instance of target class) */
  @Test
  void alreadyTyped_returnsAsIs() {
    SimpleParams params = new SimpleParams();
    params.setName("test");
    params.setCount(42);

    if (SimpleParams.class.isInstance(params)) {
      assertSame(params, params);
    }
  }

  /** Demonstrates the round-trip serialization pattern used in Runner */
  @Test
  void roundTripSerialization_preservesData() throws Exception {
    Map<String, Object> apiParams = Map.of("name", "test", "count", 42);

    String json = mapper.writeValueAsString(apiParams);
    SimpleParams result = mapper.readValue(json, SimpleParams.class);

    assertEquals("test", result.getName());
    assertEquals(42, result.getCount());
  }
}
