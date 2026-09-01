package mcp

import (
	"context"
	"fmt"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

type capturedInteractionsIn struct {
	BundleDir string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
	ID        string `json:"id" jsonschema:"test class id for every method, or a child id of the form <class>:<method> for one method"`
}

// capturedInteraction is one message between two actors with everything
// Kensa captured about it.
type capturedInteraction struct {
	Name   string          `json:"name"`
	From   string          `json:"from"`
	To     string          `json:"to"`
	Values []RenderedValue `json:"values"`
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

func capturedInteractionsFor(bundle, id string) (capturedInteractionsOut, *mcp.CallToolResult, error) {
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
				c := capturedInteraction{Name: ci.Name, From: ci.From, To: ci.To, Values: ci.Rendered.Values}
				if c.Values == nil {
					c.Values = []RenderedValue{}
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
	out, res, err := capturedInteractionsFor(in.BundleDir, in.ID)
	return res, out, err
}
