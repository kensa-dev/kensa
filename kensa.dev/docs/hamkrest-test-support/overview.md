---
sidebar_position: 1
description: "Overview of the kensa-hamkrest-test-support modules — composable, named field matchers for JSON and XML payloads."
---

# Field Assertion DSL — Overview (Hamkrest)

## What this is

The `kensa-hamkrest-test-support` family adds a lightweight field-assertion DSL on top of hamkrest matchers. Instead of writing a chain of low-level property accesses inside an assertion block, you declare *named fields* once and compose them at the test call-site using hamkrest's first-class `and` / `or` infix operators:

```kotlin
assertThat(jsonNode,
    (anAddressPostcode of "SW1A 2AA")
        and (serviceable of true)
        and (profileCount of 3)
        and (fastestProfileType of "FTTP")
        and (fastestProfileSupplier of "OpenNetwork")
        and (fastestProfileDownloadSpeed of 900))
```

The assertion reads like a specification. Each line names the field being checked and the value it must equal. Failure messages include the field's human-readable description automatically, so there is no need to add custom assertion messages.

## The problem it solves

Asserting on a JSON or XML response typically requires either:

- Direct path access scattered throughout the test (hard to scan, no naming)
- A custom helper method per assertion (verbose, hard to compose)
- A third-party library that adds a new DSL root incompatible with your assertion library

The field DSL solves this by producing standard `Matcher<T?>` values. They compose with hamkrest's `and` and `or` infix operators, work with `assertThat`, and integrate with Kensa's `then(collector, matcher)` overload without any special wiring.

## Relationship with hamkrest matchers

Every extension function (`of`, `matching`, `withListOf`, `withSetOf`) returns a `com.natpryce.hamkrest.Matcher<T?>`. You compose them using hamkrest's built-in infix operators:

```kotlin
(anAmount matching greaterThan(0)) and (anAmount matching lessThan(1000))
```

The field DSL does not replace hamkrest matchers — it wraps them. `matching(matcher)` accepts any `Matcher<R?>`, so the full hamkrest matcher library is available.

## Composition syntax vs the kotest variant

The hamkrest API surface is almost identical to the [kotest variant](../kotest-test-support/overview), with one notable difference at the call-site: hamkrest matchers compose with the `infix and` and `infix or` operators directly on `Matcher` instances, whereas the kotest variant uses `.and()` as a method call. No import of `Matcher.compose.all` is required.

```kotlin
// hamkrest — infix operators
(fieldA of valueA) and (fieldB of valueB)

// kotest — method call
(fieldA of valueA).and(fieldB of valueB)
```

## Three modules

| Artifact | What it provides |
|----------|-----------------|
| `kensa-hamkrest-test-support` | `MatcherField<T, R>` interface + all extension functions |
| `kensa-hamkrest-test-support-xml` | XML field classes backed by W3C DOM + `javax.xml.xpath` |
| `kensa-hamkrest-test-support-json` | JSON field classes backed by Jackson `JsonNode` + JSONPointer |

The XML and JSON modules depend on `kensa-hamkrest-test-support`. The only external dependency of the core module is `com.natpryce:hamkrest:1.8.0.1`. There is no kotest dependency in this family.

## Gradle dependencies

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("dev.kensa:kensa-hamkrest-test-support:0.8.0-SNAPSHOT")

    // Add the modules you need:
    testImplementation("dev.kensa:kensa-hamkrest-test-support-xml:0.8.0-SNAPSHOT")
    testImplementation("dev.kensa:kensa-hamkrest-test-support-json:0.8.0-SNAPSHOT")
}
```

These artifacts will be published to Maven Central. While the version shown is `0.8.0-SNAPSHOT`, stable releases will follow the same group ID (`dev.kensa`) and artifact naming.

## Worked example

The [clearwave-example](https://github.com/kensa-dev/clearwave-example) project contains end-to-end demonstrations for the equivalent kotest-backed fields. The hamkrest field declarations are structurally identical — only the import paths and composition syntax differ.

## When to use this DSL

Reach for it when:

- You are asserting on a structured response (JSON, XML) and want the assertion to be self-documenting
- You want failures to name the field that failed rather than printing raw paths
- You want to compose multiple field checks into a single `assertThat` or `then(collector, matcher)` call
- You are already using hamkrest and want to stay in that ecosystem
- You have domain value types and need a custom `infix` function (see [Core — `toMatcher`](./core#tomatcher))

You do not need it for simple single-value assertions where a plain hamkrest `equalTo` is already readable.

## See also

The [kotest variant](../kotest-test-support/overview) provides the same DSL backed by `io.kotest.matchers.Matcher` and the Kotest assertion library.
