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
// source they came from.
func readAllIndices(spec string) ([]TestEntry, error) {
	refs, err := resolveBundles(spec)
	if err != nil {
		return nil, err
	}
	var all []TestEntry
	for _, ref := range refs {
		entries, err := readIndices(ref.Dir)
		if err != nil {
			return nil, err
		}
		for _, e := range entries {
			e.Source = ref.Source
			all = append(all, e)
		}
	}
	return all, nil
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

// Invocation mirrors one entry of a test method's "invocations" array.
//
// The writer always emits the executionException key, using an empty object
// for a passing invocation, so a non-nil pointer says nothing about failure —
// only a message does. See JsonTransforms.executionExceptionFrom in core.
type Invocation struct {
	ElapsedTime        string     `json:"elapsedTime"`
	State              string     `json:"state"`
	Sentences          []Sentence `json:"sentences"`
	ExecutionException *Exception `json:"executionException"`
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

// readResult loads the result file for a test class. Results are written per
// class, but list_tests also hands out child ids of the form
// "<class>:<method>", so a child id resolves to its owning class.
func readResult(bundle, id string) (Result, error) {
	var r Result
	b, err := os.ReadFile(filepath.Join(bundle, "results", classOf(id)+".json"))
	if err != nil {
		return r, err
	}
	return r, json.Unmarshal(b, &r)
}

// findResult locates a test class across every resolved bundle, so a class in
// any source of a site is reachable without naming the source.
func findResult(spec, id string) (Result, error) {
	refs, err := resolveBundles(spec)
	if err != nil {
		return Result{}, err
	}
	for _, ref := range refs {
		if r, err := readResult(ref.Dir, id); err == nil {
			return r, nil
		}
	}
	return Result{}, fmt.Errorf("no result for %q in %s", classOf(id), sourceLabels(refs))
}

func classOf(id string) string {
	if class, _, found := strings.Cut(id, ":"); found {
		return class
	}
	return id
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
