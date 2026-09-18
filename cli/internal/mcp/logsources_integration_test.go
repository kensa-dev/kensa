package mcp

import (
	"context"
	"testing"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

func TestIntegrationListLogSources(t *testing.T) {
	ctx := context.Background()
	session := newConnectedSession(t, ctx)

	res, err := session.CallTool(ctx, &mcp.CallToolParams{
		Name:      "list_log_sources",
		Arguments: map[string]any{"bundle_dir": "testdata/bundle"},
	})
	if err != nil {
		t.Fatalf("CallTool list_log_sources: %v", err)
	}
	if res.IsError {
		t.Fatalf("list_log_sources returned error result: %+v", res.Content)
	}

	var out listLogSourcesOut
	decodeStructured(t, res, &out)
	if len(out.Sources) != 3 {
		t.Fatalf("sources = %+v, want 3", out.Sources)
	}
	if out.Notice != "" {
		t.Errorf("notice = %q, want empty", out.Notice)
	}
}

func TestIntegrationListLogSourcesNoticeForOldBundle(t *testing.T) {
	ctx := context.Background()
	session := newConnectedSession(t, ctx)

	res, err := session.CallTool(ctx, &mcp.CallToolParams{
		Name:      "list_log_sources",
		Arguments: map[string]any{"bundle_dir": "testdata/multi"},
	})
	if err != nil {
		t.Fatalf("CallTool list_log_sources: %v", err)
	}
	if res.IsError {
		t.Fatalf("list_log_sources returned error result: %+v", res.Content)
	}

	var out listLogSourcesOut
	decodeStructured(t, res, &out)
	if len(out.Sources) != 0 {
		t.Errorf("sources = %+v, want empty", out.Sources)
	}
	if out.Notice != "this bundle was written before log sources were recorded; needs kensa 1.0.0 or later" {
		t.Errorf("notice = %q", out.Notice)
	}
}
