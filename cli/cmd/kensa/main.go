package main

import (
	"flag"
	"fmt"
	"log"
	"net"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"time"

	"gopkg.in/yaml.v3"
)

var (
	dir  = flag.String("dir", "", "directory containing the kensa web app files (overrides config)")
	port = flag.Int("port", 8080, "port to listen on")
	open = flag.Bool("open", true, "automatically open the browser")
)

const configFile = ".kensa-properties"

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

	// Make absolute for consistency
	absDir, err := filepath.Abs(serveDir)
	if err != nil {
		log.Fatalf("Could not resolve directory %s: %v", serveDir, err)
	}
	serveDir = absDir

	if _, err := os.Stat(serveDir); os.IsNotExist(err) {
		log.Fatalf("Directory %s does not exist. Please verify the path.", serveDir)
	}

	// Check required files
	if _, err := os.Stat(filepath.Join(serveDir, "index.html")); os.IsNotExist(err) {
		log.Fatalf("Missing index.html in %s. This file is required.", serveDir)
	}
	if _, err := os.Stat(filepath.Join(serveDir, "kensa.js")); os.IsNotExist(err) {
		log.Fatalf("Missing kensa.js in %s. Please ensure the output folder is correctly set up.", serveDir)
	}

	http.Handle("/", http.FileServer(http.Dir(serveDir)))

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
	time.Sleep(500 * time.Millisecond) // Allow the server to start.

	var cmd string
	var args []string

	switch runtime.GOOS {
	case "windows":
		cmd = "cmd"
		args = []string{"/c", "start", url}
	case "darwin":
		cmd = "open"
		args = []string{url}
	default: // Linux, BSD, etc.
		cmd = "xdg-open"
		args = []string{url}
	}

	err := exec.Command(cmd, args...).Run()
	if err != nil {
		log.Printf("Failed to open browser: %v. Please navigate to %s manually.", err, url)
	}
}
