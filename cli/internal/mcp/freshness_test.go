package mcp

import (
	"os"
	"path/filepath"
	"testing"
	"time"
)

func staleBundle(t *testing.T, writtenAt time.Time) string {
	t.Helper()
	dir := t.TempDir()
	b, err := os.ReadFile("testdata/bundle/indices.json")
	if err != nil {
		t.Fatal(err)
	}
	path := filepath.Join(dir, "indices.json")
	if err := os.WriteFile(path, b, 0o644); err != nil {
		t.Fatal(err)
	}
	if err := os.Chtimes(path, writtenAt, writtenAt); err != nil {
		t.Fatal(err)
	}
	return dir
}

func fixNow(t *testing.T, at time.Time) {
	t.Helper()
	prev := now
	now = func() time.Time { return at }
	t.Cleanup(func() { now = prev })
}

func TestListTestsReportsBundleFreshness(t *testing.T) {
	written := time.Date(2026, 8, 27, 9, 0, 0, 0, time.UTC)
	fixNow(t, written.Add(3*time.Hour+12*time.Minute))
	out, _, err := listTestsHandlerFor(staleBundle(t, written), "")
	if err != nil {
		t.Fatalf("listTests: %v", err)
	}
	if out.BundleWrittenAt != "2026-08-27T09:00:00Z" {
		t.Errorf("bundleWrittenAt = %q", out.BundleWrittenAt)
	}
	if out.BundleAge != "3h12m" {
		t.Errorf("bundleAge = %q", out.BundleAge)
	}
}

func TestListFailuresReportsBundleFreshness(t *testing.T) {
	written := time.Date(2026, 8, 25, 9, 0, 0, 0, time.UTC)
	fixNow(t, written.Add(49*time.Hour))
	out, _, err := listFailuresFor(staleBundle(t, written))
	if err != nil {
		t.Fatalf("listFailures: %v", err)
	}
	if out.BundleWrittenAt != "2026-08-25T09:00:00Z" {
		t.Errorf("bundleWrittenAt = %q", out.BundleWrittenAt)
	}
	if out.BundleAge != "2d1h" {
		t.Errorf("bundleAge = %q", out.BundleAge)
	}
}

func TestFormatAge(t *testing.T) {
	cases := map[time.Duration]string{
		20 * time.Second:              "under 1m",
		5 * time.Minute:               "5m",
		time.Hour:                     "1h",
		3*time.Hour + 12*time.Minute:  "3h12m",
		24 * time.Hour:                "1d",
		49*time.Hour + 30*time.Minute: "2d1h",
	}
	for d, want := range cases {
		if got := formatAge(d); got != want {
			t.Errorf("formatAge(%v) = %q, want %q", d, got, want)
		}
	}
}
