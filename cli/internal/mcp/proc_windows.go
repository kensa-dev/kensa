//go:build windows

package mcp

import (
	"errors"
	"os"
	"syscall"
)

// processAlive reports whether a process with the given pid exists. On
// Windows os.FindProcess opens a handle and fails when there is none; access
// denied means there is one we may not open (elevated, or another user's).
func processAlive(pid int) bool {
	if pid <= 0 {
		return false
	}
	p, err := os.FindProcess(pid)
	if err != nil {
		return errors.Is(err, syscall.ERROR_ACCESS_DENIED)
	}
	p.Release()
	return true
}
