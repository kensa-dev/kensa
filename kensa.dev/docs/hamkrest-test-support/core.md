---
sidebar_position: 2
description: "Reference for the kensa-hamkrest-test-support core module: MatcherField interface, extension functions, and extension points."
---

# Core

**Artifact:** `dev.kensa:kensa-hamkrest-test-support`

Source:
[`test-support/hamkrest/core/src/main/kotlin/dev/kensa/hamkrest/testsupport/field/`](https://github.com/kensa-dev/kensa/blob/master/test-support/hamkrest/core/src/main/kotlin/dev/kensa/hamkrest/testsupport/field/)

---

## `MatcherField<T, R>`

```kotlin
interface MatcherField<T, R> {
    val name: String
    fun extract(value: T): R?
    val description: String
}
```

| Member | Description |
|--------|-------------|
| `name` | Identifier of the field; also the basis for `description`. |
| `extract(value: T): R?` | Pulls the field's value out of a subject of type `T`. Returns `null` when absent. |
| `description` | Human-readable label used in failure messages. Derived from `name` by default. |

### Description derivation

The default `description` property splits `name` on camelCase boundaries, drops any leading article (`a`, `an`, `the` — case-insensitive), and drops a trailing `Field` token:

| `name` | `description` |
|--------|--------------|
| `aProviderCode` | `Provider Code` |
| `anAddressPostcode` | `Address Postcode` |
| `aFooField` | `Foo` |
| `firstProfileType` | `First Profile Type` |

For JSON fields, `description` is overridden to return the JSONPointer path directly (e.g. `/profiles/0/supplier`), since the path is already human-readable and unique.

---

## Extension functions

All functions return `com.natpryce.hamkrest.Matcher<T?>`. Results compose with hamkrest's `infix and` and `infix or` operators.

### `of`

```kotlin
infix fun <T : Any, R> MatcherField<T, R>.of(expected: R?): Matcher<T?>
```

Equality check. The most common operator.

```kotlin
anAddressPostcode of "SW1A 2AA"
serviceable of true
profileCount of 3
```

### `matching(matcher)`

```kotlin
infix fun <T : Any, R> MatcherField<T, R>.matching(matcher: Matcher<R?>): Matcher<T?>
```

Delegates to any hamkrest `Matcher<R?>`.

```kotlin
anAmount matching greaterThan(0)
profileTypes matching hasElement("FTTC")
```

### `matching(matcher, vararg others)`

```kotlin
fun <T : Any, R> MatcherField<T, R>.matching(matcher: Matcher<R?>, vararg others: Matcher<R?>): Matcher<T?>
```

Composes multiple matchers against the same field using hamkrest `allOf`. Equivalent to chaining `and` but scoped to one field.

```kotlin
anAmount.matching(greaterThan(0), lessThan(1000))
```

### `matching(expectedRegex)` — String fields only

```kotlin
infix fun <T : Any> MatcherField<T, String>.matching(expectedRegex: String): Matcher<T?>
```

Applies `present(matches(Regex(...)))` to the extracted string. A `null` extracted value produces a mismatch rather than throwing.

```kotlin
aSku matching "[A-Z]{3}-\\d+"
```

### `withListOf`

```kotlin
fun <T : Any, R> MatcherField<T, List<R>>.withListOf(vararg expected: R): Matcher<T?>
```

Exact ordered list equality (`equalTo(list)`). The field must extract a `List<R>`.

```kotlin
profileDescriptions.withListOf("Full Fibre 900", "Full Fibre 500")
```

Note: hamkrest does not ship `containExactly`. `withListOf` uses `equalTo` on the collected list, which requires the same element order.

### `withSetOf`

```kotlin
fun <T : Any, R> MatcherField<T, Set<R>>.withSetOf(vararg expected: R): Matcher<T?>
```

Order-insensitive set equality (`equalTo(set)`). The field must extract a `Set<R>`. Because `Set.equals` is structurally order-insensitive, `equalTo` works correctly here even though hamkrest has no dedicated `containExactlyInAnyOrder` combinator.

```kotlin
supportedProtocols.withSetOf("FTTC", "FTTP")
```

---

## Extension points

### `toMatcher`

```kotlin
fun <T : Any, R, OUT> MatcherField<T, R>.toMatcher(expected: OUT?, convertActual: (R) -> OUT?): Matcher<T?>
```

Use `toMatcher` when you have a domain value type and want a custom infix function instead of `of`. `convertActual` transforms the extracted value before the equality check, so `expected` can be a domain type different from `R`.

```kotlin
// Define once alongside the field:
infix fun <T : Any> MatcherField<T, ProviderCode>.withValue(expected: String) =
    toMatcher(expected) { it.code }

// In tests:
aProviderCode withValue "FW"
```

This keeps domain-type knowledge out of the test body and away from the DSL core.

### `extractingMatcher`

```kotlin
fun <T : Any, R> extractingMatcher(
    name: String,
    extractValue: (T) -> R?,
    matcher: Matcher<R?>
): Matcher<T?>
```

The building block used by all extension functions. Exposed for users writing their own field-aware matchers that do not fit the `MatcherField` interface. `extractValue` exceptions are surfaced with `name` in the message; `matcher` receives `null` when the subject itself is `null` or extraction returns `null`.

The returned matcher's `description` is prefixed with `name`, so failure output always identifies which field failed.

```kotlin
val myMatcher = extractingMatcher(
    name = "Order Status",
    extractValue = { order: Order -> order.status },
    matcher = equalTo("CONFIRMED")
)
assertThat(order, myMatcher)
```

Note: this function is named `extractingMatcher` (not `nullableExtractingMatcher` as in the kotest variant) because hamkrest's `Matcher` is directly invocable — there is no separate `MatcherResult`-builder to wrap.
