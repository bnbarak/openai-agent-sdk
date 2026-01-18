Your example is bad because it violates that rule and hides the flow.

Bad example (too many breaks, noisy spacing)

```java
@Test
void testAdd_withNullValues_shouldHandleGracefully() {
    Usage usage1 = Usage.builder()
        .inputTokens(100.0)
        .outputTokens(50.0)
        .build();

    Usage usage2 = Usage.builder()
        .requests(1.0)
        .totalTokens(150.0)
        .build();

    Usage result = usage1.add(usage2);

    assertEquals(1.0, result.getRequests());
    assertEquals(100.0, result.getInputTokens());
    assertEquals(50.0, result.getOutputTokens());
    assertEquals(150.0, result.getTotalTokens());
}
```

Why this is bad

You cannot immediately see the Act line.
The eye has to scan through multiple blank lines that add no meaning.
Spacing is not intentional. It is just empty air.

Good example with your rule
Exactly one space before and one space after the Act

```java
@Test
void add_withNullValues_handlesGracefully() {
    Usage usage1 = Usage.builder()
        .inputTokens(100.0)
        .outputTokens(50.0)
        .build();
    Usage usage2 = Usage.builder()
        .requests(1.0)
        .totalTokens(150.0)
        .build();

    Usage result = usage1.add(usage2);

    assertEquals(1.0, result.getRequests());
    assertEquals(100.0, result.getInputTokens());
    assertEquals(50.0, result.getOutputTokens());
    assertEquals(150.0, result.getTotalTokens());
}
```

The visual contract is now clear

Arrange is dense and uninterrupted
Act is isolated by exactly two spaces
Assert is dense and uninterrupted

That is AAA enforced by whitespace, not comments. We don't need any comment that is not very important
to understand code that is not well written. And NEVER add // Arrange // Act // Assert.

If a test cannot be made readable with only those two spaces, the test is too big and should be split.

## No Comments in Tests

DO NOT add comments inside tests. The code should be self-explanatory through:
- Clear variable names
- Descriptive test names
- Well-structured AAA pattern

Bad example (unnecessary comments):

```java
@Test
void run_callsExecuteRun() {
    // Arrange: Create agent
    Agent<UnknownContext, TextOutput> agent = Agent.builder()
            .name("TestAgent")
            .build();

    // Act: Run the agent
    RunResult result = Runner.run(agent, "Hello");

    // Assert: Verify result
    assertNotNull(result);
}
```

Good example (no comments):

```java
@Test
void run_callsExecuteRun() {
    Agent<UnknownContext, TextOutput> agent = Agent.builder()
            .name("TestAgent")
            .build();

    RunResult result = Runner.run(agent, "Hello");

    assertNotNull(result);
}
```

The only acceptable comment is when explaining WHY something unusual is being done, not WHAT is being done.

## No Print Statements in Tests

DO NOT add print statements (System.out.println, System.err.println) in tests. Tests should be silent and only communicate through assertions.

Bad example:
```java
@Test
void structuredOutput_returnsData() {
    BookList result = agent.process(input);

    System.out.println("Books: " + result.books.size());  // NO!
    assertNotNull(result.books);
}
```

Good example:
```java
@Test
void structuredOutput_returnsData() {
    BookList result = agent.process(input);

    assertNotNull(result.books);
    assertFalse(result.books.isEmpty());
}
```

Use assertions to verify behavior. If debugging is needed, use a debugger, not print statements.
