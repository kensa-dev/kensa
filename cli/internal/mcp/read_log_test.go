package mcp

import (
	"encoding/json"
	"strings"
	"testing"
)

const (
	failedLogID  = logsTestClass + ":canAdoptAnAvailableRobot"
	passedLogID  = logsTestClass + ":canCheckAvailabilityOfRobots"
	plainLogID   = "dev.kensa.example.adoptabot.ShelterTest:canListShelters"
	appLogSource = "appLog"
)

func readLogOrFail(t *testing.T, in readLogIn) readLogOut {
	t.Helper()
	out, err := readLogFor("testdata/bundle", in)
	if err != nil {
		t.Fatalf("readLogFor(%+v): %v", in, err)
	}
	return out
}

func TestReadLogReturnsEveryRecordInFileOrder(t *testing.T) {
	out := readLogOrFail(t, readLogIn{ID: failedLogID, Source: appLogSource})

	if out.Source != appLogSource {
		t.Errorf("source = %q, want %q", out.Source, appLogSource)
	}
	if out.Matched != 3 || out.Total != 3 {
		t.Errorf("matched = %d, total = %d, want 3 and 3", out.Matched, out.Total)
	}
	if len(out.Entries) != 3 {
		t.Fatalf("entries = %d, want 3", len(out.Entries))
	}
	for i, e := range out.Entries {
		if e.N != i+1 {
			t.Errorf("entries[%d].N = %d, want %d", i, e.N, i+1)
		}
		if e.Truncated {
			t.Errorf("entries[%d] truncated at the default cap", i)
		}
	}
	if !strings.Contains(out.Entries[0].Text, "ERROR") {
		t.Errorf("entries[0].Text = %q, want the ERROR record", out.Entries[0].Text)
	}
	if out.Notice != "" {
		t.Errorf("notice = %q, want empty", out.Notice)
	}
}

func TestReadLogPatternMatchesTheWholeRecordNotJustItsFirstLine(t *testing.T) {
	out := readLogOrFail(t, readLogIn{ID: failedLogID, Source: appLogSource, Pattern: `AdoptionServiceTest\.java:143`})

	if out.Matched != 1 || out.Total != 3 {
		t.Errorf("matched = %d, total = %d, want 1 and 3", out.Matched, out.Total)
	}
	if len(out.Entries) != 1 {
		t.Fatalf("entries = %d, want 1", len(out.Entries))
	}
	if out.Entries[0].N != 2 {
		t.Errorf("entries[0].N = %d, want 2", out.Entries[0].N)
	}
}

func TestReadLogLevelMatchesOnlyTheFirstLine(t *testing.T) {
	out := readLogOrFail(t, readLogIn{ID: failedLogID, Source: appLogSource, Level: "ERROR"})

	if out.Matched != 1 || out.Total != 3 {
		t.Errorf("matched = %d, total = %d, want 1 and 3", out.Matched, out.Total)
	}
	if len(out.Entries) != 1 || out.Entries[0].N != 1 {
		t.Fatalf("entries = %+v, want only record 1", out.Entries)
	}
	if out.Notice != "" {
		t.Errorf("notice = %q, want empty", out.Notice)
	}
}

func TestReadLogPatternAndLevelBothApply(t *testing.T) {
	out := readLogOrFail(t, readLogIn{ID: failedLogID, Source: appLogSource, Level: "ERROR", Pattern: "status 200"})

	if out.Matched != 0 {
		t.Errorf("matched = %d, want 0", out.Matched)
	}
	if len(out.Entries) != 0 {
		t.Errorf("entries = %+v, want none", out.Entries)
	}
}

func TestReadLogLevelOnRecordsWithoutLevelWordsNotices(t *testing.T) {
	out := readLogOrFail(t, readLogIn{ID: plainLogID, Source: "auditLog", Level: "ERROR"})

	if out.Matched != 0 {
		t.Errorf("matched = %d, want 0", out.Matched)
	}
	if out.Total != 2 {
		t.Errorf("total = %d, want 2", out.Total)
	}
	if len(out.Entries) != 0 {
		t.Errorf("entries = %+v, want none", out.Entries)
	}
	want := `records of "auditLog" carry no level word on their first line; level filtering matches nothing`
	if out.Notice != want {
		t.Errorf("notice = %q, want %q", out.Notice, want)
	}
}

func TestReadLogWithoutLevelReadsRecordsThatCarryNoLevelWord(t *testing.T) {
	out := readLogOrFail(t, readLogIn{ID: plainLogID, Source: "auditLog"})

	if out.Matched != 2 || out.Total != 2 {
		t.Errorf("matched = %d, total = %d, want 2 and 2", out.Matched, out.Total)
	}
	if out.Notice != "" {
		t.Errorf("notice = %q, want empty", out.Notice)
	}
}

func TestReadLogMaxEntriesCapsEntriesButNotCounts(t *testing.T) {
	out := readLogOrFail(t, readLogIn{ID: failedLogID, Source: appLogSource, MaxEntries: 1})

	if out.Matched != 3 || out.Total != 3 {
		t.Errorf("matched = %d, total = %d, want 3 and 3", out.Matched, out.Total)
	}
	if len(out.Entries) != 1 || out.Entries[0].N != 1 {
		t.Fatalf("entries = %+v, want only the first record", out.Entries)
	}
}

func TestReadLogMaxEntriesMinusOneIsUnlimited(t *testing.T) {
	out := readLogOrFail(t, readLogIn{ID: failedLogID, Source: appLogSource, MaxEntries: -1})

	if len(out.Entries) != 3 {
		t.Errorf("entries = %d, want 3", len(out.Entries))
	}
}

func TestReadLogMaxEntryCharsTruncatesInRunes(t *testing.T) {
	records, err := readRecords(fixtureRecords)
	if err != nil {
		t.Fatalf("readRecords: %v", err)
	}

	out := readLogOrFail(t, readLogIn{ID: failedLogID, Source: appLogSource, MaxEntryChars: 10})

	if len(out.Entries) != 3 {
		t.Fatalf("entries = %d, want 3", len(out.Entries))
	}
	for i, e := range out.Entries {
		if !e.Truncated {
			t.Errorf("entries[%d].Truncated = false, want true", i)
		}
		if got := len([]rune(e.Text)); got != 10 {
			t.Errorf("entries[%d] text = %d runes, want 10", i, got)
		}
		if want := len([]rune(records[i].Text)); e.FullLength != want {
			t.Errorf("entries[%d].FullLength = %d, want %d runes", i, e.FullLength, want)
		}
	}
}

func TestReadLogMaxEntryCharsMinusOneIsUnlimited(t *testing.T) {
	records, err := readRecords(fixtureRecords)
	if err != nil {
		t.Fatalf("readRecords: %v", err)
	}

	out := readLogOrFail(t, readLogIn{ID: failedLogID, Source: appLogSource, MaxEntryChars: -1})

	if len(out.Entries) != 3 {
		t.Fatalf("entries = %d, want 3", len(out.Entries))
	}
	for i, e := range out.Entries {
		if e.Truncated || e.FullLength != 0 {
			t.Errorf("entries[%d] = %+v, want untruncated", i, e)
		}
		if e.Text != records[i].Text {
			t.Errorf("entries[%d].Text = %q, want the record verbatim", i, e.Text)
		}
	}
}

func TestReadLogOnTabThatRecordedNothing(t *testing.T) {
	out := readLogOrFail(t, readLogIn{ID: failedLogID, Source: "auditLog"})

	if out.Matched != 0 || out.Total != 0 {
		t.Errorf("matched = %d, total = %d, want 0 and 0", out.Matched, out.Total)
	}
	if len(out.Entries) != 0 {
		t.Errorf("entries = %+v, want none", out.Entries)
	}
	if out.Notice != "" {
		t.Errorf("notice = %q, want empty", out.Notice)
	}
}

func TestReadLogOnSkippedTabNotices(t *testing.T) {
	out := readLogOrFail(t, readLogIn{ID: passedLogID, Source: appLogSource})

	want := `source "appLog" was skipped: OnlyOnFailure`
	if out.Notice != want {
		t.Errorf("notice = %q, want %q", out.Notice, want)
	}
	if out.Matched != 0 || out.Total != 0 || len(out.Entries) != 0 {
		t.Errorf("out = %+v, want an empty result", out)
	}
}

func TestReadLogOnRegisteredSourceWithNoTabNotices(t *testing.T) {
	out := readLogOrFail(t, readLogIn{ID: failedLogID, Source: "gatewayLog"})

	want := `source "gatewayLog" is registered but has no tab for this invocation`
	if out.Notice != want {
		t.Errorf("notice = %q, want %q", out.Notice, want)
	}
	if out.Matched != 0 || out.Total != 0 || len(out.Entries) != 0 {
		t.Errorf("out = %+v, want an empty result", out)
	}
}

// A tab counting entries with no sidecar beside it is a renderer that filled
// in entries without handing over records. Answering with a bare zero would
// read as a quiet log.
func TestReadLogOnCountedEntriesWithoutASidecarNotices(t *testing.T) {
	entries := 3
	inv := Invocation{CustomTabContents: []CustomTabContent{
		{TabID: "app", Label: "App Log", SourceID: appLogSource, Entries: &entries},
	}}

	out, err := readLogOfInvocation("testdata/bundle", readLogIn{ID: failedLogID, Source: appLogSource}, inv, nil, nil)
	if err != nil {
		t.Fatalf("readLogOfInvocation: %v", err)
	}
	want := `source "appLog" recorded 3 entries but no records file; the renderer wrote no sidecar`
	if out.Notice != want {
		t.Errorf("notice = %q, want %q", out.Notice, want)
	}
	if out.Matched != 0 || out.Total != 0 || len(out.Entries) != 0 {
		t.Errorf("out = %+v, want an empty result", out)
	}
}

func TestReadLogOnUnknownSourceErrors(t *testing.T) {
	_, err := readLogFor("testdata/bundle", readLogIn{ID: failedLogID, Source: "nope"})
	if err == nil {
		t.Fatal("readLogFor on an unknown source = nil error, want an error")
	}
	want := `no log source "nope" for ` + failedLogID
	if err.Error() != want {
		t.Errorf("error = %q, want %q", err, want)
	}
}

func TestReadLogOnInvalidPatternErrors(t *testing.T) {
	_, err := readLogFor("testdata/bundle", readLogIn{ID: failedLogID, Source: appLogSource, Pattern: "("})
	if err == nil {
		t.Fatal("readLogFor with an invalid pattern = nil error, want an error")
	}
	if !strings.Contains(err.Error(), "pattern") {
		t.Errorf("error = %q, want it to name the pattern", err)
	}
}

func TestReadLogOnInvalidPatternErrorsEvenWhenTheTabIsEmpty(t *testing.T) {
	_, err := readLogFor("testdata/bundle", readLogIn{ID: failedLogID, Source: "auditLog", Pattern: "("})
	if err == nil {
		t.Fatal("readLogFor with an invalid pattern on an empty tab = nil error, want an error")
	}
}

func TestReadLogOnUnknownMethodErrors(t *testing.T) {
	_, err := readLogFor("testdata/bundle", readLogIn{ID: logsTestClass + ":noSuchMethod", Source: appLogSource})
	if err == nil {
		t.Fatal("readLogFor on an unknown method = nil error, want an error")
	}
	want := `no method "noSuchMethod" in ` + logsTestClass
	if err.Error() != want {
		t.Errorf("error = %q, want %q", err, want)
	}
}

func TestReadLogEntriesMarshalAsAnEmptyArray(t *testing.T) {
	out := readLogOrFail(t, readLogIn{ID: failedLogID, Source: "gatewayLog"})

	if b, err := json.Marshal(out.Entries); err != nil || string(b) != "[]" {
		t.Errorf("entries json = %s (err %v), want []", b, err)
	}
}
