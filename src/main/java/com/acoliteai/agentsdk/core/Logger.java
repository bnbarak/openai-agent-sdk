package com.acoliteai.agentsdk.core;

/**
 * Logger
 *
 * <p>Logger interface with debug, error, and warn methods.
 *
 * <p>Source:
 * https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/logger.ts
 */
public interface Logger {
  String getNamespace();

  void debug(String message, Object... args);

  void error(String message, Object... args);

  void warn(String message, Object... args);

  boolean isDontLogModelData();

  boolean isDontLogToolData();
}
