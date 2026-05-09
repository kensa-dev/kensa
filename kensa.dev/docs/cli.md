---
sidebar_position: 5
description: The Kensa CLI is a lightweight Go binary that serves HTML test reports over HTTP, with auto-download via a shell wrapper script.
---

# CLI

The Kensa CLI is a lightweight Go binary that serves HTML reports over HTTP — useful when your IDE doesn't have a built-in web server.

## Setup

Copy the wrapper script from the [Kensa repository](https://github.com/kensa-dev/kensa/tree/master/cli/wrappers) into your project root:

- **macOS / Linux** — `kensa` (shell script)
- **Windows** — `kensa.bat`

The wrapper auto-downloads and keeps the binary up to date from GitHub releases. Commit it to your repository; the binary itself lives in `.kensa/bin/` and can be gitignored.

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

## Building from source

```bash
cd cli
go build -o build/bin/kensa cmd/kensa/main.go
```
