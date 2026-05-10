---
sidebar_position: 1
description: The Kensa Gradle plugin wires the Kotlin compiler plugin, kensa-core dependency, and site-mode test-task system properties for you. Configure via the `kensa { … }` extension.
---

# Gradle Plugin

The plugin lives at `dev.kensa.gradle-plugin` on the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/dev.kensa.gradle-plugin). It does three things:

1. Applies the Kensa **Kotlin compiler plugin** to the configured sourcesets so `@RenderedValue` and `@ExpandableSentence` capture works at compile time.
2. Adds an **implicit `dev.kensa:kensa-core`** runtime dependency to those compilations (so you don't have to declare it manually).
3. When **site mode** is on, wires `kensa.output.root` / `kensa.source.id` system properties onto each sourceset's `Test` task and registers an `assembleKensaSite` task to produce a single multi-source viewable site.

## Apply

```kotlin reference title="build.gradle.kts — apply the plugin"
https://github.com/kensa-dev/clearwave-example/blob/master/build.gradle.kts#L1-L7
```

The plugin embeds the `kensa-core` / `kensa-compiler-plugin` coordinates it was built against, so you don't need to declare them manually. Since plugin v0.9.0, plugin and `kensa-core` version independently — see the [compatibility matrix](#kensa-core-compatibility) below if you want to pin a different `kensa-core`.

## kensa-core compatibility

| Plugin     | Default kensa-core | Min kensa-core | Notes                                |
| ---------- | ------------------ | -------------- | ------------------------------------ |
| 0.9.x      | 0.8.0              | 0.8.0          | First decoupled release              |
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
| `sourceSets` | `Set<String>` | `setOf("test")` | Which sourcesets/test-tasks Kensa attaches to. |
| `site` | `Boolean` | `false` | Enable site mode (see [Site Mode](./site-mode.md)). |
| `siteRoot` | `Directory` | `build/kensa-site` | Site-mode output root. |
| `kensaCoreVersion` | `String` | *bundled default* | Override the `kensa-core` / `kensa-compiler-plugin` version the plugin resolves. See [compatibility matrix](#kensa-core-compatibility). |

Example with multiple sourcesets in site mode:

```kotlin reference title="build.gradle.kts — kensa { } block"
https://github.com/kensa-dev/clearwave-example/blob/master/build.gradle.kts#L9-L12
```

## Behaviour

For each name in `sourceSets`, the plugin looks up a `Test` task with the same name. When found:

- A `dev.kensa:kensa-core` dependency is added to that compilation's classpath via Kotlin's compiler-plugin support.
- If `site = true`:
  - `kensa.output.root` system property is set on the test task to the resolved `siteRoot`.
  - `kensa.source.id` system property is set to the sourceset name (unless you've already set it explicitly — see [overrides](#per-source-overrides)).
  - The `assembleKensaSite` task is registered, depending on every configured Test task.

The compiler plugin is only applied to compilations whose name appears in `sourceSets`. Other sourcesets compile normally without Kensa.

### Per-source overrides

The plugin's per-task `systemProperty` wiring is **last-write-wins**, so you can override anything from the user side. For example, to prefix the source id with the CI build number:

```kotlin
tasks.named<Test>("uiTest") {
    systemProperty("kensa.source.id", "${System.getenv("BUILD_NUMBER") ?: "local"}-uiTest")
    systemProperty("kensa.source.title", "Build #${System.getenv("BUILD_NUMBER") ?: "local"} — UI Tests")
}
```

`kensa.source.title` overrides the default `Configuration.titleText` for that source's `configuration.json` (and therefore the label shown in the site sidebar).

## Tasks added

| Task | Group | Description |
|---|---|---|
| `assembleKensaSite` | `verification` | Aggregates all per-source bundles in `siteRoot/sources/<id>/` into a single viewable site. Depends on every configured Test task. Only registered when `site = true`. |

`assembleKensaSite` is `@CacheableTask` — its inputs are the per-source `configuration.json` files plus the resolved `kensa-core` jar's content. Re-running with no changes is `UP-TO-DATE`; republishing kensa-core to your local maven invalidates the cache and re-extracts the new shell.

## Source ID collisions

If two configured Test tasks resolve to the same `kensa.source.id`, the build fails fast with an actionable error pointing at how to disambiguate:

```
Kensa site mode: source id collision on 'foo' (multiple sourcesets / test tasks resolve to the same kensa.source.id).
Override one explicitly: tasks.named<Test>("<name>") { systemProperty("kensa.source.id", "<unique>") }
```

## Maven plugin

Maven users — see the [Maven plugin](./maven-plugin.md) page. The `assemble-site` mojo provides the same site-mode aggregation; per-source bundles are driven by `systemPropertyVariables` on each surefire/failsafe execution.
