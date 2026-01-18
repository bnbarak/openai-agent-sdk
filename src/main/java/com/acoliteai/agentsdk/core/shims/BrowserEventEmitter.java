package com.acoliteai.agentsdk.core.shims;

import com.acoliteai.agentsdk.core.EventEmitterEvents;

/**
 * BrowserEventEmitter
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/shims/shims-browser.ts">shims/shims-browser.ts</a>
 */
public class BrowserEventEmitter<EventTypes extends EventMap & EventEmitterEvents>
    implements EventEmitter<EventTypes> {

  @Override
  public Boolean emit(EventTypes type) {
    return null;
  }
}
