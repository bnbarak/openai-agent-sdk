package ai.acolite.agentsdk.core;

import java.util.Optional;
import lombok.Builder;
import lombok.Value;

/**
 * HostedTool
 *
 * <p>Represents tools that execute on OpenAI's hosted infrastructure rather than in your
 * application. These tools are provided and maintained by OpenAI.
 *
 * <p>Currently supported hosted tools:
 *
 * <ul>
 *   <li><b>web_search</b> - Search the web for current information
 *   <li><b>image_generation</b> - Generate images using DALL-E
 * </ul>
 *
 * <p><b>Note:</b> Other OpenAI hosted tools like {@code file_search}, {@code code_interpreter}, and
 * {@code computer_use} are not yet supported by this SDK. Attempting to use them will throw an
 * {@link UnsupportedOperationException}.
 *
 * <p>Unlike {@link FunctionTool}, hosted tools execute remotely on OpenAI's servers. You configure
 * them but do not implement their execution logic.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Web search
 * Agent<Object, TextOutput> agent = Agent.builder()
 *     .instructions("You can search the web for current information.")
 *     .tools(List.of(HostedTool.webSearch()))
 *     .build();
 *
 * // Image generation
 * Agent<Object, TextOutput> agent = Agent.builder()
 *     .instructions("You can generate images using DALL-E.")
 *     .tools(List.of(HostedTool.imageGeneration()))
 *     .build();
 *
 * // Multiple hosted tools
 * Agent<Object, TextOutput> agent = Agent.builder()
 *     .tools(List.of(
 *         HostedTool.webSearch(),
 *         HostedTool.imageGeneration()
 *     ))
 *     .build();
 * }</pre>
 *
 * <p>Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/tool.ts">openai-agents-js/tool.ts</a>
 *
 * @see FunctionTool
 * @see Agent
 */
@Value
@Builder
public class HostedTool implements Tool<Object> {
  /**
   * Tool type identifier (e.g., "web_search", "file_search", "code_interpreter",
   * "image_generation").
   */
  String type;

  /** Optional tool name (not required for hosted tools, type is sufficient). */
  @Builder.Default String name = "";

  /** Optional description (not required for hosted tools). */
  @Builder.Default String description = "";

  /** Optional vector store ID for file_search tool. */
  @Builder.Default Optional<String> vectorStoreId = Optional.empty();

  /**
   * Create a web search hosted tool.
   *
   * @return HostedTool configured for web search
   */
  public static HostedTool webSearch() {
    return HostedTool.builder().type("web_search").name("web_search").build();
  }

  /**
   * Create a file search hosted tool.
   *
   * @param vectorStoreId OpenAI vector store ID to search
   * @return HostedTool configured for file search
   * @throws UnsupportedOperationException file_search is not yet supported by this SDK
   * @deprecated file_search is not yet supported by this SDK
   */
  @Deprecated
  public static HostedTool fileSearch(String vectorStoreId) {
    throw new UnsupportedOperationException(
        "file_search hosted tool is not yet supported by this SDK. "
            + "Only web_search and image_generation are currently supported.");
  }

  /**
   * Create a code interpreter hosted tool.
   *
   * @return HostedTool configured for code interpreter
   * @throws UnsupportedOperationException code_interpreter is not yet supported by this SDK
   * @deprecated code_interpreter is not yet supported by this SDK
   */
  @Deprecated
  public static HostedTool codeInterpreter() {
    throw new UnsupportedOperationException(
        "code_interpreter hosted tool is not yet supported by this SDK. "
            + "Only web_search and image_generation are currently supported.");
  }

  /**
   * Create an image generation hosted tool.
   *
   * @return HostedTool configured for image generation
   */
  public static HostedTool imageGeneration() {
    return HostedTool.builder().type("image_generation").name("image_generation").build();
  }
}
