package mcp

import (
	"context"
	"encoding/json"
	"fmt"
	"path/filepath"
	"regexp"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

type invocationLogsIn struct {
	ID         string `json:"id" jsonschema:"test id of the form <class>:<method>"`
	Invocation int    `json:"invocation,omitempty" jsonschema:"invocation index within the method, default 0"`
	BundleDir  string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
}

// logRow is one log source as this invocation saw it: a rendered tab with its
// counts and file, a tab the run skipped, or a source registered for the run
// that produced no tab here at all.
type logRow struct {
	Source   string `json:"source"`
	Label    string `json:"label,omitempty"`
	Entries  *int   `json:"entries,omitempty"`
	Errors   *int   `json:"errors,omitempty"`
	Path     string `json:"path,omitempty"`
	Skipped  string `json:"skipped,omitempty"`
	Declared *bool  `json:"declared,omitempty"`
}

type invocationLogsOut struct {
	ID         string   `json:"id"`
	Invocation int      `json:"invocation"`
	State      string   `json:"state"`
	Identifier string   `json:"identifier,omitempty"`
	Logs       []logRow `json:"logs"`
	Notice     string   `json:"notice,omitempty"`
}

// noticeNoInvocationLogs is returned when nothing about logging reached this
// bundle: no sources in run.json and no log tab on the invocation.
const noticeNoInvocationLogs = "no log sources recorded for this bundle"

// errorLevelPattern marks a record as an error. Only the record's first line
// is tested, so a stack trace naming ERROR does not count twice.
var errorLevelPattern = regexp.MustCompile(`\bERROR\b`)

func invocationLogsFor(spec string, in invocationLogsIn) (invocationLogsOut, error) {
	dir, inv, err := findInvocation(spec, in.ID, in.Invocation)
	if err != nil {
		return invocationLogsOut{}, err
	}
	return logsOfInvocation(dir, in, inv)
}

// findInvocation resolves a child id and an invocation index to that one
// invocation, along with the bundle it came from: the tab and record files an
// invocation names are relative to that directory.
func findInvocation(spec, id string, index int) (string, Invocation, error) {
	refs, _, err := resolveComplete(spec)
	if err != nil {
		return "", Invocation{}, err
	}
	b, dir, err := findRawResultIn(refs, id)
	if err != nil {
		return "", Invocation{}, err
	}
	var result Result
	if err := json.Unmarshal(b, &result); err != nil {
		return "", Invocation{}, err
	}
	method := methodOf(id)
	for _, tc := range result.Tests {
		if tc.TestMethod != method {
			continue
		}
		if index < 0 || index >= len(tc.Invocations) {
			return "", Invocation{}, fmt.Errorf("invocation %d out of range: %s has %s",
				index, method, invocations(len(tc.Invocations)))
		}
		return dir, tc.Invocations[index], nil
	}
	return "", Invocation{}, fmt.Errorf("no method %q in %s", method, result.TestClass)
}

func logsOfInvocation(dir string, in invocationLogsIn, inv Invocation) (invocationLogsOut, error) {
	out := invocationLogsOut{ID: in.ID, Invocation: in.Invocation, State: inv.State, Logs: []logRow{}}
	var skipped []logRow
	seen := map[string]bool{}
	for _, tab := range inv.CustomTabContents {
		if out.Identifier == "" {
			out.Identifier = tab.Identifier
		}
		// A tab without a source id is an ordinary custom tab, not a log.
		if tab.SourceID == "" {
			continue
		}
		seen[tab.SourceID] = true
		if tab.Visibility != "" {
			skipped = append(skipped, logRow{Source: tab.SourceID, Label: tab.Label, Skipped: tab.Visibility})
			continue
		}
		count, err := errorCount(dir, tab)
		if err != nil {
			return invocationLogsOut{}, err
		}
		out.Logs = append(out.Logs, logRow{Source: tab.SourceID, Label: tab.Label, Entries: tab.Entries, Errors: &count, Path: tab.File})
	}
	out.Logs = append(out.Logs, skipped...)

	marker, _ := readRunMarker(dir)
	for _, source := range marker.LogSources {
		if seen[source.ID] {
			continue
		}
		declared := false
		out.Logs = append(out.Logs, logRow{Source: source.ID, Declared: &declared})
	}
	if len(marker.LogSources) == 0 && len(seen) == 0 {
		out.Notice = noticeNoInvocationLogs
	}
	return out, nil
}

// errorCount counts the tab's records whose first line reads as an error. A
// tab that recorded nothing has no sidecar, which is zero errors; a sidecar
// that is named but unreadable is an error, since a silent zero would read as
// a clean log.
func errorCount(dir string, tab CustomTabContent) (int, error) {
	if tab.Records == "" || (tab.Entries != nil && *tab.Entries == 0) {
		return 0, nil
	}
	records, err := readRecords(filepath.Join(dir, tab.Records))
	if err != nil {
		return 0, fmt.Errorf("log source %q: %w", tab.SourceID, err)
	}
	count := 0
	for _, r := range records {
		if firstLineMatches(r.Text, errorLevelPattern) {
			count++
		}
	}
	return count, nil
}

func invocations(n int) string {
	if n == 1 {
		return "1 invocation"
	}
	return fmt.Sprintf("%d invocations", n)
}

func invocationLogs(_ context.Context, _ *mcp.CallToolRequest, in invocationLogsIn) (*mcp.CallToolResult, invocationLogsOut, error) {
	out, err := invocationLogsFor(in.BundleDir, in)
	return nil, out, err
}
