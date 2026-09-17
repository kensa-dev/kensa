---
title: Support Matrix
sidebar_label: Support Matrix
sidebar_position: 97
description: The Kotlin, JDK, coroutines and test-framework versions the current Kensa release is built for and tested against, and which of them are hard requirements.
---

# Support Matrix

This page lists the toolchain and framework versions for the current Kensa release line, **0.9.x**. It is updated with each release; the next release is 1.0.0. The values come from the build itself (`gradle/libs.versions.toml` and the module build files), so they are exact rather than rounded.

The versions here are a separate axis from the API promise described on [Stability and Compatibility](./stability-and-compatibility.md). A change to a hard requirement is published as a minor release with a documented compatibility note, not as a major.

## How to read this

Two columns are hard requirements: **Kotlin**, because the compiler plugin is binary-locked to the compiler version, and **JDK**, because the published modules are compiled to Java 17 bytecode. The **coroutines** entry is a runtime floor. Everything else is the version Kensa is built and tested against; older minors of the same major usually work, but are not verified.

## Toolchain

| Requirement | Version | Kind |
| --- | --- | --- |
| Kotlin (consumer compiler, with the compiler plugin) | 2.4.10 | Hard requirement |
| JDK | 17 or later | Hard requirement |
| kotlinx-coroutines (runtime classpath) | 1.11.0 or later | Runtime floor |

### Kotlin and the compiler plugin

The compiler plugin that powers `@RenderedValue` and `@ExpandableSentence` capture is compiled against `kotlin-compiler-embeddable` **2.4.10** and loads only in that compiler. A project on a different Kotlin version needs a Kensa release built for it; there is no version range.

The [Gradle plugin](./build-plugins/gradle-plugin.md) makes this explicit at apply time: it is built with the same Kotlin version and rejects a project whose applied Kotlin plugin is older, with a message naming the minimum. The [Maven plugin](./build-plugins/maven-plugin.md) carries no such check, so a Maven project must keep its Kotlin version aligned by hand. Each build-plugin release pins the `kensa-core` and compiler-plugin coordinates it was built against; the compatibility tables on those pages say which Kotlin each release was built with.

### JDK

Every published module (`kensa-core`, `kensa-framework-*`, `kensa-assertions-*`, the integrations) is compiled with `sourceCompatibility` / `targetCompatibility` 17 and a Kotlin `jvmTarget` of 17. Running on a newer JDK is fine. The `adoptabot` example in the repository targets JDK 21, but it is not published and does not affect consumers.

### Coroutines floor

Kensa is built against kotlinx-coroutines **1.11.0**. From 0.9.0 onwards, `runBlocking` in that release has a new JVM binary signature, so an older coroutines on the **runtime** classpath makes Kensa's polling assertions fail with:

```
NoSuchMethodError: kotlinx.coroutines.BuildersKt.runBlocking
```

`kensa-core` itself declares coroutines as `compileOnly`; the `kensa-assertions-kotest` and `kensa-assertions-hamkrest` bridges bring it in. The floor only bites when other dependency management pins coroutines lower. Spring Boot's BOM does exactly that (1.8.x as of Boot 3.5), so override the managed version:

```kotlin title="build.gradle.kts"
extra["kotlin-coroutines.version"] = "1.11.0"
```

or set the `kotlin-coroutines.version` property in Maven.

## Test frameworks

Each framework module is built against the version below and its own test suite runs on it. The JUnit modules depend on `junit-jupiter-api`, `junit-jupiter-params` and `junit-platform-launcher` at that version; the Kotest and TestNG modules declare their runner compile-only, so your project supplies it.

| Framework | Module | Built and tested against |
| --- | --- | --- |
| JUnit 5 | `kensa-framework-junit5` | 5.14.3 |
| JUnit 6 | `kensa-framework-junit6` | 6.0.3 |
| Kotest | `kensa-framework-kotest` | 6.2.4 |
| TestNG | `kensa-framework-testng` | 7.12.0 |

JUnit 5 and JUnit 6 are separate modules; pick the one matching your project's JUnit major. The UI-testing modules (`kensa-framework-uitesting-*`, `kensa-framework-playwright-*`, `kensa-framework-selenium-*`) follow the same split and ship a `junit5` and a `junit6` variant each.

## Release history

| Kensa | Kotlin | Min JDK | Coroutines floor | Frameworks verified |
| --- | --- | --- | --- | --- |
| 0.9.x | 2.4.10 | 17 | 1.11.0 | JUnit 5 (5.14.3), JUnit 6 (6.0.3), Kotest (6.2.4), TestNG (7.12.0) |
| 0.8.x | 2.4.10 | 17 | none documented | JUnit 5 (5.14.x), JUnit 6 (6.0.x), Kotest (6.1.x), TestNG (7.12.x) |

If a version listed here breaks in a way this page does not describe, please [open an issue](https://github.com/kensa-dev/kensa/issues).
