package mcp

import (
	"path/filepath"
	"strings"
	"testing"
)

func TestStyleProfileForMissingDirErrors(t *testing.T) {
	if _, err := styleProfileFor("testdata/no-such-directory", true); err == nil {
		t.Error("expected error for non-existent project_dir, got nil")
	}
}

func namesFor(fx []FixtureContainer, container string) []string {
	for _, f := range fx {
		if f.Container == container {
			return f.Names
		}
	}
	return nil
}

func TestScanFixtures(t *testing.T) {
	fx := scanFixtures("testdata/proj")
	if len(fx) != 2 {
		t.Fatalf("want 2 containers, got %+v", fx)
	}
	if got := namesFor(fx, "OrderFixtures"); len(got) != 2 || got[0] != "ItemFx" || got[1] != "QuantityFx" {
		t.Errorf("OrderFixtures names = %v", got)
	}
}

// Fixture names belong to the container that declares them; a second container
// in the same file must not inherit the first one's names.
func TestScanFixturesDoesNotBleedAcrossContainers(t *testing.T) {
	fx := scanFixtures("testdata/proj")
	got := namesFor(fx, "ShippingFixtures")
	for _, n := range got {
		if n == "ItemFx" || n == "QuantityFx" {
			t.Fatalf("ShippingFixtures leaked OrderFixtures names: %v", got)
		}
	}
	if len(got) != 2 || got[0] != "CarrierFx" {
		t.Errorf("ShippingFixtures names = %v", got)
	}
}

// @Fixture factory functions are called as fixtures[consignmentFor(2)], so the
// function name is the call-site identifier the profile should report.
func TestScanFixturesFindsFactoryFunctions(t *testing.T) {
	got := namesFor(scanFixtures("testdata/proj"), "ShippingFixtures")
	var found bool
	for _, n := range got {
		if n == "consignmentFor" {
			found = true
		}
	}
	if !found {
		t.Errorf("@Fixture factory function missing from %v", got)
	}
}

// Java containers declare fixtures via createFixture / createParameterFixture
// assigned to a static field, not via Kotlin's fixture() property syntax.
func TestScanFixturesFindsJavaContainers(t *testing.T) {
	fx := scanFixtures("testdata/javaproj")
	got := namesFor(fx, "ShipmentFixtures")
	if len(got) != 2 || got[0] != "CarrierFx" || got[1] != "ConsignmentFx" {
		t.Errorf("java fixtures = %+v", fx)
	}
}

func TestDetectFramework(t *testing.T) {
	if fw := detectFramework("testdata/proj"); fw != "kotest" {
		t.Errorf("framework = %q, want kotest", fw)
	}
}

// JUnit 5 and 6 are separate Kensa artifacts and separate adapters.
func TestDetectFrameworkReadsGroovyBuildFileAndJunit6(t *testing.T) {
	if fw := detectFramework("testdata/javaproj"); fw != "junit6" {
		t.Errorf("framework = %q, want junit6", fw)
	}
}

// Kotest assertions alongside a JUnit framework adapter is a documented
// combination, so the word "kotest" alone must not decide the framework.
func TestDetectFrameworkReadsMavenAndIgnoresAssertionLibraries(t *testing.T) {
	if fw := detectFramework("testdata/mavenproj"); fw != "junit5" {
		t.Errorf("framework = %q, want junit5", fw)
	}
}

func TestScanMatcherFields(t *testing.T) {
	mf := scanMatcherFields("testdata/proj")
	var paths []string
	for _, m := range mf {
		if m.Type == "JsonIntField" {
			paths = m.Paths
		}
	}
	if len(paths) == 0 || paths[0] != "/quantity" {
		t.Errorf("matcherFields = %+v", mf)
	}
}

// The core module exposes lowercase factories (stringField, anyField) as well
// as the Json*/Xml* types.
func TestScanMatcherFieldsFindsCoreFactories(t *testing.T) {
	mf := scanMatcherFields("testdata/proj")
	for _, m := range mf {
		if m.Type == "stringField" && len(m.Paths) > 0 && m.Paths[0] == "aCarrierName" {
			return
		}
	}
	t.Errorf("stringField factory missing from %+v", mf)
}

func TestScanMatcherFieldsScansJava(t *testing.T) {
	mf := scanMatcherFields("testdata/javaproj")
	if len(mf) == 0 || mf[0].Type != "JsonTextField" {
		t.Errorf("java matcherFields = %+v", mf)
	}
}

func TestScanConventions(t *testing.T) {
	c := scanConventions("testdata/proj")
	if !c.UsesMatcherField || !c.UsesExpandable || !c.UsesEventually {
		t.Errorf("conventions = %+v", c)
	}
}

func TestScanConventionsDetectsContinually(t *testing.T) {
	if c := scanConventions("testdata/proj"); !c.UsesContinually {
		t.Errorf("thenContinually not detected: %+v", c)
	}
}

func TestScanConventionsDetectsRenderedValue(t *testing.T) {
	if c := scanConventions("testdata/proj"); !c.UsesRenderedValue {
		t.Errorf("@RenderedValue not detected: %+v", c)
	}
}

func TestScanStubHelpers(t *testing.T) {
	s := scanStubHelpers("testdata/proj")
	if len(s) == 0 || s[0].Name != "primeSupplier" {
		t.Errorf("stubHelpers = %+v", s)
	}
}

// The snippet is sent to a model, so it must be bounded, and the path is more
// useful relative to the project root than as an absolute path.
func TestExemplarIsBoundedAndProjectRelative(t *testing.T) {
	ex := pickExemplar("testdata/proj")
	if ex.Path == "" {
		t.Fatal("no exemplar chosen")
	}
	if filepath.IsAbs(ex.Path) || strings.HasPrefix(ex.Path, "testdata/") {
		t.Errorf("path = %q, want project-relative", ex.Path)
	}
	if !strings.Contains(ex.Path, "OrderJourneyTest.kt") {
		t.Errorf("path = %q, want the test class file", ex.Path)
	}
	if len(ex.Snippet) > maxExemplarBytes {
		t.Errorf("snippet is %d bytes, want <= %d", len(ex.Snippet), maxExemplarBytes)
	}
}

func TestExemplarFindsJavaTests(t *testing.T) {
	ex := pickExemplar("testdata/javaproj")
	if !strings.Contains(ex.Path, "ShipmentTest.java") {
		t.Errorf("java exemplar = %q", ex.Path)
	}
}

func TestScanStyleProfile(t *testing.T) {
	p, err := scanStyle("testdata/proj")
	if err != nil {
		t.Fatalf("scanStyle: %v", err)
	}
	if p.Framework != "kotest" || len(p.Fixtures) != 2 {
		t.Errorf("profile = %+v", p)
	}
}

// A Java-only project must invalidate its cache when Java sources change.
func TestProfileCacheKeyCoversJavaSources(t *testing.T) {
	key := profileCacheKey("testdata/javaproj")
	if key == profileCacheKey("testdata/mavenproj") {
		t.Error("cache key does not distinguish projects")
	}
	if key == "" {
		t.Error("empty cache key")
	}
}
