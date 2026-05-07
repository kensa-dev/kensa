---
sidebar_position: 8
description: Teach your AI coding agent (Claude Code, OpenCode, and others) to write idiomatic Kensa BDD tests using the kensa-development skill.
---

# AI Agent Skills

The [`kensa-dev/agent-skills`](https://github.com/kensa-dev/agent-skills) repository ships AI coding skills that teach your agent how to write and review idiomatic Kensa tests. Works with [Claude Code](https://claude.ai/code), [OpenCode](https://opencode.ai), and any agent that understands the `SKILL.md` format.

## What the skill does

The `kensa-development` skill is an expert reviewer for Kensa tests. It detects violations of Kensa best practices, produces an improved version of your test, and explains the changes. It checks for:

- Rendered code that reads as fluent English
- Action lambdas that don't belong in rendered contexts
- Correct use of `Fixtures` and `@RenderedValue`
- Composable test toolboxes built from small setup steps
- Raw assertions wrapped in semantic functions
- Typed context objects for multi-stub tests

Reference material covers Fixtures, `@RenderedValue`, setup steps, interactions and sequence diagrams, and captured outputs.

## Installation

### Claude Code

```
/plugin marketplace add kensa-dev/agent-skills
/plugin install kensa
```

The plugin auto-updates when you run `/plugin marketplace update`, so your agent's knowledge of Kensa stays in sync with releases.

### OpenCode and other agents

Any agent that reads `.claude/skills/` can use the skill content directly. Drop it into your project:

```bash
git clone --depth 1 https://github.com/kensa-dev/agent-skills.git /tmp/kensa-skills && \
  mkdir -p .claude/skills && \
  cp -r /tmp/kensa-skills/plugins/kensa/skills/kensa-development .claude/skills/ && \
  rm -rf /tmp/kensa-skills
```

Re-run when a new Kensa version drops.

### JetBrains AI Assistant, GitHub Copilot, Cursor, and others

These tools don't auto-discover `SKILL.md` files, but the skill body is plain markdown — paste it (or symlink it) into your tool's instruction file:

- **GitHub Copilot** — `.github/copilot-instructions.md` (or `.github/instructions/kensa.instructions.md` with `applyTo: "**/*.kt"`)
- **JetBrains AI Assistant** — Settings → Tools → AI Assistant → Custom Instructions
- **Cursor** — `.cursor/rules/kensa.mdc`
- **Direct Anthropic / OpenAI SDK** — load the file and prepend to your `system` prompt

## Versioning

The skill version mirrors Kensa `version.txt` exactly. Tag `0.7.1` corresponds to Kensa `0.7.1`, so installing a specific Kensa version's skill teaches your agent the API surface for that release.
