package mcp

import "testing"

const (
	failingClass = "dev.kensa.example.adoptabot.AdoptionServiceTest"
	passingClass = "dev.kensa.example.adoptabot.ShelterTest"
)

func TestReadResult(t *testing.T) {
	r, err := readResult("testdata/bundle", failingClass)
	if err != nil {
		t.Fatalf("readResult: %v", err)
	}
	if r.State != "Failed" || len(r.Tests) != 2 {
		t.Fatalf("unexpected result: state=%q tests=%d", r.State, len(r.Tests))
	}
}

func TestReadResultAcceptsChildId(t *testing.T) {
	r, err := readResult("testdata/bundle", failingClass+":canAdoptAnAvailableRobot")
	if err != nil {
		t.Fatalf("readResult with child id: %v", err)
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
	wantExc := "Status: expected:<400 Bad Request> but was:<200 OK>"
	if ev.Exception != wantExc {
		t.Errorf("exception = %q, want %q", ev.Exception, wantExc)
	}
	wantSentence := "Then the response should have status BAD_REQUEST"
	if ev.FailingSentence != wantSentence {
		t.Errorf("failingSentence = %q, want %q", ev.FailingSentence, wantSentence)
	}
	if ev.TestMethod != "canAdoptAnAvailableRobot" {
		t.Errorf("testMethod = %q, want canAdoptAnAvailableRobot", ev.TestMethod)
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
	if ev.Exception != "" || ev.FailingSentence != "" || ev.TestMethod != "" {
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
	out, _, err := listTestsHandlerFor("testdata/bundle", "")
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
		out, _, err := listTestsHandlerFor("testdata/bundle", filter)
		if err != nil {
			t.Fatalf("listTests %q: %v", filter, err)
		}
		if len(out.Tests) != 1 || out.Tests[0].TestClass != failingClass {
			t.Errorf("filter %q returned %d entries, want 1", filter, len(out.Tests))
		}
	}
	out, _, err := listTestsHandlerFor("testdata/bundle", "NotExecuted")
	if err != nil {
		t.Fatalf("listTests NotExecuted: %v", err)
	}
	if len(out.Tests) != 0 {
		t.Errorf("NotExecuted returned %d entries, want 0", len(out.Tests))
	}
}
