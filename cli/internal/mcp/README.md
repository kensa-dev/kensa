# Kensa MCP Server

An [MCP](https://modelcontextprotocol.io) server exposing Kensa test data and
project style heuristics to AI agents. Built on the
[`go-sdk`](https://github.com/modelcontextprotocol/go-sdk) and served over stdio.

Run it with:

```bash
kensa mcp
```

## Tool groups

The server registers thirteen tools in two groups.

### Group A — bundle inspection

These tools read an existing **kensa-output bundle** (the directory Kensa writes
when a test run completes, containing `indices.json` and per-class result JSON).
If you have not run a Kensa test suite, there is no bundle and these tools have
nothing to read.

| Tool | Args | Returns |
|------|------|---------|
| `list_tests` | `bundle_dir` (optional), `state` (string, optional), `children` (bool, optional) | `{ tests: TestEntry[], bundleWrittenAt, bundleAge }` — every test class, optionally filtered by state. Compact by default: each class row carries `methods` (state counts — `passed`, `failed`, `disabled`, `notExecuted`, `total`) and `elapsedMs` (its methods' `timing` summed, absent when none of them carry timing — a pre-0.9.2 bundle), with `children` left out. Pass `children: true` to include the method rows under each class. A class whose immediate children are not all methods (a nested container, such as a suite-level container holding other classes) is left as-is regardless of `children`: children kept, no counts. Each `TestEntry` has `id`, `testClass`, `testMethod` (child entries only), `displayName`, `state`, `tags`, `issues`, `epics`, `hasErrors`, `source`, `timing` (one `[startMs, elapsedMs]` pair per invocation), `participants` (interaction count per named participant), `assertions`, `expandables`, `methods`, `elapsedMs`, and nested `children`. |
| `list_failures` | `bundle_dir` (optional) | `{ failures: TestEntry[], bundleWrittenAt, bundleAge }` — only the test classes whose `state` is `Failed`, with `children`; `methods`/`elapsedMs` are populated whenever the class's children are all methods. |
| `get_test` | `bundle_dir` (optional), `id` (string), `raw` (bool, optional) | The result for one test class, rendered: `tests[]` → `{testMethod, displayName, state, elapsedTime, invocations[]}` with `sentences[{line, text}]`, `fixtures`, `interactions` (names) and `exception {message, sourceLocation}` where one failed. A child id `<class>:<method>` narrows `tests[]` to that one method. `raw: true` returns the result file verbatim, token stream and diagrams included, always for the whole class regardless of a child id. |
| `failure_evidence` | `bundle_dir` (optional), `id` (string) | `{ testClass, state, failures[], distinctExceptions }` — one entry per failed invocation with `testMethod`, `failingSentence`, `failingSentenceLine`, `exception` and `sourceLocation` (the deepest stack frame inside the test class, e.g. `PaymentTest.kt:107`). |
| `captured_interactions` | `bundle_dir` (optional), `id` (string), `max_value_chars` (int, optional, default 4000) | `{ testClass, methods[] }` — every interaction Kensa captured, per method and invocation: `name`, `from`, `to`, `values[{name, value, language, truncated, fullLength}]` (request and response bodies, URLs) and `attributes` grouped by name (`Status`, `Headers`). A child id `<class>:<method>` narrows to one method. |
| `run_status` | `bundle_dir` (optional) | `{ runState, runStartedAt, runFinishedAt, runAge, classesWritten, passed, failed, disabled, pid, sources[] }` — the state of the run that produced the bundle. `passed`, `failed` and `disabled` are the method counts so far while a run is unfinished, present only when the marker carries them. `runState` is `complete`, `running`, `abandoned` or `incomplete`. `sources` breaks a site root down per source. |
| `await_results` | `bundle_dir` (optional), `timeout_seconds` (int, optional, default 600, max 3600) | `{ completed, timedOut, runState, ... }` — blocks until the next run completes, then reports it. |
| `suite_summary` | `bundle_dir` (optional), `slowest` (int, optional, default 10) | `{ runState, runStartedAt, runFinishedAt, runDuration, classes, methods, totalElapsedMs, durations[], slowest[], failures[], byTag[], byPackage[], participants[], bundleWrittenAt, bundleAge }` — a one-call overview of a completed run: state counts for classes and methods, the run window and duration, duration buckets, the slowest methods, failure ids, counts by tag and package, and participants. Refuses an incomplete run the same way `list_tests` and `list_failures` do. |
| `list_log_sources` | `bundle_dir` (optional) | `{ sources: LogSource[], bundleWrittenAt, bundleAge, notice }` — every log source the suite registered, as recorded in `run.json`: `id`, `file` and whether it was `present` when the run finished. For a site root each row also carries the `source` it came from, since two sources register ids independently and the same id can appear in both. `notice` explains an empty list written by a bundle from before Kensa 1.0.0. |
| `invocation_logs` | `bundle_dir` (optional), `id` (string), `invocation` (int, optional, default 0) | `{ id, invocation, state, identifier, logs[], notice }` — one row per log source seen by this invocation: a rendered tab (`source`, `label`, `entries`, `errors`, `path`), a tab the run skipped (`skipped` carries the `@OnlyOnFailure` visibility), or a source the run registered with no tab here (`declared: false`). `errors` counts the tab's records whose first line reads as an error. |
| `read_log` | `bundle_dir` (optional), `id` (string), `source` (string), `invocation` (int, optional, default 0), `pattern` (regex, optional), `level` (string, optional), `max_entries` (int, optional, default 50, `-1` for unlimited), `max_entry_chars` (int, optional, default 4000, `-1` for unlimited) | `{ source, matched, total, entries: [{n, text, truncated, fullLength}], notice }` — the records of one log tab, filtered by `pattern` (matched against the whole record) or `level` (matched against the record's first line only). `matched` counts every record the filters kept, including any past `max_entries`, so a capped answer still says there is more. |

`captured_interactions` truncates each captured value to `max_value_chars`
characters (runes, not bytes), 4000 by default; a truncated value carries
`truncated: true` and `fullLength` (the untruncated length). Pass `-1` for no
cap.

**`bundle_dir` accepts four things**, so the agent rarely needs a literal path:

| Value | Resolves to |
|---|---|
| omitted | The sole entry in `.kensa-properties`; an error naming the choices if there is more than one. |
| a name from `.kensa-properties` | That entry's directory — the same names `kensa <name>` serves. |
| a single bundle (`build/kensa-output`) | Itself. |
| a [site-mode](https://kensa.dev/docs/build-plugins/site-mode) root (`build/kensa-site`) | Every source listed in its `manifest.json`. |

A site root expands to all of its sources: listings cover them all and tag each
entry with the `source` it came from, and lookups by id search every source.
Pointing at one source directory (`build/kensa-site/sources/uiTest`) also works,
since each is a complete bundle. `.kensa-properties` is read from the server's
working directory, which is the project root when the client spawns it there.

**Run state.** Kensa core (0.9.2 and later) writes `run.json` into the bundle
when the first test starts (`startedAt`, `pid`, `hostname`) and finalises it
with `finishedAt` once the whole report is on disk. From that the server
classifies a bundle as:

| `runState` | Meaning |
|---|---|
| `complete` | `finishedAt` set, or `indices.json` present. The results are the whole run. |
| `running` | No `finishedAt`, and the JVM in `pid` is alive on this host. |
| `abandoned` | No `finishedAt`, no `indices.json`, and the JVM is gone. The run crashed or was killed. |
| `incomplete` | `results/` without `indices.json` and no way to tell: no `run.json` (Kensa before 0.9.2), or a marker written on another host, whose pid means nothing here. |

`list_tests` and `list_failures` refuse anything but `complete` with an error
that says which state it is, how many classes are written so far, and what to
do next; for a site root it names each source that is not complete. A partial
listing would read as a clean one. `get_test` and `failure_evidence` still
work mid-run for classes already written.

The marker is written when the first Kensa test *starts*, so between launching
the tests and that moment the previous bundle is still on disk and still reads
as complete. `await_results` covers that gap: called straight after launching
the tests, it waits for the next completion, whether the run has started
writing yet or not, and returns `completed: true` with the new state, or
`timedOut: true` with the current state. A source abandoned by an earlier run
does not hold the wait up; it shows in the returned state. Progress
notifications go out every 5 seconds when the client supplies a progress
token.

The pid check assumes the reader and the test JVM share a host and pid
namespace. A bundle produced in a container or on a CI agent and read
elsewhere carries a different `hostname`, so it is never probed: with
`indices.json` it is `complete`, without it `incomplete`.

**Freshness.** Both listings report `bundleWrittenAt` (the run marker's
`finishedAt`, or the modification time of `indices.json` for older bundles,
RFC 3339 UTC) and `bundleAge` (`3h12m`, `2d1h`). The tools read whatever the
last run wrote, so an old bundle says nothing about the current code. The
fields are omitted if the write time cannot be read.

**States** are exactly those Kensa writes: `Passed`, `Failed`, `Disabled`, `Not Executed`.
The `state` filter ignores case and spacing, so `not executed` and `NotExecuted`
both match.

**Triage path.** `suite_summary` first when the question is about the run as a
whole (how long, how many, what was slow). `list_failures` → `failure_evidence` on the class → fix at
`sourceLocation`, or `captured_interactions` on the method when the assertion
message alone does not explain it → re-run → `await_results` → `list_failures`
again. `get_test` is for reading a whole class; it renders sentences as text
because the raw token stream is several thousand tokens per method and none of
it helps a diagnosis. When `failure_evidence` still does not explain the
failure, `invocation_logs` on the method shows which log sources carry
errors, then `read_log` with `level: "ERROR"` on the source that has them
reads the actual records, falling back to `captured_interactions` when the
logs are clean too.

**Which sentence failed.** Kensa parses sentences from source and does not
record which one threw, so `failure_evidence` derives it: the last sentence
starting at or before the `sourceLocation` line. Without a test-class frame in
the trace it falls back to the last sentence of the method.

**Ids.** `list_tests` returns a class id (`com.example.PaymentTest`) and child
ids of the form `<class>:<method>`. Results are written per class: a child id
to `failure_evidence` resolves to its owning class, and to `get_test` narrows
the rendered result to that one method (`raw: true` still returns the whole
file, since that is the file as written).

**`hasErrors`** marks a test Kensa could not fully parse or render. It is
independent of `state` — a passing test may still carry parse errors, and its
report sentences will be incomplete.

### Group B — project style profile

| Tool | Args | Returns |
|------|------|---------|
| `style_profile` | `project_dir` (string — project or module root), `no_cache` (bool, optional) | A heuristic profile of how the project writes Kensa tests: detected `framework`, `fixtures`, `matcherFields`, `stubHelpers`, `conventions`, and an `exemplar` (project-relative path + snippet of a representative test). |

Kotlin and Java sources are both scanned. Fixture containers are recognised in
either language (`object X : FixtureContainer` / `class X implements
FixtureContainer`), including `@Fixture` factory functions and Java's
`createFixture` / `createParameterFixture` declarations; names are attributed to
the container that declares them.

`framework` is one of `junit5`, `junit6`, `kotest`, `testng` or `unknown`, read
from `build.gradle.kts`, `build.gradle` or `pom.xml`. Detection keys on the
Kensa adapter artifact (`kensa-framework-*`) rather than on the presence of a
test library, because Kotest assertions alongside a JUnit adapter is a
documented combination and does not make a project a Kotest project.

`conventions` reports use of MatcherField, semantic matchers,
`@ExpandableSentence`, `@RenderedValue`, `@Highlight`, `thenEventually` and
`thenContinually`.

The `exemplar` snippet is capped at 6 KB, since it is handed to a model whole.

A thirteenth entry, `server_info`, reports the server's `name` (`kensa`) and
`version` — useful as a connectivity check.

## Install (Claude Code)

Everything after `--` is the command to run, so point it at the wrapper script
in your project root:

```bash
claude mcp add kensa -- ./kensa mcp
```

Or at a binary directly:

```bash
claude mcp add kensa -- /path/to/kensa mcp
```

Run it from the project root so the server picks up `.kensa-properties`. Check
it with `claude mcp list`, and remove it with `claude mcp remove kensa`.

The wrapper writes its download progress to stderr precisely so this works —
stdout carries the JSON-RPC stream, and a single stray line of output on it
breaks the session.

## Trust model

- **Local, with one cache write.** The server runs as a local process over stdio.
  Group A tools only read files inside the bundle they resolve to, plus
  `.kensa-properties` in the working directory; Group B only reads source under
  the `project_dir` you name. All tools are read-only **except**
  `style_profile`, which writes a hash-keyed cache to
  `<project_dir>/.kensa/style-profile.json` (gitignore it). Nothing else is
  written or modified.
- **No authentication, by design.** stdio MCP has no auth layer: the trust
  boundary is process spawn. The server runs as you and can read what you can
  read, which is why every tool takes an explicit directory rather than
  searching the filesystem.
- **Nothing leaves the machine.** There are no network calls. Test data and source
  stay on your filesystem; only the tool results requested by the agent are returned
  over the local stdio channel.

## Limitations

- **`style_profile` is a heuristic scan, not a semantic model.** It catalogues
  idioms by pattern-matching source — fixtures, matcher fields, stub helper shapes,
  framework markers. It does not compile or type-check the project, so it can miss
  unconventional code, custom abstractions, or idioms expressed in ways the scanner
  doesn't recognise. Treat the profile as a strong hint, not ground truth.
- **Group B is independent of Group A**, but **Group A requires an existing
  kensa-output bundle.** Run your Kensa suite first; without a bundle there is
  nothing for `list_tests`, `list_failures`, `get_test`, or `failure_evidence` to read.
