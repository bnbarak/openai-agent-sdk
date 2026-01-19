# OpenAI Agent SDK - Demo CLIs

Interactive CLI demos showcasing async and streaming chat using the SDK.

## Setup

```bash
export OPENAI_API_KEY='your-api-key-here'
cd demo
mvn clean package
```

## Run

**Async Demo** (responses appear all at once):
```bash
java -jar target/async-chat-demo.jar
```

**Streaming Demo** (real-time token streaming):
```bash
java -jar target/streaming-chat-demo.jar
```

Type `exit` or `quit` to end the conversation.
