package shell

import (
	_ "embed"
	"net/http"
	"os"
	"path/filepath"
)

//go:embed embed/index.html
var indexHTML []byte

//go:embed embed/kensa.js
var kensaJS []byte

//go:embed embed/logo.svg
var logoSVG []byte

type embeddedAsset struct {
	bytes       []byte
	contentType string
}

func Handler(serveDir string) http.Handler {
	embedded := map[string]embeddedAsset{
		"/index.html": {indexHTML, "text/html; charset=utf-8"},
		"/kensa.js":   {kensaJS, "application/javascript"},
		"/logo.svg":   {logoSVG, "image/svg+xml"},
	}
	fileServer := http.FileServer(http.Dir(serveDir))
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		urlPath := r.URL.Path
		if urlPath == "/" {
			urlPath = "/index.html"
		}
		asset, isShell := embedded[urlPath]
		if !isShell {
			fileServer.ServeHTTP(w, r)
			return
		}
		body := asset.bytes
		if data, err := os.ReadFile(filepath.Join(serveDir, urlPath[1:])); err == nil {
			body = data
		}
		w.Header().Set("Content-Type", asset.contentType)
		w.Write(body)
	})
}
