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

## In the report

The keywords are recognised by the sentence renderer, so

```kotlin
thenEventually(theOrderStatus()) { this shouldBe "CONFIRMED" }
```

renders as *Then eventually the order status …* — the report reads as the requirement ("the order is eventually confirmed") rather than as polling mechanics.
