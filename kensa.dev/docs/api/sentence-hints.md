---
sidebar_position: 7
description: Reference for Kensa's comment directives — ReplaceSentence and Ignore hints, and /// statement notes.
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Sentence Hints

Kensa recognises three comment directives in test sources: `/*+ ReplaceSentence: ... */` (override a statement's rendered sentence), `/*+ Ignore */` (omit lines from a rendered body), and `///` (attach a note to a statement).

## `/*+ ReplaceSentence: ... */`

A specially-formatted source comment placed immediately before a test statement replaces the rendered sentence for that statement with the hint text. Works in both Kotlin and Java test sources.

:::warning[Last resort only]

`ReplaceSentence` is an escape hatch, not the intended way to write Kensa tests. Kensa's core value is that the test **source code** is the specification — the sentence is derived directly from the method names and structure you write. Reaching for `ReplaceSentence` pushes you back toward Cucumber-style comment-driven specs, which is explicitly not the direction Kensa is going.

Before using it, try:

- Renaming the method to express the intent more clearly
- Adding `@RenderedValue` to capture the relevant value
- Using `@ExpandableSentence` to inline an action's body

Use `ReplaceSentence` only when the source structure genuinely cannot yield the sentence you need.

:::

:::tip[Legitimate temporary use]

If Kensa fails to parse a test due to unsupported syntax, `ReplaceSentence` is a reasonable short-term workaround to keep the report readable while a fix is in progress upstream. Please [raise an issue](https://github.com/kensa-dev/kensa/issues) so the underlying parser gap can be closed, and remove the hint once a fix ships.

:::

### Syntax

```
/*+ ReplaceSentence: <replacement text> */
```

The hint must appear on the line immediately before the statement it replaces. Leading words matching Given / When / Whenever / Then / And / ThenEventually / ThenContinually / AndEventually / AndContinually (case-insensitive) are treated as GWT keywords in the rendered sentence. All other statements in the test are rendered normally.

```kotlin
/*+ ReplaceSentence: given a simple replacement */
given {}
```

### Value interpolation

`{expr}` placeholders in the replacement text are resolved at parse time using the same expression language Kensa uses for normal sentence parsing.

| Placeholder | Resolves to |
|---|---|
| `{fieldName}` | Value of a `@RenderedValue` field/property on the test class |
| `{fixtures[key]}` | Fixture looked up by identifier key |
| `{fixtures[key].value}` | Chained property access on a fixture |
| `{outputs("key").value}` | Output looked up by string key, chained property access |
| `{outputs[name].value}` | Output looked up by identifier, chained property access |

Multiple placeholders are supported in a single sentence.

```kotlin
@RenderedValue
private val trackingId = "TRK-001"

// Field interpolation
/*+ ReplaceSentence: given the tracking id is {trackingId} */
given {}

// Fixture interpolation
/*+ ReplaceSentence: given the order {fixtures[trackingId]} is pending */
given {}

// Chained fixture property
/*+ ReplaceSentence: given the order {fixtures[trackingId].value} is pending */
given {}

// Output by string key
/*+ ReplaceSentence: given the result {outputs("trackingKey").value} is received */
given {}

// Multiple placeholders
/*+ ReplaceSentence: given {trackingId} and {fixtures[orderId]} are both valid */
given {}
```

### Scope

Each hint replaces only the single statement immediately following it. Subsequent statements render normally unless they have their own hint.

```kotlin
/*+ ReplaceSentence: given OpenNetwork will complete the order {fixtures[trackingId]} */
given {}
whenever { doSomething() }  // rendered from source as normal
```

Multiple statements in the same test can each carry their own hint independently:

```kotlin
/*+ ReplaceSentence: given the network will complete the order */
given {}
/*+ ReplaceSentence: then the result is confirmed */
then {}
```

---

## `/*+ Ignore */`

Excludes the following line(s) of a rendered body from the report — useful inside an [`@ExpandableSentence`](./annotations#expandablesentence) body when one line is plumbing that would add noise to the sentence. `/*+ Ignore: N */` skips the next N lines; the bare form skips one. Works in both Kotlin and Java test sources.

```kotlin
@ExpandableSentence
private fun theFixtureBackedDetails(): Action<ActionContext> {
    return matchers(
        /*+ Ignore */
        anIgnoredMatcher(),
        aFirstName of fixtures(MatcherFixture),
    )
}
```

The `anIgnoredMatcher()` line is omitted from the rendered sentence; the rest of the body renders normally.

---

## Statement notes — `///`

A `///` comment attaches a **note** to a statement — rendered in the report as an expandable annotation on that sentence rather than replacing it. Use notes for the *why* behind a step: context a reader needs that isn't part of the behaviour itself. Works in both Kotlin and Java test sources.

Place the note on the line(s) immediately before the statement:

```kotlin
@Test
fun canDeclineLoanWhenApplicantHasPoorCreditScore() {
    /// Credit score below the minimum threshold — the portal must decline
    /// without proceeding to fraud screening
    given(anApplicantWithPoorCredit())

    whenever(theLoanPortalProcessesAStandardApplication())

    then(theLoanApplicationResult()) { shouldBeDeclined() }
}
```

Consecutive `///` lines merge into one multi-line note. A note may also sit at the **end** of a statement line, after the closing parenthesis or brace:

```kotlin
given(anApplicantWithPoorCredit())   /// declined before fraud screening
```

Notes attach to statements beginning with a GWT keyword (`given`, `when`, `whenever`, `then`, `and`, and their `Eventually`/`Continually` variants). Set [`autoExpandNotes = true`](./configuration#report-layout) to render all notes expanded by default.
