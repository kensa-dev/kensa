package mcp

import (
	"encoding/json"
	"strings"
	"testing"
)

const logsTestClass = "dev.kensa.example.adoptabot.AdoptionServiceTest"

func intOf(n int) *int    { return &n }
func boolOf(b bool) *bool { return &b }

func assertRows(t *testing.T, got, want []logRow) {
	t.Helper()
	if len(got) != len(want) {
		t.Fatalf("logs = %+v, want %d rows", got, len(want))
	}
	for i := range want {
		if !sameRow(got[i], want[i]) {
			t.Errorf("logs[%d] = %s, want %s", i, showRow(got[i]), showRow(want[i]))
		}
	}
}

func sameRow(a, b logRow) bool {
	return a.Source == b.Source && a.Label == b.Label && a.Path == b.Path && a.Skipped == b.Skipped &&
		sameIntPtr(a.Entries, b.Entries) && sameIntPtr(a.Errors, b.Errors) && sameBoolPtr(a.Declared, b.Declared)
}

func sameIntPtr(a, b *int) bool {
	if a == nil || b == nil {
		return a == b
	}
	return *a == *b
}

func sameBoolPtr(a, b *bool) bool {
	if a == nil || b == nil {
		return a == b
	}
	return *a == *b
}

func showRow(r logRow) string {
	b, _ := json.Marshal(r)
	return string(b)
}

func TestInvocationLogsForFailedInvocation(t *testing.T) {
	out, err := invocationLogsFor("testdata/bundle", invocationLogsIn{ID: logsTestClass + ":canAdoptAnAvailableRobot"})
	if err != nil {
		t.Fatalf("invocationLogsFor: %v", err)
	}
	if out.ID != logsTestClass+":canAdoptAnAvailableRobot" || out.Invocation != 0 {
		t.Errorf("id = %q, invocation = %d", out.ID, out.Invocation)
	}
	if out.State != "Failed" {
		t.Errorf("state = %q, want Failed", out.State)
	}
	if out.Identifier != "canAdoptAnAvailableRobot#0" {
		t.Errorf("identifier = %q", out.Identifier)
	}
	assertRows(t, out.Logs, []logRow{
		{
			Source:  "appLog",
			Label:   "App Log",
			Entries: intOf(3),
			Errors:  intOf(1),
			Path:    "tabs/dev.kensa.example.adoptabot.AdoptionServiceTest/canAdoptAnAvailableRobot/invocation-0/appLog.txt",
		},
		{Source: "auditLog", Label: "Audit Log", Entries: intOf(0), Errors: intOf(0)},
		{Source: "gatewayLog", Declared: boolOf(false)},
	})
	if out.Notice != "" {
		t.Errorf("notice = %q, want empty", out.Notice)
	}
}

func TestInvocationLogsForPassedInvocationListsSkippedTabs(t *testing.T) {
	out, err := invocationLogsFor("testdata/bundle", invocationLogsIn{ID: logsTestClass + ":canCheckAvailabilityOfRobots"})
	if err != nil {
		t.Fatalf("invocationLogsFor: %v", err)
	}
	if out.State != "Passed" {
		t.Errorf("state = %q, want Passed", out.State)
	}
	assertRows(t, out.Logs, []logRow{
		{Source: "appLog", Label: "App Log", Skipped: "OnlyOnFailure"},
		{Source: "auditLog", Label: "Audit Log", Skipped: "OnlyOnFailure"},
		{Source: "gatewayLog", Declared: boolOf(false)},
	})
	if out.Notice != "" {
		t.Errorf("notice = %q, want empty", out.Notice)
	}
}

func TestInvocationLogsUnknownMethod(t *testing.T) {
	_, err := invocationLogsFor("testdata/bundle", invocationLogsIn{ID: logsTestClass + ":noSuchMethod"})
	if err == nil {
		t.Fatal("invocationLogsFor on an unknown method = nil error")
	}
	want := `no method "noSuchMethod" in ` + logsTestClass
	if err.Error() != want {
		t.Errorf("error = %q, want %q", err, want)
	}
}

func TestInvocationLogsInvocationOutOfRange(t *testing.T) {
	_, err := invocationLogsFor("testdata/bundle", invocationLogsIn{ID: logsTestClass + ":canAdoptAnAvailableRobot", Invocation: 3})
	if err == nil {
		t.Fatal("invocationLogsFor with an out-of-range invocation = nil error")
	}
	if !strings.Contains(err.Error(), "1 invocation") {
		t.Errorf("error = %q, want it to name the invocation count", err)
	}
}

func TestInvocationLogsWithoutLogSourcesNotices(t *testing.T) {
	out, err := invocationLogsFor("testdata/multi", invocationLogsIn{ID: "feature.migrate.StartMigrateOutHappyPathTest:rejectsUnknownSession"})
	if err != nil {
		t.Fatalf("invocationLogsFor: %v", err)
	}
	if len(out.Logs) != 0 {
		t.Errorf("logs = %+v, want none", out.Logs)
	}
	if out.Notice != "no log sources recorded for this bundle" {
		t.Errorf("notice = %q", out.Notice)
	}
	if b, err := json.Marshal(out.Logs); err != nil || string(b) != "[]" {
		t.Errorf("logs json = %s (err %v), want []", b, err)
	}
}

func TestErrorCountNamesTheSourceOfAnUnreadableSidecar(t *testing.T) {
	tab := CustomTabContent{SourceID: "appLog", Entries: intOf(3), Records: "tabs/gone.jsonl"}
	_, err := errorCount("testdata/bundle", tab)
	if err == nil {
		t.Fatal("errorCount on a missing sidecar = nil error, want an error")
	}
	if !strings.Contains(err.Error(), `log source "appLog"`) {
		t.Errorf("error = %q, want it to name the log source", err)
	}
}
