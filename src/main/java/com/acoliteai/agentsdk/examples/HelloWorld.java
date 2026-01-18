package com.acoliteai.agentsdk.examples;

/**
 * HelloWorld
 *
 * <p>Entry point for OpenAI Agents SDK examples.
 *
 * <p>This package contains several example programs demonstrating different features of the SDK:
 *
 * <p>1. BasicTextOutput.java - Simplest example: text-based agent interaction - Shows basic agent
 * creation and execution - Usage statistics tracking
 *
 * <p>2. StructuredOutput.java - Demonstrates JSON Schema structured outputs - Type-safe data
 * extraction from agent responses - Custom data classes for structured data
 *
 * <p>3. MultiTurnExecution.java - Multi-turn execution with RunConfig - Turn tracking and usage
 * accumulation - Detailed execution metrics
 *
 * <p>4. AgentConfiguration.java - Various agent configuration options - Custom instructions,
 * models, and settings - RunConfig usage patterns
 *
 * <p>5. MemorySessionExample.java - In-memory session storage for conversation memory - Multi-turn
 * conversations with context retention - Ideal for development and testing
 *
 * <p>6. SQLiteSessionExample.java - Persistent session storage with SQLite database - Conversation
 * history across application restarts - Production-ready session management
 *
 * <p>7. TracingExample.java - Tracing system demonstration with console output - Shows traces,
 * spans, and error handling - No API key required
 *
 * <p>8. AgentWithTracingExample.java - Real agent execution with tracing - Demonstrates handoff
 * with trace output - Shows what traces look like for actual agents
 *
 * <p>To run any example: OPENAI_API_KEY=sk-... java com.openai.agents.examples.<ExampleName>
 *
 * <p>Or using Maven: OPENAI_API_KEY=sk-... mvn exec:java
 * -Dexec.mainClass="com.openai.agents.examples.<ExampleName>"
 *
 * <p>Requirements: - OPENAI_API_KEY environment variable must be set - Internet connection for
 * OpenAI API calls - Java 21 or higher
 */
public class HelloWorld {

  public static void main(String[] args) {
    System.out.println("=== OpenAI Agents SDK - Examples ===\n");
    System.out.println("Welcome to the OpenAI Agents SDK for Java!\n");
    System.out.println("This package contains several example programs:");
    System.out.println();
    System.out.println("1. BasicTextOutput");
    System.out.println("   - Simplest example with text-based interaction");
    System.out.println("   - Run: java com.openai.agents.examples.BasicTextOutput");
    System.out.println();
    System.out.println("2. StructuredOutput");
    System.out.println("   - JSON Schema structured output example");
    System.out.println("   - Run: java com.openai.agents.examples.StructuredOutput");
    System.out.println();
    System.out.println("3. MultiTurnExecution");
    System.out.println("   - Multi-turn execution with detailed tracking");
    System.out.println("   - Run: java com.openai.agents.examples.MultiTurnExecution");
    System.out.println();
    System.out.println("4. AgentConfiguration");
    System.out.println("   - Various configuration options and patterns");
    System.out.println("   - Run: java com.openai.agents.examples.AgentConfiguration");
    System.out.println();
    System.out.println("5. MemorySessionExample");
    System.out.println("   - In-memory session storage for conversation memory");
    System.out.println("   - Run: java com.openai.agents.examples.MemorySessionExample");
    System.out.println();
    System.out.println("6. SQLiteSessionExample");
    System.out.println("   - Persistent SQLite session storage");
    System.out.println("   - Run: java com.openai.agents.examples.SQLiteSessionExample");
    System.out.println();
    System.out.println("7. TracingExample");
    System.out.println("   - Tracing system demonstration (no API key needed)");
    System.out.println("   - Run: java com.openai.agents.examples.TracingExample");
    System.out.println();
    System.out.println("8. AgentWithTracingExample");
    System.out.println("   - Real agent execution with tracing and handoff");
    System.out.println("   - Run: java com.openai.agents.examples.AgentWithTracingExample");
    System.out.println();
    System.out.println("To run an example:");
    System.out.println("  OPENAI_API_KEY=sk-... java com.openai.agents.examples.<ExampleName>");
    System.out.println();
    System.out.println("Or using Maven:");
    System.out.println("  OPENAI_API_KEY=sk-... mvn exec:java \\");
    System.out.println("    -Dexec.mainClass=\"com.openai.agents.examples.<ExampleName>\"");
    System.out.println();
    System.out.println("Documentation: see README.md in this directory");
  }
}
