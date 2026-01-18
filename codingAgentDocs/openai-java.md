# OpenAI Java SDK

The OpenAI Java SDK provides convenient access to the OpenAI REST API from Java applications. It offers a type-safe, immutable, and builder-pattern-based interface for interacting with OpenAI's language models, embeddings, image generation, video generation, audio transcription, content moderation, and other AI services. The SDK supports both synchronous and asynchronous execution, streaming responses, structured outputs with JSON schemas, function calling, file uploads, webhook verification, and comprehensive error handling.

The library is designed for Java 8+ and uses OkHttp as its default HTTP client, though it supports custom HTTP implementations. It includes special features like automatic pagination, retry logic with exponential backoff, connection pooling, and Azure OpenAI integration. The SDK follows semantic versioning and provides extensive configuration options through builders, environment variables, and Spring Boot integration for enterprise applications.

## Chat Completions API

Generate conversational AI responses using OpenAI's chat models with customizable parameters.

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

// Initialize client using OPENAI_API_KEY environment variable
OpenAIClient client = OpenAIOkHttpClient.fromEnv();

// Build request parameters with user and developer messages
ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
    .model(ChatModel.GPT_5_2)
    .maxCompletionTokens(2048)
    .addDeveloperMessage("Make sure you mention Stainless!")
    .addUserMessage("Tell me a story about building the best SDK!")
    .build();

// Execute synchronous request and process response
ChatCompletion completion = client.chat().completions().create(params);
completion.choices().stream()
    .flatMap(choice -> choice.message().content().stream())
    .forEach(System.out::println);
```

## Responses API

Use the newer Responses API for generating text with a simplified interface.

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

OpenAIClient client = OpenAIOkHttpClient.fromEnv();

// Create response request with input text and model
ResponseCreateParams params = ResponseCreateParams.builder()
    .input("Tell me a story about building the best SDK!")
    .model(ChatModel.GPT_4O)
    .build();

// Process response output items
Response response = client.responses().create(params);
response.output().stream()
    .flatMap(item -> item.message().stream())
    .flatMap(message -> message.content().stream())
    .flatMap(content -> content.outputText().stream())
    .forEach(outputText -> System.out.println(outputText.text()));
```

## Streaming Chat Completions

Stream responses in real-time as chunks arrive, enabling progressive display of generated content.

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.StreamResponse;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionChunk;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

OpenAIClient client = OpenAIOkHttpClient.fromEnv();

ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
    .model(ChatModel.GPT_5_2)
    .maxCompletionTokens(2048)
    .addUserMessage("Tell me a story about building the best SDK!")
    .build();

// Stream response chunks using try-with-resources
try (StreamResponse<ChatCompletionChunk> streamResponse =
        client.chat().completions().createStreaming(params)) {
    streamResponse.stream()
        .flatMap(completion -> completion.choices().stream())
        .flatMap(choice -> choice.delta().content().stream())
        .forEach(System.out::print);
}
```

## Asynchronous Execution

Execute API calls asynchronously using CompletableFuture for non-blocking operations.

```java
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import java.util.concurrent.CompletableFuture;

// Create async client from environment variables
OpenAIClientAsync client = OpenAIOkHttpClientAsync.fromEnv();

ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
    .addUserMessage("Say this is a test")
    .model(ChatModel.GPT_5_2)
    .build();

// Returns CompletableFuture for async processing
CompletableFuture<ChatCompletion> future = client.chat().completions().create(params);
future.thenAccept(completion -> {
    completion.choices().stream()
        .flatMap(choice -> choice.message().content().stream())
        .forEach(System.out::println);
});
```

## Function Calling

Define functions using Java classes and let the AI model decide when to call them with appropriate parameters.

```java
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.*;
import java.util.Collection;

@JsonClassDescription("Gets the quality of the given SDK.")
class GetSdkQuality {
    @JsonPropertyDescription("The name of the SDK.")
    public String name;

    public SdkQuality execute() {
        return new SdkQuality(name, name.contains("OpenAI") ? "It's robust!" : "*shrug*");
    }
}

class SdkQuality {
    public String quality;
    public SdkQuality(String name, String evaluation) {
        quality = name + ": " + evaluation;
    }
}

OpenAIClient client = OpenAIOkHttpClient.fromEnv();

// Register functions as tools using class definitions
ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder()
    .model(ChatModel.GPT_5_2)
    .maxCompletionTokens(2048)
    .addTool(GetSdkQuality.class)
    .addUserMessage("How good is the OpenAI Java SDK?");

client.chat().completions().create(builder.build()).choices().stream()
    .map(ChatCompletion.Choice::message)
    .peek(builder::addMessage)
    .flatMap(message -> message.toolCalls().stream().flatMap(Collection::stream))
    .forEach(toolCall -> {
        // Parse function arguments and execute
        ChatCompletionMessageFunctionToolCall.Function function = toolCall.asFunction().function();
        if (function.name().equals("GetSdkQuality")) {
            Object result = function.arguments(GetSdkQuality.class).execute();
            // Return result to AI model
            builder.addMessage(ChatCompletionToolMessageParam.builder()
                .toolCallId(toolCall.asFunction().id())
                .contentAsJson(result)
                .build());
        }
    });
```

## Structured Outputs with JSON Schema

Ensure AI responses conform to a specific structure by defining Java classes as JSON schemas.

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import java.util.List;

class Person {
    public String name;
    public int birthYear;
}

class Book {
    public String title;
    public Person author;
    public int publicationYear;
}

class BookList {
    public List<Book> books;
}

OpenAIClient client = OpenAIOkHttpClient.fromEnv();

// Specify response format using Java class
StructuredChatCompletionCreateParams<BookList> params = ChatCompletionCreateParams.builder()
    .addUserMessage("List some famous late twentieth century novels.")
    .model(ChatModel.GPT_4_1)
    .responseFormat(BookList.class)
    .build();

// Response is automatically deserialized to BookList
client.chat().completions().create(params).choices().stream()
    .flatMap(choice -> choice.message().content().stream())
    .flatMap(bookList -> bookList.books.stream())
    .forEach(book -> System.out.println(book.title + " by " + book.author.name));
```

## Embeddings

Generate vector embeddings for text input to enable semantic search and similarity comparisons.

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.embeddings.Embedding;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.openai.models.embeddings.EmbeddingModel;
import com.openai.models.embeddings.EmbeddingCreateResponse;

OpenAIClient client = OpenAIOkHttpClient.fromEnv();

// Create embeddings for input text
EmbeddingCreateParams params = EmbeddingCreateParams.builder()
    .input("The quick brown fox jumped over the lazy dog")
    .model(EmbeddingModel.TEXT_EMBEDDING_3_SMALL)
    .build();

// Retrieve embedding vectors
EmbeddingCreateResponse response = client.embeddings().create(params);
response.data().forEach(embedding -> {
    System.out.println("Index: " + embedding.index());
    System.out.println("Embedding: " + embedding.embedding());
});
```

## Image Generation

Generate images from text prompts using DALL-E models with various size and format options.

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.images.ImageGenerateParams;
import com.openai.models.images.ImageModel;
import com.openai.models.images.ImagesResponse;

OpenAIClient client = OpenAIOkHttpClient.fromEnv();

// Configure image generation parameters
ImageGenerateParams params = ImageGenerateParams.builder()
    .responseFormat(ImageGenerateParams.ResponseFormat.URL)
    .prompt("Two cats playing ping-pong")
    .model(ImageModel.DALL_E_2)
    .size(ImageGenerateParams.Size._512X512)
    .n(1)
    .build();

// Generate and retrieve image URLs
ImagesResponse response = client.images().generate(params);
response.data().orElseThrow().stream()
    .flatMap(image -> image.url().stream())
    .forEach(System.out::println);
```

## Video Generation

Generate videos from text prompts using Sora models with polling for completion status.

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.videos.Video;
import com.openai.models.videos.VideoCreateParams;
import com.openai.models.videos.VideoModel;

OpenAIClient client = OpenAIOkHttpClient.fromEnv();

// Configure video generation parameters
VideoCreateParams params = VideoCreateParams.builder()
    .model(VideoModel.SORA_2)
    .prompt("A video of the words 'Thank you' in sparkling letters")
    .build();

// Create video and get initial status
Video video = client.videos().create(params);

// Poll until video is completed or failed
while (video.status() != Video.Status.COMPLETED && video.status() != Video.Status.FAILED) {
    System.out.println("Polling... Progress: " + video.progress() + "%");
    Thread.sleep(10_000); // Poll every 10 seconds
    video = client.videos().retrieve(video.id());
}

// Check final status and handle result
if (video.status() == Video.Status.COMPLETED) {
    System.out.println("Video successfully completed!");
    System.out.println("Video ID: " + video.id());
} else {
    System.out.println("Video creation did not complete. Status: " + video.status());
    video.error().ifPresent(error -> {
        System.out.println("Error: " + error);
    });
}
```

## Audio Transcription

Transcribe audio files to text using Whisper models for speech recognition.

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.audio.AudioModel;
import com.openai.models.audio.transcriptions.Transcription;
import com.openai.models.audio.transcriptions.TranscriptionCreateParams;
import java.nio.file.Path;
import java.nio.file.Paths;

OpenAIClient client = OpenAIOkHttpClient.fromEnv();

// Specify audio file path
Path audioPath = Paths.get("path/to/audio.wav");

TranscriptionCreateParams params = TranscriptionCreateParams.builder()
    .file(audioPath)
    .model(AudioModel.WHISPER_1)
    .build();

// Get transcribed text
Transcription transcription = client.audio().transcriptions().create(params).asTranscription();
System.out.println(transcription.text());
```

## Content Moderation

Analyze text content for potentially harmful or inappropriate material using OpenAI's moderation models.

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.moderations.ModerationCreateParams;
import com.openai.models.moderations.ModerationCreateResponse;
import com.openai.models.moderations.ModerationModel;

OpenAIClient client = OpenAIOkHttpClient.fromEnv();

// Submit content for moderation
ModerationCreateParams params = ModerationCreateParams.builder()
    .input("I want to kill them.")
    .model(ModerationModel.OMNI_MODERATION_LATEST)
    .build();

// Analyze moderation results
ModerationCreateResponse response = client.moderations().create(params);
response.results().forEach(result -> {
    System.out.println("Flagged: " + result.flagged());
    result.categories().ifPresent(categories -> System.out.println(categories));
});
```

## Batch Processing

Submit multiple API requests as a batch for asynchronous processing with cost savings.

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.HttpResponse;
import com.openai.models.batches.Batch;
import com.openai.models.batches.BatchCreateParams;
import com.openai.models.batches.BatchCreateParams.CompletionWindow;
import com.openai.models.batches.BatchCreateParams.Endpoint;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import java.nio.file.Path;
import java.nio.file.Paths;

OpenAIClient client = OpenAIOkHttpClient.fromEnv();

// Upload batch requests file
Path requestsPath = Paths.get("requests.jsonl");
FileCreateParams fileParams = FileCreateParams.builder()
    .purpose(FilePurpose.BATCH)
    .file(requestsPath)
    .build();
FileObject file = client.files().create(fileParams);

// Create batch job
BatchCreateParams batchParams = BatchCreateParams.builder()
    .inputFileId(file.id())
    .endpoint(Endpoint.V1_CHAT_COMPLETIONS)
    .completionWindow(CompletionWindow._24H)
    .build();
Batch batch = client.batches().create(batchParams);

// Poll for completion
while (batch.outputFileId().isEmpty()) {
    Thread.sleep(60_000);
    batch = client.batches().retrieve(batch.id());
}

// Download results
try (HttpResponse response = client.files().content(batch.outputFileId().orElseThrow())) {
    response.body().transferTo(System.out);
}
```

## File Upload

Upload files for use with various OpenAI features like fine-tuning and assistants.

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import java.nio.file.Paths;
import java.io.InputStream;
import com.openai.core.MultipartField;

OpenAIClient client = OpenAIOkHttpClient.fromEnv();

// Upload file from path
FileCreateParams params1 = FileCreateParams.builder()
    .purpose(FilePurpose.FINE_TUNE)
    .file(Paths.get("input.jsonl"))
    .build();
FileObject fileObject1 = client.files().create(params1);

// Upload from InputStream with custom filename
InputStream stream = getClass().getResourceAsStream("/data.jsonl");
FileCreateParams params2 = FileCreateParams.builder()
    .purpose(FilePurpose.FINE_TUNE)
    .file(MultipartField.<InputStream>builder()
        .value(stream)
        .filename("data.jsonl")
        .build())
    .build();
FileObject fileObject2 = client.files().create(params2);

System.out.println("File ID: " + fileObject1.id());
```

## Client Configuration

Configure API credentials, base URLs, timeouts, retries, and other client options.

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import java.time.Duration;
import java.net.InetSocketAddress;
import java.net.Proxy;

// Configure using environment variables
OpenAIClient client1 = OpenAIOkHttpClient.fromEnv();

// Manual configuration
OpenAIClient client2 = OpenAIOkHttpClient.builder()
    .apiKey("sk-...")
    .organization("org-...")
    .project("proj-...")
    .baseUrl("https://api.openai.com/v1")
    .build();

// Advanced configuration with retries, timeout, and proxy
OpenAIClient client3 = OpenAIOkHttpClient.builder()
    .fromEnv()
    .maxRetries(3)
    .timeout(Duration.ofSeconds(30))
    .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080)))
    .responseValidation(true)
    .build();

// Temporary configuration override
OpenAIClient modifiedClient = client1.withOptions(optionsBuilder -> {
    optionsBuilder.baseUrl("https://custom.api.com");
    optionsBuilder.maxRetries(5);
});
```

## Error Handling

Handle various HTTP errors and exceptions with specific exception types for different status codes.

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.errors.*;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

OpenAIClient client = OpenAIOkHttpClient.fromEnv();

ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
    .addUserMessage("Hello")
    .model(ChatModel.GPT_3_5_TURBO)
    .build();

try {
    client.chat().completions().create(params);
} catch (RateLimitException e) {
    System.err.println("Rate limit exceeded: " + e.getMessage());
} catch (UnauthorizedException e) {
    System.err.println("Invalid API key: " + e.getMessage());
} catch (BadRequestException e) {
    System.err.println("Bad request: " + e.getMessage());
} catch (InternalServerException e) {
    System.err.println("Server error: " + e.getMessage());
} catch (OpenAIServiceException e) {
    System.err.println("API error: " + e.statusCode() + " - " + e.getMessage());
} catch (OpenAIIoException e) {
    System.err.println("Network error: " + e.getMessage());
} catch (OpenAIException e) {
    System.err.println("Unexpected error: " + e.getMessage());
}
```

## Pagination

Navigate paginated API responses manually or automatically across all pages.

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.finetuning.jobs.FineTuningJob;
import com.openai.models.finetuning.jobs.JobListPage;

OpenAIClient client = OpenAIOkHttpClient.fromEnv();

// Auto-pagination - iterate through all items
JobListPage page = client.fineTuning().jobs().list();
for (FineTuningJob job : page.autoPager()) {
    System.out.println("Job: " + job.id());
}

// Auto-pagination with stream processing
page.autoPager().stream()
    .limit(50)
    .forEach(job -> System.out.println(job.id()));

// Manual pagination
JobListPage currentPage = client.fineTuning().jobs().list();
while (true) {
    for (FineTuningJob job : currentPage.items()) {
        System.out.println(job.id());
    }
    if (!currentPage.hasNextPage()) {
        break;
    }
    currentPage = currentPage.nextPage();
}
```

## Azure OpenAI Integration

Use the SDK with Azure OpenAI services by configuring Azure-specific authentication and endpoints.

```java
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.BearerTokenCredential;
import com.openai.azure.AzureUrlPathMode;

// Configure with Azure credentials
TokenCredential azureCredential = new DefaultAzureCredentialBuilder().build();

OpenAIClient client = OpenAIOkHttpClient.builder()
    .baseUrl(System.getenv("OPENAI_BASE_URL"))
    .credential(BearerTokenCredential.create(() -> {
        return azureCredential.getToken(
            new com.azure.core.credential.TokenRequestContext()
                .addScopes("https://cognitiveservices.azure.com/.default")
        ).block().getToken();
    }))
    .azureUrlPathMode(AzureUrlPathMode.AUTO)
    .build();

// Use client normally with Azure endpoints
var response = client.chat().completions().create(params);
```

## Spring Boot Integration

Simplify configuration in Spring Boot applications using the provided starter and auto-configuration.

```java
// application.properties configuration
// openai.api-key=sk-...
// openai.base-url=https://api.openai.com/v1
// openai.org-id=org-...
// openai.project-id=proj-...

import com.openai.client.OpenAIClient;
import com.openai.springboot.OpenAIClientCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Configuration
public class OpenAIConfig {
    // Optional: customize client configuration
    @Bean
    public OpenAIClientCustomizer customizer() {
        return builder -> builder
            .maxRetries(3)
            .responseValidation(true);
    }
}

@Service
public class AIService {
    @Autowired
    private OpenAIClient client;

    public String generateText(String prompt) {
        var params = ChatCompletionCreateParams.builder()
            .addUserMessage(prompt)
            .model(ChatModel.GPT_3_5_TURBO)
            .build();

        return client.chat().completions().create(params)
            .choices().get(0)
            .message().content().orElse("");
    }
}
```

## Summary

The OpenAI Java SDK enables seamless integration with OpenAI's suite of AI services for chat completions, embeddings, image generation, video generation, audio transcription, content moderation, and batch processing. Primary use cases include building chatbots and conversational AI applications, implementing semantic search with vector embeddings, generating images and videos from text descriptions, transcribing audio recordings, moderating user-generated content, and processing large-scale API requests efficiently. The SDK supports advanced features like function calling for tool integration, structured outputs for type-safe JSON responses, and streaming for real-time interaction experiences.

Integration patterns follow Java best practices with immutable builders, type-safe parameter objects, and fluent APIs for constructing requests. The SDK provides both synchronous and asynchronous clients for different concurrency models, automatic retry logic with exponential backoff for resilience, and comprehensive error handling with specific exception types. Configuration can be managed through environment variables, system properties, programmatic builders, or Spring Boot auto-configuration. The library includes built-in support for Azure OpenAI deployments, custom HTTP clients for advanced networking requirements, and automatic pagination for navigating large result sets. All classes are immutable once constructed, ensuring thread safety and predictable behavior in concurrent applications.
