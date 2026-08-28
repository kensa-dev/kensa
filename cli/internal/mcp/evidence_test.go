package mcp

import (
	"encoding/json"
	"strings"
	"testing"
)

const multiClass = "feature.migrate.StartMigrateOutHappyPathTest"

func TestFailureEvidenceReportsEveryFailingMethod(t *testing.T) {
	ev, _, err := failureEvidenceFor("testdata/multi", multiClass)
	if err != nil {
		t.Fatal(err)
	}
	if ev.TestClass != multiClass || ev.State != "Failed" {
		t.Errorf("class/state = %q/%q", ev.TestClass, ev.State)
	}
	if len(ev.Failures) != 2 {
		t.Fatalf("got %d failures, want 2: %+v", len(ev.Failures), ev.Failures)
	}
	if ev.Failures[0].TestMethod != "handlesFastweb" || ev.Failures[1].TestMethod != "handlesFiberCop" {
		t.Errorf("methods = %q, %q", ev.Failures[0].TestMethod, ev.Failures[1].TestMethod)
	}
	if ev.DistinctExceptions != 1 {
		t.Errorf("distinctExceptions = %d, want 1", ev.DistinctExceptions)
	}
}

func TestFailureEvidenceLocatesTheFailureInTheTestSource(t *testing.T) {
	ev, _, err := failureEvidenceFor("testdata/multi", multiClass)
	if err != nil {
		t.Fatal(err)
	}
	f := ev.Failures[0]
	if f.SourceLocation != "StartMigrateOutHappyPathTest.kt:109" {
		t.Errorf("sourceLocation = %q", f.SourceLocation)
	}
	if f.FailingSentence != "Then the orchestration result has type SessionCodeValid" {
		t.Errorf("failingSentence = %q", f.FailingSentence)
	}
	if f.FailingSentenceLine != 107 {
		t.Errorf("failingSentenceLine = %d, want 107 (the sentence containing line 109, not the last sentence)", f.FailingSentenceLine)
	}
	if !strings.HasPrefix(f.Exception, "/type: expected") {
		t.Errorf("exception = %q", f.Exception)
	}
}

func TestFailureEvidenceWithoutTestFrameFallsBackToLastSentence(t *testing.T) {
	ev, _, err := failureEvidenceFor("testdata/bundle", failingClass)
	if err != nil {
		t.Fatal(err)
	}
	if len(ev.Failures) != 1 {
		t.Fatalf("got %+v", ev.Failures)
	}
	f := ev.Failures[0]
	if f.SourceLocation != "" || f.FailingSentence != "Then the response should have status BAD_REQUEST" || f.FailingSentenceLine != 144 {
		t.Errorf("got %+v", f)
	}
}

func TestFailureEvidenceForPassingClassHasNoFailures(t *testing.T) {
	ev, _, err := failureEvidenceFor("testdata/bundle", passingClass)
	if err != nil {
		t.Fatal(err)
	}
	if len(ev.Failures) != 0 || ev.DistinctExceptions != 0 || ev.State != "Passed" {
		t.Errorf("got %+v", ev)
	}
}

func TestTestFrameFinder(t *testing.T) {
	trace := "x.Err: boom\n\tat other.Helper.check(Helper.kt:9)\n\tat com.acme.FooTest.bar$lambda$1(FooTest.kt:57)\n\tat com.acme.FooTest.bar(FooTest.kt:55)\n"
	loc, line := testFrame(trace, "com.acme.FooTest")
	if loc != "FooTest.kt:57" || line != 57 {
		t.Errorf("got %q %d", loc, line)
	}
	if loc, line := testFrame(trace, "com.acme.Other"); loc != "" || line != 0 {
		t.Errorf("unrelated class: got %q %d", loc, line)
	}
}

func TestGetTestRendersSentencesAsText(t *testing.T) {
	out, _, err := getTestFor("testdata/multi", multiClass, false)
	if err != nil {
		t.Fatal(err)
	}
	r, ok := out.(renderedResult)
	if !ok {
		t.Fatalf("got %T", out)
	}
	if r.TestClass != multiClass || len(r.Tests) != 3 {
		t.Fatalf("got %+v", r)
	}
	inv := r.Tests[0].Invocations[0]
	if len(inv.Sentences) != 4 || inv.Sentences[2].Line != 107 || inv.Sentences[2].Text != "Then the orchestration result has type SessionCodeValid" {
		t.Errorf("sentences = %+v", inv.Sentences)
	}
	if inv.Fixtures["Operator"] != "Fastweb" {
		t.Errorf("fixtures = %+v", inv.Fixtures)
	}
	if len(inv.Interactions) != 2 || inv.Interactions[0] != "Start Request from Client to Orchestrator" {
		t.Errorf("interactions = %+v", inv.Interactions)
	}
	if inv.Exception == nil || inv.Exception.SourceLocation != "StartMigrateOutHappyPathTest.kt:109" {
		t.Errorf("exception = %+v", inv.Exception)
	}
	if passing := r.Tests[2].Invocations[0]; passing.Exception != nil {
		t.Errorf("passing invocation carries exception %+v", passing.Exception)
	}
	b, _ := json.Marshal(r)
	if strings.Contains(string(b), "tk-nl") || strings.Contains(string(b), "tokens") {
		t.Error("rendered output leaks the token stream")
	}
}

func TestGetTestRawReturnsTheFileVerbatim(t *testing.T) {
	out, _, err := getTestFor("testdata/multi", multiClass, true)
	if err != nil {
		t.Fatal(err)
	}
	raw, ok := out.(json.RawMessage)
	if !ok {
		t.Fatalf("got %T", out)
	}
	var m map[string]any
	if err := json.Unmarshal(raw, &m); err != nil {
		t.Fatal(err)
	}
	if _, ok := m["tests"].([]any)[0].(map[string]any)["invocations"].([]any)[0].(map[string]any)["sequenceDiagram"]; !ok {
		t.Error("raw output is missing fields the typed struct drops")
	}
}

func TestCapturedInteractionsForOneMethod(t *testing.T) {
	out, _, err := capturedInteractionsFor("testdata/multi", multiClass+":handlesFiberCop")
	if err != nil {
		t.Fatal(err)
	}
	if out.TestClass != multiClass || len(out.Methods) != 1 || out.Methods[0].TestMethod != "handlesFiberCop" {
		t.Fatalf("got %+v", out)
	}
	ints := out.Methods[0].Interactions
	if len(ints) != 2 {
		t.Fatalf("got %d interactions", len(ints))
	}
	resp := ints[1]
	if resp.From != "Orchestrator" || resp.To != "Client" || resp.Values[0].Name != "Response Body" || resp.Values[0].Language != "json" || !strings.Contains(resp.Values[0].Value, "MigrateOutInitiated") {
		t.Errorf("response = %+v", resp)
	}
	if resp.Attributes["Status"]["Status"] != "200" || resp.Attributes["Headers"]["content-type"] != "application/json" {
		t.Errorf("attributes = %+v", resp.Attributes)
	}
}

func TestCapturedInteractionsForWholeClass(t *testing.T) {
	out, _, err := capturedInteractionsFor("testdata/multi", multiClass)
	if err != nil {
		t.Fatal(err)
	}
	if len(out.Methods) != 3 {
		t.Errorf("got %d methods, want 3", len(out.Methods))
	}
}

func TestCapturedInteractionsUnknownMethod(t *testing.T) {
	_, _, err := capturedInteractionsFor("testdata/multi", multiClass+":nope")
	if err == nil || !strings.Contains(err.Error(), "nope") {
		t.Errorf("got %v", err)
	}
}
