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
			property: {Types: []string{"array", "null"}, Items: &jsonschema.Schema{Ref: "#/$defs/TestEntry"}},
		},
	}
}

// registerTools registers all Kensa MCP tools on the given server. It is the
// single source of truth for tool registration, exercised by both Run (production)
// and the integration test.
func registerTools(server *mcp.Server) {
	mcp.AddTool(server, &mcp.Tool{Name: "server_info", Description: "Kensa MCP server name and version"}, serverInfo)
	mcp.AddTool(server, &mcp.Tool{Name: "list_tests", Description: "List tests in a kensa-output bundle, optionally filtered by state", OutputSchema: testEntrySchema("tests")}, listTests)
	mcp.AddTool(server, &mcp.Tool{Name: "get_test", Description: "Get the full result JSON for a test class in a kensa-output bundle"}, getTest)
	mcp.AddTool(server, &mcp.Tool{Name: "list_failures", Description: "List failed tests in a kensa-output bundle", OutputSchema: testEntrySchema("failures")}, listFailures)
	mcp.AddTool(server, &mcp.Tool{Name: "failure_evidence", Description: "Failing sentence and exception for a failed test class in a kensa-output bundle"}, failureEvidence)
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
