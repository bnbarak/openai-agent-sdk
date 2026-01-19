package ai.acolite.demo;

import ai.acolite.agentsdk.core.FunctionTool;
import ai.acolite.agentsdk.core.RunContext;
import ai.acolite.agentsdk.core.types.UnknownContext;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class AddressValidationTool implements FunctionTool<UnknownContext, AddressValidationTool.Input, AddressValidationTool.Output> {

    private static final String API_URL = "https://us-street.api.smarty.com/street-address";
    private final String authId;
    private final String authToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AddressValidationTool() {
        this.authId = System.getenv("SMARTY_AUTH_ID");
        this.authToken = System.getenv("SMARTY_AUTH_TOKEN");

        if (authId == null || authToken == null) {
            System.err.println("Warning: SMARTY_AUTH_ID and SMARTY_AUTH_TOKEN environment variables not set");
            System.err.println("Address validation tool will not work without credentials");
        }

        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getType() {
        return "function";
    }

    @Override
    public String getName() {
        return "validate_address";
    }

    @Override
    public String getDescription() {
        return "Validates and standardizes US street addresses using the Smarty API. Returns the validated address with additional metadata like coordinates and delivery information.";
    }

    @Override
    public Object getParameters() {
        return Input.class;
    }

    @Override
    public boolean isStrict() {
        return true;
    }

    @Override
    public CompletableFuture<Output> invoke(RunContext<UnknownContext> context, Input input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (authId == null || authToken == null) {
                    return new Output(false, "API credentials not configured", null, null, null, null);
                }

                String url = buildUrl(input);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode results = objectMapper.readTree(response.body());

                    if (results.isArray() && results.size() > 0) {
                        JsonNode result = results.get(0);
                        JsonNode components = result.path("components");
                        JsonNode metadata = result.path("metadata");

                        String deliveryLine1 = result.path("delivery_line_1").asText();
                        String lastLine = result.path("last_line").asText();
                        String fullAddress = deliveryLine1 + ", " + lastLine;

                        Double latitude = metadata.path("latitude").asDouble();
                        Double longitude = metadata.path("longitude").asDouble();

                        return new Output(true, "Address validated successfully", fullAddress,
                                components.path("city_name").asText(),
                                components.path("state_abbreviation").asText(),
                                components.path("zipcode").asText() + "-" + components.path("plus4_code").asText());
                    } else {
                        return new Output(false, "Address not found or invalid", null, null, null, null);
                    }
                } else {
                    return new Output(false, "API error: " + response.statusCode(), null, null, null, null);
                }
            } catch (Exception e) {
                return new Output(false, "Error: " + e.getMessage(), null, null, null, null);
            }
        });
    }

    private String buildUrl(Input input) {
        StringBuilder url = new StringBuilder(API_URL);
        url.append("?auth-id=").append(encode(authId));
        url.append("&auth-token=").append(encode(authToken));
        url.append("&street=").append(encode(input.street));

        if (input.city != null && !input.city.isEmpty()) {
            url.append("&city=").append(encode(input.city));
        }
        if (input.state != null && !input.state.isEmpty()) {
            url.append("&state=").append(encode(input.state));
        }
        if (input.zipCode != null && !input.zipCode.isEmpty()) {
            url.append("&zipcode=").append(encode(input.zipCode));
        }

        return url.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public boolean needsApproval(RunContext<UnknownContext> context, Input input) {
        return false;
    }

    @Override
    public boolean isEnabled(RunContext<UnknownContext> context) {
        return authId != null && authToken != null;
    }

    @JsonTypeName("validate_address")
    @JsonClassDescription("Input parameters for US address validation")
    public static class Input {
        @JsonProperty(required = true)
        @JsonPropertyDescription("The street address (e.g., '1600 Amphitheatre Parkway')")
        public String street;

        @JsonProperty
        @JsonPropertyDescription("The city name (optional if zipcode is provided)")
        public String city;

        @JsonProperty
        @JsonPropertyDescription("The state abbreviation (e.g., 'CA')")
        public String state;

        @JsonProperty
        @JsonPropertyDescription("The ZIP code (optional if city and state are provided)")
        public String zipCode;
    }

    public record Output(
            @JsonProperty boolean valid,
            @JsonProperty String message,
            @JsonProperty String validatedAddress,
            @JsonProperty String city,
            @JsonProperty String state,
            @JsonProperty String zipCode
    ) {}
}
