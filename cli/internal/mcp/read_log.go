package mcp

import (
	"context"
	"fmt"
	"path/filepath"
	"regexp"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

type readLogIn struct {
	ID            string `json:"id" jsonschema:"test id of the form <class>:<method>"`
	Source        string `json:"source" jsonschema:"log source id, as listed by invocation_logs"`
	Invocation    int    `json:"invocation,omitempty" jsonschema:"invocation index within the method, default 0"`
	Pattern       string `json:"pattern,omitempty" jsonschema:"regular expression matched against each whole record; omit for all records"`
	Level         string `json:"level,omitempty" jsonschema:"log level word that must appear on a record's first line, such as ERROR or WARN"`
	MaxEntries    int    `json:"max_entries,omitempty" jsonschema:"cap on returned records, default 50; -1 means unlimited"`
	MaxEntryChars int    `json:"max_entry_chars,omitempty" jsonschema:"cap on each record's text in characters, default 4000; 0 means the default, -1 means unlimited"`
	BundleDir     string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
}

// logEntry is one record of a log tab: its ordinal in the file, so a filtered
// answer still says where each record sits in the whole log, and its text
// capped to max_entry_chars.
type logEntry struct {
	N          int    `json:"n"`
	Text       string `json:"text"`
	Truncated  bool   `json:"truncated,omitempty"`
	FullLength int    `json:"fullLength,omitempty"`
}

// readLogOut carries both counts: Matched is what the filters kept, Total what
// the tab holds, so a capped answer never reads as the whole log.
type readLogOut struct {
	Source  string     `json:"source"`
	Matched int        `json:"matched"`
	Total   int        `json:"total"`
	Entries []logEntry `json:"entries"`
	Notice  string     `json:"notice,omitempty"`
}

const (
	defaultMaxEntries    = 50
	defaultMaxEntryChars = 4000
)

const (
	noticeSourceWithoutTab = "source %q is registered but has no tab for this invocation"
	noticeSourceSkipped    = "source %q was skipped: %s"
	noticeNoLevelWords     = "records of %q carry no level word on their first line; level filtering matches nothing"

	noticeEntriesWithoutRecords = "source %q recorded %d entries but no records file; the renderer wrote no sidecar"
)

// levelWordPattern matches any level word a record's first line might carry.
// A tab whose records carry none cannot be filtered by level, and says so
// rather than answering with a silent empty result.
var levelWordPattern = regexp.MustCompile(`\b(TRACE|DEBUG|INFO|WARN|WARNING|ERROR|FATAL)\b`)

func readLogFor(spec string, in readLogIn) (readLogOut, error) {
	// The filters are the caller's own input, so they are checked before any
	// bundle is read: a bad pattern is a bad pattern whatever the tab holds.
	pattern, level, err := logFilters(in)
	if err != nil {
		return readLogOut{}, err
	}
	dir, inv, err := findInvocation(spec, in.ID, in.Invocation)
	if err != nil {
		return readLogOut{}, err
	}
	return readLogOfInvocation(dir, in, inv, pattern, level)
}

// readLogOfInvocation reads one log source's records out of an invocation
// already located, with the caller's filters already compiled. The record
// files the tab names are relative to dir.
func readLogOfInvocation(dir string, in readLogIn, inv Invocation, pattern, level *regexp.Regexp) (readLogOut, error) {
	out := readLogOut{Source: in.Source, Entries: []logEntry{}}

	tab, found := logTabOf(inv, in.Source)
	if !found {
		// A source the run registered but this invocation produced no tab for
		// is an ordinary quiet log; one nothing knows about is a typo.
		if !sourceRegistered(dir, in.Source) {
			return readLogOut{}, fmt.Errorf("no log source %q for %s", in.Source, in.ID)
		}
		out.Notice = fmt.Sprintf(noticeSourceWithoutTab, in.Source)
		return out, nil
	}
	if tab.Visibility != "" {
		out.Notice = fmt.Sprintf(noticeSourceSkipped, in.Source, tab.Visibility)
		return out, nil
	}
	if tab.Records == "" {
		// A tab that counted entries but named no sidecar has records nothing
		// can read; a bare zero would read as a quiet log.
		if tab.Entries != nil && *tab.Entries > 0 {
			out.Notice = fmt.Sprintf(noticeEntriesWithoutRecords, in.Source, *tab.Entries)
		}
		return out, nil
	}
	if tab.Entries != nil && *tab.Entries == 0 {
		return out, nil
	}

	records, err := readRecords(filepath.Join(dir, tab.Records))
	if err != nil {
		return readLogOut{}, fmt.Errorf("log source %q: %w", in.Source, err)
	}
	out.Total = len(records)
	if level != nil && !anyLevelWord(records) {
		out.Notice = fmt.Sprintf(noticeNoLevelWords, in.Source)
	}

	maxEntries := in.MaxEntries
	if maxEntries == 0 {
		maxEntries = defaultMaxEntries
	}
	maxChars := in.MaxEntryChars
	if maxChars == 0 {
		maxChars = defaultMaxEntryChars
	}
	for i, r := range records {
		if pattern != nil && !pattern.MatchString(r.Text) {
			continue
		}
		if level != nil && !firstLineMatches(r.Text, level) {
			continue
		}
		// Matched counts every record the filters kept, including those the
		// cap leaves out, so the caller can tell there is more to ask for.
		out.Matched++
		if maxEntries >= 0 && len(out.Entries) >= maxEntries {
			continue
		}
		text, truncated, fullLength := capRunes(r.Text, maxChars)
		out.Entries = append(out.Entries, logEntry{N: i + 1, Text: text, Truncated: truncated, FullLength: fullLength})
	}
	return out, nil
}

// logFilters compiles the caller's filters. Level is quoted and bounded by
// word breaks, so "WARN" does not match "WARNING" and a level containing
// regexp punctuation is still read as a word.
func logFilters(in readLogIn) (pattern, level *regexp.Regexp, err error) {
	if in.Pattern != "" {
		pattern, err = regexp.Compile(in.Pattern)
		if err != nil {
			return nil, nil, fmt.Errorf("invalid pattern %q: %w", in.Pattern, err)
		}
	}
	if in.Level != "" {
		level = regexp.MustCompile(`\b` + regexp.QuoteMeta(in.Level) + `\b`)
	}
	return pattern, level, nil
}

// logTabOf finds the invocation's tab for one log source. A tab without a
// source id is an ordinary custom tab, not a log.
func logTabOf(inv Invocation, source string) (CustomTabContent, bool) {
	for _, tab := range inv.CustomTabContents {
		if tab.SourceID != "" && tab.SourceID == source {
			return tab, true
		}
	}
	return CustomTabContent{}, false
}

// sourceRegistered reports whether the run declared this log source at all.
func sourceRegistered(dir, source string) bool {
	marker, _ := readRunMarker(dir)
	for _, s := range marker.LogSources {
		if s.ID == source {
			return true
		}
	}
	return false
}

func anyLevelWord(records []logRecord) bool {
	for _, r := range records {
		if firstLineMatches(r.Text, levelWordPattern) {
			return true
		}
	}
	return false
}

func readLog(_ context.Context, _ *mcp.CallToolRequest, in readLogIn) (*mcp.CallToolResult, readLogOut, error) {
	out, err := readLogFor(in.BundleDir, in)
	return nil, out, err
}
