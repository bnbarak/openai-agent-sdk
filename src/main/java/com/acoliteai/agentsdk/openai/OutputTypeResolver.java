package com.acoliteai.agentsdk.openai;

import com.acoliteai.agentsdk.core.types.AgentOutputType;
import com.acoliteai.agentsdk.core.types.JsonSchemaOutput;

public class OutputTypeResolver {

  public static boolean requiresStructuredResponse(AgentOutputType outputType) {
    return outputType instanceof JsonSchemaOutput<?>;
  }
}
