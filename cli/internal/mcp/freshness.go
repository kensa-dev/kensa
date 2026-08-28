package mcp

import (
	"fmt"
	"strings"
	"time"
)

// now is the clock used to age a bundle; tests replace it.
var now = time.Now

// bundleFreshness tells the caller how old the data it is looking at is. The
// tools read whatever the last test run wrote, so an empty failure list can
// mean "nothing is broken" or "nobody has run the tests since the change".
// Both fields are absent when the bundle's write time could not be read.
type bundleFreshness struct {
	// BundleWrittenAt is when the newest resolved bundle was written, RFC 3339 UTC.
	BundleWrittenAt string `json:"bundleWrittenAt,omitempty"`
	// BundleAge is BundleWrittenAt relative to now, e.g. "3h12m" or "2d1h".
	BundleAge string `json:"bundleAge,omitempty"`
}

// freshnessOf reports the freshness of the newest of the resolved bundles.
func freshnessOf(shapes []bundleShape) bundleFreshness {
	var newest time.Time
	for _, s := range shapes {
		if t := s.finishedAt(); t.After(newest) {
			newest = t
		}
	}
	if newest.IsZero() {
		return bundleFreshness{}
	}
	return bundleFreshness{
		BundleWrittenAt: newest.UTC().Format(time.RFC3339),
		BundleAge:       formatAge(now().Sub(newest)),
	}
}

// formatAge renders a duration at the precision a person triaging a build
// cares about: minutes under an hour, hours and minutes under a day, days and
// hours beyond that.
func formatAge(d time.Duration) string {
	d = d.Round(time.Minute)
	if d < time.Minute {
		return "under 1m"
	}
	days := d / (24 * time.Hour)
	d -= days * 24 * time.Hour
	hours := d / time.Hour
	minutes := (d - hours*time.Hour) / time.Minute

	var parts []string
	switch {
	case days > 0:
		parts = append(parts, fmt.Sprintf("%dd", days))
		if hours > 0 {
			parts = append(parts, fmt.Sprintf("%dh", hours))
		}
	case hours > 0:
		parts = append(parts, fmt.Sprintf("%dh", hours))
		if minutes > 0 {
			parts = append(parts, fmt.Sprintf("%dm", minutes))
		}
	default:
		parts = append(parts, fmt.Sprintf("%dm", minutes))
	}
	return strings.Join(parts, "")
}
