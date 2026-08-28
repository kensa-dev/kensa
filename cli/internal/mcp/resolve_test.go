package mcp

import (
	"os"
	"path/filepath"
	"strings"
	"testing"
)

func dirsOf(refs []bundleRef) []string {
	out := make([]string, len(refs))
	for i, r := range refs {
		out[i] = filepath.ToSlash(r.Dir)
	}
	return out
}

func TestResolveSingleBundle(t *testing.T) {
	refs, err := resolveBundlesIn(".", "testdata/bundle")
	if err != nil {
		t.Fatalf("resolve: %v", err)
	}
	if got := dirsOf(refs); len(got) != 1 || got[0] != "testdata/bundle" {
		t.Errorf("dirs = %v", got)
	}
	if refs[0].Source != "" {
		t.Errorf("single bundle should have no source id, got %q", refs[0].Source)
	}
}

// A site-mode root is a shell plus a sources/ directory; every source is a
// complete data-only bundle in its own right.
func TestResolveSiteRootExpandsToEverySource(t *testing.T) {
	refs, err := resolveBundlesIn(".", "testdata/site")
	if err != nil {
		t.Fatalf("resolve: %v", err)
	}
	got := dirsOf(refs)
	if len(got) != 2 || got[0] != "testdata/site/sources/test" || got[1] != "testdata/site/sources/uiTest" {
		t.Fatalf("dirs = %v", got)
	}
	if refs[0].Source != "test" || refs[1].Source != "uiTest" {
		t.Errorf("source ids = %q, %q", refs[0].Source, refs[1].Source)
	}
}

func TestResolveSingleSourceOfASite(t *testing.T) {
	refs, err := resolveBundlesIn(".", "testdata/site/sources/uiTest")
	if err != nil {
		t.Fatalf("resolve: %v", err)
	}
	if got := dirsOf(refs); len(got) != 1 || got[0] != "testdata/site/sources/uiTest" {
		t.Errorf("dirs = %v", got)
	}
}

func TestResolveRejectsNonBundleDirectory(t *testing.T) {
	_, err := resolveBundlesIn(".", "testdata/proj")
	if err == nil {
		t.Fatal("expected an error for a directory that is not Kensa output")
	}
	if !strings.Contains(err.Error(), "indices.json") || !strings.Contains(err.Error(), "manifest.json") {
		t.Errorf("error should say what was expected, got: %v", err)
	}
}

func writeProperties(t *testing.T, dir, body string) {
	t.Helper()
	if err := os.WriteFile(filepath.Join(dir, ".kensa-properties"), []byte(body), 0o644); err != nil {
		t.Fatal(err)
	}
}

// The CLI already resolves friendly names from .kensa-properties, so the same
// names should work here rather than forcing raw paths on the agent.
func TestResolveNamedFolderFromProperties(t *testing.T) {
	cwd := t.TempDir()
	bundle, _ := filepath.Abs("testdata/bundle")
	writeProperties(t, cwd, "testFolders:\n  my-tests: "+filepath.ToSlash(bundle)+"\n")

	refs, err := resolveBundlesIn(cwd, "my-tests")
	if err != nil {
		t.Fatalf("resolve: %v", err)
	}
	if len(refs) != 1 {
		t.Fatalf("refs = %v", dirsOf(refs))
	}
}

func TestResolveEmptySpecUsesSoleConfiguredFolder(t *testing.T) {
	cwd := t.TempDir()
	bundle, _ := filepath.Abs("testdata/bundle")
	writeProperties(t, cwd, "testFolders:\n  only: "+filepath.ToSlash(bundle)+"\n")

	refs, err := resolveBundlesIn(cwd, "")
	if err != nil {
		t.Fatalf("resolve: %v", err)
	}
	if len(refs) != 1 {
		t.Fatalf("refs = %v", dirsOf(refs))
	}
}

// With more than one configured folder there is no safe default, so the error
// has to name the choices rather than guess.
func TestResolveEmptySpecWithSeveralFoldersListsThem(t *testing.T) {
	cwd := t.TempDir()
	bundle, _ := filepath.Abs("testdata/bundle")
	writeProperties(t, cwd, "testFolders:\n  unit: "+filepath.ToSlash(bundle)+"\n  ui: "+filepath.ToSlash(bundle)+"\n")

	_, err := resolveBundlesIn(cwd, "")
	if err == nil {
		t.Fatal("expected an error when the choice is ambiguous")
	}
	for _, name := range []string{"ui", "unit"} {
		if !strings.Contains(err.Error(), name) {
			t.Errorf("error should name %q, got: %v", name, err)
		}
	}
}

func TestResolveEmptySpecWithoutPropertiesErrors(t *testing.T) {
	if _, err := resolveBundlesIn(t.TempDir(), ""); err == nil {
		t.Fatal("expected an error when there is nothing to resolve")
	}
}

// Listing a site reports every source, tagging each entry with the source it
// came from rather than rewriting ids.
func TestListTestsAcrossSiteSources(t *testing.T) {
	out, _, err := listTestsHandlerFor("testdata/site", "")
	if err != nil {
		t.Fatalf("listTests: %v", err)
	}
	if len(out.Tests) != 3 {
		t.Fatalf("got %d entries, want 3: %+v", len(out.Tests), out.Tests)
	}
	var ui bool
	for _, e := range out.Tests {
		if e.TestClass == "dev.kensa.example.adoptabot.ShelterUiTest" {
			ui = true
			if e.Source != "uiTest" {
				t.Errorf("source = %q, want uiTest", e.Source)
			}
		}
	}
	if !ui {
		t.Error("uiTest source missing from the listing")
	}
}

func TestGetTestFindsClassInAnySiteSource(t *testing.T) {
	out, _, err := getTestFor("testdata/site", "dev.kensa.example.adoptabot.ShelterUiTest", false)
	if err != nil {
		t.Fatalf("getTest: %v", err)
	}
	if r := out.(renderedResult); r.TestClass != "dev.kensa.example.adoptabot.ShelterUiTest" {
		t.Errorf("testClass = %q", r.TestClass)
	}
}

func TestGetTestReportsWhereItLooked(t *testing.T) {
	_, _, err := getTestFor("testdata/site", "com.example.NotHere", false)
	if err == nil {
		t.Fatal("expected an error for an unknown class")
	}
	if !strings.Contains(err.Error(), "uiTest") {
		t.Errorf("error should name the sources searched, got: %v", err)
	}
}

func TestListFailuresAcrossSite(t *testing.T) {
	out, _, err := listFailuresFor("testdata/site")
	if err != nil {
		t.Fatalf("listFailures: %v", err)
	}
	if len(out.Failures) != 1 || out.Failures[0].Source != "test" {
		t.Errorf("failures = %+v", out.Failures)
	}
}
