package shell

import (
	"bytes"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestServesTheFaviconFromTheShell(t *testing.T) {
	recorder := httptest.NewRecorder()
	Handler(t.TempDir()).ServeHTTP(recorder, httptest.NewRequest(http.MethodGet, "/favicon.png", nil))

	if recorder.Code != http.StatusOK {
		t.Fatalf("status %d", recorder.Code)
	}
	if got := recorder.Header().Get("Content-Type"); got != "image/png" {
		t.Fatalf("content type %q", got)
	}
	if !bytes.HasPrefix(recorder.Body.Bytes(), []byte("\x89PNG")) {
		t.Fatalf("body is not a png")
	}
}
