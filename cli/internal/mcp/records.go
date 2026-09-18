package mcp

import (
	"encoding/json"
	"fmt"
	"os"
	"regexp"
	"strings"
)

// logRecord is one line of a log tab's .jsonl sidecar: the record's text as
// the log source produced it, and the invocation identifier it was captured
// under.
type logRecord struct {
	Identifier string `json:"identifier"`
	Text       string `json:"text"`
}

// readRecords reads a log tab's .jsonl sidecar, one record per line. A record
// spanning several lines (a stack trace) is escaped into its one line by the
// writer, so lines and records are the same thing.
func readRecords(path string) ([]logRecord, error) {
	b, err := os.ReadFile(path)
	if err != nil {
		return nil, err
	}
	var records []logRecord
	for i, line := range strings.Split(string(b), "\n") {
		if strings.TrimSpace(line) == "" {
			continue
		}
		var r logRecord
		if err := json.Unmarshal([]byte(line), &r); err != nil {
			return nil, fmt.Errorf("%s line %d: %w", path, i+1, err)
		}
		records = append(records, r)
	}
	return records, nil
}

// firstLineMatches reports whether re matches the first line of a record. Only
// the first line carries the level; the rest is the stack trace, where a
// matching word says nothing about this record's severity.
func firstLineMatches(text string, re *regexp.Regexp) bool {
	first, _, _ := strings.Cut(text, "\n")
	return re.MatchString(first)
}

// capRunes cuts s to max runes, reporting whether it cut and how long s was in
// runes. Cutting on runes keeps the text valid UTF-8, so a cap of 10 is ten
// characters and never half a character. A negative max means no cap; a caller
// with its own default maps that default before calling.
func capRunes(s string, max int) (text string, truncated bool, fullLength int) {
	if max < 0 {
		return s, false, 0
	}
	runes := []rune(s)
	if len(runes) <= max {
		return s, false, 0
	}
	return string(runes[:max]), true, len(runes)
}
