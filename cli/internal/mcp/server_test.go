package mcp

import "testing"

func TestServerInfoHandler(t *testing.T) {
	_, out, err := serverInfo(nil, nil, serverInfoIn{})
	if err != nil {
		t.Fatalf("serverInfo error: %v", err)
	}
	if out.Name != "kensa" {
		t.Errorf("Name = %q, want kensa", out.Name)
	}
}
