package mcp

import (
	"context"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"testing"
	"time"
)

// deadPid is above every platform's pid ceiling, so no process ever owns it.
const deadPid = 999999999

func writeFile(t *testing.T, path, content string) {
	t.Helper()
	if err := os.MkdirAll(filepath.Dir(path), 0o755); err != nil {
		t.Fatal(err)
	}
	if err := os.WriteFile(path, []byte(content), 0o644); err != nil {
		t.Fatal(err)
	}
}

func indicesJSON(t *testing.T) string {
	t.Helper()
	b, err := os.ReadFile("testdata/bundle/indices.json")
	if err != nil {
		t.Fatal(err)
	}
	return string(b)
}

func markerJSON(startedAt, finishedAt string, pid int) string {
	finished := "null"
	if finishedAt != "" {
		finished = `"` + finishedAt + `"`
	}
	return `{"startedAt":"` + startedAt + `","pid":` + itoa(pid) + `,"finishedAt":` + finished + `}`
}

func itoa(i int) string {
	return strconv.Itoa(i)
}

func completeBundle(t *testing.T) string {
	dir := t.TempDir()
	writeFile(t, filepath.Join(dir, "indices.json"), indicesJSON(t))
	writeFile(t, filepath.Join(dir, "run.json"), markerJSON("2026-08-27T09:00:00Z", "2026-08-27T09:03:00Z", deadPid))
	return dir
}

func runningBundle(t *testing.T, pid int) string {
	dir := t.TempDir()
	writeFile(t, filepath.Join(dir, "run.json"), markerJSON("2026-08-27T09:00:00Z", "", pid))
	writeFile(t, filepath.Join(dir, "results", "a.A.json"), `{}`)
	writeFile(t, filepath.Join(dir, "results", "b.B.json"), `{}`)
	return dir
}

func legacyBundle(t *testing.T, complete bool) string {
	dir := t.TempDir()
	if complete {
		writeFile(t, filepath.Join(dir, "indices.json"), indicesJSON(t))
	}
	writeFile(t, filepath.Join(dir, "results", "a.A.json"), `{}`)
	return dir
}

func TestRunInfoComplete(t *testing.T) {
	info := runInfoOf(completeBundle(t))
	if info.RunState != "complete" || info.RunStartedAt != "2026-08-27T09:00:00Z" || info.RunFinishedAt != "2026-08-27T09:03:00Z" {
		t.Errorf("got %+v", info)
	}
}

func TestRunInfoRunningWhenProcessAlive(t *testing.T) {
	fixNow(t, time.Date(2026, 8, 27, 9, 2, 0, 0, time.UTC))
	info := runInfoOf(runningBundle(t, os.Getpid()))
	if info.RunState != "running" || info.ClassesWritten != 2 || info.RunAge != "2m" || info.Pid != os.Getpid() {
		t.Errorf("got %+v", info)
	}
}

func TestRunInfoAbandonedWhenProcessDead(t *testing.T) {
	info := runInfoOf(runningBundle(t, deadPid))
	if info.RunState != "abandoned" || info.ClassesWritten != 2 {
		t.Errorf("got %+v", info)
	}
}

func TestRunInfoLegacyBundles(t *testing.T) {
	if info := runInfoOf(legacyBundle(t, true)); info.RunState != "complete" {
		t.Errorf("legacy complete: got %+v", info)
	}
	if info := runInfoOf(legacyBundle(t, false)); info.RunState != "incomplete" || info.ClassesWritten != 1 {
		t.Errorf("legacy incomplete: got %+v", info)
	}
}

func TestExpandAcceptsBundleInProgress(t *testing.T) {
	for _, dir := range []string{runningBundle(t, os.Getpid()), legacyBundle(t, false)} {
		refs, err := expand(dir)
		if err != nil || len(refs) != 1 {
			t.Errorf("expand(%s): refs=%v err=%v", dir, refs, err)
		}
	}
}

func TestListingsRefuseBundleInProgress(t *testing.T) {
	fixNow(t, time.Date(2026, 8, 27, 9, 2, 0, 0, time.UTC))
	dir := runningBundle(t, os.Getpid())
	_, _, err := listFailuresFor(dir)
	if err == nil || !strings.Contains(err.Error(), "in progress") || !strings.Contains(err.Error(), "2 classes") || !strings.Contains(err.Error(), "await_results") {
		t.Errorf("listFailures on running bundle: %v", err)
	}
	_, _, err = listTestsHandlerFor(dir, "")
	if err == nil || !strings.Contains(err.Error(), "in progress") {
		t.Errorf("listTests on running bundle: %v", err)
	}
	_, _, err = listFailuresFor(runningBundle(t, deadPid))
	if err == nil || !strings.Contains(err.Error(), "never completed") {
		t.Errorf("listFailures on abandoned bundle: %v", err)
	}
	_, _, err = listFailuresFor(legacyBundle(t, false))
	if err == nil || !strings.Contains(err.Error(), "incomplete") {
		t.Errorf("listFailures on legacy incomplete bundle: %v", err)
	}
}

func TestListingsUseMarkerFinishTime(t *testing.T) {
	fixNow(t, time.Date(2026, 8, 27, 10, 3, 0, 0, time.UTC))
	out, _, err := listFailuresFor(completeBundle(t))
	if err != nil {
		t.Fatal(err)
	}
	if out.BundleWrittenAt != "2026-08-27T09:03:00Z" || out.BundleAge != "1h" {
		t.Errorf("got %+v", out.bundleFreshness)
	}
}

func TestRunStatusAggregatesSite(t *testing.T) {
	site := t.TempDir()
	writeFile(t, filepath.Join(site, "manifest.json"), `{"sources":[{"id":"api","url":"sources/api"},{"id":"ui","url":"sources/ui"}]}`)
	writeFile(t, filepath.Join(site, "sources", "api", "indices.json"), indicesJSON(t))
	writeFile(t, filepath.Join(site, "sources", "api", "run.json"), markerJSON("2026-08-27T09:00:00Z", "2026-08-27T09:03:00Z", deadPid))
	writeFile(t, filepath.Join(site, "sources", "ui", "run.json"), markerJSON("2026-08-27T09:04:00Z", "", os.Getpid()))

	out, _, err := runStatusFor(site)
	if err != nil {
		t.Fatal(err)
	}
	if out.RunState != "running" || len(out.Sources) != 2 || out.Sources[0].Source != "api" || out.Sources[0].RunState != "complete" || out.Sources[1].RunState != "running" {
		t.Errorf("got %+v", out)
	}
}

func TestAwaitResultsReturnsWhenANewRunCompletes(t *testing.T) {
	dir := completeBundle(t)
	go func() {
		time.Sleep(150 * time.Millisecond)
		writeFile(t, filepath.Join(dir, "run.json"), markerJSON("2026-08-27T09:10:00Z", "", os.Getpid()))
		time.Sleep(150 * time.Millisecond)
		writeFile(t, filepath.Join(dir, "run.json"), markerJSON("2026-08-27T09:10:00Z", "2026-08-27T09:12:00Z", os.Getpid()))
	}()
	out, err := awaitResultsFor(context.Background(), dir, 5*time.Second, nil)
	if err != nil {
		t.Fatal(err)
	}
	if !out.Completed || out.TimedOut || out.RunFinishedAt != "2026-08-27T09:12:00Z" {
		t.Errorf("got %+v", out)
	}
}

func TestAwaitResultsWaitsForRunAlreadyInProgress(t *testing.T) {
	dir := runningBundle(t, os.Getpid())
	go func() {
		time.Sleep(150 * time.Millisecond)
		writeFile(t, filepath.Join(dir, "indices.json"), indicesJSON(t))
		writeFile(t, filepath.Join(dir, "run.json"), markerJSON("2026-08-27T09:00:00Z", "2026-08-27T09:05:00Z", os.Getpid()))
	}()
	out, err := awaitResultsFor(context.Background(), dir, 5*time.Second, nil)
	if err != nil {
		t.Fatal(err)
	}
	if !out.Completed || out.RunState != "complete" {
		t.Errorf("got %+v", out)
	}
}

func TestAwaitResultsTimesOutWithCurrentState(t *testing.T) {
	out, err := awaitResultsFor(context.Background(), completeBundle(t), 100*time.Millisecond, nil)
	if err != nil {
		t.Fatal(err)
	}
	if out.Completed || !out.TimedOut || out.RunState != "complete" {
		t.Errorf("got %+v", out)
	}
}

func TestAwaitResultsLegacyBundleUsesIndicesWriteTime(t *testing.T) {
	dir := legacyBundle(t, true)
	old := time.Now().Add(-time.Hour)
	if err := os.Chtimes(filepath.Join(dir, "indices.json"), old, old); err != nil {
		t.Fatal(err)
	}
	go func() {
		time.Sleep(150 * time.Millisecond)
		writeFile(t, filepath.Join(dir, "indices.json"), indicesJSON(t))
	}()
	out, err := awaitResultsFor(context.Background(), dir, 5*time.Second, nil)
	if err != nil {
		t.Fatal(err)
	}
	if !out.Completed {
		t.Errorf("got %+v", out)
	}
}
