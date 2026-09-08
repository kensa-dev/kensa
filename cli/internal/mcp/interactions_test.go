package mcp

import (
	"strings"
	"testing"
)

func TestNewCapturedValueTruncatesLongValueAtDefaultCap(t *testing.T) {
	long := strings.Repeat("a", 5000)
	cv := newCapturedValue(RenderedValue{Name: "Body", Value: long, Language: "json"}, 0)
	if !cv.Truncated {
		t.Errorf("got truncated=%v, want true", cv.Truncated)
	}
	if cv.FullLength != 5000 {
		t.Errorf("got fullLength=%d, want 5000", cv.FullLength)
	}
	if len(cv.Value) != 4000 {
		t.Errorf("got value length %d, want 4000", len(cv.Value))
	}
}

func TestNewCapturedValueLeavesShortValueUntouched(t *testing.T) {
	cv := newCapturedValue(RenderedValue{Name: "Body", Value: "short", Language: "json"}, 0)
	if cv.Truncated {
		t.Errorf("got truncated=%v, want false", cv.Truncated)
	}
	if cv.FullLength != 0 {
		t.Errorf("got fullLength=%d, want 0", cv.FullLength)
	}
	if cv.Value != "short" {
		t.Errorf("got value=%q, want %q", cv.Value, "short")
	}
}

func TestNewCapturedValueUnlimitedWhenMaxIsMinusOne(t *testing.T) {
	long := strings.Repeat("a", 5000)
	cv := newCapturedValue(RenderedValue{Name: "Body", Value: long, Language: "json"}, -1)
	if cv.Truncated {
		t.Errorf("got truncated=%v, want false", cv.Truncated)
	}
	if len(cv.Value) != 5000 {
		t.Errorf("got value length %d, want 5000", len(cv.Value))
	}
}

func TestNewCapturedValueCutsAtGivenCap(t *testing.T) {
	cv := newCapturedValue(RenderedValue{Name: "Body", Value: "0123456789abcdef", Language: "json"}, 10)
	if !cv.Truncated {
		t.Errorf("got truncated=%v, want true", cv.Truncated)
	}
	if cv.FullLength != 16 {
		t.Errorf("got fullLength=%d, want 16", cv.FullLength)
	}
	if cv.Value != "0123456789" {
		t.Errorf("got value=%q, want %q", cv.Value, "0123456789")
	}
}

func TestNewCapturedValueTruncatesOnRunesNotBytes(t *testing.T) {
	value := strings.Repeat("é", 12)
	cv := newCapturedValue(RenderedValue{Name: "Body", Value: value, Language: "json"}, 10)
	if !cv.Truncated {
		t.Errorf("got truncated=%v, want true", cv.Truncated)
	}
	if cv.FullLength != 12 {
		t.Errorf("got fullLength=%d, want 12", cv.FullLength)
	}
	if got := []rune(cv.Value); len(got) != 10 {
		t.Errorf("got %d runes, want 10", len(got))
	}
}
