package ai.acolite.agentsdk.openai;

import ai.acolite.agentsdk.core.*;
import ai.acolite.agentsdk.core.types.JsonSchemaOutput;
import ai.acolite.agentsdk.exceptions.NotImplementedException;
import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.StructuredResponse;
import com.openai.models.responses.StructuredResponseCreateParams;
import com.openai.models.responses.Tool;
import com.openai.models.responses.WebSearchTool;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * OpenAIResponsesModel
 *
 * <p>Model implementation using OpenAI Responses API.
 *
 * <p>Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-openai/src/openaiResponsesModel.ts">...</a>
 */
public class OpenAIResponsesModel implements Model {
  private final OpenAIClient client;
  private final String modelName;

  public OpenAIResponsesModel(OpenAIClient client, String modelName) {
    this.client = client;
    this.modelName = modelName;
  }

  @Override
  public CompletableFuture<ModelResponse> getResponse(ModelRequest request) {
    return CompletableFuture.supplyAsync(
        () -> {
          if (OutputTypeResolver.requiresStructuredResponse(request.getOutputType())) {
            return getStructuredResponse(request, (JsonSchemaOutput<?>) request.getOutputType());
          } else {
            return getTextResponse(request);
          }
        });
  }

  private ModelResponse getTextResponse(ModelRequest request) {
    ResponseCreateParams.Builder paramsBuilder = ResponseCreateParams.builder().model(modelName);

    if (request.getInput() != null && !request.getInput().isEmpty()) {
      List<ResponseInputItem> inputItems =
          ConversionUtils.convertToResponseInputItems(request.getInput());
      paramsBuilder.input(ResponseCreateParams.Input.ofResponse(inputItems));
    }

    if (request.getInstructions() != null && !request.getInstructions().isEmpty()) {
      paramsBuilder.instructions(request.getInstructions());
    }

    if (request.getTools() != null && !request.getTools().isEmpty()) {
      registerTools(paramsBuilder, request.getTools());
      // Configure maxToolCalls from settings or use default
      int maxToolCalls =
          request.getSettings() != null
                  && request.getSettings().getMaxToolCalls() != null
                  && request.getSettings().getMaxToolCalls().isPresent()
              ? request.getSettings().getMaxToolCalls().get()
              : 10;
      paramsBuilder.maxToolCalls(maxToolCalls);
    }

    ResponseCreateParams params = paramsBuilder.build();
    Response response = client.responses().create(params);
    return convertToModelResponse(response);
  }

  private void registerTools(ResponseCreateParams.Builder paramsBuilder, List<Object> tools) {
    for (Object tool : tools) {
      if (tool instanceof FunctionTool<?, ?, ?> functionTool) {
        registerFunctionTool(paramsBuilder, functionTool);
      } else if (tool instanceof HostedTool hostedTool) {
        registerHostedTool(paramsBuilder, hostedTool);
      } else {
        throw new IllegalArgumentException(
            "OpenAI Responses API only supports FunctionTool and HostedTool. Got: "
                + tool.getClass().getName());
      }
    }
  }

  /**
   * Registers a FunctionTool with the OpenAI API.
   *
   * @param paramsBuilder Builder to add tool to
   * @param functionTool FunctionTool to register
   */
  private void registerFunctionTool(
      ResponseCreateParams.Builder paramsBuilder, FunctionTool<?, ?, ?> functionTool) {
    Class<?> parameterClass = extractParameterClass(functionTool);
    paramsBuilder.addTool(parameterClass);
  }

  /**
   * Registers a HostedTool with the OpenAI API.
   *
   * @param paramsBuilder Builder to add tool to
   * @param hostedTool HostedTool to register
   */
  private void registerHostedTool(
      ResponseCreateParams.Builder paramsBuilder, HostedTool hostedTool) {
    switch (hostedTool.getType()) {
      case "web_search":
        paramsBuilder.addTool(WebSearchTool.builder().type(WebSearchTool.Type.WEB_SEARCH).build());
        break;
      case "image_generation":
        paramsBuilder.addTool(Tool.ofImageGeneration(Tool.ImageGeneration.builder().build()));
        break;
      case "file_search":
      case "code_interpreter":
      case "computer_use":
        throw new UnsupportedOperationException(
            hostedTool.getType()
                + " hosted tool is not yet supported by this SDK. "
                + "Only web_search and image_generation are currently supported.");
      default:
        throw new IllegalArgumentException("Unknown hosted tool type: " + hostedTool.getType());
    }
  }

  /**
   * Extracts and validates the parameter class from a FunctionTool.
   *
   * @param tool FunctionTool to extract from
   * @return Parameter class for Jackson serialization
   * @throws IllegalArgumentException if parameters is not a Class
   */
  static Class<?> extractParameterClass(FunctionTool<?, ?, ?> tool) {
    Object parameters = tool.getParameters();

    if (parameters instanceof Class<?> parameterClass) {
      return parameterClass;
    }

    throw new IllegalArgumentException(
        String.format(
            "FunctionTool.getParameters() must return a Class for OpenAI Responses API. "
                + "Tool '%s' returned: %s",
            tool.getName(), parameters.getClass().getName()));
  }

  private <T> ModelResponse getStructuredResponse(
      ModelRequest request, JsonSchemaOutput<T> schemaOutput) {
    String inputText = "";
    if (request.getInput() != null && !request.getInput().isEmpty()) {
      inputText =
          request.getInput().stream().map(Object::toString).collect(Collectors.joining("\n"));
    }

    StructuredResponseCreateParams<T> params =
        ResponseCreateParams.builder()
            .model(modelName)
            .input(inputText)
            .instructions(request.getInstructions() != null ? request.getInstructions() : "")
            .text(schemaOutput.getTargetClass())
            .build();

    StructuredResponse<T> structuredResponse = client.responses().create(params);
    return convertStructuredToModelResponse(structuredResponse);
  }

  private <T> ModelResponse convertStructuredToModelResponse(StructuredResponse<T> response) {
    Usage usage = extractUsageFromStructured(response);
    List<Object> output = new ArrayList<>();

    response.output().stream()
        .flatMap(item -> item.message().stream())
        .flatMap(message -> message.content().stream())
        .forEach(content -> content.outputText().ifPresent(output::add));

    return ModelResponse.builder()
        .usage(usage)
        .output(output)
        .responseId(Optional.ofNullable(response.id()))
        .providerData(Optional.empty())
        .build();
  }

  private <T> Usage extractUsageFromStructured(StructuredResponse<T> response) {
    if (response.usage().isEmpty() || response.usage().isEmpty()) {
      return Usage.empty();
    }

    com.openai.models.responses.ResponseUsage apiUsage = response.usage().get();
    return Usage.builder()
        .inputTokens((double) apiUsage.inputTokens())
        .outputTokens((double) apiUsage.outputTokens())
        .totalTokens((double) apiUsage.totalTokens())
        .build();
  }

  private ModelResponse convertToModelResponse(Response response) {
    Usage usage = extractUsage(response);
    List<Object> output = extractOutput(response);

    return ModelResponse.builder()
        .usage(usage)
        .output(output)
        .responseId(Optional.ofNullable(response.id()))
        .providerData(Optional.empty())
        .build();
  }

  private Usage extractUsage(Response response) {
    if (response.usage() == null || response.usage().isEmpty()) {
      return Usage.empty();
    }

    com.openai.models.responses.ResponseUsage apiUsage = response.usage().get();
    return Usage.builder()
        .inputTokens((double) apiUsage.inputTokens())
        .outputTokens((double) apiUsage.outputTokens())
        .totalTokens((double) apiUsage.totalTokens())
        .build();
  }

  /** Extracts output from OpenAI response (text, function calls, and hosted tool calls) */
  private List<Object> extractOutput(Response response) {
    List<Object> output = new ArrayList<>();

    for (var item : response.output()) {
      if (item.isFunctionCall()) {
        output.add(item.asFunctionCall());
      } else if (item.isWebSearchCall()) {
        output.add(item.asWebSearchCall());
      } else if (item.isCodeInterpreterCall()) {
        output.add(item.asCodeInterpreterCall());
      } else if (item.isImageGenerationCall()) {
        output.add(item.asImageGenerationCall());
      } else if (item.isFileSearchCall()) {
        output.add(item.asFileSearchCall());
      } else {
        item.message().stream()
            .flatMap(message -> message.content().stream())
            .flatMap(content -> content.outputText().stream())
            .forEach(outputText -> output.add(outputText.text()));
      }
    }

    return output;
  }

  @Override
  public AsyncIterable<StreamEvent> getStreamedResponse(ModelRequest request) {
    throw new NotImplementedException("Streaming not yet implemented");
  }
}
