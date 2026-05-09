package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"net"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strconv"
	"strings"
	"time"

	"github.com/kensa-dev/kensa/cli/internal/shell"
	"gopkg.in/yaml.v3"
)

var (
	dir  = flag.String("dir", "", "directory containing the kensa web app files (overrides config)")
	port = flag.Int("port", 8080, "port to listen on")
	open = flag.Bool("open", true, "automatically open the browser")
)

const configFile = ".kensa-properties"

// version is the Kensa release the embedded shell artifacts were built from.
// Overridden at build time via `-ldflags "-X main.version=$(cat version.txt)"`.
var version = "dev"

type Config struct {
	TestFolders map[string]string `yaml:"testFolders"`
	Port        int               `yaml:"port,omitempty"`
}

func main() {
	flag.Parse()

	var portSet bool
	flag.Visit(func(f *flag.Flag) {
		if f.Name == "port" {
			portSet = true
		}
	})

	var serveDir string
	args := flag.Args()

	if *dir != "" {
		serveDir = *dir
	} else if len(args) > 0 {
		cfg, err := loadConfig()
		if err != nil {
			log.Fatal(err)
		}
		path, ok := cfg.TestFolders[args[0]]
		if !ok {
			log.Fatalf("No test folder named '%s' found in %s config. Please check the name or add it to the config.", args[0], configFile)
		}
		serveDir = path

		if !portSet && cfg.Port != 0 {
			*port = cfg.Port
		}
	} else {
		log.Fatal("Please specify a test name (from .kensa-properties config) or use --dir /path/to/folder.")
	}

	absDir, err := filepath.Abs(serveDir)
	if err != nil {
		log.Fatalf("Could not resolve directory %s: %v", serveDir, err)
	}
	serveDir = absDir

	if _, err := os.Stat(serveDir); os.IsNotExist(err) {
		log.Fatalf("Directory %s does not exist. Please verify the path.", serveDir)
	}

	if err := validateKensaDir(serveDir); err != nil {
		log.Fatal(err)
	}

	if reportVersion := detectReportVersion(serveDir); reportVersion != "" && versionLess(version, reportVersion) {
		log.Printf("warning: kensa CLI version %s is older than report version %s; some UI features may not render correctly. Update the CLI to match.", version, reportVersion)
	}

	http.Handle("/", shell.Handler(serveDir))

	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))
	if err != nil {
		log.Fatalf("Could not listen on port %d: %v", *port, err)
	}

	url := fmt.Sprintf("http://localhost:%d/index.html", *port)
	log.Printf("Serving kensa web app from %s at http://localhost:%d/", serveDir, *port)

	if *open {
		go openBrowser(url)
	}

	log.Fatal(http.Serve(lis, nil))
}

// validateKensaDir confirms serveDir looks like a Kensa report. It accepts:
//   - Single-bundle output (build/kensa-output/) — has configuration.json + indices.json at root.
//   - Site-mode root (build/kensa-site/) — has manifest.json + sources/ subdir.
//   - Data-only source bundle (build/kensa-site/sources/<id>/) — has configuration.json + indices.json.
func validateKensaDir(serveDir string) error {
	hasConfig := fileExists(filepath.Join(serveDir, "configuration.json"))
	hasIndices := fileExists(filepath.Join(serveDir, "indices.json"))
	hasManifest := fileExists(filepath.Join(serveDir, "manifest.json"))
	if (hasConfig && hasIndices) || hasManifest {
		return nil
	}
	return fmt.Errorf("%s does not look like a Kensa output directory: expected configuration.json + indices.json, or a manifest.json (site-mode root)", serveDir)
}

// detectReportVersion reads kensaVersion from configuration.json (single-bundle/data-only) or
// manifest.json (site-mode root). Returns "" if neither is parseable.
func detectReportVersion(serveDir string) string {
	if v := readKensaVersion(filepath.Join(serveDir, "configuration.json")); v != "" {
		return v
	}
	if v := readKensaVersion(filepath.Join(serveDir, "manifest.json")); v != "" {
		return v
	}
	return ""
}

func readKensaVersion(path string) string {
	data, err := os.ReadFile(path)
	if err != nil {
		return ""
	}
	var doc struct {
		KensaVersion string `json:"kensaVersion"`
	}
	if err := json.Unmarshal(data, &doc); err != nil {
		return ""
	}
	return doc.KensaVersion
}

// versionLess reports whether a < b for version strings like "0.7.1".
// Returns false if either side is empty, "dev", or unparseable — so we don't
// nag about version skew in dev builds.
func versionLess(a, b string) bool {
	pa, ok := parseVersion(a)
	if !ok {
		return false
	}
	pb, ok := parseVersion(b)
	if !ok {
		return false
	}
	for i := 0; i < len(pa) && i < len(pb); i++ {
		if pa[i] != pb[i] {
			return pa[i] < pb[i]
		}
	}
	return len(pa) < len(pb)
}

func parseVersion(v string) ([]int, bool) {
	v = strings.TrimSpace(v)
	if v == "" || v == "dev" {
		return nil, false
	}
	if dash := strings.IndexAny(v, "-+"); dash >= 0 {
		v = v[:dash]
	}
	parts := strings.Split(v, ".")
	out := make([]int, len(parts))
	for i, p := range parts {
		n, err := strconv.Atoi(p)
		if err != nil {
			return nil, false
		}
		out[i] = n
	}
	return out, true
}

func fileExists(path string) bool {
	_, err := os.Stat(path)
	return err == nil
}

func loadConfig() (*Config, error) {
	data, err := os.ReadFile(configFile)
	if err != nil {
		return nil, fmt.Errorf("Failed to read %s: %v. Please ensure the file exists.", configFile, err)
	}

	var cfg Config
	err = yaml.Unmarshal(data, &cfg)
	if err != nil {
		return nil, fmt.Errorf("Invalid YAML in %s: %v. Please check the file format.", configFile, err)
	}

	if cfg.TestFolders == nil {
		return nil, fmt.Errorf("%s has no 'testFolders' section. Please add this section.", configFile)
	}

	return &cfg, nil
}

func openBrowser(url string) {
	time.Sleep(500 * time.Millisecond)

	var cmd string
	var args []string

	switch runtime.GOOS {
	case "windows":
		cmd = "cmd"
		args = []string{"/c", "start", url}
	case "darwin":
		cmd = "open"
		args = []string{url}
	default:
		cmd = "xdg-open"
		args = []string{url}
	}

	err := exec.Command(cmd, args...).Run()
	if err != nil {
		log.Printf("Failed to open browser: %v. Please navigate to %s manually.", err, url)
	}
}
