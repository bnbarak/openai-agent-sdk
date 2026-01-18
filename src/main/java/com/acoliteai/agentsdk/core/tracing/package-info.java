/**
 * Distributed tracing and observability for agent execution.
 *
 * <p>This package provides tracing infrastructure to monitor and debug agent execution:
 *
 * <ul>
 *   <li>{@link com.acoliteai.agentsdk.core.tracing.TraceProvider} - Global trace management
 *   <li>{@link com.acoliteai.agentsdk.core.tracing.Trace} - Trace context for tracking execution
 *   <li>{@link com.acoliteai.agentsdk.core.tracing.Span} - Individual operation tracking
 *   <li>{@link com.acoliteai.agentsdk.core.tracing.TraceProcessor} - Interface for processing
 *       trace events
 * </ul>
 *
 * <h2>Example: Custom Trace Processor</h2>
 *
 * <pre>{@code
 * // Register a custom trace processor
 * TraceProvider.getInstance().registerProcessor(new TraceProcessor() {
 *     @Override
 *     public void processTrace(Trace trace) {
 *         System.out.println("Trace: " + trace.getId());
 *         trace.getSpans().forEach(span ->
 *             System.out.println("  Span: " + span.getName())
 *         );
 *     }
 * });
 *
 * // Traces are automatically created during agent execution
 * Runner.run(agent, "Hello!");
 * }</pre>
 *
 * <h2>OpenAI Platform Integration</h2>
 *
 * <p>Tracing data is automatically sent to the OpenAI platform for visualization when the {@code
 * OPENAI_API_KEY} environment variable is set.
 *
 * @see com.acoliteai.agentsdk.core.Agent
 * @see com.acoliteai.agentsdk.core.Runner
 */
package com.acoliteai.agentsdk.core.tracing;
