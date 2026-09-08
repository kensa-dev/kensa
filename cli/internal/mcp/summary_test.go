package mcp

import (
	"os"
	"path/filepath"
	"reflect"
	"strings"
	"testing"
)

func TestSuiteSummaryBasicCounts(t *testing.T) {
	out, _, err := suiteSummaryFor("testdata/bundle", 10)
	if err != nil {
		t.Fatalf("suiteSummaryFor: %v", err)
	}
	wantClasses := stateCounts{Passed: 1, Failed: 1, Total: 2}
	if out.Classes != wantClasses {
		t.Errorf("classes = %+v, want %+v", out.Classes, wantClasses)
	}
	wantMethods := stateCounts{Passed: 2, Failed: 1, Total: 3}
	if out.Methods != wantMethods {
		t.Errorf("methods = %+v, want %+v", out.Methods, wantMethods)
	}
	wantFailures := []string{"dev.kensa.example.adoptabot.AdoptionServiceTest:canAdoptAnAvailableRobot"}
	if !reflect.DeepEqual(out.Failures, wantFailures) {
		t.Errorf("failures = %+v, want %+v", out.Failures, wantFailures)
	}
	if out.TotalElapsedMs != 6550 {
		t.Errorf("totalElapsedMs = %d, want 6550", out.TotalElapsedMs)
	}
	if len(out.Slowest) != 3 {
		t.Fatalf("slowest = %+v, want 3 rows", out.Slowest)
	}
	if out.Slowest[0].ElapsedMs != 6200 || out.Slowest[0].Invocations != 1 {
		t.Errorf("slowest[0] = %+v", out.Slowest[0])
	}
	if out.Slowest[2].Invocations != 2 {
		t.Errorf("slowest[2] = %+v", out.Slowest[2])
	}
	wantDurations := []durationBucket{
		{Label: "<100ms", Count: 0},
		{Label: "100ms–1s", Count: 2},
		{Label: "1–5s", Count: 0},
		{Label: "5–30s", Count: 1},
		{Label: ">30s", Count: 0},
	}
	if !reflect.DeepEqual(out.Durations, wantDurations) {
		t.Errorf("durations = %+v, want %+v", out.Durations, wantDurations)
	}
	if len(out.ByPackage) != 1 {
		t.Fatalf("byPackage = %+v", out.ByPackage)
	}
	wantPkg := groupCounts{Key: "dev.kensa.example.adoptabot", stateCounts: stateCounts{Passed: 2, Failed: 1, Total: 3}}
	if out.ByPackage[0] != wantPkg {
		t.Errorf("byPackage[0] = %+v, want %+v", out.ByPackage[0], wantPkg)
	}
	wantParticipants := []groupCount{{Key: "Robot Shelter", Count: 2}}
	if !reflect.DeepEqual(out.Participants, wantParticipants) {
		t.Errorf("participants = %+v, want %+v", out.Participants, wantParticipants)
	}
	if len(out.ByTag) != 0 {
		t.Errorf("byTag = %+v, want empty: fixture has no tags", out.ByTag)
	}
	if out.RunState != runComplete {
		t.Errorf("runState = %q, want %q", out.RunState, runComplete)
	}
}

func TestSuiteSummarySlowestLimit(t *testing.T) {
	out, _, err := suiteSummaryFor("testdata/bundle", 1)
	if err != nil {
		t.Fatalf("suiteSummaryFor: %v", err)
	}
	if len(out.Slowest) != 1 {
		t.Fatalf("slowest = %+v, want 1 row", out.Slowest)
	}
	if out.Slowest[0].ElapsedMs != 6200 {
		t.Errorf("slowest[0].elapsedMs = %d, want 6200", out.Slowest[0].ElapsedMs)
	}

	def, _, err := suiteSummaryFor("testdata/bundle", 0)
	if err != nil {
		t.Fatalf("suiteSummaryFor: %v", err)
	}
	ten, _, err := suiteSummaryFor("testdata/bundle", 10)
	if err != nil {
		t.Fatalf("suiteSummaryFor: %v", err)
	}
	if !reflect.DeepEqual(def.Slowest, ten.Slowest) {
		t.Errorf("slowest=0 (default) = %+v, want same as slowest=10: %+v", def.Slowest, ten.Slowest)
	}
}

func TestSuiteSummaryRefusesIncompleteRun(t *testing.T) {
	dir := runningBundle(t, os.Getpid())
	_, _, err := suiteSummaryFor(dir, 10)
	if err == nil || !strings.Contains(err.Error(), "in progress") {
		t.Errorf("suiteSummaryFor on running bundle: %v", err)
	}
}

func TestGroupByTagCountsClassTagOnEveryMethod(t *testing.T) {
	class := TestEntry{
		TestClass: "pkg.TaggedTest",
		Tags:      []string{"a"},
		Children: []TestEntry{
			{TestMethod: "one", State: "Passed"},
			{TestMethod: "two", State: "Failed", Tags: []string{"b"}},
		},
	}
	_, _, leaves := classAndMethodCounts([]TestEntry{class})
	byTag := groupBy(leaves, effectiveTags)
	want := []groupCounts{
		{Key: "a", stateCounts: stateCounts{Passed: 1, Failed: 1, Total: 2}},
		{Key: "b", stateCounts: stateCounts{Failed: 1, Total: 1}},
	}
	if !reflect.DeepEqual(byTag, want) {
		t.Errorf("byTag = %+v, want %+v", byTag, want)
	}
}

func TestSummariseLeavesElapsedMsNilWithoutTiming(t *testing.T) {
	e := TestEntry{
		TestClass: "pkg.NoTimingTest",
		Children: []TestEntry{
			{TestMethod: "one", State: "Passed"},
			{TestMethod: "two", State: "Passed"},
		},
	}
	if !summarise(&e) {
		t.Fatal("summarise returned false, want true")
	}
	if e.Methods == nil || e.Methods.Total != 2 {
		t.Errorf("methods = %+v", e.Methods)
	}
	if e.ElapsedMs != nil {
		t.Errorf("elapsedMs = %v, want nil", e.ElapsedMs)
	}
}

func TestSuiteSummaryRunWindowSpansSources(t *testing.T) {
	site := t.TempDir()
	writeFile(t, filepath.Join(site, "manifest.json"), `{"sources":[{"id":"api","url":"sources/api"},{"id":"ui","url":"sources/ui"}]}`)
	writeFile(t, filepath.Join(site, "sources", "api", "indices.json"), indicesJSON(t))
	writeFile(t, filepath.Join(site, "sources", "api", "run.json"), markerJSON("2026-08-27T09:00:00Z", "2026-08-27T09:03:00Z", deadPid))
	writeFile(t, filepath.Join(site, "sources", "ui", "indices.json"), indicesJSON(t))
	writeFile(t, filepath.Join(site, "sources", "ui", "run.json"), markerJSON("2026-08-27T09:01:00Z", "2026-08-27T09:05:00Z", deadPid))

	out, _, err := suiteSummaryFor(site, 10)
	if err != nil {
		t.Fatalf("suiteSummaryFor: %v", err)
	}
	if out.RunState != runComplete {
		t.Errorf("runState = %q, want %q", out.RunState, runComplete)
	}
	if out.RunStartedAt != "2026-08-27T09:00:00Z" {
		t.Errorf("runStartedAt = %q, want the earlier source's start", out.RunStartedAt)
	}
	if out.RunFinishedAt != "2026-08-27T09:05:00Z" {
		t.Errorf("runFinishedAt = %q, want the later source's finish", out.RunFinishedAt)
	}
	if out.RunDuration != "5m0s" {
		t.Errorf("runDuration = %q, want 5m0s", out.RunDuration)
	}
}

func TestSuiteSummaryRunWindowAndDuration(t *testing.T) {
	dir := completeBundle(t)
	out, _, err := suiteSummaryFor(dir, 10)
	if err != nil {
		t.Fatalf("suiteSummaryFor: %v", err)
	}
	if out.RunState != runComplete {
		t.Errorf("runState = %q, want %q", out.RunState, runComplete)
	}
	if out.RunStartedAt != "2026-08-27T09:00:00Z" || out.RunFinishedAt != "2026-08-27T09:03:00Z" {
		t.Errorf("run window = started %q finished %q", out.RunStartedAt, out.RunFinishedAt)
	}
	if out.RunDuration != "3m0s" {
		t.Errorf("runDuration = %q, want 3m0s", out.RunDuration)
	}
}
