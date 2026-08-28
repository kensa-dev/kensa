package mcp

import (
	"context"
	"encoding/json"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

type listTestsIn struct {
	BundleDir string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
	State     string `json:"state,omitempty" jsonschema:"optional state filter: Passed, Failed, Disabled or Not Executed"`
}
type listTestsOut struct {
	Tests []TestEntry `json:"tests"`
	bundleFreshness
}

// listTestsHandlerFor is the pure core (no MCP types) — directly unit-testable.
func listTestsHandlerFor(bundle, state string) (listTestsOut, *mcp.CallToolResult, error) {
	all, fresh, err := readAllIndices(bundle)
	if err != nil {
		return listTestsOut{}, nil, err
	}
	if state == "" {
		return listTestsOut{Tests: all, bundleFreshness: fresh}, nil, nil
	}
	want := normaliseState(state)
	var filtered []TestEntry
	for _, t := range all {
		if normaliseState(t.State) == want {
			filtered = append(filtered, t)
		}
	}
	return listTestsOut{Tests: filtered, bundleFreshness: fresh}, nil, nil
}

func listTests(_ context.Context, _ *mcp.CallToolRequest, in listTestsIn) (*mcp.CallToolResult, listTestsOut, error) {
	out, res, err := listTestsHandlerFor(in.BundleDir, in.State)
	return res, out, err
}

type getTestIn struct {
	BundleDir string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
	ID        string `json:"id" jsonschema:"test class id, e.g. com.example.PaymentTest; a child id of the form <class>:<method> resolves to its class"`
	Raw       bool   `json:"raw,omitempty" jsonschema:"return the result file verbatim, token stream and diagrams included, instead of the rendered form"`
}

// getTestFor returns the rendered form of a class result, or the file
// verbatim when raw is set.
func getTestFor(bundle, id string, raw bool) (any, *mcp.CallToolResult, error) {
	b, err := findRawResult(bundle, id)
	if err != nil {
		return nil, nil, err
	}
	if raw {
		return json.RawMessage(b), nil, nil
	}
	var r Result
	if err := json.Unmarshal(b, &r); err != nil {
		return nil, nil, err
	}
	return render(r), nil, nil
}

func getTest(_ context.Context, _ *mcp.CallToolRequest, in getTestIn) (*mcp.CallToolResult, any, error) {
	out, res, err := getTestFor(in.BundleDir, in.ID, in.Raw)
	return res, out, err
}

type listFailuresIn struct {
	BundleDir string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
}
type listFailuresOut struct {
	Failures []TestEntry `json:"failures"`
	bundleFreshness
}

func listFailuresFor(bundle string) (listFailuresOut, *mcp.CallToolResult, error) {
	all, fresh, err := readAllIndices(bundle)
	if err != nil {
		return listFailuresOut{}, nil, err
	}
	var failures []TestEntry
	for _, t := range all {
		if t.State == "Failed" {
			failures = append(failures, t)
		}
	}
	return listFailuresOut{Failures: failures, bundleFreshness: fresh}, nil, nil
}

func listFailures(_ context.Context, _ *mcp.CallToolRequest, in listFailuresIn) (*mcp.CallToolResult, listFailuresOut, error) {
	out, res, err := listFailuresFor(in.BundleDir)
	return res, out, err
}

type failureEvidenceIn struct {
	BundleDir string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
	ID        string `json:"id" jsonschema:"test class id, e.g. com.example.PaymentTest; a child id of the form <class>:<method> resolves to its class"`
}

// failingMethod is the evidence for one failed invocation.
type failingMethod struct {
	TestMethod  string `json:"testMethod"`
	DisplayName string `json:"displayName"`
	// Invocation is the index within the method, which matters for parameterised tests.
	Invocation int `json:"invocation"`
	// FailingSentence is the Given/When/Then sentence the failure sits in.
	FailingSentence     string `json:"failingSentence"`
	FailingSentenceLine int    `json:"failingSentenceLine,omitempty"`
	Exception           string `json:"exception"`
	// SourceLocation is the deepest stack frame inside the test class itself,
	// e.g. PaymentTest.kt:107: the statement that threw, which may be in a
	// helper the sentence calls. Absent when the trace holds no such frame.
	SourceLocation string `json:"sourceLocation,omitempty"`
}

type failureEvidenceOut struct {
	TestClass string          `json:"testClass"`
	State     string          `json:"state"`
	Failures  []failingMethod `json:"failures"`
	// DistinctExceptions counts different exception messages across Failures;
	// 1 with several failures usually means one cause.
	DistinctExceptions int `json:"distinctExceptions"`
}

func failureEvidenceFor(bundle, id string) (failureEvidenceOut, *mcp.CallToolResult, error) {
	r, err := findResult(bundle, id)
	if err != nil {
		return failureEvidenceOut{}, nil, err
	}
	out := failureEvidenceOut{TestClass: r.TestClass, State: r.State, Failures: []failingMethod{}}
	distinct := map[string]bool{}
	for _, tc := range r.Tests {
		for i, inv := range tc.Invocations {
			if !inv.failed() {
				continue
			}
			f := failingMethod{
				TestMethod:  tc.TestMethod,
				DisplayName: tc.DisplayName,
				Invocation:  i,
				Exception:   inv.ExecutionException.Message,
			}
			fr := testFrames(inv.ExecutionException.StackTrace, r.TestClass)
			f.SourceLocation = fr.deepest
			if s, ok := failingSentence(inv.Sentences, fr.methodLine(tc.TestMethod)); ok {
				f.FailingSentence = sentenceText(s)
				f.FailingSentenceLine = s.LineNumber
			}
			distinct[f.Exception] = true
			out.Failures = append(out.Failures, f)
		}
	}
	out.DistinctExceptions = len(distinct)
	return out, nil, nil
}

// failingSentence picks the sentence a failure belongs to: the last one
// starting at or before the failing source line, or the last sentence when
// the trace gives no line.
func failingSentence(sentences []Sentence, line int) (Sentence, bool) {
	if len(sentences) == 0 {
		return Sentence{}, false
	}
	if line > 0 {
		for i := len(sentences) - 1; i >= 0; i-- {
			if sentences[i].LineNumber <= line {
				return sentences[i], true
			}
		}
	}
	return sentences[len(sentences)-1], true
}

func failureEvidence(_ context.Context, _ *mcp.CallToolRequest, in failureEvidenceIn) (*mcp.CallToolResult, failureEvidenceOut, error) {
	out, res, err := failureEvidenceFor(in.BundleDir, in.ID)
	return res, out, err
}
