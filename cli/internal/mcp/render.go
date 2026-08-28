package mcp

import (
	"regexp"
	"strconv"
	"strings"
)

// The rendered form of a class result: what a person reads in the report,
// without the token stream, diagrams and per-token markup that make the raw
// file several thousand tokens per method.

type renderedSentence struct {
	Line int    `json:"line"`
	Text string `json:"text"`
}

type renderedException struct {
	Message        string `json:"message"`
	SourceLocation string `json:"sourceLocation,omitempty"`
}

type renderedInvocation struct {
	DisplayName string             `json:"displayName"`
	State       string             `json:"state"`
	ElapsedTime string             `json:"elapsedTime"`
	Sentences   []renderedSentence `json:"sentences"`
	Fixtures    map[string]any     `json:"fixtures,omitempty"`
	// Interactions names the captured interactions in order; bodies come
	// from captured_interactions.
	Interactions []string           `json:"interactions,omitempty"`
	Exception    *renderedException `json:"exception,omitempty"`
}

type renderedTest struct {
	TestMethod  string               `json:"testMethod"`
	DisplayName string               `json:"displayName"`
	State       string               `json:"state"`
	Invocations []renderedInvocation `json:"invocations"`
}

type renderedResult struct {
	TestClass   string         `json:"testClass"`
	DisplayName string         `json:"displayName"`
	State       string         `json:"state"`
	PackageName string         `json:"packageName"`
	Tests       []renderedTest `json:"tests"`
}

func render(r Result) renderedResult {
	out := renderedResult{TestClass: r.TestClass, DisplayName: r.DisplayName, State: r.State, PackageName: r.PackageName, Tests: []renderedTest{}}
	for _, tc := range r.Tests {
		t := renderedTest{TestMethod: tc.TestMethod, DisplayName: tc.DisplayName, State: tc.State, Invocations: []renderedInvocation{}}
		for _, inv := range tc.Invocations {
			ri := renderedInvocation{DisplayName: inv.DisplayName, State: inv.State, ElapsedTime: inv.ElapsedTime, Sentences: []renderedSentence{}, Fixtures: flattenPairs(inv.Fixtures)}
			for _, s := range inv.Sentences {
				ri.Sentences = append(ri.Sentences, renderedSentence{Line: s.LineNumber, Text: sentenceText(s)})
			}
			for _, ci := range inv.CapturedInteractions {
				ri.Interactions = append(ri.Interactions, ci.Name)
			}
			if inv.failed() {
				ri.Exception = &renderedException{Message: inv.ExecutionException.Message, SourceLocation: testFrames(inv.ExecutionException.StackTrace, r.TestClass).deepest}
			}
			t.Invocations = append(t.Invocations, ri)
		}
		out.Tests = append(out.Tests, t)
	}
	return out
}

// framePattern matches a JVM stack frame, "at pkg.Class.method(File.kt:12)",
// allowing a module or class-loader prefix ("app//", "acme@1.0/"), spaces and
// non-ASCII in backtick method names, and any file name.
var framePattern = regexp.MustCompile(`^\s*at\s+(?:[^/\s]*/)*(.+?)\(([^:()]+):(\d+)\)\s*$`)

type frame struct {
	method   string // method name within the test class, lambda suffix stripped
	location string // File.kt:line
	line     int
}

// frames is the stack frames that belong to the test class (its methods,
// lambdas and inner classes), deepest first.
type frames struct {
	all     []frame
	deepest string
}

func testFrames(stackTrace, testClass string) frames {
	var f frames
	if testClass == "" {
		return f
	}
	for _, line := range strings.Split(stackTrace, "\n") {
		m := framePattern.FindStringSubmatch(line)
		if m == nil {
			continue
		}
		qualified := m[1]
		var rest string
		switch {
		case strings.HasPrefix(qualified, testClass+"."):
			rest = qualified[len(testClass)+1:]
		case strings.HasPrefix(qualified, testClass+"$"):
			rest = qualified[len(testClass)+1:]
		default:
			continue
		}
		n, _ := strconv.Atoi(m[3])
		method, _, _ := strings.Cut(rest, "$")
		f.all = append(f.all, frame{method: method, location: m[2] + ":" + m[3], line: n})
	}
	if len(f.all) > 0 {
		f.deepest = f.all[0].location
	}
	return f
}

// methodLine is the line inside the named test method that the failure
// passed through: the deepest frame in that method, since helpers declared
// elsewhere in the class (actions, matchers, @BeforeEach) sit below it in the
// trace. Without a frame in the method it falls back to the outermost frame
// in the class, and 0 when there is none.
func (f frames) methodLine(method string) int {
	for _, fr := range f.all {
		if fr.method == method {
			return fr.line
		}
	}
	if len(f.all) == 0 {
		return 0
	}
	return f.all[len(f.all)-1].line
}

// testFrame is the deepest test-class frame as "File.kt:line" with its line.
func testFrame(stackTrace, testClass string) (string, int) {
	f := testFrames(stackTrace, testClass)
	if len(f.all) == 0 {
		return "", 0
	}
	return f.all[0].location, f.all[0].line
}
