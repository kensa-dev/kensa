---
title: Stability and Compatibility
sidebar_label: Stability and Compatibility
sidebar_position: 98
description: What Kensa promises from 1.0 onwards - the semver-governed stable surface, the internal implementation, the two opt-in markers, and how Kotlin and JDK requirements are handled.
---

# Stability and Compatibility

From 1.0.0 Kensa follows [Semantic Versioning](https://semver.org/) for its stable public API. This page says which declarations that covers, which it does not, and how you can tell the two apart at compile time. The Kotlin, JDK and test-framework versions each release supports are a separate axis, listed on the [support matrix](./support-matrix.md) page.

## What the version number means

- **MAJOR** - a source-incompatible change to the stable surface.
- **MINOR** - backwards-compatible additions to the stable surface.
- **PATCH** - backwards-compatible fixes.

The promise applies to the stable surface only. Anything marked `internal`, `@KensaInternalApi` or `@KensaExperimental` may change in any release, including a patch.

## The stable surface

These are frozen and governed by the rules above.

- **The authoring DSL** on `KensaTest`: `given` / `and` / `whenever` / `then`, plus `Action`, [`SetupStep`](./api/setup-steps.md), `StateCollector` and `RefinedSugar`.
- **Authoring [annotations](./api/annotations.md)** and their enums: `@RenderedValue`, `@ExpandableRenderedValue`, `@RenderedValueWithHint`, `@ExpandableSentence`, `@Highlight`, `@Issue`, `@Notes`, `@Sources`, `@KensaTab`, `@AutoOpenTab`, `@UseSetupStrategy` and `@ParameterizedTestDescription`.
- **[Configuration](./api/configuration.md)**: `Kensa`, `KensaConfigurator`, `Configuration`, and the documented `kensa.*` system properties, including the `kensa.source.id` site-mode behaviour.
- **Renderers**: `ValueRenderer`, [`InteractionRenderer`](./api/interaction-renderers.md) and `TableRenderer`.
- **The [fixtures](./api/fixtures.md) API** in `dev.kensa.fixture`.
- **The custom-tab SPI**: `KensaTabRenderer`, `KensaTabContext` and `@KensaTab`.
- **The tab-service registry**: `Configuration.registerTabService` and `KensaTabServices`, as used on the [log tabs](./api/log-tabs.md) page.
- **The sequence-diagram DSL**: `sequenceDiagram { }` and `Party`.
- **The dictionary types**: `Acronym`, `Keyword`, `ProtectedPhrase` and `Dictionary`.
- **The log-source SPI**: `LogQueryService`, `LogQueryServiceRegistry`, `LogRecord`, `LogPatterns`, and the `rawFile` / `indexedFile` registration helpers.
- **The test-context accessors** used by application code: `TestContext`, and the `TestContextHolder` thread-local wrapper through which it is reached.

`KensaTest` itself is provided by each framework module (`dev.kensa.junit`, `dev.kensa.testng`, `dev.kensa.kotest`) over the same core DSL, so the promise is the same whichever runner you use.

## The implementation is not public API

The parser, runtime, state machine, sentence scanner, output writers and most utility code are `internal` to the `core` module, so a consumer module cannot reference them. They may change in any release. `dev.kensa.parse`, `dev.kensa.output` and `dev.kensa.util` are implementation packages, as are the implementation parts of `dev.kensa.state`, `dev.kensa.context`, `dev.kensa.sentence` and `dev.kensa.service`. If you are importing a type from one of those that is not on the list above, you are depending on something unsupported.

## Two opt-in markers

Some declarations are `public` without being supported. Kensa uses two `@RequiresOptIn` markers to say why, because the two reasons carry opposite advice. Opting in to one does not opt you in to the other.

### `@KensaInternalApi`: ours, please don't call it

These declarations are public only because Kensa's framework adapters and the Kotlin compiler plugin live in separate Gradle modules and cannot see `internal`. They are implementation detail and may change or be removed in any release.

The marker is enforced at **error** level, so using one is a compile failure unless you acknowledge it deliberately:

```kotlin
@file:OptIn(dev.kensa.KensaInternalApi::class)
```

If a test suite needs this, something is missing from the supported API. Open an issue rather than depending on these declarations.

The internal tier currently holds:

- The **core-to-framework integration SPI**: `FrameworkDescriptor`, `KensaLifecycleManager`, `TestContainer`, and the invocation-context runtime hooks `ExpandableInvocationContext(Holder)` and `RenderedValueInvocationContext(Holder)`.
- The **parser surfaces a framework adapter needs to classify a test**: `dev.kensa.parse.kotlin.findAnnotationNames`, `ElementDescriptor`, `MethodParameters` and `ParsedExpandableMethod`. These expose ANTLR-generated parser types and are expected to be redesigned in a later 1.x release.

### `@KensaExperimental`: new, still being designed

These are features that ship but are not yet frozen, and feedback on them is welcome. They may change in a source-incompatible way, or be removed, in any 1.x release without a major bump.

The marker is enforced at **warning** level, so using one compiles but tells you what you are signing up for:

```kotlin
@file:OptIn(dev.kensa.KensaExperimental::class)
```

The experimental tier currently holds:

- The **org-flow surfaces**: `@OrgFlow`, `@OrgFlowMarker`, `OrgFlowSpec`, `SimpleOrgFlowSpec`, `orgFlowOf` and `SeamDefinition`.

:::note[Java callers]
`@RequiresOptIn` is checked by the Kotlin compiler. Java code that calls a marked declaration compiles without a warning, but the declaration is no more stable for it.
:::

## Kotlin, JDK and framework versions

The required Kotlin version and the minimum JDK are a separate support axis from the API promise, and are listed per release on the [support matrix](./support-matrix.md) page.

Kensa's Kotlin compiler plugin, which powers `@RenderedValue` and `@ExpandableSentence` capture, is binary-locked to the Kotlin compiler version: a plugin built against Kotlin _X_ loads only in the Kotlin _X_ compiler. The Kotlin requirement is therefore a property of the toolchain rather than of the authored API, so a Kotlin bump is published as a **minor release with a documented compatibility note**, paired with a [build plugin](./build-plugins/gradle-plugin.md) release that pins the matching compiler-plugin coordinate. It is not an API-major.

## Reporting a break

If a change to the stable surface breaks your build in a non-major release, that is a bug: please [open an issue](https://github.com/kensa-dev/kensa/issues). Changes to `@KensaExperimental` or internal declarations are expected and are not covered by the promise.
