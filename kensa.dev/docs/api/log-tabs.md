---
sidebar_position: 8
description: How to surface external log output — files or Docker container stdout — in a per-invocation report tab, correlated to each test by a tracking identifier.
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Log Tabs

A log tab pulls log output from outside the JVM — a file on disk, or the stdout of a Docker container — and shows the slice that belongs to one test invocation. Each block of output is keyed by a correlation identifier (typically the tracking id the test sent into the system under test), so the tab for each invocation shows only the lines for that run.

Log tabs combine three pieces:

| Piece | Role |
|---|---|
| `LogQueryService` | Provides log records, indexed by `sourceId` and `identifier`. |
| `@KensaTab` with `LogsTabRenderer` | Declares the tab and binds it to a `sourceId`. |
| `InvocationIdentifierProvider` | Returns the correlation identifier for the current invocation. |

The Docker integration (`kensa-docker-logs`) is a `LogQueryService` implementation that shells out to `docker logs <container>` and parses the result. The rest of the wiring is shared with file-based log sources.

---

## Module

```kotlin
testImplementation("dev.kensa:kensa-docker-logs:<version>")
```

The module depends on `kensa-core` and uses the local `docker` CLI on the host running the tests. There is no Docker SDK dependency — `ProcessBuilder` invokes `docker logs` directly.

---

## Quick start

The minimum: one tab annotation, one identifier provider, one `LogQueryService` registration.

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
import dev.kensa.KensaTab
import dev.kensa.Kensa.konfigure
import dev.kensa.service.logs.LogPatterns.idField
import dev.kensa.service.logs.LogQueryService
import dev.kensa.service.logs.LogQueryServiceRegistry.Companion.compositeLogQueryService
import dev.kensa.service.logs.docker.dockerCli
import dev.kensa.tabs.InvocationIdentifierProvider
import dev.kensa.tabs.KensaTabContext
import dev.kensa.tabs.logs.LogsTabRenderer

object TrackingIdProvider : InvocationIdentifierProvider {
    override fun identifier(ctx: KensaTabContext): String? =
        ctx.fixtures[TrackingIdFx]
}

@KensaTab(
    name = "App Logs",
    renderer = LogsTabRenderer::class,
    identifierProvider = TrackingIdProvider::class,
    sourceId = "appLog"
)
interface WithAppLogs

class MyTest : KensaTest, WithAppLogs { /* ... */ }
```

Then register the `LogQueryService` once, somewhere that runs before tests (a JUnit extension `init` block, a `@BeforeAll`, a Kotest project config):

```kotlin
konfigure {
    registerTabService(LogQueryService::class) {
        compositeLogQueryService {
            dockerCli(
                id = "appLog",
                container = "my-app",
                idPattern = idField("TrackingId"),
                delimiterLine = "***********"
            )
        }
    }
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
@KensaTab(
    name = "App Logs",
    renderer = LogsTabRenderer.class,
    identifierProvider = TrackingIdProvider.class,
    sourceId = "appLog"
)
public interface WithAppLogs {}

public class MyTest implements KensaTest, WithAppLogs { /* ... */ }
```

</TabItem>
</Tabs>

When a test in `MyTest` runs, the tab generator:

1. Calls `TrackingIdProvider.identifier(ctx)` to get the tracking id used by this invocation.
2. Calls `LogQueryService.query(sourceId = "appLog", identifier = <tracking-id>)`.
3. Renders the joined `text` of each returned `LogRecord` into the tab.

If no identifier is available, or no records match, the renderer falls back to `LogQueryService.queryAll(sourceId)`. If both come back empty, the tab is omitted for that invocation.

---

## How blocks are extracted

`dockerCli` (and `indexedFile`, its file-based sibling) does not parse log lines one at a time. It splits the stream into **blocks** using a delimiter pattern, then extracts an identifier from each block.

```
*********** <-- delimiter line: start of block
TrackingId: AAA-123
2026-02-02T12:34:56Z hit /orders endpoint
2026-02-02T12:34:57Z persisted order id=42
*********** <-- delimiter line: start of next block
TrackingId: BBB-456
...
```

A block is everything from one delimiter line up to (but not including) the next one. The identifier pattern is then matched against each line in the block, and the first matching line's capture group 1 is the identifier.

The delimiter can be specified two ways:

- **`delimiterLine: String`** — convenience form. The exact string (trimmed) must appear at the start of a line, with optional trailing suffix. Equivalent to `LogPatterns.delimiterPrefix(line)`.
- **`delimiterPattern: Regex`** — full-line regex. Anchor with `^...$`; use this when blocks are delimited by something structural like an ISO-8601 timestamp.

### Pattern helpers

`LogPatterns` provides ready-made regexes for the common cases:

| Helper | Matches |
|---|---|
| `idField("TrackingId")` | `TrackingId: <value>` (default separator `:`) |
| `idField("TrackingId", listOf(":", "=", "->"))` | Multiple allowed separators |
| `idFieldAnySeparator("TrackingId")` | `TrackingId <any-non-word-punct> <value>` |
| `iso8601Timestamp` | `2026-02-02T12:34:56Z` (optional offset) |
| `iso8601TimestampMillis` | `2026-02-02T12:34:56.123Z` |
| `logbackTimestamp` | `2026-02-02 12:34:56,123` |
| `timeOnlyTimestamp` | `12:34:56` or `12:34:56.123` |
| `delimiterPrefix("***")` | Lines starting with `***`, trailing suffix allowed |

Both identifier and delimiter regexes must match against a **whole line** (group 1 of the identifier regex captures the value). When using a timestamp as the delimiter, the timestamp line itself counts as the start of a new block — useful when logs don't have explicit separators.

---

## Wiring the tab

### `@KensaTab` fields

| Field | Purpose |
|---|---|
| `id` | Optional explicit stable tab id. When blank, an id is derived from renderer + identifier provider + source + name. Set it if you need the tab id to stay stable across refactors. |
| `name` | Label shown on the tab button in the UI. |
| `renderer` | Use `LogsTabRenderer::class` for log tabs. |
| `identifierProvider` | Class returning the correlation id for the current invocation. |
| `sourceId` | Selects which registered `LogQueryService` source to query. Must match the `id` passed to `dockerCli` / `rawFile` / `indexedFile`. |
| `visibility` | `Always` (default) or `OnlyOnFailure` — useful for keeping passing-test reports lean. |
| `scope` | `PerInvocation` (default) — the only meaningful scope for log tabs. |

`@KensaTab` is `@Repeatable`. Apply multiple annotations to surface one tab per source. A marker interface per source is the recommended pattern — test classes opt in by implementing the interfaces they need.

### Identifier providers

A provider receives the full `KensaTabContext` and returns the correlation key as a string. The context exposes the invocation's `fixtures`, `capturedOutputs`, `attachments`, the test class and method names, and the invocation index.

```kotlin
object TrackingIdProvider : InvocationIdentifierProvider {
    override fun identifier(ctx: KensaTabContext): String? =
        ctx.fixtures[TrackingIdFx]
}
```

Returning `null` falls back to `queryAll(sourceId)` — the tab will show every block from the source. That is usually too noisy; prefer a stable per-invocation key.

---

## Multiple containers

Register one source per container. Each gets its own `sourceId`, its own tab annotation, and is queried independently.

```kotlin
konfigure {
    registerTabService(LogQueryService::class) {
        compositeLogQueryService {
            dockerCli(
                id = "appLog",
                container = "my-app",
                idPattern = idField("TrackingId"),
                delimiterLine = "***********"
            )
            dockerCli(
                id = "workerLog",
                container = "my-worker",
                idPattern = idField("TrackingId"),
                delimiterLine = "***********"
            )
        }
    }
}

@KensaTab(name = "App Logs",    renderer = LogsTabRenderer::class, identifierProvider = TrackingIdProvider::class, sourceId = "appLog")
@KensaTab(name = "Worker Logs", renderer = LogsTabRenderer::class, identifierProvider = TrackingIdProvider::class, sourceId = "workerLog")
interface WithServiceLogs
```

Logs are fetched lazily, on demand, the first time a test for that source queries — `docker logs` runs once per source per test suite (the result is indexed and cached).

---

## File-based sources

The same machinery works for log files written to disk. Replace `dockerCli` with `rawFile` (tail the last N lines) or `indexedFile` (block-index by identifier, as with Docker):

```kotlin
compositeLogQueryService {
    rawFile("appLog", Path("build/logs/app.log"), tailLines = 200)
    indexedFile(
        "auditLog",
        Path("build/logs/audit.log"),
        idPattern = idField("TrackingId"),
        delimiterLine = "[SKY_LOG_START]"
    )
}
```

`rawFile` ignores the identifier and shows the tail of the file — useful when correlation isn't possible but you still want eyes on recent output. `indexedFile` behaves exactly like `dockerCli`, just reading from a file instead of `docker logs`.

You can mix sources of all three types in one registry; each is keyed by its own `sourceId`.

---

## Troubleshooting

- **Empty tab on every test.** The identifier provider is returning `null`, or none of the blocks contain a line matching `idPattern`. Inspect the raw `docker logs <container>` output and confirm the pattern captures group 1 from a real line.
- **Wrong block returned.** Blocks are delimited by `delimiterLine`/`delimiterPattern`, not by the identifier line. If the identifier appears multiple times within one block, the first match wins.
- **`IllegalStateException: docker logs failed`.** The CLI returned non-zero. Run the same `docker logs <container>` manually to see the underlying error (container name typo, daemon not running, permissions).
- **Logs missing from later tests.** The Docker query is built lazily and cached for the lifetime of the test suite — log lines emitted after the index is first built are not picked up. If you need a fresh snapshot, restart the suite.
- **Block has no identifier.** Blocks without an id-matching line are silently dropped from the per-test view but are still returned by `queryAll`. Add an id field to the producer, or use `rawFile` if correlation isn't feasible.

---

## API reference

### `dockerCli` (extension on `LogQueryServiceRegistry`)

```kotlin
fun LogQueryServiceRegistry.dockerCli(
    id: String,
    container: String,
    idPattern: Regex,
    delimiterLine: String
)

fun LogQueryServiceRegistry.dockerCli(
    id: String,
    container: String,
    idPattern: Regex,
    delimiterPattern: Regex
)
```

| Parameter | Purpose |
|---|---|
| `id` | `sourceId` for this source — referenced by `@KensaTab.sourceId`. |
| `container` | Docker container name or id passed to `docker logs`. |
| `idPattern` | Whole-line regex; group 1 captures the identifier. |
| `delimiterLine` / `delimiterPattern` | Boundary between blocks. |

### `DockerCliLogQueryService`

Constructed directly when you need multiple containers indexed under one service, or want to inject a custom `DockerLogsRunner` for testing:

```kotlin
class DockerCliLogQueryService(
    sources: List<DockerSource>,
    idPattern: Regex,
    delimiterRegex: Regex,
    runner: DockerLogsRunner = ProcessDockerLogsRunner()
) : LogQueryService
```

### `DockerLogsRunner`

```kotlin
fun interface DockerLogsRunner {
    fun logs(container: String): Sequence<String>
}
```

The default `ProcessDockerLogsRunner` shells out to `docker logs <container>`. Substitute a fake in unit tests to feed known lines.
