package com.acoliteai.agentsdk.core;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * RunItemBase
 *
 * <p>Base class for all run items with common fields.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: <a
 * href="https://github.com/openai/openai-agents-js/blob/main/packages/agents-core/src/items.ts">items.ts</a>
 */
@Getter
@SuperBuilder
public abstract class RunItemBase implements RunItem {}
