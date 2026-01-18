package com.acoliteai.agentsdk.core.tracing;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAITraceExporter
 *
 * <p>Exports traces and spans to the OpenAI tracing platform via HTTP. Handles serialization, HTTP
 * requests, retries, and error handling.
 *
 * <p>Key Features: - HTTP POST to OpenAI tracing API - Groups items by API key for multi-tenant
 * support - Exponential backoff with jitter for retries - Respects 5xx for retry, 4xx for
 * log-and-continue - Thread-safe HTTP client
 *
 * <p>API Endpoint: POST https://api.openai.com/v1/traces/ingest Authorization: Bearer YOUR_API_KEY
 * Content-Type: application/json OpenAI-Beta: traces=v1
 *
 * <p>Payload Format: { "data": [ { "type": "trace", "trace_id": "...", ... }, { "type": "span",
 * "span_id": "...", ... } ] }
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tracing/exporter.ts
 */
@Slf4j
public class OpenAITraceExporter implements TraceExporter {
  private static final String DEFAULT_ENDPOINT = "https://api.openai.com/v1/traces/ingest";

  private final HttpClient httpClient;
  private final String endpoint;
  private final String defaultApiKey;
  private final RetryConfig retryConfig;
  private final ObjectMapper objectMapper;

  /** Create exporter with default API key from environment */
  public OpenAITraceExporter() {
    this(System.getenv("OPENAI_API_KEY"));
  }

  /** Create exporter with specific API key */
  public OpenAITraceExporter(String apiKey) {
    this(apiKey, RetryConfig.builder().build());
  }

  /** Create exporter with custom configuration */
  public OpenAITraceExporter(String apiKey, RetryConfig retryConfig) {
    this(apiKey, DEFAULT_ENDPOINT, retryConfig);
  }

  /** Create exporter with full configuration */
  public OpenAITraceExporter(String apiKey, String endpoint, RetryConfig retryConfig) {
    this.defaultApiKey = apiKey;
    this.endpoint = endpoint;
    this.retryConfig = retryConfig;
    this.objectMapper = new ObjectMapper();
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    if (apiKey == null || apiKey.isEmpty()) {
      log.warn("No OpenAI API key provided. Traces will not be exported.");
    }
  }

  @Override
  public CompletableFuture<Void> export(List<Object> items) {
    if (items == null || items.isEmpty()) {
      return CompletableFuture.completedFuture(null);
    }

    if (defaultApiKey == null || defaultApiKey.isEmpty()) {
      log.debug("No API key, skipping export of {} items", items.size());
      return CompletableFuture.completedFuture(null);
    }

    try {
      // Convert items to JSON
      List<Map<String, Object>> jsonItems =
          items.stream().map(this::toJson).collect(Collectors.toList());

      // Group by API key (supports per-trace API keys)
      Map<String, List<Map<String, Object>>> grouped =
          jsonItems.stream()
              .collect(
                  Collectors.groupingBy(
                      json -> {
                        String key = (String) json.get("tracingApiKey");
                        return key != null ? key : defaultApiKey;
                      }));

      // Export each group
      List<CompletableFuture<Void>> futures =
          grouped.entrySet().stream()
              .map(entry -> exportBatch(entry.getKey(), entry.getValue()))
              .collect(Collectors.toList());

      return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

    } catch (Exception e) {
      log.error("Error preparing export batch", e);
      return CompletableFuture.completedFuture(null);
    }
  }

  /** Convert Trace or Span to JSON map */
  private Map<String, Object> toJson(Object item) {
    if (item instanceof Trace) {
      Trace trace = (Trace) item;
      return trace.toJson(true); // Include API key for routing
    } else if (item instanceof Span) {
      Span<?> span = (Span<?>) item;
      return span.toJson();
    } else {
      log.warn("Unknown item type: {}", item.getClass());
      return Map.of("object", "unknown");
    }
  }

  /** Export a batch of items with a specific API key */
  private CompletableFuture<Void> exportBatch(String apiKey, List<Map<String, Object>> items) {
    // Remove API key from items before sending (it's in the header)
    List<Map<String, Object>> cleanedItems =
        items.stream().peek(item -> item.remove("tracingApiKey")).collect(Collectors.toList());

    return retryWithBackoff(() -> sendHttpRequest(apiKey, cleanedItems), retryConfig);
  }

  /** Send HTTP request to OpenAI tracing API */
  private CompletableFuture<Void> sendHttpRequest(String apiKey, List<Map<String, Object>> items) {
    try {
      // Wrap items in {"data": [...]} payload format
      Map<String, Object> payload = Map.of("data", items);
      String jsonBody = objectMapper.writeValueAsString(payload);

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(endpoint))
              .header("Authorization", "Bearer " + apiKey)
              .header("Content-Type", "application/json")
              .header("OpenAI-Beta", "traces=v1")
              .header("User-Agent", "openai-agents-java/1.0")
              .timeout(Duration.ofSeconds(30))
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .build();

      return httpClient
          .sendAsync(request, HttpResponse.BodyHandlers.ofString())
          .thenApply(
              response -> {
                int status = response.statusCode();

                if (status >= 200 && status < 300) {
                  log.debug("Successfully exported {} items to OpenAI", items.size());
                  return null;
                }

                // Retry on 5xx server errors
                if (status >= 500) {
                  log.warn("Server error ({}), will retry: {}", status, response.body());
                  throw new RetriableException("Server error: " + status);
                }

                // Don't retry on 4xx client errors - throw to fail the request
                String errorBody = response.body();
                log.error("Client error ({}): {}", status, errorBody);
                throw new RuntimeException("HTTP " + status + ": " + errorBody);
              });

    } catch (Exception e) {
      log.error("Error sending HTTP request", e);
      return CompletableFuture.completedFuture(null);
    }
  }

  /** Retry with exponential backoff and jitter */
  private CompletableFuture<Void> retryWithBackoff(
      java.util.function.Supplier<CompletableFuture<Void>> action, RetryConfig config) {

    return action
        .get()
        .exceptionally(
            error -> {
              if (!(error.getCause() instanceof RetriableException) || config.attempts <= 0) {
                log.error(
                    "Export failed after {} retries", config.maxAttempts - config.attempts, error);
                // Re-throw to propagate the error
                if (error instanceof RuntimeException) {
                  throw (RuntimeException) error;
                }
                throw new RuntimeException("Export failed", error);
              }

              // Calculate delay with exponential backoff and jitter
              long delay =
                  (long) (config.baseDelayMs * Math.pow(2, config.maxAttempts - config.attempts));
              delay = Math.min(delay, config.maxDelayMs);
              delay += (long) (delay * 0.1 * Math.random()); // 10% jitter

              log.info(
                  "Retrying export after {}ms (attempt {}/{})",
                  delay,
                  config.maxAttempts - config.attempts + 1,
                  config.maxAttempts);

              // Schedule retry
              CompletableFuture<Void> delayed = new CompletableFuture<>();
              CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS)
                  .execute(
                      () -> {
                        retryWithBackoff(action, config.withFewerAttempts())
                            .whenComplete(
                                (result, retryError) -> {
                                  if (retryError != null) {
                                    delayed.completeExceptionally(retryError);
                                  } else {
                                    delayed.complete(null);
                                  }
                                });
                      });

              return delayed.join();
            });
  }

  @Override
  public CompletableFuture<Void> shutdown(long timeoutMs) {
    log.info("Shutting down OpenAITraceExporter");
    // HTTP client doesn't need explicit shutdown in Java 11+
    return CompletableFuture.completedFuture(null);
  }

  /** Retry configuration */
  @Value
  @Builder
  public static class RetryConfig {
    /** Maximum number of retry attempts Default: 3 */
    @Builder.Default int maxAttempts = 3;

    /** Current remaining attempts (internal) */
    @Builder.Default int attempts = 3;

    /** Base delay for exponential backoff (milliseconds) Default: 1000 (1 second) */
    @Builder.Default long baseDelayMs = 1000;

    /** Maximum delay between retries (milliseconds) Default: 30000 (30 seconds) */
    @Builder.Default long maxDelayMs = 30000;

    /** Create new config with one fewer attempt */
    public RetryConfig withFewerAttempts() {
      return RetryConfig.builder()
          .maxAttempts(maxAttempts)
          .attempts(attempts - 1)
          .baseDelayMs(baseDelayMs)
          .maxDelayMs(maxDelayMs)
          .build();
    }
  }

  /** Exception indicating a retriable error */
  public static class RetriableException extends RuntimeException {
    public RetriableException(String message) {
      super(message);
    }
  }
}
