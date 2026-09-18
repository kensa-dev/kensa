package mcp

import (
	"regexp"
	"strings"
	"testing"
)

const fixtureRecords = "testdata/bundle/tabs/dev.kensa.example.adoptabot.AdoptionServiceTest/canAdoptAnAvailableRobot/invocation-0/appLog.jsonl"

func TestReadRecordsParsesEveryLine(t *testing.T) {
	records, err := readRecords(fixtureRecords)
	if err != nil {
		t.Fatalf("readRecords: %v", err)
	}
	if len(records) != 3 {
		t.Fatalf("records = %d, want 3", len(records))
	}
	for i, r := range records {
		if r.Identifier != "canAdoptAnAvailableRobot#0" {
			t.Errorf("records[%d].Identifier = %q", i, r.Identifier)
		}
	}
	if got := records[2].Text; got != "2026-09-17T09:15:03.150Z INFO dev.kensa.example.adoptabot.AdoptionService - adoption request completed with status 200" {
		t.Errorf("records[2].Text = %q", got)
	}
}

func TestReadRecordsErrorsOnMissingFile(t *testing.T) {
	if _, err := readRecords("testdata/bundle/tabs/nope.jsonl"); err == nil {
		t.Fatal("readRecords on a missing file = nil error, want an error")
	}
}

func TestFirstLineMatchesOnlyTheFirstLine(t *testing.T) {
	re := regexp.MustCompile(`\bERROR\b`)
	cases := []struct {
		name string
		text string
		want bool
	}{
		{"error on the first line", "09:15 ERROR boom", true},
		{"error only on a later line", "java.lang.IllegalStateException\n\tat ERROR", false},
		{"no error at all", "09:15 INFO fine", false},
		{"error inside a longer word", "09:15 ERRORS boom", false},
		{"empty text", "", false},
	}
	for _, c := range cases {
		t.Run(c.name, func(t *testing.T) {
			if got := firstLineMatches(c.text, re); got != c.want {
				t.Errorf("firstLineMatches(%q) = %v, want %v", c.text, got, c.want)
			}
		})
	}
}

func TestCapRunes(t *testing.T) {
	cases := []struct {
		name       string
		s          string
		max        int
		text       string
		truncated  bool
		fullLength int
	}{
		{"under the cap", "short", 10, "short", false, 0},
		{"exactly at the cap", "0123456789", 10, "0123456789", false, 0},
		{"over the cap", "0123456789abcdef", 10, "0123456789", true, 16},
		{"cuts on runes not bytes", strings.Repeat("é", 12), 10, strings.Repeat("é", 10), true, 12},
		{"negative max is unlimited", "0123456789abcdef", -1, "0123456789abcdef", false, 0},
		{"zero max cuts everything", "abc", 0, "", true, 3},
	}
	for _, c := range cases {
		t.Run(c.name, func(t *testing.T) {
			text, truncated, fullLength := capRunes(c.s, c.max)
			if text != c.text || truncated != c.truncated || fullLength != c.fullLength {
				t.Errorf("capRunes(%q, %d) = %q, %v, %d; want %q, %v, %d",
					c.s, c.max, text, truncated, fullLength, c.text, c.truncated, c.fullLength)
			}
		})
	}
}
