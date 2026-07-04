---
sidebar_position: 5.5
description: Reference for thenEventually and thenContinually — polling assertions for asynchronous behaviour, with timeout, interval and initial-delay tuning.
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Async Assertions

When the action under test completes asynchronously — a message lands on a queue, a status flips after a callback — a plain `then()` races the system. The assertion mixins provide two polling variants:

| Method | Semantics |
|---|---|
| `thenEventually(...)` / `andEventually(...)` | Re-runs the collector + assertion until it **passes**, or the timeout expires (fails with the last assertion error) |
| `thenContinually(...)` | Re-runs the collector + assertion for the whole duration and fails the moment it **stops passing** — "this must keep being true" |

Both render in the report as first-class sentence keywords (*Then eventually …*), and every assertion mixin (`WithKotest`, `WithAssertJ`, `WithHamcrest`, `WithHamkrest`) provides them.

**Defaults:** 10-second timeout, 25 ms poll interval, no initial delay.

## Kotest

```kotlin
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Test
fun `order is eventually confirmed`() {
    whenever { orderService.placeOrder() }

    // polls the collector until the block passes (default: 10s timeout, 25ms interval)
    thenEventually(theOrderStatus()) { this shouldBe "CONFIRMED" }
}

@Test
fun `order is eventually confirmed with custom timeout`() {
    whenever { orderService.placeOrder() }

    thenEventually(30.seconds, theOrderStatus()) { this shouldBe "CONFIRMED" }
    andEventually(theOrderReference(), startWith("ORD-"))
}
```

Timing is fully tunable via named parameters:

```kotlin
thenEventually(
    initialDelay = 500.milliseconds,
    duration = 30.seconds,
    interval = 100.milliseconds,
    collector = theOrderStatus(),
    match = startWith("CONF")
)
```

And the inverse — assert a condition *keeps holding*:

```kotlin
@Test
fun `order is never cancelled`() {
    whenever { orderService.placeOrder() }

    // asserts the condition holds for the whole duration
    thenContinually(5.seconds, theOrderStatus()) { this shouldBe "CONFIRMED" }
}
```

Both accept either a matcher (`Matcher<T>`) or an assertion block (`T.() -> Unit`), just like `then()`.

## AssertJ (Java)

`WithAssertJ` provides `thenEventually` with the same collector-plus-assertion shape as its `then`:

```java
import java.time.temporal.ChronoUnit;

@Test
void orderIsEventuallyConfirmed() {
    whenever(ctx -> orderService.placeOrder());

    // polls the collector until the assertion passes (default: 10 seconds)
    thenEventually(theOrderStatus(), status -> assertThat(status).isEqualTo("CONFIRMED"));
}

@Test
void orderIsEventuallyConfirmedWithCustomTimeout() {
    whenever(ctx -> orderService.placeOrder());

    thenEventually(30L, ChronoUnit.SECONDS, theOrderStatus(),
            status -> assertThat(status).isEqualTo("CONFIRMED"));
}
```

## Hamcrest / HamKrest

`WithHamcrest` and `WithHamkrest` provide `thenEventually` and `thenContinually` taking their respective `Matcher<T>` types, with the same optional leading `Duration`:

```kotlin
thenEventually(theOrderStatus(), equalTo("CONFIRMED"))
thenContinually(5.seconds, theOrderStatus(), equalTo("CONFIRMED"))
```

## Multiple assertions in one window

Chaining `andEventually` waits for each assertion in turn — the timeouts add up, and each assertion samples the system at a different moment. When several things should all become true within the *same* window, pass a block instead:

```kotlin
// all assertions poll in parallel inside one shared 10s window
thenEventually(10.seconds) {
    then(theOrderStatus(), be("CONFIRMED"))
    and(theOrderReference()) { this should startWith("ORD-") }
}
```

- All assertions poll **in parallel** — the total wait is the slowest assertion, never the sum.
- Each assertion must pass at some point inside the window. Once it passes it stops polling ("locks in"), so a later regression of an already-passed assertion is not detected.
- Collectors inside one block run concurrently: if they touch shared mutable state, it must be thread-safe.
- If exactly one assertion never passes, its failure is thrown directly. If several never pass, they are aggregated — first failure as the cause, the rest attached as suppressed exceptions so no stack trace is lost. For example, a block with three assertions where two never pass:

```
2 of 3 assertions did not pass within 10s:
  [2] expected: a value that is equal to "CONFIRMED" ...
  [3] expected: a value that is equal to "ORD-1" ...
```

The per-assertion lines above come from the hamkrest bridge — each carries that assertion's own last failure message, so kotest users see kotest-style messages instead.

The `thenContinually` block is the inverse: every assertion must hold on **every** tick for the whole duration, polled in parallel, and the block fails immediately when any assertion stops passing:

```kotlin
// every assertion must hold on every tick for the whole duration
thenContinually(5.seconds) {
    then(theOrderStatus(), be("CONFIRMED"))
    and(theOrderReference()) { this should startWith("ORD-") }
}
```

Matcher, assertion-block and `ThenSpec` forms are all usable inside the braces, and timing is configured at block level only: the full `initialDelay` / `duration` / `interval` triple on `thenEventually`, `duration` alone on `thenContinually`. The duration argument is optional on both block forms — omit it and the page's defaults apply (10-second timeout, 25 ms interval, no initial delay). The block forms are available on `WithKotest` and `WithHamkrest` (the lambda-friendly mixins) — not on `WithAssertJ` or `WithHamcrest`.

## In the report

The keywords are recognised by the sentence renderer, so

```kotlin
thenEventually(theOrderStatus()) { this shouldBe "CONFIRMED" }
```

renders as *Then eventually the order status …* — the report reads as the requirement ("the order is eventually confirmed") rather than as polling mechanics.

A multi-assertion block renders as a head sentence for the `thenEventually`/`thenContinually` clause, with each nested `then`/`and` appearing as its own indented line underneath. For example, a block asserting two things within a 2-second window renders as:

```
Then eventually 2 seconds
   then the first value equal to "first"
   and the second value equal to "second"
```

Note that the nested `then`/`and` lines are plain text, not styled report keywords — only the top-level `Then`/`And` that begin a BDD step get keyword styling. The inner `then(...)`/`and(...)` calls inside a block are report content, not top-level steps, so they read as part of the sentence fragment rather than as bolded keywords.
