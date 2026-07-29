// Package config reads the .kensa-properties file that maps friendly test
// folder names to output directories. It is shared by the serve command and
// the MCP server so both understand the same names.
package config

import (
	"fmt"
	"os"
	"path/filepath"
	"sort"

	"gopkg.in/yaml.v3"
)

// FileName is the per-project config file, read relative to the working
// directory.
const FileName = ".kensa-properties"

type Config struct {
	TestFolders map[string]string `yaml:"testFolders"`
	Port        int               `yaml:"port,omitempty"`
}

// Load reads the config from the current working directory.
func Load() (*Config, error) { return LoadFrom(".") }

// LoadFrom reads the config from dir.
func LoadFrom(dir string) (*Config, error) {
	path := filepath.Join(dir, FileName)
	data, err := os.ReadFile(path)
	if err != nil {
		return nil, fmt.Errorf("Failed to read %s: %v. Please ensure the file exists.", FileName, err)
	}

	var cfg Config
	if err := yaml.Unmarshal(data, &cfg); err != nil {
		return nil, fmt.Errorf("Invalid YAML in %s: %v. Please check the file format.", FileName, err)
	}

	if cfg.TestFolders == nil {
		return nil, fmt.Errorf("%s has no 'testFolders' section. Please add this section.", FileName)
	}

	return &cfg, nil
}

// FolderNames returns the configured names in a stable order, for error
// messages that have to offer the caller a choice.
func (c *Config) FolderNames() []string {
	names := make([]string, 0, len(c.TestFolders))
	for name := range c.TestFolders {
		names = append(names, name)
	}
	sort.Strings(names)
	return names
}
