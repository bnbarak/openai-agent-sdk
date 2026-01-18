/**
 * Core framework for building AI agents with OpenAI's API.
 *
 * <p>This package contains the main classes for creating and running agents:
 *
 * <ul>
 *   <li>{@link com.acoliteai.agentsdk.core.Agent} - Main agent class for defining AI agents
 *   <li>{@link com.acoliteai.agentsdk.core.Runner} - Execute agents synchronously or asynchronously
 *   <li>{@link com.acoliteai.agentsdk.core.RunContext} - Runtime context for tool execution
 *   <li>{@link com.acoliteai.agentsdk.core.FunctionTool} - Interface for defining custom tools
 *   <li>{@link com.acoliteai.agentsdk.core.RunResult} - Results from agent execution
 * </ul>
 *
 * <h2>Quick Example</h2>
 *
 * <pre>{@code
 * Agent<UnknownContext, TextOutput> agent = Agent.<UnknownContext, TextOutput>builder()
 *     .model("gpt-4o")
 *     .instructions("You are a helpful assistant.")
 *     .build();
 *
 * RunResult<UnknownContext, ?> result = Runner.run(agent, "Hello!");
 * System.out.println(result.getFinalOutput());
 * }</pre>
 *
 * @see com.acoliteai.agentsdk.core.types
 * @see com.acoliteai.agentsdk.core.sessions
 * @see com.acoliteai.agentsdk.core.tracing
 */
package com.acoliteai.agentsdk.core;
