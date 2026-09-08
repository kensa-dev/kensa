package mcp

import "testing"

const (
	failingClass = "dev.kensa.example.adoptabot.AdoptionServiceTest"
	passingClass = "dev.kensa.example.adoptabot.ShelterTest"
)

func TestReadResult(t *testing.T) {
	r, err := findResult("testdata/bundle", failingClass)
	if err != nil {
		t.Fatalf("findResult: %v", err)
	}
	if r.State != "Failed" || len(r.Tests) != 2 {
		t.Fatalf("unexpected result: state=%q tests=%d", r.State, len(r.Tests))
	}
}

func TestReadResultAcceptsChildId(t *testing.T) {
	r, err := findResult("testdata/bundle", failingClass+":canAdoptAnAvailableRobot")
	if err != nil {
		t.Fatalf("findResult with child id: %v", err)
	}
	if r.TestClass != failingClass {
		t.Errorf("testClass = %q, want %q", r.TestClass, failingClass)
	}
}

func TestFailureEvidenceReadsExecutionException(t *testing.T) {
	ev, _, err := failureEvidenceFor("testdata/bundle", failingClass)
	if err != nil {
		t.Fatalf("failureEvidence: %v", err)
	}
	if len(ev.Failures) != 1 {
		t.Fatalf("failures = %+v", ev.Failures)
	}
	f := ev.Failures[0]
	wantExc := "Status: expected:<400 Bad Request> but was:<200 OK>"
	if f.Exception != wantExc {
		t.Errorf("exception = %q, want %q", f.Exception, wantExc)
	}
	wantSentence := "Then the response should have status BAD_REQUEST"
	if f.FailingSentence != wantSentence {
		t.Errorf("failingSentence = %q, want %q", f.FailingSentence, wantSentence)
	}
	if f.TestMethod != "canAdoptAnAvailableRobot" {
		t.Errorf("testMethod = %q, want canAdoptAnAvailableRobot", f.TestMethod)
	}
}

// A passing invocation still carries an executionException key — the writer
// emits an empty object rather than omitting it — so presence must not be
// mistaken for failure.
func TestFailureEvidenceIgnoresEmptyExecutionException(t *testing.T) {
	ev, _, err := failureEvidenceFor("testdata/bundle", passingClass)
	if err != nil {
		t.Fatalf("failureEvidence: %v", err)
	}
	if len(ev.Failures) != 0 {
		t.Errorf("expected no evidence for a passing class, got %+v", ev)
	}
}

func TestListFailures(t *testing.T) {
	out, _, err := listFailuresFor("testdata/bundle")
	if err != nil {
		t.Fatalf("listFailures: %v", err)
	}
	if len(out.Failures) != 1 || out.Failures[0].TestClass != failingClass {
		t.Fatalf("listFailures = %+v", out.Failures)
	}
}

func TestListTestsSurfacesHasErrors(t *testing.T) {
	out, _, err := listTestsHandlerFor("testdata/bundle", "", true)
	if err != nil {
		t.Fatalf("listTests: %v", err)
	}
	var checked bool
	for _, e := range out.Tests {
		if e.TestClass != passingClass {
			continue
		}
		checked = true
		if !e.HasErrors {
			t.Error("expected hasErrors on the class entry")
		}
		if len(e.Children) != 1 || !e.Children[0].HasErrors {
			t.Error("expected hasErrors on the child entry")
		}
	}
	if !checked {
		t.Fatalf("%s missing from indices", passingClass)
	}
}

// The writer emits "Not Executed" with a space, and a model asked for a state
// filter has no way to know that, so matching tolerates case and spacing.
func TestListTestsStateFilterIsLenient(t *testing.T) {
	for _, filter := range []string{"Failed", "failed", "FAILED"} {
		out, _, err := listTestsHandlerFor("testdata/bundle", filter, false)
		if err != nil {
			t.Fatalf("listTests %q: %v", filter, err)
		}
		if len(out.Tests) != 1 || out.Tests[0].TestClass != failingClass {
			t.Errorf("filter %q returned %d entries, want 1", filter, len(out.Tests))
		}
	}
	out, _, err := listTestsHandlerFor("testdata/bundle", "NotExecuted", false)
	if err != nil {
		t.Fatalf("listTests NotExecuted: %v", err)
	}
	if len(out.Tests) != 0 {
		t.Errorf("NotExecuted returned %d entries, want 0", len(out.Tests))
	}
}

func TestListTestsCompactByDefault(t *testing.T) {
	out, _, err := listTestsHandlerFor("testdata/bundle", "", false)
	if err != nil {
		t.Fatalf("listTests: %v", err)
	}
	var checked bool
	for _, e := range out.Tests {
		if e.Children != nil {
			t.Errorf("%s: children = %+v, want nil", e.TestClass, e.Children)
		}
		if e.TestClass != failingClass {
			continue
		}
		checked = true
		if e.Methods == nil || e.Methods.Total != 2 || e.Methods.Failed != 1 {
			t.Errorf("methods = %+v", e.Methods)
		}
		if e.ElapsedMs == nil || *e.ElapsedMs != 6450 {
			t.Errorf("elapsedMs = %v", e.ElapsedMs)
		}
	}
	if !checked {
		t.Fatalf("%s missing from indices", failingClass)
	}
}

func TestListTestsChildrenTrueKeepsChildren(t *testing.T) {
	out, _, err := listTestsHandlerFor("testdata/bundle", "", true)
	if err != nil {
		t.Fatalf("listTests: %v", err)
	}
	var checked bool
	for _, e := range out.Tests {
		if e.TestClass != failingClass {
			continue
		}
		checked = true
		if len(e.Children) != 2 {
			t.Errorf("children = %+v, want 2", e.Children)
		}
	}
	if !checked {
		t.Fatalf("%s missing from indices", failingClass)
	}
}

func TestListTestsLeavesNestedContainersAlone(t *testing.T) {
	out, _, err := listTestsHandlerFor("testdata/multi", "", false)
	if err != nil {
		t.Fatalf("listTests: %v", err)
	}
	if len(out.Tests) != 1 {
		t.Fatalf("tests = %+v", out.Tests)
	}
	e := out.Tests[0]
	if len(e.Children) != 3 {
		t.Errorf("children = %+v, want 3", e.Children)
	}
	if e.Methods != nil {
		t.Errorf("methods = %+v, want nil", e.Methods)
	}
	if e.ElapsedMs != nil {
		t.Errorf("elapsedMs = %v, want nil", e.ElapsedMs)
	}
}

func TestListFailuresPopulatesMethodsAndElapsed(t *testing.T) {
	out, _, err := listFailuresFor("testdata/bundle")
	if err != nil {
		t.Fatalf("listFailures: %v", err)
	}
	if len(out.Failures) != 1 {
		t.Fatalf("listFailures = %+v", out.Failures)
	}
	f := out.Failures[0]
	if len(f.Children) != 2 {
		t.Errorf("children = %+v, want 2", f.Children)
	}
	if f.Methods == nil || f.Methods.Total != 2 || f.Methods.Failed != 1 {
		t.Errorf("methods = %+v", f.Methods)
	}
	if f.ElapsedMs == nil || *f.ElapsedMs != 6450 {
		t.Errorf("elapsedMs = %v", f.ElapsedMs)
	}
}
