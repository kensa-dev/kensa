# Compatibility & Versioning Policy

This document defines what Kensa promises across releases, what is excluded from those
promises, and the language/runtime support matrix. It complements (and will eventually be
mirrored by) the kensa.dev documentation.

## Semantic versioning — what it covers

From **1.0.0** onward Kensa follows [Semantic Versioning](https://semver.org/) **for the
stable public API only**:

- **MAJOR** — a source-incompatible change to the stable surface.
- **MINOR** — backwards-compatible additions to the stable surface.
- **PATCH** — backwards-compatible fixes.

### Stable surface (frozen, semver-governed)

- The `KensaTest` authoring DSL: `given` / `and` / `whenever` / `then`, plus
  `Action`, `SetupStep`, `StateCollector`, and `RefinedSugar`.
- Authoring annotations and their enums: `@RenderedValue`, `@ExpandableRenderedValue`,
  `@RenderedValueWithHint`, `@ExpandableSentence`, `@Highlight`, `@Issue`, `@Notes`,
  `@Sources`, `@KensaTab`, `@AutoOpenTab`, `@UseSetupStrategy`,
  `@ParameterizedTestDescription`.
- Configuration: `Kensa`, `KensaConfigurator`, `Configuration`, and the documented
  `kensa.*` system properties (including the `kensa.source.id` site-mode behaviour).
- Renderers: `ValueRenderer`, `InteractionRenderer`, `TableRenderer`.
- The fixtures API (`dev.kensa.fixture`).
- The custom-tab SPI: `KensaTabRenderer`, `KensaTabContext`, `@KensaTab`.
- The tab-service registry: `Configuration.registerTabService` and `KensaTabServices`,
  as documented on the log-tabs page.
- The sequence-diagram DSL: `sequenceDiagram { }` and `Party`.
- The dictionary types: `Acronym`, `Keyword`, `ProtectedPhrase`, `Dictionary`.
- The log-source SPI: `LogQueryService`, `LogQueryServiceRegistry`, `LogRecord`,
  `LogPatterns`, and the `rawFile` / `indexedFile` registration helpers.
- The test-context accessors used by application code: `TestContext` and the
  `TestContextHolder` thread-local wrapper through which it is reached.

### Implementation (not part of the public API)

The parser, runtime, state machine, sentence scanner, output writers, and most utility
code are `internal` to the `core` module and cannot be referenced by consumer modules.
These may change in any release. If you are importing types from `dev.kensa.parse`,
`dev.kensa.output`, `dev.kensa.service`, `dev.kensa.util`, or the implementation parts of
`dev.kensa.state` / `dev.kensa.context` / `dev.kensa.sentence`, you are depending on
internals that are explicitly not supported.

## Two opt-in markers — both excluded from semver

Some declarations are `public` without being part of the supported API. Kensa distinguishes
two reasons for that, because they carry opposite advice.

### `@KensaInternalApi` — ours, please don't call it

These declarations are public **only** because Kensa's framework adapters and the Kotlin
compiler plugin live in separate Gradle modules and cannot see `internal`. They are
implementation detail and may change or be removed in any release, including a patch.

The marker is `@RequiresOptIn` at **ERROR** level, so using one is a compile failure unless
you deliberately acknowledge it:

```kotlin
@file:OptIn(dev.kensa.KensaInternalApi::class)
```

If you find yourself needing this in a test suite, something is missing from the supported
API. Please open an issue rather than depending on these declarations.

The internal tier currently includes:

- The **core↔framework integration SPI**: `FrameworkDescriptor`, `KensaLifecycleManager`,
  `TestContainer`, and the invocation-context runtime hooks
  (`ExpandableInvocationContext(Holder)`, `RenderedValueInvocationContext(Holder)`).
- The **parser surfaces a framework adapter needs to classify a test**:
  `dev.kensa.parse.kotlin.findAnnotationNames`, `ElementDescriptor`, `MethodParameters`,
  and `ParsedExpandableMethod`. These leak ANTLR-generated parser types and are expected to
  be redesigned in a future 1.x release.

### `@KensaExperimental` — new, still being designed

These are features we are still shaping and would like feedback on. They are shipped but not
frozen: they may change in a source-incompatible way, or be removed, in **any 1.x release**
without a major bump. The marker is `@RequiresOptIn` at **WARNING** level, so using one
compiles but tells you what you are signing up for:

```kotlin
@file:OptIn(dev.kensa.KensaExperimental::class)
```

The experimental tier currently includes:

- The **org-flow surfaces**: `@OrgFlow`, `@OrgFlowMarker`, `OrgFlowSpec`,
  `SimpleOrgFlowSpec`, `orgFlowOf`, `SeamDefinition`.

Opting in to one marker does not opt you in to the other.

## Language & runtime support matrix

The required Kotlin version and the minimum JDK are a **separate support axis** from the
API-semver promise. A Kotlin bump is published as a **documented compatibility note**
(a minor release with a caveat), **not** an API-major — see the binary-lock note below.

| Kensa | Required Kotlin (consumer) | Min JDK | Test frameworks verified |
|-------|----------------------------|---------|--------------------------|
| 0.8.x | 2.4.10                     | 17      | JUnit 5 (5.14.x), JUnit 6 (6.0.x), TestNG (7.12.x), Kotest (6.1.x) |

Notes:
- **Min JDK 17** for the published modules (`core`, `frameworks/*`, `assertions/*`,
  integrations). JVM bytecode target is 17. (The `adoptabot` example targets JDK 21, but
  it is not a published artifact.)
- **Required Kotlin 2.4.10**: consumers must compile with Kotlin 2.4.10. This is driven by
  the compiler plugin (below), not merely by stdlib usage.

## Why a Kotlin bump is a compatibility note, not an API-major

Kensa's Kotlin **compiler plugin** (which powers `@RenderedValue` and `@ExpandableSentence`
capture) is **binary-locked to the Kotlin compiler version**: a compiler plugin built
against Kotlin _X_ only loads in the Kotlin _X_ compiler. Consequently:

- A kensa-core Kotlin version bump **forces a paired build-plugins release** that bumps the
  bundled default `kensaCoreVersion` (and the pinned `MIN_KOTLIN_VERSION` / compiler-plugin
  coordinate). Without that paired release, site-mode reports and the Gradle/build
  integration would ship against the wrong compiler.
- Because the Kotlin requirement is a property of the toolchain rather than of Kensa's
  authored API, we treat it as an explicit entry in the support matrix above rather than
  pretending API-semver covers it. Bumping the required Kotlin is a minor-with-caveat
  release accompanied by a note here.

## Reporting

If a change to the stable surface breaks you in a non-major release, that is a bug —
please open an issue. Changes to `@KensaExperimental` or internal types are expected and
not covered by the promise.
