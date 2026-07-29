package mcp

import (
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"os"
	"path/filepath"
	"sort"
	"strconv"
)

func profileCacheKey(dir string) string {
	h := sha256.New()
	files := append(sourceFiles(dir), buildFiles(dir)...)
	sort.Strings(files)
	for _, f := range files {
		h.Write([]byte(f + "|" + fileStat(f) + "\n"))
	}
	return hex.EncodeToString(h.Sum(nil))
}

func fileStat(f string) string {
	fi, err := os.Stat(f)
	if err != nil {
		return ""
	}
	return fi.ModTime().UTC().String() + ":" + strconv.FormatInt(fi.Size(), 10)
}

type cachedProfile struct {
	Key     string       `json:"key"`
	Profile StyleProfile `json:"profile"`
}

func cachePath(dir string) string { return filepath.Join(dir, ".kensa", "style-profile.json") }

func storeCachedProfile(dir, key string, p StyleProfile) error {
	if err := os.MkdirAll(filepath.Join(dir, ".kensa"), 0o755); err != nil {
		return err
	}
	b, err := json.Marshal(cachedProfile{Key: key, Profile: p})
	if err != nil {
		return err
	}
	return os.WriteFile(cachePath(dir), b, 0o644)
}

func loadCachedProfile(dir, key string) (StyleProfile, bool) {
	b, err := os.ReadFile(cachePath(dir))
	if err != nil {
		return StyleProfile{}, false
	}
	var c cachedProfile
	if json.Unmarshal(b, &c) != nil || c.Key != key {
		return StyleProfile{}, false
	}
	return c.Profile, true
}
