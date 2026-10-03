package mcp

import (
	"encoding/json"
	"os"
	"path/filepath"
	"sort"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

func partialFailuresFor(spec string) (listFailuresOut, *mcp.CallToolResult, error) {
	refs, err := resolveBundles(spec)
	if err != nil {
		return listFailuresOut{}, nil, err
	}
	shapes, err := probeAll(refs)
	if err != nil {
		return listFailuresOut{}, nil, err
	}
	sources := states(refs, shapes)
	out := listFailuresOut{Failures: []TestEntry{}}
	for i, ref := range refs {
		var entries []TestEntry
		if sources[i].RunState == runComplete {
			entries, err = readIndices(ref.Dir)
		}
		// indices.json is written in place at run end, so a read can land on half a file.
		if sources[i].RunState != runComplete || err != nil {
			entries = readWrittenResults(ref.Dir)
			out.Partial = true
		}
		for _, e := range entries {
			if e.State != "Failed" {
				continue
			}
			e.Source = ref.Source
			summarise(&e)
			out.Failures = append(out.Failures, e)
		}
	}
	if out.Partial {
		out.RunState = aggregateRun(sources).RunState
	} else {
		out.bundleFreshness = freshnessOf(shapes)
	}
	return out, nil, nil
}

func readWrittenResults(dir string) []TestEntry {
	paths, _ := filepath.Glob(filepath.Join(dir, "results", "*.json"))
	sort.Strings(paths)
	var entries []TestEntry
	for _, p := range paths {
		b, err := os.ReadFile(p)
		if err != nil {
			continue
		}
		var r Result
		// The writer may be part-way through a class file; it reads whole on the next call.
		if json.Unmarshal(b, &r) != nil || r.TestClass == "" {
			continue
		}
		entries = append(entries, entryOf(r))
	}
	return entries
}

func entryOf(r Result) TestEntry {
	e := TestEntry{ID: r.TestClass, TestClass: r.TestClass, DisplayName: r.DisplayName, State: r.State}
	for _, tc := range r.Tests {
		e.Children = append(e.Children, TestEntry{
			ID:          r.TestClass + ":" + tc.TestMethod,
			TestMethod:  tc.TestMethod,
			DisplayName: tc.DisplayName,
			State:       tc.State,
		})
	}
	return e
}
