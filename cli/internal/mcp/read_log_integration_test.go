package mcp

import (
	"context"
	"testing"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

func TestIntegrationReadLog(t *testing.T) {
	ctx := context.Background()
	session := newConnectedSession(t, ctx)

	res, err := session.CallTool(ctx, &mcp.CallToolParams{
		Name: "read_log",
		Arguments: map[string]any{
			"bundle_dir":      "testdata/bundle",
			"id":              failedLogID,
			"source":          appLogSource,
			"level":           "ERROR",
			"max_entry_chars": 20,
		},
	})
	if err != nil {
		t.Fatalf("CallTool read_log: %v", err)
	}
	if res.IsError {
		t.Fatalf("read_log returned error result: %+v", res.Content)
	}

	var out readLogOut
	decodeStructured(t, res, &out)
	if out.Source != appLogSource {
		t.Errorf("source = %q, want %q", out.Source, appLogSource)
	}
	if out.Matched != 1 || out.Total != 3 {
		t.Errorf("matched = %d, total = %d, want 1 and 3", out.Matched, out.Total)
	}
	if len(out.Entries) != 1 {
		t.Fatalf("entries = %+v, want one", out.Entries)
	}
	entry := out.Entries[0]
	if entry.N != 1 || !entry.Truncated || len([]rune(entry.Text)) != 20 {
		t.Errorf("entry = %+v, want record 1 truncated to 20 runes", entry)
	}
}
