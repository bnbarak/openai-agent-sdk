package com.acoliteai.agentsdk.openai;

import static org.junit.jupiter.api.Assertions.*;

import com.acoliteai.agentsdk.core.types.JsonSchemaOutput;
import com.acoliteai.agentsdk.core.types.TextOutput;
import com.acoliteai.agentsdk.realworldapi.testdata.BookList;
import org.junit.jupiter.api.Test;

class OutputTypeResolverTest {

  @Test
  void requiresStructuredResponse_withJsonSchemaOutput_returnsTrue() {
    JsonSchemaOutput<BookList> outputType = JsonSchemaOutput.of(BookList.class);

    assertTrue(OutputTypeResolver.requiresStructuredResponse(outputType));
  }

  @Test
  void requiresStructuredResponse_withTextOutput_returnsFalse() {
    assertFalse(OutputTypeResolver.requiresStructuredResponse(TextOutput.INSTANCE));
  }
}
