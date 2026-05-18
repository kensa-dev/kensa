---
sidebar_position: 4
description: Surface framework-native tag annotations (JUnit @Tag, Kotest @Tags, TestNG groups) as badges and a multi-select sidebar filter in the Kensa report.
---

# Tags

Kensa bridges your test framework's tag/group annotations into the HTML report as badges on each test and a multi-select filter in the sidebar. There is no Kensa-native `@Tag` annotation — Kensa reads whatever your framework already exposes.

## Supported annotations

| Framework | Annotation | Targets |
|---|---|---|
| JUnit 5 / 6 | `org.junit.jupiter.api.Tag` (repeatable) | Class, Method |
| Kotest | `io.kotest.core.annotation.Tags` | Class |
| TestNG | `org.testng.annotations.Test(groups = [...])` | Class, Method |

Tags from class-level and method-level annotations are merged and de-duplicated. For JUnit and TestNG, class-level tags apply to every test in the class.

> Per-test Kotest tags set via the spec DSL (`.config(tags = setOf(...))`) are not yet bridged — only class-level `@Tags` are read.

## Examples

### JUnit

```kotlin
@Tag("smoke")
@Tag("regression")
class CheckoutTest : KensaTest, WithKotest {

    @Tag("slow")
    @Test
    fun `large basket checks out under SLA`() { ... }
}
```

```java
@Tag("smoke")
@Tag("regression")
class CheckoutTest implements KensaTest, WithAssertJ {

    @Tag("slow")
    @Test
    void largeBasketChecksOutUnderSla() { ... }
}
```

### Kotest

```kotlin
@Tags("smoke", "regression")
class CheckoutSpec : FunSpec(), WithKensa {
    init {
        test("large basket checks out under SLA") { ... }
    }
}
```

### TestNG

```java
@Test(groups = {"smoke", "regression"})
class CheckoutTest implements KensaTest {

    @Test(groups = "slow")
    void largeBasketChecksOutUnderSla() { ... }
}
```

## In the report

**Badges** appear on each test card alongside any `@Issue` badges. A `+N more` popover handles long tag lists.

**Sidebar filter** collects every tag seen in the run into a multi-select cloud. Selecting one or more tags filters the tree using OR semantics — a test is shown if it carries *any* of the selected tags.

**Click-to-filter** — click a tag badge on a test card to set the sidebar filter to that tag. Hold <kbd>Cmd</kbd> / <kbd>Ctrl</kbd> / <kbd>Shift</kbd> while clicking to toggle the tag into (or out of) the current selection rather than replacing it. Selected tags are highlighted on every card they appear on.

Tests that carry no tags render exactly as before.
