---
sidebar_position: 3
description: Site mode aggregates multiple Kensa-producing test executions (e.g. unit tests + UI tests + integration tests) into a single viewable HTML site, grouped per sourceset rather than flat-merged.
---

# Site Mode

Most projects run more than one Kensa-producing test execution: unit tests, UI tests, integration tests, often in distinct sourcesets. Default Kensa output gives each its own `kensa-output/` directory — three separate sites to open. Site mode aggregates them into one.

The aggregation **groups** rather than flat-merges: each sourceset's tests stay under their own root in the sidebar, with their own version, generated-at timestamp, and configuration. Switching between source roots is one click.

## Disk layout

**Default (single bundle, unchanged):**

```
build/kensa-output/
├── index.html
├── kensa.js
├── logo.svg
├── configuration.json
├── indices.json
├── results/
└── tabs/
```

**Site mode:**

```
build/kensa-site/
├── index.html          # shell — written by assembleKensaSite
├── kensa.js
├── logo.svg
├── manifest.json       # written by assembleKensaSite
└── sources/
    ├── uiTest/
    │   ├── configuration.json
    │   ├── indices.json
    │   ├── results/
    │   └── tabs/
    ├── scenarioTest/
    └── test/
```

Each `sources/<id>/` directory is a complete data-only bundle — the IntelliJ plugin and the Kensa CLI single-bundle viewer both work pointed at it directly.

## Manifest schema

`manifest.json` lists the sources the shell renders:

```json
{
  "schemaVersion": 1,
  "kensaVersion": "0.8.0",
  "sources": [
    { "id": "uiTest",       "title": "UI Tests",       "url": "sources/uiTest" },
    { "id": "scenarioTest", "title": "Scenario Tests", "url": "sources/scenarioTest" },
    { "id": "test",         "title": "Acceptance Tests","url": "sources/test" }
  ]
}
```

- `url` is resolved relative to `manifest.json`, so the same site works behind any host.
- Unknown future fields **must** be ignored by older shells; `schemaVersion` only bumps for breaking changes.

## Enabling site mode

Two lines in `build.gradle.kts`:

```kotlin
kensa {
    sourceSets = setOf("uiTest", "scenarioTest", "test")
    site = true
}
```

Then run the tests and assemble:

```bash
./gradlew check assembleKensaSite
```

Open `build/kensa-site/index.html`. The sidebar shows one root per sourceset.

### Partial runs

`assembleKensaSite` is **partial-run friendly** — running only some of the configured sourcesets produces a site with only those sources. Sourcesets that didn't run are warned about in the build output, not errored:

```
> Kensa source 'test' was expected but no bundle is present at build/kensa-site/sources/test;
  omitting from manifest. Run its test task/execution to include it.
```

So `./gradlew uiTest assembleKensaSite` is a valid dev-loop command — just produces a site with only the UI source.

### Stale source cleanup

If you remove a sourceset from `sourceSets`, `assembleKensaSite` deletes its bundle from `sources/` on the next run. No manual cleanup needed.

## Source IDs and titles

The runtime contract uses two system properties:

| Property | Effect |
|---|---|
| `kensa.source.id` | Per-source bundle directory name. Defaults to the sourceset name. |
| `kensa.source.title` | Overrides `Configuration.titleText` — the label shown for that source in the sidebar. |

Both are pre-populated by the Gradle plugin (with `id = sourceset name`, `title` from `Configuration.titleText`). Override either via `task.systemProperty` on the relevant Test task — see [per-source overrides](./gradle-plugin.md#per-source-overrides).

### Cross-build uniqueness in CI

If you publish per-build sites and want to point a Tauri/remote loader at multiple builds, override `kensa.source.id` to include the build number:

```kotlin
tasks.named<Test>("uiTest") {
    systemProperty("kensa.source.id", "${System.getenv("BUILD_NUMBER") ?: "local"}-uiTest")
    systemProperty("kensa.source.title", "Build #${System.getenv("BUILD_NUMBER") ?: "local"} — UI Tests")
}
```

## Multi-module builds

Since plugin **0.9.3**, site mode can aggregate sources across an entire Gradle multi-project build into one viewable site at the rootProject. No new plugin id, no enumeration of participating subprojects — apply the plugin where it's needed and turn `site = true` on at the root.

```kotlin title="rootProject/build.gradle.kts"
plugins {
    id("dev.kensa.gradle-plugin")
}

kensa {
    site = true
    // Optional cross-module title overrides, keyed by namespaced id:
    sourceTitles["web__test"]         = "Web tests"
    sourceTitles["api__test"]         = "API tests"
    sourceTitles["libs-billing__test"]= "Billing tests"
}
```

```kotlin title=":web/build.gradle.kts (subproject — unchanged from single-project usage)"
plugins {
    id("dev.kensa.gradle-plugin")
}

kensa {
    site = true
    sourceSets = setOf("test", "uiTest")
    // Optional contributor-local titles (override per source set name, not namespaced id):
    sourceTitles["uiTest"] = "Web UI"
}
```

Run `./gradlew test` — `:assembleKensaSite` is automatically finalized by every contributing Test task across modules, and `<rootDir>/build/kensa-site/` ends up with one aggregated manifest:

```
<rootDir>/build/kensa-site/
├── index.html
├── kensa.js
├── logo.svg
├── manifest.json
└── sources/
    ├── api__test/
    ├── libs-billing__test/
    ├── web__test/
    └── web__uiTest/
```

### Role discovery

The plugin decides each project's role automatically once every project has been evaluated:

| Project context                                                                            | Role        |
| ------------------------------------------------------------------------------------------ | ----------- |
| Is `rootProject`, `site = true`, ≥1 subproject also applies the plugin with `site = true`  | Aggregator  |
| Not root, `site = true`, root is Aggregator                                                | Contributor |
| `site = true` with no aggregator on root (single-project, or root doesn't apply the plugin) | Standalone  |
| `site = false` (default)                                                                   | (no site)   |

Subprojects that don't apply `dev.kensa.gradle-plugin` simply never register — they're not part of the site. Subprojects with `site = false` likewise don't contribute, even if they apply the plugin.

### Namespaced source ids

Each contributor's sources are identified by `<slug>__<sourceSet>` in the aggregated manifest, where slug strips the leading `:` from the project path and replaces inner `:` with `-`:

| Project path     | Source set | Namespaced id           |
| ---------------- | ---------- | ----------------------- |
| `:web`           | `test`     | `web__test`             |
| `:web`           | `uiTest`   | `web__uiTest`           |
| `:libs:billing`  | `test`     | `libs-billing__test`    |
| `:` (root's own) | `test`     | `<rootProjectName>__test` |

`__` is reserved as the separator — a contributor source-set name (or contributor `sourceTitles` key) containing `__` is rejected at configuration time.

### Title precedence

Aggregator mode adds one more rung at the top of the existing [title precedence](./gradle-plugin.md#site-mode-source-titles):

1. **Root `kensa.sourceTitles["<slug>__<sourceSet>"]`** — namespaced key, wins everything.
2. **Contributor `kensa.sourceTitles["<sourceSet>"]`** — bare source-set name, carried into the aggregator from the registering subproject.
3. Per-source `configuration.json#titleText` (from `Kensa.konfigure { titleText = ... }` or the runtime default).
4. The namespaced id itself.

### kensa-core version consistency

Each contributor's resolved `kensa.kensaCoreVersion` is captured at registration. `:assembleKensaSite` fails fast if any contributor's version differs from the aggregator's, naming the divergent modules:

```
Kensa site: kensa-core version mismatch. Aggregator: 0.8.3. :legacy reports 0.8.0.
Align kensa.kensaCoreVersion across modules, or override kensa.kensaCoreVersion on the root.
```

This prevents shipping a site whose shell (resolved on the root) doesn't match a contributor's bundle format.

### Partial runs

`./gradlew :web:test` writes only `web__test`'s bundle and finalizes the root `:assembleKensaSite`. The aggregated manifest lists every source that's present on disk — other modules' previously-produced bundles stay and remain listed. If you removed a subproject from `settings.gradle.kts` between runs, its `sources/<id>/` directory is pruned on the next assembly (same stale-cleanup semantics as single-project mode, applied across the aggregate set).

### Single-project unchanged

A single-project build (no participating subprojects) falls through to the Standalone path — byte-identical output to pre-0.9.3 site mode. No namespaced ids, no new task names, no manifest schema change.

:::note[Maven]
The Maven plugin is single-module today — each `assemble-site` mojo execution produces its own per-module site. Reactor-level aggregation is a future feature.
:::

## CI / hosted use

The site is fully self-contained and uses **relative URLs** in the manifest, so it works behind any static host without extra configuration:

1. CI runs `./gradlew check assembleKensaSite`.
2. CI publishes `build/kensa-site/` as a build artifact.
3. The CI report tab points at `kensa-site/index.html`.

Works zero-config in TeamCity's report-tab feature, GitHub Pages, Jenkins HTML Publisher, etc.

:::warning[Browser caching of `manifest.json`]
Static hosts often serve everything with long `Cache-Control: max-age=...`. The Kensa shell defeats this for the manifest by fetching with `cache: 'no-store'`, so the source list always reflects the latest build. Per-source data (`configuration.json`, `indices.json`, `results/*.json`) inherit your host's cache policy — if you publish per-build sites under stable URLs and want viewers to always see fresh data, set `Cache-Control: no-store` on the artifact server, or add per-build URL prefixes (e.g. `/<build-id>/kensa-site/...`).
:::

## What's NOT in site mode

- The **single-bundle layout is unchanged** when `site = false`. Default Kensa workflow is untouched.
- Cross-build aggregation (e.g. "compare last 5 nightly UI runs") is Tauri remote-source territory, not a CI artifact concern.
- Per-task `kensa { … }` Gradle DSL extension (set source-specific titles inside the DSL rather than via `systemProperty`) is on the roadmap.
