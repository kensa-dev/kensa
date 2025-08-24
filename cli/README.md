# Kensa CLI

This is a lightweight Go-based CLI for serving Kensa web reports. It starts a local HTTP server for static files in specified directories.

## Building Locally

From the `cli/` dir:

- `go build -o kensa cmd/kensa/main.go` (for your current OS/arch)

For cross-platform:

- macOS (amd64): `GOOS=darwin GOARCH=amd64 go build -o kensa-darwin-amd64 cmd/kensa/main.go`
- Linux (amd64): `GOOS=linux GOARCH=amd64 go build -o kensa-linux-amd64 cmd/kensa/main.go`
- Windows (amd64): `GOOS=windows GOARCH=amd64 go build -o kensa-windows-amd64.exe cmd/kensa/main.go`

## Usage

See the main project README for integration. Wrappers (kensa (.bat for windows)) handle auto-download.