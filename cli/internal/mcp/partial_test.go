package mcp

import (
	"os"
	"path/filepath"
	"testing"
)

func copyResult(t *testing.T, dir, class string) {
	t.Helper()
	b, err := os.ReadFile(filepath.Join("testdata/bundle/results", class+".json"))
	if err != nil {
		t.Fatal(err)
	}
	writeFile(t, filepath.Join(dir, "results", class+".json"), string(b))
}

func liveBundle(t *testing.T, pid int) string {
	dir := t.TempDir()
	writeFile(t, filepath.Join(dir, "run.json"), markerJSON("2026-08-27T09:00:00Z", "", pid))
	copyResult(t, dir, failingClass)
	copyResult(t, dir, passingClass)
	return dir
}

func TestPartialFailuresReadWrittenResults(t *testing.T) {
	out, _, err := listFailuresFor(liveBundle(t, os.Getpid()), true)
	if err != nil {
		t.Fatalf("listFailures partial: %v", err)
	}
	if !out.Partial || out.RunState != runRunning {
		t.Errorf("partial = %v, runState = %q", out.Partial, out.RunState)
	}
	if len(out.Failures) != 1 || out.Failures[0].ID != failingClass {
		t.Fatalf("failures = %+v", out.Failures)
	}
	f := out.Failures[0]
	if f.Methods == nil || f.Methods.Failed != 1 || f.Methods.Total != 2 {
		t.Errorf("methods = %+v", f.Methods)
	}
	if len(f.Children) != 2 || f.Children[1].ID != failingClass+":canAdoptAnAvailableRobot" || f.Children[1].State != "Failed" {
		t.Errorf("children = %+v", f.Children)
	}
}

func TestPartialFailuresSkipResultBeingWritten(t *testing.T) {
	dir := liveBundle(t, os.Getpid())
	writeFile(t, filepath.Join(dir, "results", "c.C.json"), `{"testClass": "c.C", "sta`)
	out, _, err := listFailuresFor(dir, true)
	if err != nil {
		t.Fatalf("listFailures partial: %v", err)
	}
	if len(out.Failures) != 1 {
		t.Errorf("failures = %+v", out.Failures)
	}
}

func TestPartialFailuresOfAbandonedRun(t *testing.T) {
	out, _, err := listFailuresFor(liveBundle(t, deadPid), true)
	if err != nil {
		t.Fatalf("listFailures partial: %v", err)
	}
	if !out.Partial || out.RunState != runAbandoned || len(out.Failures) != 1 {
		t.Errorf("got %+v", out)
	}
}

func TestPartialOnCompleteBundleIsTheFullListing(t *testing.T) {
	dir := completeBundle(t)
	full, _, err := listFailuresFor(dir, false)
	if err != nil {
		t.Fatal(err)
	}
	out, _, err := listFailuresFor(dir, true)
	if err != nil {
		t.Fatal(err)
	}
	if out.Partial || out.RunState != "" || out.BundleWrittenAt != full.BundleWrittenAt || len(out.Failures) != len(full.Failures) {
		t.Errorf("partial on complete = %+v, full = %+v", out, full)
	}
}

func TestPartialFailuresFallBackWhenIndicesAreMidWrite(t *testing.T) {
	dir := liveBundle(t, os.Getpid())
	writeFile(t, filepath.Join(dir, "indices.json"), `{"indices": [`)
	out, _, err := listFailuresFor(dir, true)
	if err != nil {
		t.Fatalf("listFailures partial: %v", err)
	}
	if !out.Partial || len(out.Failures) != 1 {
		t.Errorf("got %+v", out)
	}
}
