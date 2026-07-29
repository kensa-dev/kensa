package mcp

import (
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"github.com/kensa-dev/kensa/cli/internal/config"
)

// bundleRef is one data-only bundle: a directory holding indices.json and
// results/. Source names the site-mode source it came from, and is empty for a
// standalone bundle.
type bundleRef struct {
	Dir    string
	Source string
}

type siteManifest struct {
	Sources []struct {
		ID  string `json:"id"`
		URL string `json:"url"`
	} `json:"sources"`
}

// resolveBundles turns whatever the caller supplied into the bundles to read.
// spec may be a directory (single bundle, site root, or one source of a site),
// a folder name from .kensa-properties, or empty to fall back to the config.
func resolveBundles(spec string) ([]bundleRef, error) {
	return resolveBundlesIn(".", spec)
}

// resolveBundlesIn is resolveBundles with an explicit working directory, which
// is where .kensa-properties is looked up.
func resolveBundlesIn(cwd, spec string) ([]bundleRef, error) {
	dir, err := resolveDir(cwd, spec)
	if err != nil {
		return nil, err
	}
	return expand(dir)
}

func resolveDir(cwd, spec string) (string, error) {
	if spec == "" {
		cfg, err := config.LoadFrom(cwd)
		if err != nil {
			return "", fmt.Errorf("no bundle_dir given and no usable %s in %s: %w", config.FileName, cwd, err)
		}
		names := cfg.FolderNames()
		if len(names) != 1 {
			return "", fmt.Errorf("no bundle_dir given and %s defines %d test folders (%s) — pass one of those names, or a path",
				config.FileName, len(names), strings.Join(names, ", "))
		}
		return cfg.TestFolders[names[0]], nil
	}

	// A name from .kensa-properties wins over a same-named directory only when
	// no such directory exists, so an explicit path is never shadowed.
	if !isDir(spec) {
		if cfg, err := config.LoadFrom(cwd); err == nil {
			if path, ok := cfg.TestFolders[spec]; ok {
				return path, nil
			}
		}
	}
	return spec, nil
}

// expand turns a directory into the bundles it contains: itself for a single
// bundle, or one per source for a site-mode root.
func expand(dir string) ([]bundleRef, error) {
	if fileExists(filepath.Join(dir, "indices.json")) {
		return []bundleRef{{Dir: dir}}, nil
	}

	manifestPath := filepath.Join(dir, "manifest.json")
	if fileExists(manifestPath) {
		b, err := os.ReadFile(manifestPath)
		if err != nil {
			return nil, err
		}
		var m siteManifest
		if err := json.Unmarshal(b, &m); err != nil {
			return nil, fmt.Errorf("could not read %s: %w", manifestPath, err)
		}
		if len(m.Sources) == 0 {
			return nil, fmt.Errorf("%s lists no sources", manifestPath)
		}
		refs := make([]bundleRef, 0, len(m.Sources))
		for _, s := range m.Sources {
			refs = append(refs, bundleRef{Dir: filepath.Join(dir, filepath.FromSlash(s.URL)), Source: s.ID})
		}
		return refs, nil
	}

	if !isDir(dir) {
		return nil, fmt.Errorf("%s is not a readable directory", dir)
	}
	return nil, fmt.Errorf("%s is not a Kensa output directory: expected indices.json (a test bundle), or manifest.json (a site-mode root)", dir)
}

// sourceLabels names the bundles searched, for error messages.
func sourceLabels(refs []bundleRef) string {
	labels := make([]string, 0, len(refs))
	for _, r := range refs {
		if r.Source != "" {
			labels = append(labels, r.Source)
		} else {
			labels = append(labels, r.Dir)
		}
	}
	return strings.Join(labels, ", ")
}

func isDir(path string) bool {
	fi, err := os.Stat(path)
	return err == nil && fi.IsDir()
}

func fileExists(path string) bool {
	_, err := os.Stat(path)
	return err == nil
}
