package mcp

import (
	"context"

	"github.com/google/jsonschema-go/jsonschema"
	"github.com/modelcontextprotocol/go-sdk/mcp"
)

type serverInfoIn struct{}
type serverInfoOut struct {
	Name    string `json:"name"`
	Version string `json:"version"`
}

var version = "dev"

func serverInfo(_ context.Context, _ *mcp.CallToolRequest, _ serverInfoIn) (*mcp.CallToolResult, serverInfoOut, error) {
	return nil, serverInfoOut{Name: "kensa", Version: version}, nil
}

// testEntrySchema is an explicit, recursive output schema for the TestEntry
// type. The SDK's schema inference (github.com/google/jsonschema-go) cannot
// infer schemas for self-referential Go types (TestEntry.Children []TestEntry)
// and panics with "cycle detected". We supply the recursive schema by hand
// using $defs + $ref, which the SDK respects in place of inference.
func testEntrySchema(property string) *jsonschema.Schema {
	// nil Go slices marshal to JSON null, so array-typed properties must also
	// permit "null" to satisfy output validation.
	stringArray := &jsonschema.Schema{Types: []string{"array", "null"}, Items: &jsonschema.Schema{Type: "string"}}
	entry := &jsonschema.Schema{
		Type: "object",
		Properties: map[string]*jsonschema.Schema{
			"id":          {Type: "string"},
			"testClass":   {Type: "string"},
			"displayName": {Type: "string"},
			"state":       {Type: "string", Description: "one of Passed, Failed, Disabled, Not Executed"},
			"tags":        stringArray,
			"issues":      stringArray,
			"hasErrors":   {Type: "boolean", Description: "Kensa could not fully parse or render this test; its sentences are incomplete"},
			"source":      {Type: "string", Description: "site-mode source this test came from; absent for a standalone bundle"},
			"children":    {Types: []string{"array", "null"}, Items: &jsonschema.Schema{Ref: "#/$defs/TestEntry"}},
		},
	}
	return &jsonschema.Schema{
		Type: "object",
		Defs: map[string]*jsonschema.Schema{"TestEntry": entry},
		Properties: map[string]*jsonschema.Schema{
			property:          {Types: []string{"array", "null"}, Items: &jsonschema.Schema{Ref: "#/$defs/TestEntry"}},
			"bundleWrittenAt": {Type: "string", Description: "when the newest resolved bundle was written, RFC 3339 UTC; absent if unknown"},
			"bundleAge":       {Type: "string", Description: "age of the bundle relative to now, e.g. 3h12m or 2d1h; a stale bundle reflects an old test run, not the current code"},
		},
	}
}

// registerTools registers all Kensa MCP tools on the given server. It is the
// single source of truth for tool registration, exercised by both Run (production)
// and the integration test.
func registerTools(server *mcp.Server) {
	mcp.AddTool(server, &mcp.Tool{Name: "server_info", Description: "Kensa MCP server name and version"}, serverInfo)
	mcp.AddTool(server, &mcp.Tool{Name: "list_tests", Description: "List tests in a kensa-output bundle, optionally filtered by state", OutputSchema: testEntrySchema("tests")}, listTests)
	mcp.AddTool(server, &mcp.Tool{Name: "get_test", Description: "The result for a test class: per method and invocation, the sentences as text, fixtures, interaction names and any failure with its source location. Pass raw for the file verbatim"}, getTest)
	mcp.AddTool(server, &mcp.Tool{Name: "list_failures", Description: "List failed tests in a kensa-output bundle", OutputSchema: testEntrySchema("failures")}, listFailures)
	mcp.AddTool(server, &mcp.Tool{Name: "failure_evidence", Description: "Every failed method of a test class with its failing sentence, exception message and the source location inside the test that threw"}, failureEvidence)
	mcp.AddTool(server, &mcp.Tool{Name: "captured_interactions", Description: "Every interaction Kensa captured for a test class or one method: actors, request and response bodies, status and headers"}, capturedInteractions)
	mcp.AddTool(server, &mcp.Tool{Name: "run_status", Description: "State of the run that produced a kensa-output bundle: complete, running, abandoned or incomplete, with start and finish times"}, runStatus)
	mcp.AddTool(server, &mcp.Tool{Name: "await_results", Description: "Block until the next test run completes (one in progress now, or one that starts after the call), then report its state; call straight after launching the tests"}, awaitResults)
	mcp.AddTool(server, &mcp.Tool{Name: "style_profile", Description: "Heuristic style profile of a Kensa project: fixtures, MatcherFields, stub helpers, conventions, framework, exemplar"}, styleProfile)
}

// Run builds the MCP server, registers tools, and serves over stdio.
func Run(ctx context.Context, ver string) error {
	if ver != "" {
		version = ver
	}
	server := mcp.NewServer(&mcp.Implementation{Name: "kensa", Version: version}, nil)
	registerTools(server)
	return server.Run(ctx, &mcp.StdioTransport{})
}
