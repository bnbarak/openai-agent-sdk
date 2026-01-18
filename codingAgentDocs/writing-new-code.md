# Writing New Code

This document defines the workflow for adding new code to the project.

Follow these steps in order to ensure quality and consistency.

---

## Required Steps

### 1. Read the Style Guides

Before writing any code, read:
- `codingAgentDocs/java.md` - Java code style and best practices
- `codingAgentDocs/unittest.md` - Unit test structure and patterns

These define how we write code and tests.

### 2. Create Branch from Main

Always branch from main and always verify where we are with `git status`:

```bash
git checkout main
git pull
git checkout -b feature/your-feature-name
```

### 3. Write Code Following Java Style Guide

Follow all rules from `java.md`:
- Return early
- Avoid nested conditionals
- Use Lombok builders for data objects
- Keep classes tight and focused
- Small methods, one concept each
- Names describe what it does, not how

### 4. Add Unit Tests

**Every new class or method must have unit tests.**

Follow patterns from `unittest.md`:
- Use AAA pattern (Arrange-Act-Assert)
- Exactly one blank line before Act, one after
- No comments in tests
- No print statements
- Self-explanatory code through naming

Example:
```java
@Test
void add_withValidInputs_returnsSum() {
    Calculator calc = new Calculator();

    int result = calc.add(2, 3);

    assertEquals(5, result);
}
```

### 5. Add Real-Life Tests

Include integration tests or examples that demonstrate real-world usage:
- Test actual API calls if applicable
- Test with realistic data
- Verify behavior in production-like scenarios

### 6. Add Examples

Provide usage examples:
- Add example code in test classes or separate example classes
- Document common use cases
- Show both simple and complex scenarios

### 7. Run Spotless

Before committing, format code with Spotless:

```bash
mvn spotless:apply
```

Fix any issues:
```bash
mvn spotless:check
```

### 8. Run Tests

Verify all tests pass:

```bash
mvn test
```

Fix any failures before proceeding.

### 9. Create Pull Request

Follow the PR creation process from `git-workflow.md`:

```bash
# Commit changes
git add <files>
git commit -m "$(cat <<'EOF'
Add feature X

Implements Y to solve Z problem.

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
EOF
)"

# Push branch
git push -u origin feature/your-feature-name

# Create PR
gh pr create --title "Add feature X" --body "$(cat <<'EOF'
## Summary
- Implement feature X
- Add comprehensive unit tests
- Add usage examples

## Test plan
- [ ] Run full test suite
- [ ] Verify examples work
- [ ] Test real-world scenarios

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

---

## Checklist

Before creating a PR, verify:

- [ ] Read `java.md` and `unittest.md`
- [ ] Branched from main
- [ ] Code follows Java style guide
- [ ] Unit tests added for all new code
- [ ] Real-life tests/integration tests added
- [ ] Usage examples provided
- [ ] Spotless applied and passes
- [ ] All tests pass
- [ ] PR created with proper summary and test plan

---

## Quick Reference

```bash
# Start
git checkout main && git pull
git checkout -b feature/new-feature

# Write code + tests

# Format and test
mvn spotless:apply
mvn test

# Commit and PR
git add <files>
git commit -m "..."
git push -u origin feature/new-feature
gh pr create --title "..." --body "..."
```

---
