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
kensa --dir build/kensa-output
```

### Serve a named folder from config

Add a `.kensa-properties` file to your project root:

```yaml
testFolders:
  my-tests: build/kensa-output
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

## Building from source

```bash
cd cli
go build -o build/bin/kensa cmd/kensa/main.go
```
