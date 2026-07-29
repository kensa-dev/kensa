package mcp

import (
	"os"
	"path/filepath"
	"testing"
)

func TestProfileCacheKeyChangesWithSource(t *testing.T) {
	dir := t.TempDir()
	src := filepath.Join(dir, "src", "test", "kotlin")
	os.MkdirAll(src, 0o755)
	os.WriteFile(filepath.Join(src, "A.kt"), []byte("object F : FixtureContainer"), 0o644)
	k1 := profileCacheKey(dir)
	os.WriteFile(filepath.Join(src, "A.kt"), []byte("object F : FixtureContainer { val X = fixture(\"x\"){1} }"), 0o644)
	k2 := profileCacheKey(dir)
	if k1 == k2 || k1 == "" {
		t.Errorf("cache key did not change with source: %q vs %q", k1, k2)
	}
}

func TestCacheRoundTrip(t *testing.T) {
	dir := t.TempDir()
	p := StyleProfile{Framework: "kotest"}
	if err := storeCachedProfile(dir, "k1", p); err != nil {
		t.Fatal(err)
	}
	got, ok := loadCachedProfile(dir, "k1")
	if !ok || got.Framework != "kotest" {
		t.Errorf("round trip failed: %+v ok=%v", got, ok)
	}
	if _, ok := loadCachedProfile(dir, "k2"); ok {
		t.Errorf("stale key should miss")
	}
}

// A cached profile written with null lists — by an older build, or before the
// lists were initialised — must not come back with nil slices, which fail
// output-schema validation on the way to the client.
func TestCachedProfileWithNullListsIsUsable(t *testing.T) {
	dir := t.TempDir()
	key := profileCacheKey(dir)
	stale := `{"key":"` + key + `","profile":{"framework":"junit5","fixtures":null,` +
		`"matcherFields":null,"stubHelpers":null,"conventions":{},"exemplar":{"path":"","snippet":""}}}`
	os.MkdirAll(filepath.Join(dir, ".kensa"), 0o755)
	if err := os.WriteFile(cachePath(dir), []byte(stale), 0o644); err != nil {
		t.Fatal(err)
	}

	p, err := styleProfileFor(dir, false)
	if err != nil {
		t.Fatalf("styleProfileFor: %v", err)
	}
	if p.Fixtures == nil || p.MatcherFields == nil || p.StubHelpers == nil {
		t.Errorf("nil lists survived the cache: %+v", p)
	}
}

func TestStyleProfileForCaches(t *testing.T) {
	p, err := styleProfileFor("testdata/proj", false)
	if err != nil || p.Framework != "kotest" {
		t.Fatalf("styleProfileFor: %+v err=%v", p, err)
	}
	// second call should hit cache (file exists now)
	if _, err := os.Stat(filepath.Join("testdata/proj", ".kensa", "style-profile.json")); err != nil {
		t.Errorf("cache file not written: %v", err)
	}
}
