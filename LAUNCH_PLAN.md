# Launch Plan: OpenAI Agent SDK

Checklist to ship a Maven library on GitHub (modern, low overhead)

---

## Stage 1: Foundation & Cleanup

### 1.1. Repo Basics

- [ ] Pick stable Maven coordinates in `pom.xml`
  - [ ] `groupId`
  - [ ] `artifactId`
  - [ ] `version` using SemVer
- [ ] Add project metadata in `pom.xml`
  - [ ] `name`
  - [ ] `description`
  - [ ] `url`
  - [ ] `licenses`
  - [ ] `developers`
  - [ ] `scm`
- [ ] Add required root files
  - [ ] `.gitignore`
  - [ ] `README.md`
  - [ ] `LICENSE`
  - [ ] `SECURITY.md`

### 1.2. Clean Old Code Agent `.md` Files

**Goal:** prevent stale AI instructions from rotting the repo.

- [ ] Audit existing `.md` files created for code agents
  - [ ] Old refactor instructions
  - [ ] Temporary prompts
  - [ ] One off investigation notes
- [ ] Decide for each file
  - [ ] Delete it
  - [ ] Move it to `docs/decisions/`
  - [ ] Rewrite it as permanent documentation
- [ ] Enforce rules going forward
  - [ ] Agent instructions must be short lived unless promoted
  - [ ] Permanent agent guidance lives under `docs/agents/`
  - [ ] No agent instructions in the repo root
- [ ] Optional but recommended
  - [ ] Add a `docs/agents/README.md` explaining scope and lifecycle
  - [ ] Add a CI check that blocks new root level `.md` files except allowlist

**This keeps AI generated docs intentional instead of accidental.**

### 1.3. API Surface Area and Public Entrypoint

**Goal:** Expose it through the package structure and one clean public entrypoint. You do not want users browsing your folders. You want them importing a small surface area.

#### Pick the public API packages

- [ ] Decide on public API packages:
  - [ ] `ai.acolite.agentsdk` - public entrypoint
  - [ ] `ai.acolite.agentsdk.core` - stable public types that users import often
  - [ ] `ai.acolite.agentsdk.openai` - provider types if users need direct provider control
  - [ ] `ai.acolite.agentsdk.extensions` - optional add ons
  - [ ] `ai.acolite.agentsdk.exceptions` - your public exceptions

**Not recommended as public API:**
- `ai.acolite.agentsdk.examples` - should not be depended on
- `ai.acolite.agentsdk.realtime` - should not be a package users import from. Keep this as docs only, or move real types to `extensions.realtime`

#### Create a single front door class

- [ ] Create `ai.acolite.agentsdk.AgentSdk` or `AcoliteAgentSdk` class
- [ ] Add static factory methods for common operations

Example shape:

```java
package ai.acolite.agentsdk;

import ai.acolite.agentsdk.core.Agent;
import ai.acolite.agentsdk.core.AgentRunner;
import ai.acolite.agentsdk.openai.OpenAiClient;

public final class AgentSdk {

  private AgentSdk() {}

  public static AgentRunner runner() {
    return AgentRunner.defaultRunner();
  }

  public static OpenAiClient openai(OpenAiClient.Config config) {
    return OpenAiClient.create(config);
  }
}
```

This keeps discovery simple and prevents import soup.

#### Make examples not ship as part of the library

- [ ] Move examples to separate Maven module: `agentsdk-examples`
  - [ ] `agentsdk-core` - published
  - [ ] `agentsdk-examples` - not published or published separately as a demo artifact
- [ ] See **Stage 5.3** for the complete modern approach to examples using `@snippet` tags

#### Package documentation

- [ ] Add `package-info.java` to each public package
- [ ] Create internal package for non-public code: `ai.acolite.agentsdk.internal`
- [ ] Refactor or relocate `realtime` package (docs only or `extensions.realtime`)

Example `ai.acolite.agentsdk.core` package info:

```java
/**
 * Core SDK types: agents, tools, memory, runner, tracing.
 *
 * <p>Most users only need this package plus {@code ai.acolite.agentsdk}.</p>
 */
package ai.acolite.agentsdk.core;
```

---

## Stage 2: Code Quality & Formatting

### 2.1. Introduce Spotless

**Goal:** Enforce consistent code formatting automatically. Spotless fails the build on unformatted code, keeping your codebase clean without manual work.

**CRITICAL:** Spotless check must be a required status check that blocks merges to main. No unformatted code should ever reach main.

- [ ] Add Spotless Maven plugin to `pom.xml` with pinned versions
- [ ] Configure Google Java Format or custom formatting rules
- [ ] Set up `licenseHeader` check for file headers
- [ ] Add Maven target to format code: `mvn spotless:apply`
- [ ] Configure `spotless:check` to run in `verify` phase
- [ ] Configure Spotless to fail build on any formatting diff
- [ ] Document formatting commands in README
  - [ ] How to format: `mvn spotless:apply`
  - [ ] How to check: `mvn spotless:check`
  - [ ] Warning that PRs will be blocked if formatting fails

Example minimal `pom.xml` configuration:

```xml
<plugin>
  <groupId>com.diffplug.spotless</groupId>
  <artifactId>spotless-maven-plugin</artifactId>
  <version>2.43.0</version>
  <configuration>
    <java>
      <googleJavaFormat>
        <version>1.17.0</version>
      </googleJavaFormat>
    </java>
  </configuration>
  <executions>
    <execution>
      <goals>
        <goal>check</goal>
      </goals>
      <phase>verify</phase>
    </execution>
  </executions>
</plugin>
```

### 2.2. Maven Build Hygiene

- [ ] Unit tests using Surefire
- [ ] Clear test separation
  - [ ] Unit tests run by default
  - [ ] Real world API tests behind a Maven profile like `e2e`
- [ ] Fail build on formatting or test failure
  - [ ] `mvn verify` must fail if Spotless check fails
  - [ ] `mvn verify` must fail if tests fail
- [ ] Pin all plugin versions in `pom.xml`

**Local development workflow:**
1. Developer makes changes
2. Run `mvn spotless:apply` to format code before committing
3. Run `mvn verify` to ensure tests pass and formatting is correct
4. Commit and push
5. PR checks will fail if formatting was missed

---

## Stage 3: Security First

### 3.1. Git Leak Detection

**Goal:** never leak secrets.

- [ ] Add Gitleaks GitHub Action
- [ ] Run on PRs and main branch pushes
- [ ] Fetch full history
- [ ] Fail the build on detection

Example `.github/workflows/gitleaks.yml`:

```yaml
name: gitleaks
on: [push, pull_request]
jobs:
  scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
      - uses: gitleaks/gitleaks-action@v2
```

---

## Stage 4: CI/CD Foundation

### 4.1. GitHub Actions for Pull Requests

**Goal:** fast feedback, zero secrets, blocks bad merges.

- [ ] Create PR workflow `.github/workflows/pr.yml`
- [ ] Run unit tests
- [ ] Run Spotless check (`mvn spotless:check`) - **must fail if any formatting diff**
- [ ] Optionally run Javadoc lint
- [ ] Optionally run dependency review

**Required checks:**
- [ ] Tests pass
- [ ] Spotless check passes (blocks merge if formatting is incorrect)
- [ ] Gitleaks passes

Example `.github/workflows/pr.yml`:

```yaml
name: PR Checks
on:
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run Spotless check
        run: mvn spotless:check

      - name: Run tests
        run: mvn verify
```

**Important:** The Spotless check runs `mvn spotless:check` which will exit with non-zero status if any files need formatting. This prevents unformatted code from being merged.

### 4.2. Manual GitHub Action for E2E Tests

**Goal:** controlled execution, explicit intent.

- [ ] Create workflow `.github/workflows/e2e.yml`
- [ ] Triggered via `workflow_dispatch`
- [ ] Uses Maven profile like `e2e`
- [ ] Configure required secrets
- [ ] Add concurrency guard to avoid overlapping runs

**This is where real APIs and end to end examples live.**

**E2E workflow should run:**
- Expensive example tests (see **Stage 5.3** for example testing approach)
- Real API integration tests requiring secrets
- Long-running scenarios not suitable for PR checks

---

## Stage 5: Documentation

### 5.1. Modern Documentation with MkDocs

**Goal:** docs people actually read.

- [ ] Set up MkDocs with [mkdocs-shadcn](https://github.com/asiffer/mkdocs-shadcn) theme
  - [ ] Install: `pip install mkdocs-shadcn`
  - [ ] Configure in `mkdocs.yml`: `theme: name: shadcn`
  - [ ] Modern, clean UI inspired by shadcn/ui
- [ ] README is the entry point
- [ ] Create `docs/` directory structure
  - [ ] `docs/getting-started.md`
  - [ ] `docs/api-surface.md` - curated package map
  - [ ] `docs/guides/`
  - [ ] `docs/examples/` - links to or includes from `agentsdk-examples` (see **Stage 5.3**)
- [ ] Configure Javadoc generation into `/api`
- [ ] Set up GitHub Pages deployment via Actions
- [ ] Add explicit references section in docs
- [ ] Document rule: AI generated docs must be reviewed before merge
- [ ] Add mkdocs-include-markdown-plugin for embedding example code directly

Example `mkdocs.yml` configuration:

```yaml
site_name: OpenAI Agent SDK
site_url: https://your-org.github.io/agentsdk/
repo_url: https://github.com/your-org/agentsdk
repo_name: your-org/agentsdk

theme:
  name: shadcn

plugins:
  - search
  - include-markdown

nav:
  - Home: index.md
  - Getting Started: getting-started.md
  - API Surface: api-surface.md
  - Guides: guides/
  - Examples: examples/
  - API Reference: api/
```

Example `docs/api-surface.md` structure:
- `agentsdk` - entrypoint
- `core` - stable primitives
- `openai` - provider implementation
- `extensions` - opt in features
- `exceptions` - public error types

### 5.2. Javadoc Setup

- [ ] Configure Javadoc plugin in `pom.xml`
- [ ] Add overview Javadoc for the whole SDK
- [ ] Ensure all public APIs have Javadoc
- [ ] Configure Javadoc to fail on warnings (optional)
- [ ] Enable JDK 18+ `@snippet` tag support for including code examples

### 5.3. Examples: Modern Approach

**Goal:** Keep examples as real, runnable Java code that's automatically included in docs. Never copy-paste. Never let examples go stale.

**The modern way uses JDK 18+ `@snippet` tag ([JEP 413](https://openjdk.org/jeps/413)) to pull code directly from source files into Javadoc.**

#### A. Keep example source as real Java files

- [ ] Move examples to separate Maven module: `agentsdk-examples`
  - [ ] Module compiles against published SDK
  - [ ] Contains runnable example mains
  - [ ] Contains tests that run them
  - [ ] Docs reference these sources
- [ ] This prevents `ai.acolite.agentsdk.examples` from becoming accidental API

**Why separate module?**
- Examples don't bloat the published JAR
- Examples can't become accidentally supported API
- Clean separation between SDK and demonstrations

#### B. Use `@snippet` tag to include code in Javadoc

**Pattern 1: Include a region from example source**

In your example file, mark regions:

```java
// region:tracing-create-trace
Trace trace = Trace.builder()
    .traceId(TracingUtils.generateTraceId())
    .name("Example workflow")
    .groupId(TracingUtils.generateGroupId())
    .metadata(Map.of(
        "user", "demo-user",
        "environment", "example"
    ))
    .processor(processor)
    .build();
// endregion:tracing-create-trace
```

In the Javadoc for your public API, include it:

```java
/**
 * Creates a trace.
 *
 * {@snippet file="ai/acolite/agentsdk/examples/TracingExample.java"
 *   region="tracing-create-trace"}
 */
```

**Pattern 2: Include whole example file**

For small examples:

```java
/**
 * Full tracing example.
 *
 * {@snippet file="ai/acolite/agentsdk/examples/TracingExample.java"}
 */
```

**Checklist:**

- [ ] Add region markers around key parts of examples
- [ ] Use `@snippet` in public API Javadoc to embed regions
- [ ] Configure Javadoc plugin to find example sources
- [ ] Verify snippets render correctly in generated Javadoc

#### C. Make sure examples actually run

**Never rely on humans running examples manually. Automate it.**

**Option 1: JUnit test that launches example main (recommended for fast examples)**

```java
@Test
void tracingExampleRuns() {
    TracingExample.main(new String[0]);
}
```

**Option 2: Separate Maven profile for expensive examples**

- [ ] Create `e2e` Maven profile
- [ ] Put expensive examples behind this profile
- [ ] Run in manual GitHub Action workflow only
- [ ] Fast PR checks, slow intentional runs

**Option 3: Exec plugin to run examples as processes**

For examples that need full process isolation.

**Checklist:**

- [ ] Add JUnit tests for fast examples
- [ ] Put expensive examples in `e2e` Maven profile
- [ ] Verify all examples run successfully
- [ ] Include example tests in PR checks or E2E workflow

#### D. MkDocs integration: Show examples without copy-paste

**Approach 1: Link to the exact file on GitHub (cheapest, effective)**

Your docs page:
- Shows snippet via `@snippet` in Javadoc
- Links to full file on GitHub for complete example

```markdown
## Tracing Example

See the [complete example on GitHub](https://github.com/your-org/agentsdk/blob/main/agentsdk-examples/src/main/java/ai/acolite/agentsdk/examples/TracingExample.java).
```

**Approach 2: Include file content directly in MkDocs**

Use [mkdocs-include-markdown-plugin](https://github.com/mondeja/mkdocs-include-markdown-plugin):

```markdown
## Tracing Example

```java
--8<-- "agentsdk-examples/src/main/java/ai/acolite/agentsdk/examples/TracingExample.java"
```
```

**Checklist:**

- [ ] Choose approach: GitHub links or include plugin
- [ ] If using include plugin, add to MkDocs config
- [ ] Create `docs/examples/` directory
- [ ] Link or include all example files
- [ ] Verify docs render examples correctly

#### E. Summary: One source of truth

**This setup ensures:**

1. ✅ Examples are real Java code that compiles
2. ✅ Examples are tested (they actually run)
3. ✅ Javadoc shows examples via `@snippet` (no copy-paste)
4. ✅ MkDocs shows examples via links or includes (no copy-paste)
5. ✅ Examples cannot go stale
6. ✅ Examples are not part of public API surface

**Workflow:**

1. Developer writes example in `agentsdk-examples/`
2. Adds region markers for key sections
3. Adds `@snippet` references in public API Javadoc
4. Adds test to verify example runs
5. MkDocs links to or includes the example
6. Result: One source of truth, always in sync

**Required tools:**

- [ ] JDK 18+ for `@snippet` support
- [ ] Maven Javadoc plugin configured to find examples
- [ ] JUnit for example tests
- [ ] Optional: mkdocs-include-markdown-plugin for embedding

---

## Stage 6: Release & Publishing

### 6.1. Maven Publishing Setup

**Goal:** tag driven, repeatable releases.

- [ ] Add `distributionManagement` to `pom.xml`
- [ ] Use GitHub Packages as the registry
- [ ] Create release workflow `.github/workflows/release.yml`
  - [ ] Triggered by tags like `v1.2.3`
  - [ ] Runs `mvn deploy`
  - [ ] Uses `GITHUB_TOKEN`, no local credentials required

**Release flow:**
1. Merge to main
2. Bump version
3. Tag
4. Push tag
5. Package is published

### 6.2. Branch Protection

**Goal:** main is always releasable. No unformatted code should ever reach main.

- [ ] Protect `main` branch
- [ ] Require PRs
- [ ] Require status checks to pass before merging
  - [ ] **Required check: `PR Checks / build` (includes Spotless)**
  - [ ] **Required check: `gitleaks / scan`**
  - [ ] Any other CI checks
- [ ] Require at least one review
- [ ] Disable force pushes
- [ ] Enable "Require branches to be up to date before merging"

**Setup in GitHub:**
1. Go to Settings → Branches → Add branch protection rule
2. Branch name pattern: `main`
3. Enable "Require a pull request before merging"
4. Enable "Require status checks to pass before merging"
5. Search for and add required checks: `build`, `scan` (Gitleaks)
6. Enable "Require branches to be up to date before merging"
7. Enable "Do not allow bypassing the above settings"

This ensures that all code merged to main has passed Spotless formatting checks.

---

## Stage 7: Maintenance & Optional

### 7.1. Dependabot

**Goal:** keep dependencies fresh without manual work.

- [ ] Enable Dependabot in repository settings
- [ ] Configure `.github/dependabot.yml`
  - [ ] Enable Maven updates
  - [ ] Enable GitHub Actions updates
  - [ ] Set weekly cadence
- [ ] Require PR checks to pass before merge

### 7.2. Optional but Strongly Recommended

- [ ] CodeQL for Java security scanning
- [ ] Dependency review action
- [ ] Sources jar and Javadoc jar publication
- [ ] Changelog strategy
  - [ ] Conventional commits, or
  - [ ] Release notes from PR titles
- [ ] License header enforcement via Spotless

---

## Mental Model (this matters)

* PR checks are fast and cheap
* E2E is slow and intentional
* Docs are generated but reviewed
* Releases are boring and repeatable
* AI artifacts are cleaned, not accumulated

---

## Progress Tracking

### Stage Completion Checklist

- [ ] Stage 1: Foundation & Cleanup
- [ ] Stage 2: Code Quality & Formatting
- [ ] Stage 3: Security First
- [ ] Stage 4: CI/CD Foundation
- [ ] Stage 5: Documentation
- [ ] Stage 6: Release & Publishing
- [ ] Stage 7: Maintenance & Optional

---

## Next Actions

When ready to implement, choose a stage to start:

1. **Start with Stage 1** - Clean up the repo and define public API
2. **Stage 2 next** - Write the Spotless config and enforce formatting
3. **Stage 3 early** - Add Gitleaks before you accidentally commit secrets
4. **Stages 4-7** - Build out automation incrementally

Specific implementation tasks available:
* Generate the **exact repo file tree**
* Write the **Spotless config** with license headers
* Create a **CI rule to block new root `.md` files**
* Define a **docs promotion workflow for AI generated content**
* Set up **GitHub Actions for PR checks**
