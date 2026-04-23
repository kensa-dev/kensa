---
sidebar_position: 7
description: Reference for the ReplaceSentence hint comment, which overrides the rendered sentence for a single test statement.
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Sentence Hints

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

The hint must appear on the line immediately before the statement it replaces. Leading words matching Given / When / Then / And (case-insensitive) are treated as GWT keywords in the rendered sentence. All other statements in the test are rendered normally.

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
/*+ ReplaceSentence: given a simple replacement */
given {}
```

</TabItem>
<TabItem value="java" label="Java">

```java
/*+ ReplaceSentence: given a simple replacement */
given(() -> {});
```

</TabItem>
</Tabs>

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

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

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

</TabItem>
<TabItem value="java" label="Java">

```java
@RenderedValue
private final String trackingId = "TRK-001";

// Field interpolation
/*+ ReplaceSentence: given the tracking id is {trackingId} */
given(() -> {});

// Fixture interpolation
/*+ ReplaceSentence: given the order {fixtures[trackingId]} is pending */
given(() -> {});

// Multiple placeholders
/*+ ReplaceSentence: given {trackingId} and {fixtures[orderId]} are both valid */
given(() -> {});
```

</TabItem>
</Tabs>

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
