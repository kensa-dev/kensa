package mcp

import (
	"reflect"
	"testing"
)

func TestReadIndices(t *testing.T) {
	tests, err := readIndices("testdata/bundle")
	if err != nil {
		t.Fatalf("readIndices: %v", err)
	}
	if len(tests) != 2 {
		t.Fatalf("got %d classes, want 2", len(tests))
	}
	if tests[0].State != "Failed" || tests[0].TestClass != failingClass {
		t.Errorf("unexpected first entry: %+v", tests[0])
	}
	if len(tests[0].Children) != 2 {
		t.Errorf("got %d children, want 2", len(tests[0].Children))
	}
	child := tests[0].Children[0]
	if child.TestMethod != "canCheckAvailabilityOfRobots" {
		t.Errorf("testMethod = %q, want %q", child.TestMethod, "canCheckAvailabilityOfRobots")
	}
	if !reflect.DeepEqual(child.Timing, [][2]int64{{1000, 250}}) {
		t.Errorf("timing = %+v", child.Timing)
	}
	if !reflect.DeepEqual(child.Participants, map[string]int{"Robot Shelter": 2}) {
		t.Errorf("participants = %+v", child.Participants)
	}
	if child.Assertions != 3 {
		t.Errorf("assertions = %d, want 3", child.Assertions)
	}
	if child.Expandables != 1 {
		t.Errorf("expandables = %d, want 1", child.Expandables)
	}
	if !reflect.DeepEqual(child.Epics, []string{"Adoption"}) {
		t.Errorf("epics = %+v", child.Epics)
	}
}

func TestListTestsFilterByState(t *testing.T) {
	out, _, err := listTestsHandlerFor("testdata/bundle", "Failed", false)
	if err != nil {
		t.Fatalf("listTests: %v", err)
	}
	if len(out.Tests) != 1 || out.Tests[0].State != "Failed" {
		t.Errorf("filter Failed returned %+v", out.Tests)
	}
}
