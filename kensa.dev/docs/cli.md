---
sidebar_position: 5
description: The Kensa CLI is a lightweight Go binary that serves HTML test reports over HTTP and exposes your test results to AI agents over MCP, with auto-download via a shell wrapper script.
---

# CLI

The Kensa CLI is a lightweight Go binary that does two jobs: it serves HTML reports over HTTP — useful when your IDE doesn't have a built-in web server — and it runs an [MCP server](#mcp-server) that lets an AI agent read your test results.

## Setup

Copy the wrapper script from the [Kensa repository](https://github.com/kensa-dev/kensa/tree/master/cli/wrappers) into your project root:

- **macOS / Linux** — `kensa` (shell script)
- **Windows** — `kensa.bat`

The wrapper auto-downloads and keeps the binary up to date from GitHub releases. Commit it to your repository; the binary itself lives in `.kensa/bin/` and can be gitignored.

:::note
The wrapper currently always fetches the `amd64` binary — on Apple Silicon it runs via Rosetta 2.
:::

## Usage

### Serve a directory directly

```bash
kensa --dir build/kensa-output           # single bundle
kensa --dir build/kensa-site             # site mode — full aggregated site
kensa --dir build/kensa-site/sources/uiTest   # site mode — one source bundle
```

The CLI accepts any of three layouts:

| Target | Notes |
|---|---|
| `build/kensa-output/` | Default single-bundle output (`index.html` + `kensa.js` + data). |
| `build/kensa-site/` | [Site-mode](./build-plugins/site-mode.md) root with shell + `manifest.json` + `sources/`. UI sidebar shows one root per sourceset. |
| `build/kensa-site/sources/<id>/` | A single data-only bundle from a site (no shell on disk). The CLI falls back to its embedded UI shell so the bundle renders standalone. |

### Serve a named folder from config

Add a `.kensa-properties` file to your project root:

```yaml
testFolders:
  my-tests: build/kensa-output
  ui:      build/kensa-site/sources/uiTest
  site:    build/kensa-site
port: 8080        # optional, default 8080
```

Then run:

```bash
kensa my-tests
```

### Flags

| Flag | Default | Description |
|------|---------|-------------|
| `--dir <path>` | — | Directory to serve (overrides config) |
| `--port <n>` | `8080` | Port to listen on |
| `--open` | `true` | Auto-open browser on start |

### Version-skew warning

The CLI binary embeds a copy of the Kensa UI shell from the version it was built at. When you point it at a report whose `kensaVersion` is newer, you'll see:

```
warning: kensa CLI version 0.7.1 is older than report version 0.8.0; some UI features may not render correctly. Update the CLI to match.
```

For a full-site directory or single-bundle output the on-disk shell takes precedence, so this is only advisory. For a data-only `sources/<id>/` directory the CLI's embedded shell is what you'll see — the wrapper script self-updates on each run, so the warning typically goes away after the next invocation.

## MCP server

`kensa mcp` runs a [Model Context Protocol](https://modelcontextprotocol.io) server that gives an AI agent structured access to your test results. Instead of the agent grepping a stack trace out of a log, it asks which tests failed and gets back the Given/When/Then sentence Kensa parsed from your test source.

This is not a network service. The agent starts `kensa mcp` as a child process and talks JSON-RPC to it over stdin and stdout — there is no port, no URL, and no authentication, because the trust boundary is process spawn. Nothing leaves your machine.

:::note
This complements the [Kensa agent skill](./agent-skills.md). The skill teaches an agent how to *write* idiomatic Kensa tests; the MCP server lets it *read* the results of running them.
:::

### Registering with your agent

Everything after `--` is the command to run. From your project root:

```bash
claude mcp add kensa -- ./kensa mcp
```

That points at the wrapper script, so the binary stays up to date. To use a binary directly, give its path instead of `./kensa`. Verify with `claude mcp list`; remove with `claude mcp remove kensa`.

Other MCP clients take the same command (`./kensa mcp`) in whatever form their config expects — typically a `command` and `args` pair.

:::caution
If you wrap the CLI in a script of your own, keep its diagnostics on **stderr**. Stdout carries the JSON-RPC stream, and a single stray line of output on it breaks the session.
:::

### Tools

Four tools read a completed test run:

| Tool | Returns |
|------|---------|
| `list_tests` | Every test class, optionally filtered by state (`Passed`, `Failed`, `Disabled`, `Not Executed`). |
| `list_failures` | Just the failures. |
| `get_test` | The full result JSON for one test class — invocations, sentences, tokens, exceptions. |
| `failure_evidence` | The failing sentence and exception message for one class, distilled for triage. |

One tool reads your sources: `style_profile` catalogues how the project writes Kensa tests — framework, fixture containers, MatcherFields, stub helpers, conventions, and a representative test — so an agent proposing a fix writes it in your idiom rather than generic Kotlin. It scans both Kotlin and Java by pattern-matching, without compiling, so treat it as a strong hint rather than ground truth. It is the only tool that writes anything: a cache at `.kensa/style-profile.json`, which you should gitignore.

`server_info` reports the server name and version, useful as a connectivity check.

### Which report the tools read

Every bundle tool takes an optional `bundle_dir`, which accepts the same things the serve command does:

| Value | Resolves to |
|---|---|
| omitted | The sole entry in `.kensa-properties`. If several are configured, the error names them. |
| a name from `.kensa-properties` | That entry's directory — the same names `kensa <name>` serves. |
| `build/kensa-output` | A single bundle. |
| `build/kensa-site` | A [site-mode](./build-plugins/site-mode.md) root: **all** of its sources. |
| `build/kensa-site/sources/uiTest` | One source of a site, which is a complete bundle in its own right. |

Given a site root, listings cover every source and tag each entry with the `source` it came from, and lookups by test class id search all of them. So with a `.kensa-properties` in place, "why is the build red?" needs no paths at all.

Since `.kensa-properties` is read from the working directory, register the server from your project root.

### Try it without an agent

The protocol is newline-delimited JSON, so you can drive it by hand. The server shuts down as soon as stdin closes, so hold the pipe open long enough for the replies to come back:

```bash
{ printf '%s\n' \
  '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"probe","version":"1"}}}' \
  '{"jsonrpc":"2.0","method":"notifications/initialized"}' \
  '{"jsonrpc":"2.0","id":2,"method":"tools/list"}'; sleep 1; } | ./kensa mcp
```

A plain `printf ... | ./kensa mcp` prints nothing — stdin closes immediately and the server exits before it can reply. Real clients hold the pipe open for the life of the session, so this only bites when probing by hand.

Replies carry the `id` of the request they answer and may arrive out of order, since the server handles calls concurrently.

## Building from source

```bash
cd cli
go build -o build/bin/kensa cmd/kensa/main.go
```
