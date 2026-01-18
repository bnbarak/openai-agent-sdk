# Git Workflow Guide

This document explains how to commit changes, push to remote, create GitHub pull requests, and address PR comments.

---

## Safety Rules

- **NEVER** update git config
- **NEVER** run destructive commands (`push --force`, `reset --hard`) unless explicitly requested
- **NEVER** skip hooks (`--no-verify`, `--no-gpg-sign`) unless explicitly requested
- **NEVER** force push to main/master (warn user if requested)
- **NEVER** use `git commit --amend` unless explicitly requested
- **NEVER** use interactive git commands with `-i` flag
- **NEVER** commit without explicit user request

---

## Committing Changes

Only commit when explicitly requested by the user.

### Workflow

1. **Analyze changes** - Run in parallel:
```bash
git status     # See untracked files (NEVER use -uall)
git diff       # See staged and unstaged changes
git log        # See recent commits for style consistency
```

2. **Draft commit message**:
   - Summarize nature of changes (new feature, bug fix, refactoring, etc.)
   - Focus on "why" rather than "what"
   - Keep it concise (1-2 sentences)
   - Use accurate verbs: "add" = new, "update" = enhancement, "fix" = bug fix

3. **Security check** - Do not commit secrets (.env, credentials.json, etc.)

4. **Create commit**:
```bash
git add <files>

git commit -m "$(cat <<'EOF'
Your commit message here.

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
EOF
)"

git status    # Verify success
```

**Key points:**
- ALWAYS use HEREDOC for commit messages
- Run `git status` AFTER commit completes (not in parallel)
- If pre-commit hook fails: fix issue and create NEW commit (never skip hooks)

### Example

Good:
```bash
git add src/service/QuoteService.java tests/QuoteServiceTest.java

git commit -m "$(cat <<'EOF'
Add validation for employee count in quote requests

Prevents negative or zero employee counts from being processed,
which was causing downstream calculation errors.

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
EOF
)"
```

Bad:
```bash
git add . && git commit -m "updates"
```

---

## Pushing to Remote

Only push when explicitly requested by the user.

```bash
# For new branches
git push -u origin <branch-name>

# For existing tracked branches
git push
```

Never use `--force` unless explicitly requested.

---

## Creating Pull Requests

### Step 1: Analyze Branch

Run in parallel:
```bash
git status                              # Untracked files
git diff                                # Staged/unstaged changes
git branch -vv                          # Remote tracking status
git log <base-branch>..HEAD             # ALL commits in PR
git diff <base-branch>...HEAD           # Full diff from base
```

**CRITICAL**: Analyze ALL commits that will be included, not just the latest.

### Step 2: Draft PR Summary

Include:
1. **Summary** - 1-3 bullet points describing changes
2. **Test plan** - Markdown checklist of testing steps
3. **Attribution** - Claude Code footer

### Step 3: Create PR

```bash
# Push branch if needed
git push -u origin <branch-name>

# Create PR with HEREDOC
gh pr create --title "descriptive pr title" --body "$(cat <<'EOF'
## Summary
- First major change
- Second major change
- Third major change

## Test plan
- [ ] Verify feature X works
- [ ] Run unit tests
- [ ] Test edge case Y

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

**Important:**
- DO NOT use TodoWrite or Task tools during PR creation
- Return PR URL to user when done

---

## Working with GitHub

Use `gh` command for all GitHub operations.

### View PR

```bash
gh pr view <pr-number>              # View PR details
gh pr status                         # View PR status/checks
gh pr diff <pr-number>               # View PR diff
```

### View PR Comments

```bash
gh api repos/<owner>/<repo>/pulls/<pr-number>/comments
# Or
gh pr view <pr-number>
```

---

## Addressing PR Comments

### Workflow

1. **Fetch comments**:
```bash
gh pr view <pr-number>
```

2. **Address each comment**:
   - Read comment carefully
   - Make requested changes
   - Create NEW commit addressing feedback
   - Reference feedback in commit message

3. **Push updates**:
```bash
git commit -m "$(cat <<'EOF'
Address PR feedback: improve error messages

Updated validation error messages to be more specific,
as suggested in review.

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
EOF
)"

git push
```

4. **Reply to comments** (optional):
```bash
gh pr comment <pr-number> --body "Updated in commit abc123"
```

PR automatically updates with new commits.

---

## Best Practices

1. Only commit when explicitly requested
2. Always analyze with `git status` and `git diff` before committing
3. Write clear messages explaining why, not what
4. Always include Co-Authored-By attribution
5. Use HEREDOC for commit messages and PR descriptions
6. Never skip hooks or force push unless requested
7. Analyze ALL commits when creating PRs
8. Include test plan in PR descriptions
9. Create NEW commits for PR feedback (never amend)
10. Never use interactive git commands (`-i` flag)

---
