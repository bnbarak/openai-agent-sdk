package com.acoliteai.agentsdk.core.shims;

/**
 * Timeout
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/shims/interface.ts">shims/interface.ts</a>
 */
public interface Timeout {

  /**
   * ref
   *
   * @return Timeout
   */
  Timeout ref();

  /**
   * unref
   *
   * @return Timeout
   */
  Timeout unref();

  /**
   * hasRef
   *
   * @return Boolean
   */
  Boolean hasRef();

  /**
   * refresh
   *
   * @return Timeout
   */
  Timeout refresh();
}
