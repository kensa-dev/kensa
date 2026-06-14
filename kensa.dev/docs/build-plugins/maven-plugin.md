---
sidebar_position: 2
description: The Kensa Maven plugin provides an `assemble-site` mojo that aggregates per-execution Kensa output into a single multi-source viewable site. Surefire/failsafe drive the per-source bundles via system properties.
---

# Maven Plugin

`dev.kensa:kensa-maven-plugin` provides one mojo: **`assemble-site`**. It does the same job as Gradle's `assembleKensaSite` task — collects every `sources/<id>/` bundle that test executions wrote into a shared site root and produces the shell + manifest on top.

The Maven plugin does **not** wire system properties for you; you set them on each surefire/failsafe execution. This keeps the surface minimal and avoids interfering with existing surefire configurations.

## Apply

```xml title="pom.xml"
<plugin>
  <groupId>dev.kensa</groupId>
  <artifactId>kensa-maven-plugin</artifactId>
  <version>${kensa.plugin.version}</version>
  <executions>
    <execution>
      <id>assemble-site</id>
      <phase>post-integration-test</phase>
      <goals><goal>assemble-site</goal></goals>
      <configuration>
        <expectedSourceIds>
          <expectedSourceId>uiTest</expectedSourceId>
          <expectedSourceId>scenarioTest</expectedSourceId>
        </expectedSourceIds>
      </configuration>
    </execution>
  </executions>
</plugin>
```

You also need `dev.kensa:kensa-core` on the test classpath — the Kotlin compiler plugin and runtime are responsible for emitting per-source bundles, the Maven plugin only assembles them.

## kensa-core compatibility

Since plugin v0.9.0, the Maven plugin and `kensa-core` version independently.

| Plugin     | Default kensa-core | Min kensa-core | Notes                                |
| ---------- | ------------------ | -------------- | ------------------------------------ |
| 0.9.4      | 0.8.5              | 0.8.0          | Default `kensaCoreVersion` bumped to 0.8.5, whose listener short-circuits when no Kensa tests ran so a transitive kensa-core no longer writes empty reports or prints the banner. (The `outputSourceSets` split is Gradle-only.) |
| 0.9.3      | 0.8.3              | 0.8.0          | Multi-submodule site aggregation: apply the plugin at the rootProject of a multi-project build and contributor subprojects auto-register their source sets into a single aggregated site. Default `kensaCoreVersion` unchanged at 0.8.3. |
| 0.9.2      | 0.8.3              | 0.8.0          | Default `kensaCoreVersion` bumped to 0.8.3 so site-mode aggregation picks up the parameterised test display, sidebar tree expand/collapse toolbar, and absolute-path console banner without each project having to override. |
| 0.9.0–0.9.1 | 0.8.0             | 0.8.0          | Plugin and kensa-core versioned independently; `<sourceTitles>` mojo parameter added in 0.9.1 |
| 0.7.x      | 0.7.x              | —              | Same-version pairing (no override)   |

> v0.8.0 was withdrawn — its POM declared an unpublished `dev.kensa:site-common` dep. Use 0.9.0 or later.

Override the default with `<kensaCoreVersion>` on the mojo configuration:

```xml
<configuration>
  <kensaCoreVersion>0.8.1</kensaCoreVersion>
</configuration>
```

No upper bound — newer `kensa-core` versions are assumed compatible until proven otherwise. A version below the minimum fails fast at execution time.

## Mojo configuration

| Parameter | Default | Effect |
|---|---|---|
| `siteRoot` | `${project.build.directory}/kensa-site` | Site root directory. |
| `expectedSourceIds` | *(required)* | List of source ids the manifest should include. Same set you pass via `kensa.source.id` to the per-execution test runs. |
| `kensaVersion` | `${plugin.version}` | Recorded in `manifest.json`. |
| `kensaCoreVersion` | *bundled default* | Version of `dev.kensa:kensa-core` to resolve for shell extraction. Defaults to the version this plugin release was tested against. See [compatibility matrix](#kensa-core-compatibility). |
| `sourceTitles` | empty | `Map<String, String>` of per-source display labels for the aggregated site, keyed by source id. Entries override the `titleText` the test runtime wrote to each `configuration.json`. See [Site-mode source titles](#site-mode-source-titles). Added in 0.9.1. |

## Site-mode source titles

Set per-source display labels via `<sourceTitles>` on the mojo configuration. Entries here override the `titleText` the test runtime wrote to each source's `configuration.json` (and rewrite the file so the standalone per-source HTML page `<title>` matches the manifest sidebar label).

```xml
<configuration>
  <expectedSourceIds>
    <expectedSourceId>uiTest</expectedSourceId>
    <expectedSourceId>scenarioTest</expectedSourceId>
  </expectedSourceIds>
  <sourceTitles>
    <uiTest>UI Tests</uiTest>
    <scenarioTest>Scenario Tests</scenarioTest>
  </sourceTitles>
</configuration>
```

Precedence when more than one path declares a title for the same source id:

1. `<sourceTitles>` mojo parameter — build DSL, wins
2. `Kensa.konfigure { titleText = "..." }` in code — wins when no build entry
3. `kensa.source.title` via `<systemPropertyVariables>` on the surefire/failsafe execution — legacy, soft-deprecated since 0.9.1
4. `"Index"` / source id fallback when none of the above is set

## Driving per-source bundles via surefire/failsafe

Each test execution that should produce its own source bundle sets `kensa.output.root` and `kensa.source.id` via `systemPropertyVariables`:

```xml title="pom.xml — failsafe per-execution wiring"
<plugin>
  <artifactId>maven-failsafe-plugin</artifactId>
  <executions>
    <execution>
      <id>uiTest</id>
      <goals><goal>integration-test</goal></goals>
      <configuration>
        <systemPropertyVariables>
          <kensa.output.root>${project.build.directory}/kensa-site</kensa.output.root>
          <kensa.source.id>uiTest</kensa.source.id>
        </systemPropertyVariables>
      </configuration>
    </execution>
    <execution>
      <id>scenarioTest</id>
      <goals><goal>integration-test</goal></goals>
      <configuration>
        <systemPropertyVariables>
          <kensa.output.root>${project.build.directory}/kensa-site</kensa.output.root>
          <kensa.source.id>scenarioTest</kensa.source.id>
        </systemPropertyVariables>
      </configuration>
    </execution>
  </executions>
</plugin>
```

When `kensa.source.id` is set, Kensa core writes its bundle to `${kensa.output.root}/sources/${kensa.source.id}/` instead of the default `${kensa.output.root}/kensa-output/`. This is the same runtime contract that powers the Gradle plugin — see [Site Mode](./site-mode.md) for the disk layout.

## Multi-module builds

Bind the `assemble-site` execution on the root `pom.xml` and set `siteRoot` to a shared location (e.g. `${session.executionRootDirectory}/target/kensa-site`). Every submodule's surefire/failsafe execution writes into that shared root using its own `kensa.source.id`; the root-level `assemble-site` then aggregates.

## Run

```bash
mvn verify
```

The `assemble-site` goal binds to `post-integration-test`, so it runs after all `*-IT` tests have written their per-source bundles. Open `target/kensa-site/index.html` afterwards.

## Source IDs and titles

Same contract as Gradle:

| Property | Effect |
|---|---|
| `kensa.source.id` | Per-source bundle directory name. **Required** to opt into site mode (otherwise Kensa core writes to the default `kensa-output/` location). |
| `kensa.source.title` | Overrides `Configuration.titleText` — the label shown for that source in the sidebar. |

If `kensa.source.title` is not set, the source's sidebar label falls back to whatever `Configuration.titleText` was set to programmatically (or `"Index"` if untouched).

## Limitations relative to the Gradle plugin

- No automatic source-id collision detection — set unique ids per execution yourself.
- No partial-run warnings — the mojo logs a notice for missing expected sources but doesn't fail the build (same behaviour as Gradle).
- The mojo is `@DisableCachingByDefault` equivalent — Maven has no build cache to participate in. Re-running rebuilds the manifest unconditionally.
