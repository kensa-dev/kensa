package mcp

import (
	"context"
	"fmt"
	"os"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

type styleProfileIn struct {
	ProjectDir string `json:"project_dir" jsonschema:"path to the project (or module) root"`
	NoCache    bool   `json:"no_cache,omitempty"`
}

func styleProfileFor(dir string, noCache bool) (StyleProfile, error) {
	fi, err := os.Stat(dir)
	if err != nil || !fi.IsDir() {
		return StyleProfile{}, fmt.Errorf("project_dir %q is not a readable directory", dir)
	}
	key := profileCacheKey(dir)
	if !noCache {
		if p, ok := loadCachedProfile(dir, key); ok {
			return p.normalised(), nil
		}
	}
	p, err := scanStyle(dir)
	if err != nil {
		return p, err
	}
	_ = storeCachedProfile(dir, key, p)
	return p, nil
}

func styleProfile(_ context.Context, _ *mcp.CallToolRequest, in styleProfileIn) (*mcp.CallToolResult, StyleProfile, error) {
	p, err := styleProfileFor(in.ProjectDir, in.NoCache)
	return nil, p, err
}
