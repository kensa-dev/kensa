package mcp

import (
	"context"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

type listTestsIn struct {
	BundleDir string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
	State     string `json:"state,omitempty" jsonschema:"optional state filter: Passed, Failed, Disabled or Not Executed"`
}
type listTestsOut struct {
	Tests []TestEntry `json:"tests"`
}

// listTestsHandlerFor is the pure core (no MCP types) — directly unit-testable.
func listTestsHandlerFor(bundle, state string) (listTestsOut, *mcp.CallToolResult, error) {
	all, err := readAllIndices(bundle)
	if err != nil {
		return listTestsOut{}, nil, err
	}
	if state == "" {
		return listTestsOut{Tests: all}, nil, nil
	}
	want := normaliseState(state)
	var filtered []TestEntry
	for _, t := range all {
		if normaliseState(t.State) == want {
			filtered = append(filtered, t)
		}
	}
	return listTestsOut{Tests: filtered}, nil, nil
}

func listTests(_ context.Context, _ *mcp.CallToolRequest, in listTestsIn) (*mcp.CallToolResult, listTestsOut, error) {
	out, res, err := listTestsHandlerFor(in.BundleDir, in.State)
	return res, out, err
}

type getTestIn struct {
	BundleDir string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
	ID        string `json:"id" jsonschema:"test class id, e.g. com.example.PaymentTest; a child id of the form <class>:<method> resolves to its class"`
}

func getTestFor(bundle, id string) (Result, *mcp.CallToolResult, error) {
	r, err := findResult(bundle, id)
	if err != nil {
		return Result{}, nil, err
	}
	return r, nil, nil
}

func getTest(_ context.Context, _ *mcp.CallToolRequest, in getTestIn) (*mcp.CallToolResult, Result, error) {
	out, res, err := getTestFor(in.BundleDir, in.ID)
	return res, out, err
}

type listFailuresIn struct {
	BundleDir string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
}
type listFailuresOut struct {
	Failures []TestEntry `json:"failures"`
}

func listFailuresFor(bundle string) (listFailuresOut, *mcp.CallToolResult, error) {
	all, err := readAllIndices(bundle)
	if err != nil {
		return listFailuresOut{}, nil, err
	}
	var failures []TestEntry
	for _, t := range all {
		if t.State == "Failed" {
			failures = append(failures, t)
		}
	}
	return listFailuresOut{Failures: failures}, nil, nil
}

func listFailures(_ context.Context, _ *mcp.CallToolRequest, in listFailuresIn) (*mcp.CallToolResult, listFailuresOut, error) {
	out, res, err := listFailuresFor(in.BundleDir)
	return res, out, err
}

type failureEvidenceIn struct {
	BundleDir string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
	ID        string `json:"id" jsonschema:"test class id, e.g. com.example.PaymentTest; a child id of the form <class>:<method> resolves to its class"`
}
type failureEvidenceOut struct {
	TestClass       string `json:"testClass"`
	TestMethod      string `json:"testMethod"`
	FailingSentence string `json:"failingSentence"`
	Exception       string `json:"exception"`
	State           string `json:"state"`
}

func failureEvidenceFor(bundle, id string) (failureEvidenceOut, *mcp.CallToolResult, error) {
	r, err := findResult(bundle, id)
	if err != nil {
		return failureEvidenceOut{}, nil, err
	}
	out := failureEvidenceOut{TestClass: r.TestClass, State: r.State}
	for _, tc := range r.Tests {
		for _, inv := range tc.Invocations {
			if !inv.failed() {
				continue
			}
			out.TestMethod = tc.TestMethod
			out.Exception = inv.ExecutionException.Message
			// The last sentence reached is the one that failed.
			if len(inv.Sentences) > 0 {
				out.FailingSentence = sentenceText(inv.Sentences[len(inv.Sentences)-1])
			}
		}
	}
	return out, nil, nil
}

func failureEvidence(_ context.Context, _ *mcp.CallToolRequest, in failureEvidenceIn) (*mcp.CallToolResult, failureEvidenceOut, error) {
	out, res, err := failureEvidenceFor(in.BundleDir, in.ID)
	return res, out, err
}
