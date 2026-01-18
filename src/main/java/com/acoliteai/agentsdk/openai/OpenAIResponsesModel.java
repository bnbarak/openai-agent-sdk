package com.acoliteai.agentsdk.openai;

import com.acoliteai.agentsdk.core.*;
import com.acoliteai.agentsdk.core.types.JsonSchemaOutput;
import com.acoliteai.agentsdk.exceptions.NotImplementedException;
import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.StructuredResponse;
import com.openai.models.responses.StructuredResponseCreateParams;
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
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-openai/src/openaiResponsesModel.ts
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
    }

    ResponseCreateParams params = paramsBuilder.build();
    Response response = client.responses().create(params);
    return convertToModelResponse(response);
  }

  private void registerTools(ResponseCreateParams.Builder paramsBuilder, List<Object> tools) {
    tools.stream()
        .map(OpenAIResponsesModel::validateAndCastToFunctionTool)
        .map(OpenAIResponsesModel::extractParameterClass)
        .forEach(paramsBuilder::addTool);
  }

  /**
   * Validates that a tool is a FunctionTool and casts it.
   *
   * @param tool Tool to validate
   * @return FunctionTool instance
   * @throws IllegalArgumentException if tool is not a FunctionTool
   */
  static FunctionTool<?, ?, ?> validateAndCastToFunctionTool(Object tool) {
    if (tool instanceof FunctionTool<?, ?, ?> functionTool) {
      return functionTool;
    }

    throw new IllegalArgumentException(
        "OpenAI Responses API only supports FunctionTool. Got: " + tool.getClass().getName());
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

  /** Extracts output from OpenAI response (text and function calls) */
  private List<Object> extractOutput(Response response) {
    List<Object> output = new ArrayList<>();

    for (var item : response.output()) {
      if (item.isFunctionCall()) {
        output.add(item.asFunctionCall());
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
