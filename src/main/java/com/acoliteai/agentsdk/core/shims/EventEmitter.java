package com.acoliteai.agentsdk.core.shims;

import com.acoliteai.agentsdk.core.EventEmitterEvents;

/**
 * EventEmitter
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/shims/interface.ts">shims/interface.ts</a>
 */
public interface EventEmitter<EventTypes extends EventEmitterEvents> {

  /**
   * emit
   *
   * @param type K
   * @return Boolean
   */
  Boolean emit(EventTypes type);
}
