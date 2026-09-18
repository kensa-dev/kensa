package mcp

import (
	"context"
	"testing"

	"github.com/modelcontextprotocol/go-sdk/mcp"
)

func TestIntegrationInvocationLogs(t *testing.T) {
	ctx := context.Background()
	session := newConnectedSession(t, ctx)

	res, err := session.CallTool(ctx, &mcp.CallToolParams{
		Name: "invocation_logs",
		Arguments: map[string]any{
			"bundle_dir": "testdata/bundle",
			"id":         logsTestClass + ":canAdoptAnAvailableRobot",
		},
	})
	if err != nil {
		t.Fatalf("CallTool invocation_logs: %v", err)
	}
	if res.IsError {
		t.Fatalf("invocation_logs returned error result: %+v", res.Content)
	}

	var out invocationLogsOut
	decodeStructured(t, res, &out)
	if out.State != "Failed" || out.Identifier != "canAdoptAnAvailableRobot#0" {
		t.Fatalf("state = %q, identifier = %q", out.State, out.Identifier)
	}
	assertRows(t, out.Logs, []logRow{
		{
			Source:  "appLog",
			Label:   "App Log",
			Entries: intOf(3),
			Errors:  intOf(1),
			Path:    "tabs/dev.kensa.example.adoptabot.AdoptionServiceTest/canAdoptAnAvailableRobot/invocation-0/appLog.txt",
		},
		{Source: "auditLog", Label: "Audit Log", Entries: intOf(0), Errors: intOf(0)},
		{Source: "gatewayLog", Declared: boolOf(false)},
	})
}
