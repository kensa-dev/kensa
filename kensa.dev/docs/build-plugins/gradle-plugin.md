---
sidebar_position: 1
description: The Kensa Gradle plugin wires the Kotlin compiler plugin, kensa-core dependency, and site-mode test-task system properties for you. Configure via the `kensa { … }` extension.
---

# Gradle Plugin

The plugin lives at `dev.kensa.gradle-plugin` on the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/dev.kensa.gradle-plugin). It does three things:

1. Applies the Kensa **Kotlin compiler plugin** to the `sourceSets` you list so `@RenderedValue` and `@ExpandableSentence` capture works at compile time.
2. Adds an **implicit `dev.kensa:kensa-core`** runtime dependency to those compilations and to every `outputSourceSets` compilation (so you don't have to declare it manually).
3. When **site mode** is on, wires `kensa.output.root` / `kensa.source.id` system properties onto each **output sourceset's** `Test` task and registers an `assembleKensaSite` task to produce a single multi-source viewable site.

Since **0.9.4** the plugin separates *which sourcesets the compiler plugin instruments* (`sourceSets`) from *which Test tasks emit Kensa output / contribute site bundles* (`outputSourceSets`) — see [Compiler-plugin vs output sourcesets](#compiler-plugin-vs-output-sourcesets).

## Apply

```kotlin reference title="build.gradle.kts — apply the plugin"
https://github.com/kensa-dev/clearwave-example/blob/master/build.gradle.kts#L1-L7
```

The plugin embeds the `kensa-core` / `kensa-compiler-plugin` coordinates it was built against, so you don't need to declare them manually. Since plugin v0.9.0, plugin and `kensa-core` version independently — see the [compatibility matrix](#kensa-core-compatibility) below if you want to pin a different `kensa-core`.

## kensa-core compatibility

| Plugin     | Default kensa-core | Min kensa-core | Notes                                |
| ---------- | ------------------ | -------------- | ------------------------------------ |
| 0.9.6      | 0.8.8              | 0.8.0          | Default `kensaCoreVersion` bumped to 0.8.8. |
| 0.9.5      | 0.8.7              | 0.8.0          | Built against **Kotlin 2.4.0** — the apply-time minimum Kotlin version is now 2.4.0 (the consuming project must apply Kotlin ≥ 2.4.0). Default `kensaCoreVersion` bumped to 0.8.7. |
| 0.9.4      | 0.8.5              | 0.8.0          | Splits compiler-plugin source sets from output source sets — new `outputSourceSets` controls which Test tasks emit reports (`sourceSets` is now strictly compiler-plugin source sets). Default `kensaCoreVersion` bumped to 0.8.5, whose listener short-circuits when no Kensa tests ran so a transitive kensa-core no longer writes empty reports or prints the banner. |
| 0.9.3      | 0.8.4              | 0.8.0          | Adds multi-module site aggregation — applying the plugin to the rootProject with `site = true` aggregates every kensa-enabled subproject's sources into a single site at `<rootDir>/build/kensa-site/`. See [Site Mode — Multi-module builds](./site-mode.md#multi-module-builds). Default `kensaCoreVersion` bumped to 0.8.4. |
| 0.9.2      | 0.8.3              | 0.8.0          | Default `kensaCoreVersion` bumped to 0.8.3 so site-mode aggregation picks up the parameterised test display, sidebar tree expand/collapse toolbar, and absolute-path console banner without each project having to override. |
| 0.9.1      | 0.8.1              | 0.8.0          | Site-mode ergonomics: `sourceTitles` DSL, auto-assemble, per-source component diagrams. |
| 0.9.0      | 0.8.0              | 0.8.0          | First decoupled release — plugin and kensa-core versioned independently. |
| 0.7.x      | 0.7.x              | —              | Same-version pairing (no override)   |

> v0.8.0 was withdrawn from the Gradle Plugin Portal — its POM declared an unpublished `dev.kensa:site-common` dep. Use 0.9.0 or later.

Override the default with `kensaCoreVersion`:

```kotlin
kensa {
    kensaCoreVersion.set("0.8.1")
}
```

No upper bound — newer `kensa-core` versions are assumed compatible until proven otherwise. A version below the minimum fails fast at apply time.

## DSL

Configure via the `kensa { … }` extension:

| Property | Type | Default | Effect |
|---|---|---|---|
| `enabled` | `Boolean` | `true` | Master switch. When `false`, the compiler plugin is not applied and no kensa-core dep is added. |
| `debug` | `Boolean` | `false` | Forwards `debug=true` to the compiler plugin (verbose logs during compilation). |
| `sourceSets` | `Set<String>` | `setOf("test")` | Sourcesets the **Kotlin compiler plugin** instruments (so `@RenderedValue` / `@ExpandableSentence` argument capture works at compile time). Set to `emptySet()` if no code in the project uses those features. Independent of `outputSourceSets`. |
| `outputSourceSets` | `Set<String>` | `setOf("test")` | Sourcesets whose **Test tasks emit Kensa output** — the on-disk reports under `build/kensa` (HTML, JSON indices), the `Kensa Output : …` banner, and (in site mode) the per-source bundles contributed to the assembled site. A sourceset listed here gets `dev.kensa:kensa-core` on its runtime classpath even when the compiler plugin is not applied to it. Independent of `sourceSets`. Added in 0.9.4. |
| `site` | `Boolean` | `false` | Enable site mode (see [Site Mode](./site-mode.md)). On the rootProject of a multi-project build, also acts as the opt-in for [multi-module aggregation](./site-mode.md#multi-module-builds) (since 0.9.3). |
| `siteRoot` | `Directory` | `build/kensa-site` | Site-mode output root. In multi-module aggregator mode this is the rootProject's path; contributor subprojects' siteRoot is overridden to write into this shared directory. |
| `kensaCoreVersion` | `String` | *bundled default* | Override the `kensa-core` / `kensa-compiler-plugin` version the plugin resolves. See [compatibility matrix](#kensa-core-compatibility). |
| `sourceTitles` | `Map<String, String>` | empty | Per-source display labels for site mode, keyed by source id. Entries override the `titleText` the test runtime wrote to that source's `configuration.json`. See [Site-mode source titles](#site-mode-source-titles). Added in 0.9.1. |

Example with multiple sourcesets in site mode:

```kotlin reference title="build.gradle.kts — kensa { } block"
https://github.com/kensa-dev/clearwave-example/blob/master/build.gradle.kts#L9-L12
```

## Compiler-plugin vs output sourcesets

Since **0.9.4** the plugin keeps two independent concerns apart:

- **`sourceSets`** — sourcesets the **Kotlin compiler plugin** instruments. Needed only where source code uses the `@RenderedValue` / `@ExpandableSentence` argument-capture features.
- **`outputSourceSets`** — sourcesets whose **Test tasks emit Kensa output** (reports, banner, and the per-source bundles in site mode).

They're independent because the two concerns are: a project can keep `@ExpandableSentence` support code in `main` (needing the compiler plugin there) while only its `test` Test task produces reports. A sourceset listed in `outputSourceSets` gets `kensa-core` on its runtime classpath even if the compiler plugin is not applied to it.

| Pattern | `sourceSets` | `outputSourceSets` |
|---|---|---|
| No compiler-plugin features used — just `KensaTest` + the runtime | `emptySet()` | `setOf("test")` |
| `@ExpandableSentence` support code in `main`; tests only consume it | `setOf("main")` | `setOf("test")` |
| Expandable code across multiple sets, multiple output Test tasks | `setOf("main", "test", "acceptanceTest")` | `setOf("test", "acceptanceTest")` |

```kotlin
kensa {
    sourceSets = setOf("main")                       // compiler plugin here
    outputSourceSets = setOf("test", "acceptanceTest") // these Test tasks emit / contribute bundles
}
```

:::note[Migrating from ≤ 0.9.3]
Before 0.9.4 the single `sourceSets` property drove both concerns. If you previously set `sourceSets = setOf("test", "acceptanceTest")` to wire **both** Test tasks for Kensa output, also set `outputSourceSets = setOf("test", "acceptanceTest")` — `sourceSets` no longer drives output/site wiring. Projects on the default config (`test` for both) need no change.
:::

## Behaviour

The compiler plugin is applied only to compilations whose name appears in **`sourceSets`**. Other sourcesets compile normally without Kensa.

For each name in **`outputSourceSets`**, the plugin looks up a `Test` task with the same name. When found:

- `dev.kensa:kensa-core` is on that Test task's runtime classpath (added directly for output-only sourcesets that the compiler plugin doesn't already cover).
- If `site = true`:
  - `kensa.output.root` / `kensa.source.id` are passed to the test task's JVM via a `CommandLineArgumentProvider` (since 0.9.1). The per-source bundle dir is declared as a Test `@OutputDirectory` — Gradle tracks it correctly, UP-TO-DATE checks become accurate, and absolute paths don't enter the cache key (friendly to shared / remote Gradle build caches).
  - `kensa.source.id` defaults to the sourceset name, unless you've already set it explicitly — see [overrides](#per-source-overrides).
  - The `assembleKensaSite` task is registered. Each configured Test task is `finalizedBy(assembleKensaSite)`, so running any of them refreshes the site automatically (since 0.9.1). `assembleKensaSite` itself uses `mustRunAfter` (not `dependsOn`) — invoking it standalone aggregates whatever bundles are on disk without re-running tests.

### Site-mode source titles

Set per-source display labels via the `sourceTitles` map. Entries here override whatever the test runtime wrote to that source's `configuration.json` (and rewrite the file so the standalone per-source HTML page `<title>` matches the manifest sidebar label).

```kotlin
kensa {
    site = true
    outputSourceSets = setOf("test", "uiTest")
    sourceTitles["uiTest"] = "UI Tests"
    sourceTitles["test"] = "Unit Tests"
}
```

Or set the whole map at once:

```kotlin
kensa {
    site = true
    outputSourceSets = setOf("test", "uiTest")
    sourceTitles = mapOf(
        "uiTest" to "UI Tests",
        "test"   to "Unit Tests",
    )
}
```

Precedence when more than one path declares a title for the same source id:

1. `kensa { sourceTitles.put(id, ...) }` — build DSL, wins
2. `Kensa.konfigure { titleText = "..." }` in code (e.g. a per-sourceset base class) — wins when no build entry. Works in site mode because each Gradle `Test` task forks its own JVM, so per-sourceset `Configuration` singletons are isolated.
3. `kensa.source.title` system property — legacy, soft-deprecated since 0.9.1.
4. `"Index"` / source id fallback when none of the above is set.

### Per-source overrides

Any per-task `systemProperty` you set in your build script flows through to the test task's JVM as a `-D` argument. Source-id overrides still work the same way — useful for CI-driven id schemes:

```kotlin
tasks.named<Test>("uiTest") {
    systemProperty("kensa.source.id", "${System.getenv("BUILD_NUMBER") ?: "local"}-uiTest")
}
```

For per-source titles, prefer the [`sourceTitles` extension map](#site-mode-source-titles) over `systemProperty("kensa.source.title", ...)` — declarative, build-cache-friendly, and works for downstream consumers without a per-task hook.

## Tasks added

| Task | Group | Description |
|---|---|---|
| `assembleKensaSite` | `verification` | Aggregates all per-source bundles in `siteRoot/sources/<id>/` into a single viewable site. `mustRunAfter` every configured Test task (since 0.9.1 — was `dependsOn` prior). Only registered when `site = true`. |

`assembleKensaSite` is `@CacheableTask` — its inputs are the per-source `configuration.json` files plus the resolved `kensa-core` jar's content. Re-running with no changes is `UP-TO-DATE`; republishing kensa-core to your local maven invalidates the cache and re-extracts the new shell.

Since 0.9.1, every configured `Test` task is `finalizedBy(assembleKensaSite)` — `gradle test` (or any configured Test task) refreshes the aggregated site automatically as a finalizer. The finalizer runs once after all participating tests complete, regardless of pass/fail (a partial site is useful when triaging failures). Standalone `gradle assembleKensaSite` aggregates from disk without forcing a Test re-run.

## Source ID collisions

If two configured Test tasks resolve to the same `kensa.source.id`, the build fails fast with an actionable error pointing at how to disambiguate:

```
Kensa site mode: source id collision on 'foo' (multiple sourcesets / test tasks resolve to the same kensa.source.id).
Override one explicitly: tasks.named<Test>("<name>") { systemProperty("kensa.source.id", "<unique>") }
```

In a multi-module aggregator build, contributors' source ids are automatically prefixed by a slug derived from the project path (`:web` → `web__test`), so two subprojects can both have a `test` source set without colliding. See [Site Mode — Namespaced source ids](./site-mode.md#namespaced-source-ids).

## Maven plugin

Maven users — see the [Maven plugin](./maven-plugin.md) page. The `assemble-site` mojo provides the same site-mode aggregation; per-source bundles are driven by `systemPropertyVariables` on each surefire/failsafe execution.
