---
title: "Kensa CLI: Serve HTML Test Reports Locally"
sidebar_label: CLI
sidebar_position: 5
description: The Kensa CLI is a lightweight Go binary that serves HTML test reports over HTTP and exposes your test results to AI agents over MCP, with auto-download via a shell wrapper script.
---

# CLI

The Kensa CLI is a lightweight Go binary that does two jobs: it serves HTML reports over HTTP — useful when your IDE doesn't have a built-in web server — and it runs an [MCP server](#mcp-server) that lets an AI agent read your test results.

## Setup

Copy the wrapper script from the [Kensa repository](https://github.com/kensa-dev/kensa/tree/master/cli/wrappers) into your project root:

- **macOS / Linux** — `kensa` (shell script)
- **Windows** — `kensa.bat`

The wrapper downloads the binary for your OS and architecture from GitHub releases and keeps it up to date. Commit it to your repository; the binary itself lives in `.kensa/bin/` next to the wrapper and can be gitignored. The shell wrapper follows symlinks to itself, so the install stays beside the real script. Set `KENSA_HOME` to put it somewhere else.

### Pinning a version

Unpinned, the wrapper checks for the latest release on every run, so a new Kensa release changes your tooling the next time anyone invokes it. To decide when that happens, pin a version:

```bash
KENSA_VERSION=0.9.1 ./kensa --dir build/kensa-output
```

A pinned wrapper never contacts GitHub for the version and only downloads when the pinned release is not the one installed, which also removes the version check from every start of the [MCP server](#mcp-server). Set the variable wherever your team keeps project environment (`.envrc`, CI variables, a shell profile), and bump it deliberately.

### Verified downloads

Releases publish a `checksums.txt` alongside the binaries. The wrapper verifies every download against it and refuses to install on a mismatch, or when the file cannot be fetched, leaving the previously installed binary in place. Releases up to 0.9.1 predate the file; for those the wrapper says so on stderr and installs unverified.

Everything the wrapper prints goes to stderr, so it is safe in front of `kensa mcp`.

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

For a full-site directory or single-bundle output the on-disk shell takes precedence, so this is only advisory. For a data-only `sources/<id>/` directory the CLI's embedded shell is what you'll see. An unpinned wrapper self-updates on each run, so the warning typically goes away after the next invocation; a pinned one needs its `KENSA_VERSION` bumped.

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

That points at the wrapper script, so the binary stays up to date (or pinned, if `KENSA_VERSION` is set in the environment the agent spawns it from). To use a binary directly, give its path instead of `./kensa`. Verify with `claude mcp list`; remove with `claude mcp remove kensa`.

The wrapper installs the binary beside itself, so it can also live outside the project, for example registered once at user scope as `~/.kensa/kensa mcp` (a wrapper already inside a `.kensa` directory installs into that directory rather than nesting another). The server still reads `.kensa-properties` from the working directory it is started in.

Other MCP clients take the same command (`./kensa mcp`) in whatever form their config expects — typically a `command` and `args` pair.

:::caution
If you wrap the CLI in a script of your own, keep its diagnostics on **stderr**. Stdout carries the JSON-RPC stream, and a single stray line of output on it breaks the session.
:::

### Tools

Five tools read a completed test run:

| Tool | Returns |
|------|---------|
| `list_tests` | Every test class, optionally filtered by state (`Passed`, `Failed`, `Disabled`, `Not Executed`). |
| `list_failures` | Just the failures. |
| `failure_evidence` | Every failed method of one class: the failing sentence, the exception message, and the line inside the test that threw (`PaymentTest.kt:107`). One call usually gives the fix location. |
| `captured_interactions` | Everything Kensa captured between actors for a class or one method: request and response bodies, status, headers. This is the evidence for a payload-shape mismatch. |
| `get_test` | One class rendered as a person reads it: sentences as text, fixtures, interaction names, any failure. `raw: true` returns the result file verbatim. |

The triage path is `list_failures`, then `failure_evidence` on the class, then `captured_interactions` on the method if the message alone does not explain it. After the fix, re-run, `await_results`, and `list_failures` once more to confirm the whole bundle is clean rather than just the class you re-ran.

One tool reads your sources: `style_profile` catalogues how the project writes Kensa tests — framework, fixture containers, MatcherFields, stub helpers, conventions, and a representative test — so an agent proposing a fix writes it in your idiom rather than generic Kotlin. It scans both Kotlin and Java by pattern-matching, without compiling, so treat it as a strong hint rather than ground truth. It is the only tool that writes anything: a cache at `.kensa/style-profile.json`, which you should gitignore.

Two tools deal with the run itself:

| Tool | Returns |
|------|---------|
| `run_status` | Whether the run that produced the bundle is `complete`, `running`, `abandoned` (the JVM died part way) or `incomplete` (a pre-0.9.2 bundle with no way to tell), with start and finish times and, while it runs, how many classes are written so far and how many methods have passed, failed or were disabled. |
| `await_results` | Blocks until the next run completes, then reports its state. Default timeout 600 seconds, maximum 3600. |

`server_info` reports the server name and version, useful as a connectivity check.

### Knowing whether the results are current

The tools read whatever the last test run wrote. An empty `list_failures` can mean nothing is broken, or that nobody has run the tests since the change, or that the tests are running right now. Kensa 0.9.2 and later write a `run.json` marker into the bundle when the first test starts and finalise it when the report is complete, and the server uses it two ways:

- `list_tests` and `list_failures` refuse a bundle whose run is `running`, `abandoned` or `incomplete`, with an error saying which and what to do. A partial listing would look like a clean one.
- Every listing carries `bundleWrittenAt` (RFC 3339 UTC) and `bundleAge` (`3h12m`, `2d1h`). An agent triaging a red build should check the age before trusting the result, and re-run the tests if it predates the change under investigation.

The marker also carries live counts. Each time a class finishes, Kensa adds it to `classes` and its methods to `passed`, `failed` and `disabled`, so `run_status` can report progress mid-run and an agent can react to the first failure without waiting for the whole run. Counts cover completed classes only; a class still executing contributes nothing until it finishes. A burst of completions is written once, and the file is replaced atomically, so a reader never sees a torn marker.

The marker appears when the first Kensa test starts, so between launching the tests and that moment the previous bundle is still on disk and still reads as complete. The workflow that avoids the gap is: launch the tests, call `await_results` straight away, and read the listings when it returns `completed: true`. If it returns `timedOut: true`, nothing finished in the window; `run_status` says where things stand.

`running` relies on the test JVM's pid being visible to the server, which holds when both run on the same machine. A bundle produced elsewhere (a CI agent, a container) records a different hostname and is judged on `indices.json` alone.

For `style_profile`, pass the module that holds the tests (`.../acceptance-tests`), not the repository root, when the tests live in a submodule. And for triage, prefer `list_failures` then `get_test` on the one class: `list_tests` with no `state` filter returns every class with its children inlined.

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
