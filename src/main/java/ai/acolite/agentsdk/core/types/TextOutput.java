package ai.acolite.agentsdk.core.types;

/**
 * TextOutput
 *
 * <p>Represents text output type for agents. In TypeScript this is the literal type 'text'
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/types/aliases.ts
 */
public class TextOutput implements AgentOutputType {
  public static final TextOutput INSTANCE = new TextOutput();

  private TextOutput() {
    // Singleton
  }
}
