# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/` contains the SDK implementation.
- `src/main/java/ai/acolite/agentsdk/examples/` holds runnable examples (for docs and demos).
- `src/test/java/` contains unit and integration tests.
- `docs/` and `mkdocs.yml` drive documentation content and site structure.
- `target/` is build output (generated; do not commit).

## Build, Test, and Development Commands
- `mvn clean install` builds the library and runs the default test suite.
- `mvn test` runs unit tests only (fast).
- `mvn verify -Pe2e` runs unit + integration + e2e tests (expects real API access).
- `mvn spotless:check` verifies formatting and import order.
- `mvn spotless:apply` formats code using Google Java Format.
- Avoid running `src/main/java/ai/acolite/agentsdk/examples/RealWorldRunContextExample.java` and `@Tag("realworld")` tests unless you are doing an e2e run; they are slow and costly.

## Coding Style & Naming Conventions
- Java 21; formatting is enforced by Spotless (Google Java Format). Do not hand-format.
- Checkstyle runs during `verify`; fix violations instead of suppressing them.
- Prefer positive conditions, early returns, and flat control flow.
- Favor immutable value objects with Lombok (`@Value`, `@Builder`); use `@Builder.Default` for defaults.
- Avoid `Optional` as a field type; use it at API boundaries only.
- Use tight visibility (`private` by default) and descriptive, behavior-focused method names.

## Testing Guidelines
- Frameworks: JUnit Jupiter, Mockito, JUnit Pioneer.
- Tests live in `src/test/java/` and should use descriptive method names like
  `add_withNullValues_handlesGracefully`.
- Follow AAA with whitespace: one blank line before and after the Act section.
- No comments or print statements in tests; clarity comes from structure and naming.
- Tag longer-running tests with `@Tag("integration")` or `@Tag("e2e")`.
- Tag real-world, costly tests with `@Tag("realworld")` and only run them in `-Pe2e`.

## Commit & Pull Request Guidelines
- No established commit message convention yet; use short, imperative summaries (e.g., "Add tracing span").
- PRs should describe intent, note tests run (e.g., `mvn test`), and include relevant example updates.

## Configuration & Security
- Set `OPENAI_API_KEY` before running examples or e2e tests.
- See `SECURITY.md` for vulnerability reporting guidance.
