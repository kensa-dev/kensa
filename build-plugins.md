# <img src="./Logo.svg" alt="Kensa Logo" style="width: 40px; vertical-align: middle;"/> Kensa Gradle Plugin

![Latest Release](https://img.shields.io/github/v/release/kensa-dev/kensa)

A Gradle plugin for the acceptance test framework Kensa. To use the full functionality of Kensa's `@NestedSentence` & `@RenderedValue` annotations (collection and rendering of function arguments), you must apply this plugin to your Kotlin project.

## What it does
- Applies the Kensa Kotlin compiler plugin `dev.kensa.compiler-plugin`
- Adds dependency `dev.kensa:kensa-core:<version>` with capability to applicable compilations. `dev.kensa:core-hooks`
- Exposes a `kensa` extension:
    - `enabled`: enable/disable compiler plugin (default: `true`).
    - `debug`: extra diagnostics from the compiler plugin (default: `false`).
    - `sourceSets`: Kotlin compilation names to apply to (default: `["test"]`).

## Quick start
1. Add the plugin to your build.gradle.kts:
2. Configure the plugin as needed.
3. Build as normal.
``` kotlin
   plugins {
       id("dev.kensa.gradle-plugin") version "<plugin-version>"
   }
```

Configure (optional)

``` kotlin
kensa {
    enabled.set(true)                                 // default true
    debug.set(false)                                  // default false
    sourceSets.set(setOf("test", "acceptanceTest"))    // default "test"
}
```

Build as normal; the plugin attaches to the selected compilations.
