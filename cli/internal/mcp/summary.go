package mcp

import (
	"context"
	"math"
	"sort"
	"strings"
	"time"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

type stateCounts struct {
	Passed      int `json:"passed"`
	Failed      int `json:"failed"`
	Disabled    int `json:"disabled"`
	NotExecuted int `json:"notExecuted"`
	Total       int `json:"total"`
}

func addState(c *stateCounts, state string) {
	c.Total++
	switch normaliseState(state) {
	case "passed":
		c.Passed++
	case "failed":
		c.Failed++
	case "disabled":
		c.Disabled++
	case "notexecuted":
		c.NotExecuted++
	}
}

type durationBucket struct {
	Label string `json:"label"`
	Count int    `json:"count"`
}

type slowRow struct {
	ID          string `json:"id"`
	TestClass   string `json:"testClass"`
	TestMethod  string `json:"testMethod"`
	DisplayName string `json:"displayName"`
	ElapsedMs   int64  `json:"elapsedMs"`
	Invocations int    `json:"invocations"`
}

type groupCounts struct {
	Key string `json:"key"`
	stateCounts
}

type groupCount struct {
	Key   string `json:"key"`
	Count int    `json:"count"`
}

// leaf pairs a method entry with the class it belongs to, mirroring
// ui/src/lib/overview.ts's LeafMethod: most summary numbers are per-method,
// but need the owning class for its package and tags.
type leaf struct {
	class  TestEntry
	method TestEntry
}

func allHaveTestMethod(children []TestEntry) bool {
	for _, c := range children {
		if c.TestMethod == "" {
			return false
		}
	}
	return true
}

// classAndMethodCounts walks the indices tree the way collectLeaves does in
// overview.ts: a class entry whose children are all methods yields one leaf
// per child and counts as one class; anything else is a container to recurse
// into, not a class to count.
func classAndMethodCounts(entries []TestEntry) (classes, methods stateCounts, leaves []leaf) {
	var walk func(list []TestEntry)
	walk = func(list []TestEntry) {
		for _, n := range list {
			if n.TestClass != "" && len(n.Children) > 0 && allHaveTestMethod(n.Children) {
				addState(&classes, n.State)
				for _, c := range n.Children {
					addState(&methods, c.State)
					leaves = append(leaves, leaf{class: n, method: c})
				}
			} else if len(n.Children) > 0 {
				walk(n.Children)
			}
		}
	}
	walk(entries)
	return classes, methods, leaves
}

// summarise fills Methods and ElapsedMs on a class entry whose children are
// all methods, summing their state and timing. A nested container (children
// present but not all methods) is left alone: no counts, reports false.
func summarise(e *TestEntry) bool {
	if len(e.Children) == 0 || !allHaveTestMethod(e.Children) {
		return false
	}
	var counts stateCounts
	var elapsedMs int64
	for _, c := range e.Children {
		addState(&counts, c.State)
		if elapsed, ok := elapsedOf(c); ok {
			elapsedMs += elapsed
		}
	}
	e.Methods = &counts
	e.ElapsedMs = &elapsedMs
	return true
}

func union(a, b []string) []string {
	seen := make(map[string]bool, len(a)+len(b))
	var out []string
	for _, s := range a {
		if !seen[s] {
			seen[s] = true
			out = append(out, s)
		}
	}
	for _, s := range b {
		if !seen[s] {
			seen[s] = true
			out = append(out, s)
		}
	}
	return out
}

func effectiveTags(l leaf) []string {
	return union(l.class.Tags, l.method.Tags)
}

// packageKey is the class name without its last segment: the full package,
// depth 0 in overview.ts terms.
func packageKey(testClass string) string {
	i := strings.LastIndex(testClass, ".")
	if i < 0 {
		return ""
	}
	return testClass[:i]
}

// groupBy folds leaves into named groups, adding a leaf's method state to
// every key it maps to. Groups sort by total descending, then key ascending,
// matching overview.ts's groupBy.
func groupBy(leaves []leaf, keysOf func(leaf) []string) []groupCounts {
	groups := map[string]*groupCounts{}
	var order []string
	for _, l := range leaves {
		for _, key := range keysOf(l) {
			g, ok := groups[key]
			if !ok {
				g = &groupCounts{Key: key}
				groups[key] = g
				order = append(order, key)
			}
			addState(&g.stateCounts, l.method.State)
		}
	}
	out := make([]groupCounts, 0, len(order))
	for _, key := range order {
		out = append(out, *groups[key])
	}
	sort.Slice(out, func(i, j int) bool {
		if out[i].Total != out[j].Total {
			return out[i].Total > out[j].Total
		}
		return out[i].Key < out[j].Key
	})
	return out
}

// elapsedOf sums a method's timing pairs; ok is false when it carries none.
func elapsedOf(m TestEntry) (elapsed int64, ok bool) {
	if len(m.Timing) == 0 {
		return 0, false
	}
	for _, t := range m.Timing {
		elapsed += t[1]
	}
	return elapsed, true
}

// durationBuckets mirrors overview.ts's BUCKETS: half-open [previous max, max)
// ranges, the last one unbounded above.
var durationBuckets = []struct {
	label string
	maxMs int64
}{
	{"<100ms", 100},
	{"100ms–1s", 1000},
	{"1–5s", 5000},
	{"5–30s", 30000},
	{">30s", math.MaxInt64},
}

func durationsFor(elapsed []int64) []durationBucket {
	out := make([]durationBucket, len(durationBuckets))
	for i, b := range durationBuckets {
		count := 0
		for _, e := range elapsed {
			if e < b.maxMs && (i == 0 || e >= durationBuckets[i-1].maxMs) {
				count++
			}
		}
		out[i] = durationBucket{Label: b.label, Count: count}
	}
	return out
}

func slowestRows(leaves []leaf, limit int) []slowRow {
	if limit <= 0 {
		limit = 10
	}
	var rows []slowRow
	for _, l := range leaves {
		elapsed, ok := elapsedOf(l.method)
		if !ok {
			continue
		}
		rows = append(rows, slowRow{
			ID:          l.method.ID,
			TestClass:   l.class.TestClass,
			TestMethod:  l.method.TestMethod,
			DisplayName: l.method.DisplayName,
			ElapsedMs:   elapsed,
			Invocations: len(l.method.Timing),
		})
	}
	sort.SliceStable(rows, func(i, j int) bool { return rows[i].ElapsedMs > rows[j].ElapsedMs })
	if len(rows) > limit {
		rows = rows[:limit]
	}
	return rows
}

func participantsFor(leaves []leaf) []groupCount {
	counts := map[string]int{}
	var order []string
	for _, l := range leaves {
		for name, n := range l.method.Participants {
			if _, ok := counts[name]; !ok {
				order = append(order, name)
			}
			counts[name] += n
		}
	}
	out := make([]groupCount, 0, len(order))
	for _, name := range order {
		out = append(out, groupCount{Key: name, Count: counts[name]})
	}
	sort.Slice(out, func(i, j int) bool {
		if out[i].Count != out[j].Count {
			return out[i].Count > out[j].Count
		}
		return out[i].Key < out[j].Key
	})
	return out
}

func failureIDs(leaves []leaf) []string {
	out := []string{}
	for _, l := range leaves {
		if normaliseState(l.method.State) == "failed" {
			out = append(out, l.method.ID)
		}
	}
	return out
}

// runDuration is finishedAt - startedAt as Go duration text, only when both
// timestamps are known and parse.
func runDuration(startedAt, finishedAt string) string {
	if startedAt == "" || finishedAt == "" {
		return ""
	}
	start, err := time.Parse(time.RFC3339Nano, startedAt)
	if err != nil {
		return ""
	}
	finish, err := time.Parse(time.RFC3339Nano, finishedAt)
	if err != nil {
		return ""
	}
	return finish.Sub(start).String()
}

type suiteSummaryIn struct {
	BundleDir string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
	Slowest   int    `json:"slowest,omitempty" jsonschema:"how many of the slowest methods to list, default 10"`
}

type suiteSummaryOut struct {
	RunState       string           `json:"runState"`
	RunStartedAt   string           `json:"runStartedAt,omitempty"`
	RunFinishedAt  string           `json:"runFinishedAt,omitempty"`
	RunDuration    string           `json:"runDuration,omitempty"`
	Classes        stateCounts      `json:"classes"`
	Methods        stateCounts      `json:"methods"`
	TotalElapsedMs int64            `json:"totalElapsedMs,omitempty"`
	Durations      []durationBucket `json:"durations,omitempty"`
	Slowest        []slowRow        `json:"slowest,omitempty"`
	Failures       []string         `json:"failures"`
	ByTag          []groupCounts    `json:"byTag,omitempty"`
	ByPackage      []groupCounts    `json:"byPackage,omitempty"`
	Participants   []groupCount     `json:"participants,omitempty"`
	bundleFreshness
}

// suiteSummaryFor is the pure core (no MCP types) — directly unit-testable.
func suiteSummaryFor(bundle string, slowestLimit int) (suiteSummaryOut, *mcp.CallToolResult, error) {
	entries, fresh, err := readAllIndices(bundle)
	if err != nil {
		return suiteSummaryOut{}, nil, err
	}
	refs, err := resolveBundles(bundle)
	if err != nil {
		return suiteSummaryOut{}, nil, err
	}
	sources, err := statesOf(refs)
	if err != nil {
		return suiteSummaryOut{}, nil, err
	}
	run := aggregateRun(sources)

	classes, methods, leaves := classAndMethodCounts(entries)

	out := suiteSummaryOut{
		RunState:        run.RunState,
		RunStartedAt:    run.RunStartedAt,
		RunFinishedAt:   run.RunFinishedAt,
		RunDuration:     runDuration(run.RunStartedAt, run.RunFinishedAt),
		Classes:         classes,
		Methods:         methods,
		Failures:        failureIDs(leaves),
		ByTag:           groupBy(leaves, effectiveTags),
		ByPackage:       groupBy(leaves, func(l leaf) []string { return []string{packageKey(l.class.TestClass)} }),
		Participants:    participantsFor(leaves),
		Slowest:         slowestRows(leaves, slowestLimit),
		bundleFreshness: fresh,
	}

	var elapsed []int64
	for _, l := range leaves {
		if e, ok := elapsedOf(l.method); ok {
			elapsed = append(elapsed, e)
			out.TotalElapsedMs += e
		}
	}
	if len(elapsed) > 0 {
		out.Durations = durationsFor(elapsed)
	}

	return out, nil, nil
}

func suiteSummary(_ context.Context, _ *mcp.CallToolRequest, in suiteSummaryIn) (*mcp.CallToolResult, suiteSummaryOut, error) {
	out, res, err := suiteSummaryFor(in.BundleDir, in.Slowest)
	return res, out, err
}
