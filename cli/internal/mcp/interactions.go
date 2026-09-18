package mcp

import (
	"context"
	"fmt"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

type capturedInteractionsIn struct {
	BundleDir     string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
	ID            string `json:"id" jsonschema:"test class id for every method, or a child id of the form <class>:<method> for one method"`
	MaxValueChars int    `json:"max_value_chars,omitempty" jsonschema:"cap on each captured value's length in characters, default 4000; 0 means the default, -1 means unlimited"`
}

// defaultMaxValueChars is the cap applied to a captured value when
// max_value_chars is not given.
const defaultMaxValueChars = 4000

// capturedValue is one captured value on an interaction, truncated to the
// caller's max_value_chars. It mirrors RenderedValue plus truncation
// metadata, kept separate since RenderedValue is shared with bundle.go.
type capturedValue struct {
	Name       string `json:"name"`
	Value      string `json:"value"`
	Language   string `json:"language"`
	Truncated  bool   `json:"truncated,omitempty"`
	FullLength int    `json:"fullLength,omitempty"`
}

// newCapturedValue truncates v.Value to maxChars runes. 0 means
// defaultMaxValueChars; a negative maxChars means unlimited.
func newCapturedValue(v RenderedValue, maxChars int) capturedValue {
	limit := maxChars
	if limit == 0 {
		limit = defaultMaxValueChars
	}
	value, truncated, fullLength := capRunes(v.Value, limit)
	return capturedValue{Name: v.Name, Value: value, Language: v.Language, Truncated: truncated, FullLength: fullLength}
}

// capturedInteraction is one message between two actors with everything
// Kensa captured about it.
type capturedInteraction struct {
	Name   string          `json:"name"`
	From   string          `json:"from"`
	To     string          `json:"to"`
	Values []capturedValue `json:"values"`
	// Attributes groups captured metadata by name, e.g. Status and Headers on
	// an HTTP response.
	Attributes map[string]map[string]any `json:"attributes,omitempty"`
}

type methodInteractions struct {
	TestMethod   string                `json:"testMethod"`
	DisplayName  string                `json:"displayName"`
	Invocation   int                   `json:"invocation"`
	State        string                `json:"state"`
	Interactions []capturedInteraction `json:"interactions"`
}

type capturedInteractionsOut struct {
	TestClass string               `json:"testClass"`
	Methods   []methodInteractions `json:"methods"`
}

func capturedInteractionsFor(bundle, id string, maxValueChars int) (capturedInteractionsOut, *mcp.CallToolResult, error) {
	r, err := findResult(bundle, id)
	if err != nil {
		return capturedInteractionsOut{}, nil, err
	}
	only := methodOf(id)
	out := capturedInteractionsOut{TestClass: r.TestClass, Methods: []methodInteractions{}}
	for _, tc := range r.Tests {
		if only != "" && tc.TestMethod != only {
			continue
		}
		for i, inv := range tc.Invocations {
			m := methodInteractions{TestMethod: tc.TestMethod, DisplayName: tc.DisplayName, Invocation: i, State: inv.State, Interactions: []capturedInteraction{}}
			for _, ci := range inv.CapturedInteractions {
				c := capturedInteraction{Name: ci.Name, From: ci.From, To: ci.To, Values: []capturedValue{}}
				for _, v := range ci.Rendered.Values {
					c.Values = append(c.Values, newCapturedValue(v, maxValueChars))
				}
				for _, g := range ci.Rendered.Attributes {
					flat := flattenPairs(g.Attributes)
					if flat == nil {
						// An empty group (a response with no headers) would
						// marshal as null and fail the output schema; omit it.
						continue
					}
					if c.Attributes == nil {
						c.Attributes = map[string]map[string]any{}
					}
					c.Attributes[g.Name] = flat
				}
				m.Interactions = append(m.Interactions, c)
			}
			out.Methods = append(out.Methods, m)
		}
	}
	if only != "" && len(out.Methods) == 0 {
		return capturedInteractionsOut{}, nil, fmt.Errorf("no method %q in %s", only, r.TestClass)
	}
	return out, nil, nil
}

func capturedInteractions(_ context.Context, _ *mcp.CallToolRequest, in capturedInteractionsIn) (*mcp.CallToolResult, capturedInteractionsOut, error) {
	out, res, err := capturedInteractionsFor(in.BundleDir, in.ID, in.MaxValueChars)
	return res, out, err
}
