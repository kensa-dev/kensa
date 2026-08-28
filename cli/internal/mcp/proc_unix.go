//go:build !windows

package mcp

import (
	"errors"
	"syscall"
)

// processAlive reports whether a process with the given pid exists. Signal 0
// probes without delivering anything; EPERM means it exists but is not ours.
func processAlive(pid int) bool {
	if pid <= 0 {
		return false
	}
	err := syscall.Kill(pid, 0)
	return err == nil || errors.Is(err, syscall.EPERM)
}
