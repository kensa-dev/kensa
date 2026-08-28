package mcp

import (
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"strings"
)

type TestEntry struct {
	ID          string      `json:"id"`
	TestClass   string      `json:"testClass"`
	DisplayName string      `json:"displayName"`
	State       string      `json:"state"`
	Tags        []string    `json:"tags"`
	Issues      []string    `json:"issues"`
	Children    []TestEntry `json:"children"`
	// HasErrors marks a test Kensa could not fully parse or render. It is
	// independent of State: a passing test may still carry parse errors, and
	// its report sentences will be incomplete.
	HasErrors bool `json:"hasErrors,omitempty"`
	// Source names the site-mode source this test came from, and is absent for
	// a standalone bundle. Ids are left untouched; this is additive.
	Source string `json:"source,omitempty"`
}

type indicesFile struct {
	Indices []TestEntry `json:"indices"`
}

func readIndices(bundle string) ([]TestEntry, error) {
	b, err := os.ReadFile(filepath.Join(bundle, "indices.json"))
	if err != nil {
		return nil, err
	}
	var f indicesFile
	if err := json.Unmarshal(b, &f); err != nil {
		return nil, err
	}
	return f.Indices, nil
}

// readAllIndices reads every resolved bundle, tagging entries with the site
// source they came from, and reports how fresh the newest of them is. A
// bundle whose run has not completed is refused with an error saying so,
// since a partial listing would read as a clean one.
func readAllIndices(spec string) ([]TestEntry, bundleFreshness, error) {
	refs, err := resolveBundles(spec)
	if err != nil {
		return nil, bundleFreshness{}, err
	}
	shapes, err := probeAll(refs)
	if err != nil {
		return nil, bundleFreshness{}, err
	}
	sources := states(refs, shapes)
	if !allComplete(sources) {
		return nil, bundleFreshness{}, runInProgressError(sources)
	}
	var all []TestEntry
	for _, s := range sources {
		entries, err := readIndices(s.Dir)
		if err != nil {
			return nil, bundleFreshness{}, err
		}
		for _, e := range entries {
			e.Source = s.Source
			all = append(all, e)
		}
	}
	return all, freshnessOf(shapes), nil
}

type Token struct {
	Types []string `json:"types"`
	Value string   `json:"value"`
}
type Sentence struct {
	LineNumber int     `json:"lineNumber"`
	Tokens     []Token `json:"tokens"`
}
type Exception struct {
	Message    string `json:"message"`
	StackTrace string `json:"stackTrace"`
}

// RenderedValue is one captured value on an interaction: a request body, a
// response body, a URL.
type RenderedValue struct {
	Name     string `json:"name"`
	Value    string `json:"value"`
	Language string `json:"language"`
}

// attributeGroup is a named group of key/value pairs on an interaction, such
// as Status or Headers. The writer encodes the pairs as a list of single-key
// objects.
type attributeGroup struct {
	Name       string           `json:"name"`
	Attributes []map[string]any `json:"attributes"`
}

// Interaction mirrors one entry of an invocation's "capturedInteractions".
type Interaction struct {
	ID       string `json:"id"`
	Name     string `json:"name"`
	From     string `json:"from"`
	To       string `json:"to"`
	Rendered struct {
		Values     []RenderedValue  `json:"values"`
		Attributes []attributeGroup `json:"attributes"`
	} `json:"rendered"`
}

// Invocation mirrors one entry of a test method's "invocations" array.
//
// The writer always emits the executionException key, using an empty object
// for a passing invocation, so a non-nil pointer says nothing about failure —
// only a message does. See JsonTransforms.executionExceptionFrom in core.
type Invocation struct {
	DisplayName          string           `json:"displayName"`
	ElapsedTime          string           `json:"elapsedTime"`
	State                string           `json:"state"`
	Sentences            []Sentence       `json:"sentences"`
	Fixtures             []map[string]any `json:"fixtures"`
	CapturedInteractions []Interaction    `json:"capturedInteractions"`
	ExecutionException   *Exception       `json:"executionException"`
}

// flattenPairs turns the writer's list of single-key objects into one map.
func flattenPairs(pairs []map[string]any) map[string]any {
	if len(pairs) == 0 {
		return nil
	}
	out := make(map[string]any, len(pairs))
	for _, p := range pairs {
		for k, v := range p {
			out[k] = v
		}
	}
	return out
}

func (i Invocation) failed() bool {
	return i.ExecutionException != nil && i.ExecutionException.Message != ""
}

type TestCase struct {
	TestMethod  string       `json:"testMethod"`
	DisplayName string       `json:"displayName"`
	State       string       `json:"state"`
	Invocations []Invocation `json:"invocations"`
}
type Result struct {
	TestClass   string     `json:"testClass"`
	DisplayName string     `json:"displayName"`
	State       string     `json:"state"`
	PackageName string     `json:"packageName"`
	Tests       []TestCase `json:"tests"`
}

// readRawResult loads the result file for a test class as written. Results
// are written per class, but list_tests also hands out child ids of the form
// "<class>:<method>", so a child id resolves to its owning class.
func readRawResult(bundle, id string) ([]byte, error) {
	return os.ReadFile(filepath.Join(bundle, "results", classOf(id)+".json"))
}

// findRawResult locates a test class across every resolved bundle, so a class
// in any source of a site is reachable without naming the source.
func findRawResult(spec, id string) ([]byte, error) {
	refs, err := resolveBundles(spec)
	if err != nil {
		return nil, err
	}
	for _, ref := range refs {
		if b, err := readRawResult(ref.Dir, id); err == nil {
			return b, nil
		}
	}
	return nil, fmt.Errorf("no result for %q in %s", classOf(id), sourceLabels(refs))
}

func findResult(spec, id string) (Result, error) {
	b, err := findRawResult(spec, id)
	if err != nil {
		return Result{}, err
	}
	var r Result
	return r, json.Unmarshal(b, &r)
}

func classOf(id string) string {
	if class, _, found := strings.Cut(id, ":"); found {
		return class
	}
	return id
}

// methodOf returns the method part of a child id, or "" for a class id.
func methodOf(id string) string {
	_, method, _ := strings.Cut(id, ":")
	return method
}

// normaliseState makes state filters forgiving. The writer emits "Not Executed"
// with a space, which a model has no way to guess from the enum name.
func normaliseState(s string) string {
	var b strings.Builder
	for _, r := range strings.ToLower(s) {
		if r != ' ' && r != '_' && r != '-' {
			b.WriteRune(r)
		}
	}
	return b.String()
}

func sentenceText(s Sentence) string {
	var out string
	for _, tk := range s.Tokens {
		if tk.Value != "" {
			if out != "" {
				out += " "
			}
			out += tk.Value
		}
	}
	return out
}
