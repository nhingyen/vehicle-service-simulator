# Shared AI Agent Instructions

This file contains the shared project instructions for Codex and Claude Code. Read [`docs/PROJECT.md`](docs/PROJECT.md), [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md), and the assigned task before changing code.

- Work only on the requested task; do not start the next task automatically.
- Inspect the project and Git state before editing. Check the actual package name, SDK versions, Gradle configuration, and dependencies instead of assuming them.
- Keep changes small and readable, consistent with the existing Kotlin and Jetpack Compose project. Add dependencies or architectural layers only when the task needs them.
- Preserve other people's existing changes. Use `develop` for routine work; if another branch is checked out, ask before switching. Commit and push only after explicit user confirmation.
- Run build and test checks appropriate to the completed task, and report the result or why a check could not run.
- Before finishing, check that documentation matches the code and report the changed files.

## Token-efficient Agent Workflow

- Read only files relevant to the active task.
- Do not repeatedly summarize known project context.
- For small, well-scoped tasks, plan briefly and implement.
- Ask questions only when blocked by important ambiguity.
- Keep final reports concise.
- Avoid generating unnecessary documentation.
- Prefer targeted builds/tests over redundant checks.
- Reuse existing hooks and GitHub CI.
- Explain code in detail only when requested.

## Git and GitHub Workflow

### Branch Strategy

- `main` is the stable branch; `develop` is the primary development branch.
- Work directly on `develop` by default. Do not create a feature branch unless the user explicitly requests one.
- Keep `main` unchanged during routine task work.

### Commit Convention

Propose a Conventional Commit message for each completed task. Use the prefix that matches the change:

- `feat:` New functionality
- `fix:` Bug fixes
- `refactor:` Code restructuring without behavior changes
- `test:` Tests
- `docs:` Documentation
- `chore:` Project configuration or maintenance

Examples: `feat: implement vehicle dashboard`, `docs: define git workflow`.

### After Each Task

1. Run the relevant build and test checks. Report the commands, results, and any check that was skipped or blocked.
2. Inspect `git status`, unstaged and staged diffs, and new files. Preserve unrelated work already in the tree or index.
3. Prepare a specific list of task files to commit, a proposed Conventional Commit message, and a short summary of changes and risks. Do not stage files yet.
4. Wait for explicit user confirmation before staging, committing, or pushing. Approval for one action does not imply approval for another.
5. If both commit and push are approved, stage only the approved files, commit, and push to `develop`. Report the commit hash and push result.

### GitHub CLI

- Use `gh` for GitHub Issues or Pull Requests only when the user requests that work. Check that the CLI is available and authenticated first; report a missing CLI or invalid login.
- Do not create, close, or merge Issues or Pull Requests on your own.
- Do not merge, reset, rebase, force push, or rewrite Git history as part of routine task completion. Perform these actions only when explicitly requested.
- Never commit credentials, local tokens or generated build artifacts.

### Agent Collaboration

- These Git rules apply equally to Codex and Claude Code.
- Do not let multiple agents modify the same working tree simultaneously.
- Keep architecture decisions and task status in repository documentation.
