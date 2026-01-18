# Troubleshooting

Common issues and quick fixes when running the SDK locally.

## API Key Not Found

If you see an error about a missing API key, export it before running examples:

```bash
export OPENAI_API_KEY="your-api-key-here"
```

## Java Version Mismatch

This SDK requires Java 21+:

```bash
java -version
```

If you're on an older version, update your JDK and ensure `JAVA_HOME` points to it.

## Model Not Available

If a model name is rejected, confirm it is available for your account and matches the provider:

- Prefer documented model IDs from OpenAI.
- If you configured a custom model provider, verify its naming rules.

## Build or Example Failures

If examples fail to compile or run:

```bash
mvn clean install
```

If you only want unit tests:

```bash
mvn test
```

## Still Stuck?

- Open an issue on GitHub: https://github.com/bnbarak/openai-agent-sdk/issues
- Include the command you ran and the full error output.
