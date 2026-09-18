package mcp

import (
	"reflect"
	"testing"
)

func TestReadIndices(t *testing.T) {
	tests, err := readIndices("testdata/bundle")
	if err != nil {
		t.Fatalf("readIndices: %v", err)
	}
	if len(tests) != 2 {
		t.Fatalf("got %d classes, want 2", len(tests))
	}
	if tests[0].State != "Failed" || tests[0].TestClass != failingClass {
		t.Errorf("unexpected first entry: %+v", tests[0])
	}
	if len(tests[0].Children) != 2 {
		t.Errorf("got %d children, want 2", len(tests[0].Children))
	}
	child := tests[0].Children[0]
	if child.TestMethod != "canCheckAvailabilityOfRobots" {
		t.Errorf("testMethod = %q, want %q", child.TestMethod, "canCheckAvailabilityOfRobots")
	}
	if !reflect.DeepEqual(child.Timing, [][2]int64{{1000, 250}}) {
		t.Errorf("timing = %+v", child.Timing)
	}
	if !reflect.DeepEqual(child.Participants, map[string]int{"Robot Shelter": 2}) {
		t.Errorf("participants = %+v", child.Participants)
	}
	if child.Assertions != 3 {
		t.Errorf("assertions = %d, want 3", child.Assertions)
	}
	if child.Expandables != 1 {
		t.Errorf("expandables = %d, want 1", child.Expandables)
	}
	if !reflect.DeepEqual(child.Epics, []string{"Adoption"}) {
		t.Errorf("epics = %+v", child.Epics)
	}
}

func TestListTestsFilterByState(t *testing.T) {
	out, _, err := listTestsHandlerFor("testdata/bundle", "Failed", false)
	if err != nil {
		t.Fatalf("listTests: %v", err)
	}
	if len(out.Tests) != 1 || out.Tests[0].State != "Failed" {
		t.Errorf("filter Failed returned %+v", out.Tests)
	}
}

func findInvocationBundle(t *testing.T, r Result, method string) Invocation {
	t.Helper()
	for _, tc := range r.Tests {
		if tc.TestMethod == method {
			if len(tc.Invocations) == 0 {
				t.Fatalf("test method %q has no invocations", method)
			}
			return tc.Invocations[0]
		}
	}
	t.Fatalf("test method %q not found", method)
	return Invocation{}
}

func findCustomTabBundle(t *testing.T, tabs []CustomTabContent, sourceID string) CustomTabContent {
	t.Helper()
	for _, tab := range tabs {
		if tab.SourceID == sourceID {
			return tab
		}
	}
	t.Fatalf("no custom tab content with sourceId %q among %+v", sourceID, tabs)
	return CustomTabContent{}
}

func TestBundleDecodesCustomTabContents(t *testing.T) {
	r, err := findResult("testdata/bundle", failingClass)
	if err != nil {
		t.Fatalf("findResult: %v", err)
	}

	failed := findInvocationBundle(t, r, "canAdoptAnAvailableRobot")
	appLog := findCustomTabBundle(t, failed.CustomTabContents, "appLog")
	if appLog.Label != "App Log" {
		t.Errorf("appLog label = %q, want %q", appLog.Label, "App Log")
	}
	if appLog.Entries == nil || *appLog.Entries != 3 {
		t.Errorf("appLog entries = %v, want pointer to 3", appLog.Entries)
	}
	if appLog.File == "" {
		t.Errorf("appLog file not set")
	}
	if appLog.Records == "" {
		t.Errorf("appLog records not set")
	}
	if appLog.MediaType != "text/plain" {
		t.Errorf("appLog mediaType = %q, want text/plain", appLog.MediaType)
	}
	if appLog.Identifier == "" {
		t.Errorf("appLog identifier not set")
	}

	auditLog := findCustomTabBundle(t, failed.CustomTabContents, "auditLog")
	if auditLog.Label != "Audit Log" {
		t.Errorf("auditLog label = %q, want %q", auditLog.Label, "Audit Log")
	}
	if auditLog.Entries == nil || *auditLog.Entries != 0 {
		t.Errorf("auditLog entries = %v, want pointer to 0 (present, not absent)", auditLog.Entries)
	}
	if auditLog.File != "" {
		t.Errorf("auditLog file = %q, want empty (no records this run)", auditLog.File)
	}

	passed := findInvocationBundle(t, r, "canCheckAvailabilityOfRobots")
	skippedAppLog := findCustomTabBundle(t, passed.CustomTabContents, "appLog")
	if skippedAppLog.Visibility != "OnlyOnFailure" {
		t.Errorf("passed appLog visibility = %q, want OnlyOnFailure", skippedAppLog.Visibility)
	}
	if skippedAppLog.Entries != nil {
		t.Errorf("passed appLog entries = %v, want nil (absent, not zero)", skippedAppLog.Entries)
	}
	if skippedAppLog.File != "" {
		t.Errorf("passed appLog file = %q, want empty", skippedAppLog.File)
	}
	skippedAuditLog := findCustomTabBundle(t, passed.CustomTabContents, "auditLog")
	if skippedAuditLog.Visibility != "OnlyOnFailure" {
		t.Errorf("passed auditLog visibility = %q, want OnlyOnFailure", skippedAuditLog.Visibility)
	}
}

func TestReadRunMarkerDecodesLogSources(t *testing.T) {
	m, ok := readRunMarker("testdata/bundle")
	if !ok {
		t.Fatalf("readRunMarker: marker not found")
	}
	want := map[string]LogSource{
		"appLog":     {ID: "appLog", File: "app.log", Present: true},
		"auditLog":   {ID: "auditLog", File: "audit.log", Present: true},
		"gatewayLog": {ID: "gatewayLog", File: "gateway.log", Present: false},
	}
	if len(m.LogSources) != len(want) {
		t.Fatalf("got %d log sources, want %d: %+v", len(m.LogSources), len(want), m.LogSources)
	}
	for _, ls := range m.LogSources {
		w, ok := want[ls.ID]
		if !ok {
			t.Errorf("unexpected log source id %q", ls.ID)
			continue
		}
		if ls != w {
			t.Errorf("log source %q = %+v, want %+v", ls.ID, ls, w)
		}
	}
}
