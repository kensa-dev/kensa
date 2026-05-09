---
sidebar_position: 1
description: "Overview of the kensa-kotest-test-support modules — composable, named field matchers for JSON and XML payloads."
---

# Field Assertion DSL — Overview

## What this is

The `kensa-kotest-test-support` family adds a lightweight field-assertion DSL on top of Kotest matchers. Instead of writing a chain of low-level property accesses inside an assertion block, you declare *named fields* once and compose them at the test call-site with short infix operators:

```kotlin
then(theFeasibilityResponseAsJson(),
    (anAddressPostcode of fixtures[postcode])
        .and(serviceable of true)
        .and(profileCount of 3)
        .and(fastestProfileType of "FTTP")
        .and(fastestProfileSupplier of fixtures[voiceSupplier])
        .and(fastestProfileDownloadSpeed of fixtures[voiceDownloadSpeed])
)
```

The assertion reads like a specification. Each line names the field being checked and the value it must equal. Failure messages include the field's human-readable description automatically, so there is no need to add custom assertion messages.

## The problem it solves

Asserting on a JSON or XML response typically requires either:

- Direct path access scattered throughout the test (hard to scan, no naming)
- A custom helper method per assertion (verbose, hard to compose)
- A third-party library that adds a new DSL root incompatible with Kotest's `.and()`

The field DSL solves this by producing standard `Matcher<T?>` values. They compose with Kotest's `.and()`, work inside `shouldSatisfy`, and integrate with Kensa's `then(collector, matcher)` overload without any special wiring.

## Relationship with Kotest matchers

Every extension function (`of`, `matching`, `withListOf`, `withSetOf`) returns a `Matcher<T?>`. You can chain them with `.and()` just as you would any other Kotest matcher:

```kotlin
(anAmount matching beGreaterThan(0)).and(anAmount matching beLessThan(1000))
```

The field DSL does not replace Kotest matchers — it wraps them. `matching(matcher)` accepts any `Matcher<R?>`, so the full Kotest matcher library is available.

## Three modules

| Artifact | What it provides |
|----------|-----------------|
| `kensa-kotest-test-support` | `MatcherField<T, R>` interface + all extension functions |
| `kensa-kotest-test-support-xml` | XML field classes backed by W3C DOM + `javax.xml.xpath` |
| `kensa-kotest-test-support-json` | JSON field classes backed by Jackson `JsonNode` + JSONPointer |

The XML and JSON modules depend on `kensa-kotest-test-support`. There is no other external XML or JSON library dependency beyond the JDK XPath API and Jackson respectively.

## Gradle dependencies

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("dev.kensa:kensa-kotest-test-support:0.8.0-SNAPSHOT")

    // Add the modules you need:
    testImplementation("dev.kensa:kensa-kotest-test-support-xml:0.8.0-SNAPSHOT")
    testImplementation("dev.kensa:kensa-kotest-test-support-json:0.8.0-SNAPSHOT")
}
```

These artifacts will be published to Maven Central. While the version shown is `0.8.0-SNAPSHOT`, stable releases will follow the same group ID (`dev.kensa`) and artifact naming.

## Worked example

The [clearwave-example](https://github.com/kensa-dev/clearwave-example) project contains end-to-end demonstrations:

- [FeasibilityResponseFields.kt](https://github.com/kensa-dev/clearwave-example/blob/master/src/test/kotlin/com/clearwave/fields/FeasibilityResponseFields.kt) — JSON field declarations
- [FibreVisionResponseFields.kt](https://github.com/kensa-dev/clearwave-example/blob/master/src/test/kotlin/com/clearwave/fields/FibreVisionResponseFields.kt) — XML field declarations
- [FieldDslExamplesTest.kt](https://github.com/kensa-dev/clearwave-example/blob/master/src/test/kotlin/com/clearwave/fields/FieldDslExamplesTest.kt) — one JSON test and one XML test using those fields

The rendered HTML report for these tests is at [kensa-dev.github.io/clearwave-example](https://kensa-dev.github.io/clearwave-example).

## When to use this DSL

Reach for it when:

- You are asserting on a structured response (JSON, XML) and want the assertion to be self-documenting
- You want failures to name the field that failed rather than printing raw paths
- You want to compose multiple field checks into a single `then(collector, matcher)` call
- You have domain value types and need a custom `infix` function (see [Core — `toMatcher`](./core#tomatcher))

You do not need it for simple single-value assertions where a plain `shouldBe` is already readable.
