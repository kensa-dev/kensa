package mcp

import (
	"context"
	"encoding/json"
	"os"
	"path/filepath"
	"strings"
	"testing"
	"time"
)

func hostMarker(startedAt, finishedAt, host string, pid int) string {
	finished := "null"
	if finishedAt != "" {
		finished = `"` + finishedAt + `"`
	}
	return `{"startedAt":"` + startedAt + `","pid":` + itoa(pid) + `,"hostname":"` + host + `","finishedAt":` + finished + `}`
}

// A JVM killed after indices.json but before the marker was finalised has
// still written the whole run; the wipe at run start guarantees the indices
// belong to this run.
func TestRunInfoUnfinishedMarkerWithIndicesAndDeadPidIsComplete(t *testing.T) {
	dir := runningBundle(t, deadPid)
	writeFile(t, filepath.Join(dir, "indices.json"), indicesJSON(t))
	if info := runInfoOf(dir); info.RunState != runComplete {
		t.Errorf("got %+v", info)
	}
	if info := runInfoOf(runningBundle(t, deadPid)); info.RunState != runAbandoned {
		t.Errorf("without indices: got %+v", info)
	}
}

// A bundle written on another host cannot have its pid probed here.
func TestRunInfoFromAnotherHostDoesNotProbePid(t *testing.T) {
	dir := t.TempDir()
	writeFile(t, filepath.Join(dir, "run.json"), hostMarker("2026-08-27T09:00:00Z", "", "build-agent-7", os.Getpid()))
	writeFile(t, filepath.Join(dir, "results", "a.A.json"), `{}`)
	if info := runInfoOf(dir); info.RunState != runIncomplete {
		t.Errorf("unfinished foreign marker without indices: got %+v", info)
	}
	writeFile(t, filepath.Join(dir, "indices.json"), indicesJSON(t))
	if info := runInfoOf(dir); info.RunState != runComplete {
		t.Errorf("unfinished foreign marker with indices: got %+v", info)
	}
	host, _ := os.Hostname()
	writeFile(t, filepath.Join(dir, "run.json"), hostMarker("2026-08-27T09:00:00Z", "", host, os.Getpid()))
	os.Remove(filepath.Join(dir, "indices.json"))
	if info := runInfoOf(dir); info.RunState != runRunning {
		t.Errorf("same host: got %+v", info)
	}
}

func TestExpandPrefersManifestOverStrayResultsDir(t *testing.T) {
	site := t.TempDir()
	writeFile(t, filepath.Join(site, "manifest.json"), `{"sources":[{"id":"api","url":"sources/api"}]}`)
	writeFile(t, filepath.Join(site, "sources", "api", "indices.json"), indicesJSON(t))
	if err := os.MkdirAll(filepath.Join(site, "results"), 0o755); err != nil {
		t.Fatal(err)
	}
	refs, err := expand(site)
	if err != nil || len(refs) != 1 || refs[0].Source != "api" {
		t.Errorf("refs=%v err=%v", refs, err)
	}
}

func TestExpandRejectsEmptyResultsDir(t *testing.T) {
	dir := t.TempDir()
	if err := os.MkdirAll(filepath.Join(dir, "results"), 0o755); err != nil {
		t.Fatal(err)
	}
	_, err := expand(dir)
	if err == nil || !strings.Contains(err.Error(), "not a Kensa output directory") {
		t.Errorf("got %v", err)
	}
}

func TestMissingSiteSourceIsNamedNotMistakenForARun(t *testing.T) {
	site := t.TempDir()
	writeFile(t, filepath.Join(site, "manifest.json"), `{"sources":[{"id":"api","url":"sources/api"},{"id":"ui","url":"sources/ui"}]}`)
	writeFile(t, filepath.Join(site, "sources", "api", "indices.json"), indicesJSON(t))
	_, _, err := listFailuresFor(site)
	if err == nil || !strings.Contains(err.Error(), "ui") || !strings.Contains(err.Error(), "sources/ui") || strings.Contains(err.Error(), "await_results") {
		t.Errorf("listFailures: %v", err)
	}
	_, _, err = runStatusFor(site)
	if err == nil || !strings.Contains(err.Error(), "ui") {
		t.Errorf("runStatus: %v", err)
	}
	_, err = awaitResultsFor(context.Background(), site, time.Second, nil)
	if err == nil || !strings.Contains(err.Error(), "ui") {
		t.Errorf("await: %v", err)
	}
}

func TestSiteListingErrorNamesEachIncompleteSource(t *testing.T) {
	fixNow(t, time.Date(2026, 8, 27, 9, 5, 0, 0, time.UTC))
	site := t.TempDir()
	writeFile(t, filepath.Join(site, "manifest.json"), `{"sources":[{"id":"api","url":"sources/api"},{"id":"ui","url":"sources/ui"},{"id":"db","url":"sources/db"}]}`)
	writeFile(t, filepath.Join(site, "sources", "api", "run.json"), markerJSON("2026-08-27T08:00:00Z", "", deadPid))
	writeFile(t, filepath.Join(site, "sources", "ui", "run.json"), markerJSON("2026-08-27T09:04:00Z", "", os.Getpid()))
	writeFile(t, filepath.Join(site, "sources", "db", "indices.json"), indicesJSON(t))
	_, _, err := listFailuresFor(site)
	if err == nil {
		t.Fatal("expected error")
	}
	msg := err.Error()
	for _, want := range []string{"api: abandoned", "ui: running", "await_results", "re-run"} {
		if !strings.Contains(msg, want) {
			t.Errorf("error lacks %q: %s", want, msg)
		}
	}
	if strings.Contains(msg, "db") {
		t.Errorf("complete source named in error: %s", msg)
	}
}

// An abandoned source from an earlier run must not stop await_results from
// returning when the run that is actually in progress completes.
func TestAwaitResultsCompletesDespiteAbandonedSibling(t *testing.T) {
	site := t.TempDir()
	writeFile(t, filepath.Join(site, "manifest.json"), `{"sources":[{"id":"api","url":"sources/api"},{"id":"ui","url":"sources/ui"}]}`)
	writeFile(t, filepath.Join(site, "sources", "api", "run.json"), markerJSON("2026-08-27T08:00:00Z", "", deadPid))
	writeFile(t, filepath.Join(site, "sources", "api", "results", "a.json"), `{}`)
	writeFile(t, filepath.Join(site, "sources", "ui", "run.json"), markerJSON("2026-08-27T09:04:00Z", "", os.Getpid()))
	go func() {
		time.Sleep(150 * time.Millisecond)
		writeFile(t, filepath.Join(site, "sources", "ui", "indices.json"), indicesJSON(t))
		writeFile(t, filepath.Join(site, "sources", "ui", "run.json"), markerJSON("2026-08-27T09:04:00Z", "2026-08-27T09:06:00Z", os.Getpid()))
	}()
	out, err := awaitResultsFor(context.Background(), site, 5*time.Second, nil)
	if err != nil {
		t.Fatal(err)
	}
	if !out.Completed || out.RunState != runAbandoned {
		t.Errorf("got %+v", out)
	}
}

func TestListingsNoLongerCarryAConstantRunState(t *testing.T) {
	out, _, err := listFailuresFor(completeBundle(t))
	if err != nil {
		t.Fatal(err)
	}
	b, _ := json.Marshal(out)
	if strings.Contains(string(b), "runState") {
		t.Errorf("listing output should not carry runState: %s", b)
	}
	if out.BundleWrittenAt == "" {
		t.Error("bundleWrittenAt missing")
	}
}

func TestTestFrameHandlesBacktickNamesModulePrefixesAndHelpers(t *testing.T) {
	trace := "x.Err: boom\n" +
		"\tat app//other.Helper.check(Helper.kt:9)\n" +
		"\tat acme@1.0/com.acme.FooTest.a client requests to adopt$lambda$4(FooTest.kt:271)\n" +
		"\tat com.acme.FooTest.customer can adopt an available robot$lambda$2(Foo-Test.kt:158)\n" +
		"\tat com.acme.FooTest.customer can adopt an available robot(FooTest.kt:157)\n" +
		"\tat java.base/java.lang.reflect.Method.invoke(Method.java:580)\n"
	f := testFrames(trace, "com.acme.FooTest")
	if f.deepest != "FooTest.kt:271" {
		t.Errorf("deepest = %q", f.deepest)
	}
	if line := f.methodLine("customer can adopt an available robot"); line != 158 {
		t.Errorf("method line = %d, want 158 (deepest frame inside the test method)", line)
	}
	if line := f.methodLine("somethingElse"); line != 157 {
		t.Errorf("no method match should fall back to the outermost test-class frame, got %d", line)
	}
}

// A failure thrown from a helper declared below the test methods must still be
// attributed to the sentence in the test method that called it.
func TestFailureEvidenceAttributesHelperFailuresToTheCallingSentence(t *testing.T) {
	dir := t.TempDir()
	b, _ := os.ReadFile("testdata/multi/results/feature.migrate.StartMigrateOutHappyPathTest.json")
	s := strings.Replace(string(b),
		`\tat feature.migrate.StartMigrateOutHappyPathTest.handlesFastweb$lambda$3(StartMigrateOutHappyPathTest.kt:109)\n\tat feature.migrate.StartMigrateOutHappyPathTest.handlesFastweb(StartMigrateOutHappyPathTest.kt:107)`,
		`\tat feature.migrate.StartMigrateOutHappyPathTest.startMigrateOut$lambda$1(StartMigrateOutHappyPathTest.kt:271)\n\tat feature.migrate.StartMigrateOutHappyPathTest.handlesFastweb(StartMigrateOutHappyPathTest.kt:104)`, 1)
	if s == string(b) {
		t.Fatal("fixture replace did not apply")
	}
	writeFile(t, filepath.Join(dir, "indices.json"), indicesJSON(t))
	writeFile(t, filepath.Join(dir, "results", "feature.migrate.StartMigrateOutHappyPathTest.json"), s)
	ev, _, err := failureEvidenceFor(dir, multiClass)
	if err != nil {
		t.Fatal(err)
	}
	f := ev.Failures[0]
	if f.SourceLocation != "StartMigrateOutHappyPathTest.kt:271" {
		t.Errorf("sourceLocation = %q", f.SourceLocation)
	}
	if f.FailingSentenceLine != 103 || !strings.HasPrefix(f.FailingSentence, "When") {
		t.Errorf("failing sentence = %d %q, want the When at 103", f.FailingSentenceLine, f.FailingSentence)
	}
}

func TestSameHostComparesCaseInsensitivelyWithoutDomain(t *testing.T) {
	cases := [][2]string{{"PAULS-LAPTOP", "Pauls-Laptop"}, {"agent7.example.com", "agent7"}, {"", "anything"}}
	for _, c := range cases {
		if !sameHost(c[0], c[1]) {
			t.Errorf("sameHost(%q, %q) = false", c[0], c[1])
		}
	}
	if sameHost("agent7", "agent8") {
		t.Error("different hosts compared equal")
	}
}

func TestRunInfoIndicesWinOverALivePid(t *testing.T) {
	dir := runningBundle(t, os.Getpid())
	writeFile(t, filepath.Join(dir, "indices.json"), indicesJSON(t))
	if info := runInfoOf(dir); info.RunState != runComplete {
		t.Errorf("got %+v", info)
	}
}

// Kensa before 0.9.2 writes no marker: the wipe makes the bundle incomplete
// and only indices.json makes it complete again. The wait must not return in
// between.
func TestAwaitResultsLegacyBundleWaitsForIndices(t *testing.T) {
	dir := legacyBundle(t, true)
	old := time.Now().Add(-time.Hour)
	os.Chtimes(filepath.Join(dir, "indices.json"), old, old)
	go func() {
		time.Sleep(150 * time.Millisecond)
		os.RemoveAll(filepath.Join(dir, "results"))
		os.Remove(filepath.Join(dir, "indices.json"))
		time.Sleep(900 * time.Millisecond)
		writeFile(t, filepath.Join(dir, "results", "a.json"), `{}`)
		time.Sleep(900 * time.Millisecond)
		writeFile(t, filepath.Join(dir, "indices.json"), indicesJSON(t))
	}()
	started := time.Now()
	out, err := awaitResultsFor(context.Background(), dir, 10*time.Second, nil)
	if err != nil {
		t.Fatal(err)
	}
	if !out.Completed || out.RunState != runComplete {
		t.Errorf("got %+v", out)
	}
	if time.Since(started) < 1900*time.Millisecond {
		t.Errorf("returned after %v, before the indices were written", time.Since(started))
	}
}

// ResultWriter.init deletes the bundle tree and recreates the directory
// before writing the marker; a poll in that window must not abort the wait.
func TestAwaitResultsSurvivesTheWipeWindow(t *testing.T) {
	dir := completeBundle(t)
	go func() {
		time.Sleep(150 * time.Millisecond)
		os.RemoveAll(dir)
		time.Sleep(700 * time.Millisecond)
		os.MkdirAll(dir, 0o755)
		writeFile(t, filepath.Join(dir, "run.json"), markerJSON("2026-08-27T09:10:00Z", "", os.Getpid()))
		time.Sleep(700 * time.Millisecond)
		writeFile(t, filepath.Join(dir, "indices.json"), indicesJSON(t))
		writeFile(t, filepath.Join(dir, "run.json"), markerJSON("2026-08-27T09:10:00Z", "2026-08-27T09:12:00Z", os.Getpid()))
	}()
	out, err := awaitResultsFor(context.Background(), dir, 10*time.Second, nil)
	if err != nil {
		t.Fatalf("wait aborted: %v", err)
	}
	if !out.Completed || out.RunFinishedAt != "2026-08-27T09:12:00Z" {
		t.Errorf("got %+v", out)
	}
}

func TestEmptyExistingSourceDirIsIncompleteNotAnError(t *testing.T) {
	dir := t.TempDir()
	sources, err := statesOf([]bundleRef{{Dir: dir}})
	if err != nil || len(sources) != 1 || sources[0].RunState != runIncomplete {
		t.Errorf("sources=%+v err=%v", sources, err)
	}
}
