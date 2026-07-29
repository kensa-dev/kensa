package mcp

import (
	"os"
	"path/filepath"
	"regexp"
	"strings"
)

// maxExemplarBytes bounds the exemplar snippet. The whole snippet is handed to
// a model, so a large test file would otherwise dominate the tool result.
const maxExemplarBytes = 6000

type FixtureContainer struct {
	Container string   `json:"container"`
	Names     []string `json:"names"`
}

type MatcherFieldUse struct {
	Type  string   `json:"type"`
	Paths []string `json:"paths"`
}
type StubHelper struct {
	Name string `json:"name"`
	Kind string `json:"kind"`
}
type Conventions struct {
	UsesMatcherField  bool `json:"usesMatcherField"`
	UsesSemantic      bool `json:"usesSemanticMatchers"`
	UsesExpandable    bool `json:"usesExpandable"`
	UsesEventually    bool `json:"usesEventually"`
	UsesContinually   bool `json:"usesContinually"`
	UsesRenderedValue bool `json:"usesRenderedValue"`
	UsesHighlight     bool `json:"usesHighlight"`
}
type Exemplar struct {
	Path    string `json:"path"`
	Snippet string `json:"snippet"`
}

var (
	// Kotlin `object X : FixtureContainer` and Java `class X implements FixtureContainer`.
	reContainer = regexp.MustCompile(`(?:object|class)\s+(\w+)\s*(?::|implements)\s*[^{]*\bFixtureContainer\b`)
	// Kotlin `val ItemFx = fixture(...)` and Java
	// `static final PrimaryFixture<T> ITEM = createFixture(...)`.
	reFixtureDecl = regexp.MustCompile(`(\w+)\s*=\s*(?:fixture|createFixture|createParameterFixture)\(`)
	// @Fixture("Key") fun productFor(...) — called as fixtures[productFor(arg)].
	reFixtureFactory = regexp.MustCompile(`@Fixture\("[^"]*"\)\s*(?:@\w+(?:\([^)]*\))?\s*)*(?:private\s+|internal\s+|public\s+|static\s+)*fun\s+(\w+)`)
	// Json*/Xml* field types plus the core module's lowercase factories.
	reField = regexp.MustCompile(`\b((?:Json|Xml)\w*Field|ArrayNodeField|stringField|anyField)\(\s*"([^"]+)"`)
	rePrime = regexp.MustCompile(`fun\s+(\w+)\s*\([^)]*\)[^{]*\{[^}]*prime`)
)

func scanMatcherFields(dir string) []MatcherFieldUse {
	byType := map[string][]string{}
	var order []string
	for _, f := range sourceFiles(dir) {
		b, _ := os.ReadFile(f)
		for _, m := range reField.FindAllStringSubmatch(string(b), -1) {
			if _, seen := byType[m[1]]; !seen {
				order = append(order, m[1])
			}
			byType[m[1]] = append(byType[m[1]], m[2])
		}
	}
	out := make([]MatcherFieldUse, 0, len(order))
	for _, t := range order {
		out = append(out, MatcherFieldUse{Type: t, Paths: byType[t]})
	}
	return out
}

func scanConventions(dir string) Conventions {
	var all strings.Builder
	for _, f := range sourceFiles(dir) {
		b, _ := os.ReadFile(f)
		all.Write(b)
	}
	s := all.String()
	return Conventions{
		UsesMatcherField:  reField.MatchString(s),
		UsesSemantic:      strings.Contains(s, "shouldBe") && strings.Contains(s, "private fun"),
		UsesExpandable:    strings.Contains(s, "@ExpandableSentence"),
		UsesEventually:    strings.Contains(s, "thenEventually"),
		UsesContinually:   strings.Contains(s, "thenContinually"),
		UsesRenderedValue: strings.Contains(s, "@RenderedValue"),
		UsesHighlight:     strings.Contains(s, "@Highlight"),
	}
}

func scanStubHelpers(dir string) []StubHelper {
	out := []StubHelper{}
	for _, f := range sourceFiles(dir) {
		b, _ := os.ReadFile(f)
		for _, m := range rePrime.FindAllStringSubmatch(string(b), -1) {
			out = append(out, StubHelper{Name: m[1], Kind: "http-stub-priming"})
		}
	}
	return out
}

// pickExemplar chooses the most representative test source: the largest file
// that declares a test class, preferring a *Test.kt / *Test.java name.
func pickExemplar(dir string) Exemplar {
	var best string
	var bestScore int
	for _, f := range sourceFiles(dir) {
		b, err := os.ReadFile(f)
		if err != nil {
			continue
		}
		src := string(b)
		named := strings.HasSuffix(f, "Test.kt") || strings.HasSuffix(f, "Test.java")
		if !named && !strings.Contains(src, "KensaTest") {
			continue
		}
		score := len(b)
		if named {
			score += 1_000_000
		}
		if score > bestScore {
			bestScore, best = score, f
		}
	}
	if best == "" {
		return Exemplar{}
	}
	b, _ := os.ReadFile(best)
	snippet := string(b)
	if len(snippet) > maxExemplarBytes {
		snippet = snippet[:maxExemplarBytes] + "\n// … truncated\n"
	}
	rel, err := filepath.Rel(dir, best)
	if err != nil {
		rel = best
	}
	return Exemplar{Path: rel, Snippet: snippet}
}

// sourceFiles lists the Kotlin and Java sources under dir. Kensa supports both
// languages, so a Java-only project must profile like a Kotlin one.
func sourceFiles(dir string) []string {
	var out []string
	_ = filepath.WalkDir(dir, func(p string, d os.DirEntry, err error) error {
		if err != nil {
			return nil
		}
		if d.IsDir() {
			switch d.Name() {
			case "build", "target", ".git", ".gradle", ".kensa", "node_modules":
				return filepath.SkipDir
			}
			return nil
		}
		if strings.HasSuffix(p, ".kt") || strings.HasSuffix(p, ".java") {
			out = append(out, p)
		}
		return nil
	})
	return out
}

// buildFiles lists the build descriptors that identify the test framework.
func buildFiles(dir string) []string {
	var out []string
	for _, name := range []string{"build.gradle.kts", "build.gradle", "pom.xml"} {
		p := filepath.Join(dir, name)
		if fileStat(p) != "" {
			out = append(out, p)
		}
	}
	return out
}

func scanFixtures(dir string) []FixtureContainer {
	out := []FixtureContainer{}
	for _, f := range sourceFiles(dir) {
		b, err := os.ReadFile(f)
		if err != nil {
			continue
		}
		src := string(b)
		for _, loc := range reContainer.FindAllStringSubmatchIndex(src, -1) {
			fc := FixtureContainer{Container: src[loc[2]:loc[3]], Names: []string{}}
			body := containerBody(src, loc[1])
			for _, m := range reFixtureDecl.FindAllStringSubmatch(body, -1) {
				fc.Names = append(fc.Names, m[1])
			}
			for _, m := range reFixtureFactory.FindAllStringSubmatch(body, -1) {
				fc.Names = append(fc.Names, m[1])
			}
			out = append(out, fc)
		}
	}
	return out
}

// containerBody returns the brace-delimited body that starts at or after from,
// so fixture declarations are attributed to the container that owns them.
func containerBody(src string, from int) string {
	open := strings.IndexByte(src[from:], '{')
	if open < 0 {
		return ""
	}
	open += from
	depth := 0
	for i := open; i < len(src); i++ {
		switch src[i] {
		case '{':
			depth++
		case '}':
			depth--
			if depth == 0 {
				return src[open+1 : i]
			}
		}
	}
	return src[open:]
}

// detectFramework keys on the Kensa framework artifact rather than on the
// presence of a test library: kensa-assertions-kotest alongside JUnit is a
// documented combination and must not read as a Kotest project.
func detectFramework(dir string) string {
	var all strings.Builder
	for _, f := range buildFiles(dir) {
		b, _ := os.ReadFile(f)
		all.Write(b)
		all.WriteByte('\n')
	}
	s := all.String()
	for _, fw := range []string{"junit5", "junit6", "kotest", "testng"} {
		if strings.Contains(s, "kensa-framework-"+fw) || strings.Contains(s, "framework-"+fw) {
			return fw
		}
	}
	// No Kensa adapter declared — fall back to whichever runner is present.
	switch {
	case strings.Contains(s, "kotest-runner") || strings.Contains(s, "kotest-framework-engine"):
		return "kotest"
	case strings.Contains(s, "testng"):
		return "testng"
	case strings.Contains(s, "junit-jupiter") || strings.Contains(s, "junit5"):
		return "junit5"
	}
	return "unknown"
}
