/**
 * Core framework for building AI agents with OpenAI's API.
 *
 * <p>This package contains the main classes for creating and running agents:
 *
 * <ul>
 *   <li>{@link ai.acolite.agentsdk.core.Agent} - Main agent class for defining AI agents
 *   <li>{@link ai.acolite.agentsdk.core.Runner} - Execute agents synchronously or asynchronously
 *   <li>{@link ai.acolite.agentsdk.core.RunContext} - Runtime context for tool execution
 *   <li>{@link ai.acolite.agentsdk.core.FunctionTool} - Interface for defining custom tools
 *   <li>{@link ai.acolite.agentsdk.core.RunResult} - Results from agent execution
 * </ul>
 *
 * <h2>Quick Example</h2>
 *
 * <pre>{@code
 * Agent<UnknownContext, TextOutput> agent = Agent.<UnknownContext, TextOutput>builder()
 *     .model("gpt-4.1")
 *     .instructions("You are a helpful assistant.")
 *     .build();
 *
 * RunResult<UnknownContext, ?> result = Runner.run(agent, "Hello!");
 * System.out.println(result.getFinalOutput());
 * }</pre>
 *
 * @see ai.acolite.agentsdk.core.types
 * @see ai.acolite.agentsdk.core.sessions
 * @see ai.acolite.agentsdk.core.tracing
 */
package ai.acolite.agentsdk.core;
