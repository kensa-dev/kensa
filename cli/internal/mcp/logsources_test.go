package mcp

import (
	"encoding/json"
	"testing"
	"time"
)

func TestListLogSourcesReturnsThreeSourcesWithFreshness(t *testing.T) {
	written := time.Date(2026, 9, 17, 9, 15, 5, 0, time.UTC)
	fixNow(t, written.Add(time.Hour))

	out, err := listLogSourcesFor("testdata/bundle", listLogSourcesIn{})
	if err != nil {
		t.Fatalf("listLogSourcesFor: %v", err)
	}
	if out.BundleWrittenAt != "2026-09-17T09:15:05Z" || out.BundleAge != "1h" {
		t.Errorf("freshness = %+v", out.bundleFreshness)
	}
	want := []logSourceRow{
		{LogSource: LogSource{ID: "appLog", File: "app.log", Present: true}},
		{LogSource: LogSource{ID: "auditLog", File: "audit.log", Present: true}},
		{LogSource: LogSource{ID: "gatewayLog", File: "gateway.log", Present: false}},
	}
	if len(out.Sources) != len(want) {
		t.Fatalf("sources = %+v, want %+v", out.Sources, want)
	}
	for i, w := range want {
		if out.Sources[i] != w {
			t.Errorf("sources[%d] = %+v, want %+v", i, out.Sources[i], w)
		}
	}
	if out.Notice != "" {
		t.Errorf("notice = %q, want empty", out.Notice)
	}
}

// Two sources of a site may each register an id of their own choosing, so the
// same id can appear twice. Without the source label the rows are
// indistinguishable, and neither can be read back.
func TestListLogSourcesAcrossSiteLabelsEachRowWithItsSource(t *testing.T) {
	out, err := listLogSourcesFor("testdata/site", listLogSourcesIn{})
	if err != nil {
		t.Fatalf("listLogSourcesFor: %v", err)
	}
	want := []logSourceRow{
		{LogSource: LogSource{ID: "appLog", File: "app.log", Present: true}, Source: "test"},
		{LogSource: LogSource{ID: "auditLog", File: "audit.log", Present: true}, Source: "test"},
		{LogSource: LogSource{ID: "appLog", File: "ui-app.log", Present: true}, Source: "uiTest"},
		{LogSource: LogSource{ID: "browserLog", File: "browser.log", Present: false}, Source: "uiTest"},
	}
	if len(out.Sources) != len(want) {
		t.Fatalf("sources = %+v, want %+v", out.Sources, want)
	}
	for i, w := range want {
		if out.Sources[i] != w {
			t.Errorf("sources[%d] = %+v, want %+v", i, out.Sources[i], w)
		}
	}
}

func TestListLogSourcesNoticeWhenBundlePredatesLogSources(t *testing.T) {
	out, err := listLogSourcesFor("testdata/multi", listLogSourcesIn{})
	if err != nil {
		t.Fatalf("listLogSourcesFor: %v", err)
	}
	if len(out.Sources) != 0 {
		t.Errorf("sources = %+v, want empty", out.Sources)
	}
	if out.Notice != "this bundle was written before log sources were recorded; needs kensa 0.9.6 or later" {
		t.Errorf("notice = %q", out.Notice)
	}
	if b, err := json.Marshal(out.Sources); err != nil || string(b) != "[]" {
		t.Errorf("sources json = %s (err %v), want []", b, err)
	}
}
