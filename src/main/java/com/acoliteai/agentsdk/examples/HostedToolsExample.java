package com.acoliteai.agentsdk.examples;

import com.acoliteai.agentsdk.core.Agent;
import com.acoliteai.agentsdk.core.HostedTool;
import com.acoliteai.agentsdk.core.RunResult;
import com.acoliteai.agentsdk.core.Runner;
import com.acoliteai.agentsdk.core.types.TextOutput;
import com.acoliteai.agentsdk.core.types.UnknownContext;
import java.util.List;

/**
 * Example demonstrating how to use OpenAI hosted tools.
 *
 * <p>Hosted tools execute on OpenAI's infrastructure rather than in your application. This SDK
 * currently supports:
 *
 * <ul>
 *   <li><b>web_search</b> - Search the web for current information
 *   <li><b>image_generation</b> - Generate images using DALL-E
 * </ul>
 *
 * <p>Note: Other hosted tools like file_search, code_interpreter, and computer_use are not yet
 * supported.
 */
public class HostedToolsExample {

  public static void main(String[] args) {
    webSearchExample();
    imageGenerationExample();
    multipleHostedToolsExample();
  }

  /** Example using web search to get current information. */
  private static void webSearchExample() {
    System.out.println("\n=== Web Search Example ===");

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("SearchAssistant")
            .instructions("You can search the web for current information using web_search.")
            .tools(List.of(HostedTool.webSearch()))
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(agent, "What is the current weather in Tokyo?");

    System.out.println("Response: " + result.getFinalOutput());
  }

  /** Example using image generation to create images. */
  private static void imageGenerationExample() {
    System.out.println("\n=== Image Generation Example ===");

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("Artist")
            .instructions("You can generate images using DALL-E via image_generation.")
            .tools(List.of(HostedTool.imageGeneration()))
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(agent, "Generate an image of a serene mountain landscape at sunset");

    System.out.println("Response: " + result.getFinalOutput());
  }

  /** Example using multiple hosted tools together. */
  private static void multipleHostedToolsExample() {
    System.out.println("\n=== Multiple Hosted Tools Example ===");

    Agent<UnknownContext, TextOutput> agent =
        Agent.<UnknownContext, TextOutput>builder()
            .name("CreativeAssistant")
            .instructions(
                "You can search the web for information and generate images. "
                    + "Use web_search to find current information and image_generation to create visualizations.")
            .tools(List.of(HostedTool.webSearch(), HostedTool.imageGeneration()))
            .build();

    RunResult<UnknownContext, ?> result =
        Runner.run(
            agent,
            "Search for information about the Northern Lights and then generate an artistic image based on what you find");

    System.out.println("Response: " + result.getFinalOutput());
  }
}
