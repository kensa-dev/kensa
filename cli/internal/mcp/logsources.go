package mcp

import (
	"context"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

type listLogSourcesIn struct {
	BundleDir string `json:"bundle_dir,omitempty" jsonschema:"kensa-output bundle, site-mode root, or a test folder name from .kensa-properties; omit when the project configures exactly one"`
}

// logSourceRow is one registered log source, labelled with the site-mode
// source it came from the way readAllIndices labels a test entry. Two sources
// of a site register ids independently, so the same id can appear twice; the
// label is what tells the rows apart. It is empty for a standalone bundle.
type logSourceRow struct {
	LogSource
	Source string `json:"source,omitempty"`
}

type listLogSourcesOut struct {
	bundleFreshness
	Sources []logSourceRow `json:"sources"`
	Notice  string         `json:"notice,omitempty"`
}

// noticeNoLogSources is returned when a bundle's run.json predates log source
// recording (before kensa 1.0.0), or has no run.json at all.
const noticeNoLogSources = "this bundle was written before log sources were recorded; needs kensa 1.0.0 or later"

func listLogSourcesFor(spec string, in listLogSourcesIn) (listLogSourcesOut, error) {
	refs, shapes, err := resolveComplete(spec)
	if err != nil {
		return listLogSourcesOut{}, err
	}
	out := listLogSourcesOut{bundleFreshness: freshnessOf(shapes), Sources: []logSourceRow{}}
	for _, ref := range refs {
		if marker, ok := readRunMarker(ref.Dir); ok {
			for _, source := range marker.LogSources {
				out.Sources = append(out.Sources, logSourceRow{LogSource: source, Source: ref.Source})
			}
		}
	}
	if len(out.Sources) == 0 {
		out.Notice = noticeNoLogSources
	}
	return out, nil
}

func listLogSources(_ context.Context, _ *mcp.CallToolRequest, in listLogSourcesIn) (*mcp.CallToolResult, listLogSourcesOut, error) {
	out, err := listLogSourcesFor(in.BundleDir, in)
	return nil, out, err
}
