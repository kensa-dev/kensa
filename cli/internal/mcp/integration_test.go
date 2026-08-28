package mcp

import (
	"context"
	"encoding/json"
	"testing"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

// newConnectedSession spins up a server with the production tool registration,
// connects an in-process client over the SDK's in-memory transport, and returns
// the live client session. This exercises the SAME registerTools used by Run.
func newConnectedSession(t *testing.T, ctx context.Context) *mcp.ClientSession {
	t.Helper()

	server := mcp.NewServer(&mcp.Implementation{Name: "kensa", Version: "test"}, nil)
	registerTools(server)

	clientTransport, serverTransport := mcp.NewInMemoryTransports()

	if _, err := server.Connect(ctx, serverTransport, nil); err != nil {
		t.Fatalf("server.Connect: %v", err)
	}

	client := mcp.NewClient(&mcp.Implementation{Name: "kensa-test-client", Version: "test"}, nil)
	session, err := client.Connect(ctx, clientTransport, nil)
	if err != nil {
		t.Fatalf("client.Connect: %v", err)
	}
	t.Cleanup(func() { _ = session.Close() })
	return session
}

func TestIntegrationListTests(t *testing.T) {
	ctx := context.Background()
	session := newConnectedSession(t, ctx)

	res, err := session.CallTool(ctx, &mcp.CallToolParams{
		Name:      "list_tests",
		Arguments: map[string]any{"bundle_dir": "testdata/bundle"},
	})
	if err != nil {
		t.Fatalf("CallTool list_tests: %v", err)
	}
	if res.IsError {
		t.Fatalf("list_tests returned error result: %+v", res.Content)
	}

	var out listTestsOut
	decodeStructured(t, res, &out)
	if len(out.Tests) != 2 {
		t.Fatalf("list_tests returned %d test classes, want 2", len(out.Tests))
	}
}

// Exercises the whole path for the triage tool — handler, output schema
// validation and the wire encoding — against a fixture taken from real
// kensa-output.
func TestIntegrationFailureEvidence(t *testing.T) {
	ctx := context.Background()
	session := newConnectedSession(t, ctx)

	res, err := session.CallTool(ctx, &mcp.CallToolParams{
		Name:      "failure_evidence",
		Arguments: map[string]any{"bundle_dir": "testdata/bundle", "id": failingClass},
	})
	if err != nil {
		t.Fatalf("CallTool failure_evidence: %v", err)
	}
	if res.IsError {
		t.Fatalf("failure_evidence returned error result: %+v", res.Content)
	}

	var out failureEvidenceOut
	decodeStructured(t, res, &out)
	if len(out.Failures) != 1 || out.Failures[0].Exception == "" {
		t.Fatalf("failures = %+v", out.Failures)
	}
	if out.Failures[0].FailingSentence != "Then the response should have status BAD_REQUEST" {
		t.Errorf("failingSentence = %q", out.Failures[0].FailingSentence)
	}
}

// A project using none of the optional idioms still has to produce a valid
// profile: nil Go slices marshal to JSON null, which fails output-schema
// validation for an array-typed property.
func TestIntegrationStyleProfileOnProjectWithNoIdioms(t *testing.T) {
	ctx := context.Background()
	session := newConnectedSession(t, ctx)

	res, err := session.CallTool(ctx, &mcp.CallToolParams{
		Name:      "style_profile",
		Arguments: map[string]any{"project_dir": "testdata/bareproj", "no_cache": true},
	})
	if err != nil {
		t.Fatalf("CallTool style_profile: %v", err)
	}
	if res.IsError {
		t.Fatalf("style_profile returned error result: %+v", res.Content)
	}

	var out StyleProfile
	decodeStructured(t, res, &out)
	if out.Framework != "junit5" {
		t.Errorf("framework = %q, want junit5", out.Framework)
	}
}

func TestIntegrationServerInfo(t *testing.T) {
	ctx := context.Background()
	session := newConnectedSession(t, ctx)

	res, err := session.CallTool(ctx, &mcp.CallToolParams{
		Name:      "server_info",
		Arguments: map[string]any{},
	})
	if err != nil {
		t.Fatalf("CallTool server_info: %v", err)
	}
	if res.IsError {
		t.Fatalf("server_info returned error result: %+v", res.Content)
	}

	var out serverInfoOut
	decodeStructured(t, res, &out)
	if out.Name != "kensa" {
		t.Fatalf("server_info name = %q, want %q", out.Name, "kensa")
	}
}

func TestIntegrationListsNineTools(t *testing.T) {
	ctx := context.Background()
	session := newConnectedSession(t, ctx)

	res, err := session.ListTools(ctx, nil)
	if err != nil {
		t.Fatalf("ListTools: %v", err)
	}
	if len(res.Tools) != 9 {
		names := make([]string, len(res.Tools))
		for i, tool := range res.Tools {
			names[i] = tool.Name
		}
		t.Fatalf("ListTools returned %d tools %v, want 9", len(res.Tools), names)
	}
}

// decodeStructured unmarshals a tool result's structured content into dst.
// The ToolHandlerFor machinery populates StructuredContent with the typed Out
// value; over the wire it round-trips through JSON, so re-marshal then decode.
func decodeStructured(t *testing.T, res *mcp.CallToolResult, dst any) {
	t.Helper()
	if res.StructuredContent != nil {
		b, err := json.Marshal(res.StructuredContent)
		if err != nil {
			t.Fatalf("marshal structured content: %v", err)
		}
		if err := json.Unmarshal(b, dst); err != nil {
			t.Fatalf("unmarshal structured content: %v", err)
		}
		return
	}
	// Fallback: the SDK mirrors structured output into a JSON text content block.
	for _, c := range res.Content {
		if tc, ok := c.(*mcp.TextContent); ok {
			if err := json.Unmarshal([]byte(tc.Text), dst); err != nil {
				t.Fatalf("unmarshal text content: %v", err)
			}
			return
		}
	}
	t.Fatalf("no structured or text content in result: %+v", res)
}
