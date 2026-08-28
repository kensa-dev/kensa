package mcp

import (
	"context"
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"sync"
	"time"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

// Run states, in order of how much the caller can trust the bundle.
const (
	runComplete   = "complete"   // the results are the whole run
	runRunning    = "running"    // run.json without finishedAt and the JVM is alive on this host
	runAbandoned  = "abandoned"  // run.json without finishedAt, the JVM is gone, and no indices.json
	runIncomplete = "incomplete" // results/ without indices.json and no way to tell: no run.json (Kensa before 0.9.2), or a marker from another host
)

// runMarker is run.json as Kensa core writes it: startedAt, pid and hostname
// when the first test starts, finishedAt once the whole report is on disk.
type runMarker struct {
	StartedAt  string `json:"startedAt"`
	FinishedAt string `json:"finishedAt"`
	Pid        int    `json:"pid"`
	Hostname   string `json:"hostname"`
}

// runInfo describes the state of the run that produced a bundle.
type runInfo struct {
	RunState      string `json:"runState"`
	RunStartedAt  string `json:"runStartedAt,omitempty"`
	RunFinishedAt string `json:"runFinishedAt,omitempty"`
	// RunAge is how long ago the run started, for runs that have not finished.
	RunAge string `json:"runAge,omitempty"`
	// ClassesWritten counts results/*.json so far, for runs that have not finished.
	ClassesWritten int `json:"classesWritten,omitempty"`
	Pid            int `json:"pid,omitempty"`
}

// sourceRun is the run state of one resolved bundle.
type sourceRun struct {
	Source string `json:"source,omitempty"`
	Dir    string `json:"dir"`
	runInfo
}

// bundleShape is what one stat pass over a directory finds. Every reader
// (expand, run state, freshness, the await token) works from it.
type bundleShape struct {
	exists     bool
	indicesAt  time.Time // zero when indices.json is absent
	marker     runMarker
	hasMarker  bool
	classFiles int // results/*.json
}

func probe(dir string) bundleShape {
	var s bundleShape
	s.exists = isDir(dir)
	if fi, err := os.Stat(filepath.Join(dir, "indices.json")); err == nil {
		s.indicesAt = fi.ModTime()
	}
	if b, err := os.ReadFile(filepath.Join(dir, "run.json")); err == nil {
		var m runMarker
		if json.Unmarshal(b, &m) == nil {
			s.marker, s.hasMarker = m, true
		}
	}
	matches, _ := filepath.Glob(filepath.Join(dir, "results", "*.json"))
	s.classFiles = len(matches)
	return s
}

func (s bundleShape) hasIndices() bool { return !s.indicesAt.IsZero() }

// isBundle reports whether the directory is a Kensa bundle: marked as one by
// indices.json or run.json, or (Kensa before 0.9.2, mid-run) holding at least
// one class result. A stray empty results/ does not count.
func (s bundleShape) isBundle() bool { return s.hasIndices() || s.hasMarker || s.classFiles > 0 }

// finishedAt is when the run finished: the marker's finishedAt where there is
// one, else the write time of indices.json. Zero for an unfinished run.
func (s bundleShape) finishedAt() time.Time {
	if s.hasMarker && s.marker.FinishedAt != "" {
		if t, err := time.Parse(time.RFC3339Nano, s.marker.FinishedAt); err == nil {
			return t
		}
	}
	return s.indicesAt
}

func readRunMarker(dir string) (runMarker, bool) {
	s := probe(dir)
	return s.marker, s.hasMarker
}

var localHostname = sync.OnceValue(func() string {
	h, _ := os.Hostname()
	return h
})

// sameHost compares host names as the two sides report them: core may write
// COMPUTERNAME (upper-cased NetBIOS name) or a fully qualified name, Go's
// os.Hostname may not. An empty name on the marker means unknown, and is
// taken as local.
func sameHost(marker, local string) bool {
	short := func(h string) string {
		h, _, _ = strings.Cut(h, ".")
		return strings.ToLower(h)
	}
	return marker == "" || short(marker) == short(local)
}

// runInfoOf reads the state of one bundle directory.
func runInfoOf(dir string) runInfo {
	return probe(dir).runInfo()
}

func (s bundleShape) runInfo() runInfo {
	if !s.hasMarker {
		if s.hasIndices() {
			return runInfo{RunState: runComplete}
		}
		return runInfo{RunState: runIncomplete, ClassesWritten: s.classFiles}
	}
	m := s.marker
	info := runInfo{RunStartedAt: m.StartedAt, Pid: m.Pid}
	if m.FinishedAt != "" {
		info.RunState = runComplete
		info.RunFinishedAt = m.FinishedAt
		return info
	}
	// indices.json only appears at the end of a run and the wipe at run start
	// removes the previous one, so its presence beside an unfinished marker
	// means the run finished and died (or is dying) before the marker was
	// finalised. Decide that before probing the pid, which may be recycled.
	if s.hasIndices() {
		info.RunState = runComplete
		return info
	}
	// The pid is only meaningful on the host that ran the tests.
	switch {
	case !sameHost(m.Hostname, localHostname()):
		info.RunState = runIncomplete
	case processAlive(m.Pid):
		info.RunState = runRunning
	default:
		info.RunState = runAbandoned
	}
	info.ClassesWritten = s.classFiles
	if started, err := time.Parse(time.RFC3339Nano, m.StartedAt); err == nil {
		info.RunAge = formatAge(now().Sub(started))
	}
	return info
}

// statesOf reads the run state of every resolved bundle. A site source whose
// directory does not exist is an error naming it, not a run in progress; an
// existing directory with nothing in it yet is a run about to start.
func statesOf(refs []bundleRef) ([]sourceRun, error) {
	shapes, err := probeAll(refs)
	if err != nil {
		return nil, err
	}
	return states(refs, shapes), nil
}

func probeAll(refs []bundleRef) ([]bundleShape, error) {
	shapes := make([]bundleShape, 0, len(refs))
	for _, ref := range refs {
		s := probe(ref.Dir)
		if !s.exists {
			return nil, fmt.Errorf("source %q at %s does not exist", ref.Source, ref.Dir)
		}
		shapes = append(shapes, s)
	}
	return shapes, nil
}

func states(refs []bundleRef, shapes []bundleShape) []sourceRun {
	out := make([]sourceRun, 0, len(refs))
	for i, ref := range refs {
		out = append(out, sourceRun{Source: ref.Source, Dir: ref.Dir, runInfo: shapes[i].runInfo()})
	}
	return out
}

// aggregateRun folds per-source states into one: the least trustworthy state
// wins, so a site with one source still running reads as running.
func aggregateRun(sources []sourceRun) runInfo {
	rank := map[string]int{runComplete: 0, runIncomplete: 1, runAbandoned: 2, runRunning: 3}
	var worst runInfo
	for i, s := range sources {
		if i == 0 || rank[s.RunState] > rank[worst.RunState] {
			worst = s.runInfo
		}
	}
	return worst
}

func allComplete(sources []sourceRun) bool {
	for _, s := range sources {
		if s.RunState != runComplete {
			return false
		}
	}
	return true
}

func anyRunning(sources []sourceRun) bool {
	for _, s := range sources {
		if s.RunState == runRunning {
			return true
		}
	}
	return false
}

// runInProgressError explains why a listing cannot be trusted yet, and what
// to do about it.
func runInProgressError(sources []sourceRun) error {
	if len(sources) == 1 {
		return fmt.Errorf("%s; %s", describeSingle(sources[0].runInfo), advice(sources[0].RunState))
	}
	var parts []string
	running, abandoned := false, false
	for _, s := range sources {
		if s.RunState == runComplete {
			continue
		}
		parts = append(parts, fmt.Sprintf("%s: %s (%s)", s.Source, s.RunState, describeRun(s.runInfo)))
		running = running || s.RunState == runRunning
		abandoned = abandoned || s.RunState != runRunning
	}
	var advice []string
	if running {
		advice = append(advice, "call await_results for the running ones")
	}
	if abandoned {
		advice = append(advice, "re-run the tests for the others")
	}
	return fmt.Errorf("sources not complete: %s; %s", strings.Join(parts, "; "), strings.Join(advice, ", "))
}

func describeRun(info runInfo) string {
	written := classes(info.ClassesWritten)
	switch info.RunState {
	case runRunning:
		return fmt.Sprintf("started %s ago, %s written", info.RunAge, written)
	case runAbandoned:
		return fmt.Sprintf("started %s ago, process %d gone, %s written", info.RunAge, info.Pid, written)
	default:
		return fmt.Sprintf("%s written, no indices.json", written)
	}
}

func describeSingle(info runInfo) string {
	switch info.RunState {
	case runRunning:
		return "test run in progress: " + describeRun(info)
	case runAbandoned:
		return "test run never completed: " + describeRun(info)
	default:
		return "bundle is incomplete: " + describeRun(info) + "; either a run is in progress or one was abandoned (no run.json to tell them apart: Kensa before 0.9.2, or a run on another host)"
	}
}

func advice(state string) string {
	switch state {
	case runRunning:
		return "call await_results to wait for it, or run_status to check again"
	case runAbandoned:
		return "re-run the tests"
	default:
		return "call await_results or re-run the tests"
	}
}

func classes(n int) string {
	if n == 1 {
		return "1 class"
	}
	return fmt.Sprintf("%d classes", n)
}

type runStatusIn struct {
	BundleDir string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
}

type runStatusOut struct {
	runInfo
	// Sources is the per-source breakdown for a site root; absent for one bundle.
	Sources []sourceRun `json:"sources,omitempty"`
}

func runStatusFor(bundle string) (runStatusOut, *mcp.CallToolResult, error) {
	refs, err := resolveBundles(bundle)
	if err != nil {
		return runStatusOut{}, nil, err
	}
	sources, err := statesOf(refs)
	if err != nil {
		return runStatusOut{}, nil, err
	}
	out := runStatusOut{runInfo: aggregateRun(sources)}
	if len(sources) > 1 {
		out.Sources = sources
	}
	return out, nil, nil
}

func runStatus(_ context.Context, _ *mcp.CallToolRequest, in runStatusIn) (*mcp.CallToolResult, runStatusOut, error) {
	out, res, err := runStatusFor(in.BundleDir)
	return res, out, err
}

type awaitResultsIn struct {
	BundleDir      string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
	TimeoutSeconds int    `json:"timeout_seconds,omitempty" jsonschema:"how long to wait for the next completed run before giving up; default 600, maximum 3600"`
}

type awaitResultsOut struct {
	runInfo
	// Completed is true when a run finished while waiting.
	Completed bool `json:"completed"`
	// TimedOut is true when no run finished within the timeout; runState is the state at that moment.
	TimedOut bool `json:"timedOut"`
}

const (
	awaitDefaultTimeout = 10 * time.Minute
	awaitMaxTimeout     = time.Hour
	awaitPoll           = 500 * time.Millisecond
	awaitProgressEvery  = 5 * time.Second
)

// completionTokens identifies each source's most recent completion; a change
// in one means that source's run finished, or (going to zero) that a new run
// wiped it.
func completionTokens(shapes []bundleShape) []time.Time {
	out := make([]time.Time, 0, len(shapes))
	for _, s := range shapes {
		out = append(out, s.finishedAt())
	}
	return out
}

// awaitResultsFor waits for the next run to complete: one already in progress
// when called, or one that starts afterwards. Call it straight after
// launching the tests. It returns once every source that changed since the
// call is complete and none is running; a source abandoned by an earlier run
// and untouched since does not hold the wait up, and is reported in the
// returned state. progress, if non-nil, is told periodically that the wait is
// still alive.
func awaitResultsFor(ctx context.Context, bundle string, timeout time.Duration, progress func(elapsed time.Duration, info runInfo)) (awaitResultsOut, error) {
	refs, err := resolveBundles(bundle)
	if err != nil {
		return awaitResultsOut{}, err
	}
	shapes, err := probeAll(refs)
	if err != nil {
		return awaitResultsOut{}, err
	}
	baseline := completionTokens(shapes)
	changed := make([]bool, len(refs))
	start := time.Now()
	deadline := start.Add(timeout)
	lastProgress := start
	for {
		select {
		case <-ctx.Done():
			return awaitResultsOut{}, ctx.Err()
		case <-time.After(awaitPoll):
		}
		// A run start deletes and recreates the directory; a poll landing in
		// that window sees nothing, which is not an error.
		shapes, err = probeAll(refs)
		if err == nil {
			tokens := completionTokens(shapes)
			done := false
			var current []sourceRun
			for i, t := range tokens {
				if !t.Equal(baseline[i]) {
					changed[i] = true
				}
			}
			if anyChanged(changed) {
				current = states(refs, shapes)
				done = !anyRunning(current) && changedComplete(current, changed)
			}
			if done {
				return awaitResultsOut{runInfo: aggregateRun(current), Completed: true}, nil
			}
		}
		if time.Now().After(deadline) {
			return awaitResultsOut{runInfo: aggregateRun(states(refs, shapes)), TimedOut: true}, nil
		}
		if progress != nil && time.Since(lastProgress) >= awaitProgressEvery {
			progress(time.Since(start), aggregateRun(states(refs, shapes)))
			lastProgress = time.Now()
		}
	}
}

func anyChanged(changed []bool) bool {
	for _, c := range changed {
		if c {
			return true
		}
	}
	return false
}

func changedComplete(sources []sourceRun, changed []bool) bool {
	for i, s := range sources {
		if changed[i] && s.RunState != runComplete {
			return false
		}
	}
	return true
}

func awaitResults(ctx context.Context, req *mcp.CallToolRequest, in awaitResultsIn) (*mcp.CallToolResult, awaitResultsOut, error) {
	timeout := awaitDefaultTimeout
	if in.TimeoutSeconds > 0 {
		timeout = time.Duration(in.TimeoutSeconds) * time.Second
	}
	if timeout > awaitMaxTimeout {
		timeout = awaitMaxTimeout
	}
	var progress func(time.Duration, runInfo)
	if token := req.Params.GetProgressToken(); token != nil {
		progress = func(elapsed time.Duration, info runInfo) {
			_ = req.Session.NotifyProgress(ctx, &mcp.ProgressNotificationParams{
				ProgressToken: token,
				Progress:      elapsed.Seconds(),
				Total:         timeout.Seconds(),
				Message:       fmt.Sprintf("waiting for test run: %s, %d classes written", info.RunState, info.ClassesWritten),
			})
		}
	}
	out, err := awaitResultsFor(ctx, in.BundleDir, timeout, progress)
	return nil, out, err
}
