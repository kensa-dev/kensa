package mcp

import "testing"

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
}

func TestListTestsFilterByState(t *testing.T) {
	out, _, err := listTestsHandlerFor("testdata/bundle", "Failed")
	if err != nil {
		t.Fatalf("listTests: %v", err)
	}
	if len(out.Tests) != 1 || out.Tests[0].State != "Failed" {
		t.Errorf("filter Failed returned %+v", out.Tests)
	}
}
