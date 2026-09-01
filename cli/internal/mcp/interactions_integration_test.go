package mcp

import (
	"context"
	"testing"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

func TestIntegrationCapturedInteractionsWithEmptyAttributeGroup(t *testing.T) {
	ctx := context.Background()
	session := newConnectedSession(t, ctx)

	res, err := session.CallTool(ctx, &mcp.CallToolParams{
		Name:      "captured_interactions",
		Arguments: map[string]any{"bundle_dir": "testdata/multi", "id": multiClass + ":handlesFiberCop"},
	})
	if err != nil {
		t.Fatalf("CallTool captured_interactions: %v", err)
	}
	if res.IsError {
		t.Fatalf("captured_interactions returned error result: %+v", res.Content)
	}

	var out capturedInteractionsOut
	decodeStructured(t, res, &out)
	resp := out.Methods[0].Interactions[1]
	if _, ok := resp.Attributes["Cookies"]; ok {
		t.Errorf("empty attribute group should be omitted, got %+v", resp.Attributes)
	}
	if resp.Attributes["Status"]["Status"] != "200" || resp.Attributes["Headers"]["content-type"] != "application/json" {
		t.Errorf("sibling groups lost: %+v", resp.Attributes)
	}
}
