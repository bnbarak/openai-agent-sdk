package ai.acolite.agentsdk.openai;

import ai.acolite.agentsdk.core.types.AgentOutputType;
import ai.acolite.agentsdk.core.types.JsonSchemaOutput;

public class OutputTypeResolver {

  public static boolean requiresStructuredResponse(AgentOutputType outputType) {
    return outputType instanceof JsonSchemaOutput<?>;
  }
}
