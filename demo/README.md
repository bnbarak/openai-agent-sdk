# OpenAI Agent SDK - Demo CLIs

Two interactive command-line demos showcasing the OpenAI Agent SDK with real-time address validation.

## Features

- **Async Chat Demo** - Traditional chat with full responses at once
- **Streaming Chat Demo** - Real-time streaming chat with character-by-character output
- **Address Validation Tool** - Validates and standardizes US addresses using Smarty API

## Prerequisites

### Required
- Java 21 or higher
- OpenAI API key

### Optional (for address validation)
- Smarty API credentials (auth-id and auth-token)
  - Get free credentials at https://www.smarty.com/pricing/us-street-api

## Setup

### 1. Set OpenAI API Key (Required)

```bash
export OPENAI_API_KEY='sk-your-key-here'
```

### 2. Set Smarty Credentials (Optional)

To enable address validation, set these environment variables:

```bash
export SMARTY_AUTH_ID='your-auth-id'
export SMARTY_AUTH_TOKEN='your-auth-token'
```

If not set, the address validation tool will be disabled but the chat will still work.

## Build

```bash
cd demo
mvn clean package
```

This creates two executable JARs:
- `target/async-chat-demo.jar` (59MB)
- `target/streaming-chat-demo.jar` (59MB)

## Run

### Async Chat Demo

```bash
java -jar target/async-chat-demo.jar
```

### Streaming Chat Demo

```bash
java -jar target/streaming-chat-demo.jar
```

## Usage

Both demos provide an interactive chat interface:

```
╔════════════════════════════════════════════════════════════╗
║        OpenAI Agent SDK - Async Chat Demo                 ║
╚════════════════════════════════════════════════════════════╝

This demo uses async mode - responses appear all at once.
Type 'exit' or 'quit' to end the conversation.

You > Hello!
Assistant > Hi! How can I help you today?

You > Validate this address: 1600 Amphitheatre Parkway, Mountain View, CA
Assistant > Let me validate that address for you...
[Tool calls address validation API]
The validated address is: 1600 Amphitheatre Pkwy, Mountain View CA 94043-1351

You > exit
Goodbye! 👋
```

### Example Queries

Try these with the demos:

**General questions:**
- "What is the capital of France?"
- "Write a Python function to calculate fibonacci numbers"
- "Explain how HTTP works"

**Address validation (if Smarty credentials configured):**
- "Validate 1600 Pennsylvania Ave, Washington DC"
- "Is this a valid address: 123 Main Street, Springfield"
- "Standardize this address: 1 infinite loop cupertino"

## Tool Details

### Address Validation Tool

The `validate_address` tool integrates with the Smarty US Street API to:

- Validate US street addresses
- Standardize formatting (proper capitalization, abbreviations)
- Add missing ZIP+4 codes
- Provide delivery information
- Return latitude/longitude coordinates (in API response)

**Input parameters:**
- `street` (required) - Street address
- `city` (optional) - City name
- `state` (optional) - State abbreviation (e.g., "CA")
- `zipCode` (optional) - ZIP code

**Output:**
- `valid` - Whether address is valid
- `message` - Status message
- `validatedAddress` - Standardized full address
- `city` - Validated city name
- `state` - State abbreviation
- `zipCode` - ZIP+4 code

## Architecture

Both demos:
- Use `Agent.builder()` to create agents with tools
- Use `MemorySession` for conversation history
- Use `Runner.run()` or `Runner.runStreamed()` for execution
- Implement the `FunctionTool` interface for address validation

The streaming demo uses `ReadableStreamAsyncIterator` to process text chunks in real-time.

## Troubleshooting

**"OPENAI_API_KEY environment variable not set"**
- Ensure you've exported the API key: `export OPENAI_API_KEY='sk-...'`

**Address validation not working**
- Check that `SMARTY_AUTH_ID` and `SMARTY_AUTH_TOKEN` are set
- Verify credentials at https://www.smarty.com/account
- The tool will show a warning on startup if credentials are missing

**Build fails**
- Ensure Java 21+ is installed: `java -version`
- Run `mvn clean` before building again

## Learn More

- [OpenAI Agent SDK Documentation](https://bnbarak.github.io/openai-agent-sdk/)
- [Smarty US Street API Docs](https://www.smarty.com/docs/cloud/us-street-api)
- [OpenAI Java SDK](https://github.com/openai/openai-java)
