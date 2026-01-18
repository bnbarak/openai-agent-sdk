/**
 * Session management for multi-turn agent conversations.
 *
 * <p>This package provides interfaces and implementations for managing conversation state across
 * multiple agent interactions:
 *
 * <ul>
 *   <li>{@link ai.acolite.agentsdk.core.sessions.Session} - Interface for session storage
 *   <li>{@link ai.acolite.agentsdk.core.sessions.MemorySession} - In-memory session storage (for
 *       development)
 *   <li>{@link ai.acolite.agentsdk.core.sessions.SQLiteSession} - Persistent SQLite session storage
 *       (for production)
 * </ul>
 *
 * <h2>Example: Using Sessions</h2>
 *
 * <pre>{@code
 * // Create a session
 * Session session = new MemorySession();
 *
 * // Create an agent
 * Agent<UnknownContext, TextOutput> agent = Agent.<UnknownContext, TextOutput>builder()
 *     .model("gpt-4.1")
 *     .instructions("You are a helpful assistant.")
 *     .build();
 *
 * // Run multiple turns with memory
 * Runner.run(agent, "My name is Alice", RunConfig.builder().session(session).build());
 * Runner.run(agent, "What's my name?", RunConfig.builder().session(session).build());
 * }</pre>
 *
 * @see ai.acolite.agentsdk.core.Agent
 * @see ai.acolite.agentsdk.core.RunConfig
 */
package ai.acolite.agentsdk.core.sessions;
