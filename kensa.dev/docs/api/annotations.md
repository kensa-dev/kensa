---
sidebar_position: 5
description: Reference for all Kensa annotations, covering value rendering, sentence structure, report notes, issue linking, and UI behaviour overrides.
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Annotations

Kensa annotations control what appears in the HTML report. They are grouped here by purpose.

---

## Rendering Values

These annotations cause field or method values to be captured and displayed in the report.

### `@RenderedValue`

Captures a field, property, or method return value and displays it in the report using the identifier name as the label.

**Targets:** `FIELD`, `VALUE_PARAMETER`, `FUNCTION`, `PROPERTY_GETTER`

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
class PaymentTest : KensaTest, WithKotest {

    @RenderedValue
    private val paymentAmount = Money(100, "GBP")

    @RenderedValue
    private val merchantId = "merchant-42"
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
@RenderedValue
private final String merchantId = "merchant-42";

@RenderedValue
private final Money paymentAmount = new Money(100, "GBP");
```

</TabItem>
</Tabs>

---

### `@ExpandableRenderedValue`

Like `@RenderedValue`, but renders in an expandable section. Useful for complex objects.

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `renderAs` | `RenderedValueStyle` | `Default` | `Default` (toString-style) or `Tabular` (table layout) |
| `headers` | `Array<String>` | `[]` | Column headers when `renderAs = Tabular` |

**Targets:** `FIELD`, `VALUE_PARAMETER`, `FUNCTION`, `PROPERTY_GETTER`

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
@ExpandableRenderedValue(renderAs = RenderedValueStyle.Tabular, headers = ["Product", "Qty", "Price"])
private val orderLines = listOf(
    listOf("Widget", 2, "£9.99"),
    listOf("Gadget", 1, "£24.99"),
)
```

</TabItem>
<TabItem value="java" label="Java">

```java
@ExpandableRenderedValue(renderAs = RenderedValueStyle.Tabular, headers = {"Product", "Qty", "Price"})
private final List<List<Object>> orderLines = List.of(
    List.of("Widget", 2, "£9.99"),
    List.of("Gadget", 1, "£24.99")
);
```

</TabItem>
</Tabs>

---

### `@RenderedValueContainer`

Marks a field or parameter whose own fields should be rendered recursively. Useful for fixture or context objects.

**Targets:** `FIELD`, `VALUE_PARAMETER`

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
@RenderedValueContainer
private val paymentRequest: PaymentRequest = PaymentRequest(amount, currency, merchantId)
// Fields of PaymentRequest annotated with @RenderedValue will be captured
```

</TabItem>
<TabItem value="java" label="Java">

```java
@RenderedValueContainer
private final PaymentRequest paymentRequest = new PaymentRequest(amount, currency, merchantId);
```

</TabItem>
</Tabs>

---

### `@RenderedValueWithHint`

Adds a technical hint (e.g., a JSON path or XPath expression) alongside a rendered value. Repeatable — multiple hints can be applied to the same target.

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `type` | `KClass<*>` | — | The type this hint applies to |
| `valueStrategy` | `RenderedValueStrategy` | `UseIdentifierName` | How to extract the display value |
| `valueParam` | `String` | `""` | Property/method name when strategy requires it |
| `hintStrategy` | `RenderedHintStrategy` | `NoHint` | How to extract the hint |
| `hintParam` | `String` | `""` | Property/method name when hint strategy requires it |

**`RenderedValueStrategy` values:**

| Value | Description |
|-------|-------------|
| `UseIdentifierName` | Use the identifier name as the label |
| `UseToString` | Call `toString()` on the value |
| `UseProperty` | Call the property named by `valueParam` |
| `UseMethod` | Call the method named by `valueParam` |

**`RenderedHintStrategy` values:**

| Value | Description |
|-------|-------------|
| `NoHint` | No hint |
| `HintFromProperty` | Use the property named by `hintParam` |
| `HintFromMethod` | Use the method named by `hintParam` |

---

## Sentence Structure

### `@ExpandableSentence`

Marks a method so that its body is expanded inline in the HTML report sentence, rather than shown as a single step.

**Targets:** `FUNCTION`

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
@ExpandableSentence
fun aRegisteredCustomerWithId(id: String): Action<GivensContext> = Action { ctx ->
    ctx.fixtures[customer]  // sentence expands to show the inner steps
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
@ExpandableSentence
Action<GivensContext> aRegisteredCustomerWithId(String id) {
    return ctx -> ctx.getFixtures().get(CUSTOMER);
}
```

</TabItem>
</Tabs>

---

## Reporting

### `@Notes`

Attaches a note to a test class. Rendered as a styled card above the test list in the HTML report. The value supports inline markdown for rich text, including links to external URLs and internal report navigation.

**Targets:** `CLASS`

**Supported markdown syntax:**

| Syntax | Result |
|--------|--------|
| `**text**` | Bold |
| `*text*` | Italic |
| `__text__` | Underline |
| `~~text~~` | Strikethrough |
| `[label](https://...)` | External link (opens in new tab) |
| `[label](#methodName)` | Scroll to and expand a test method in the same suite |
| `[label](#ClassName)` | Navigate to the suite whose simple class name matches |
| `[label](#ClassName.methodName)` | Navigate to that suite and expand the named method |

**Internal link resolution:**
- `#methodName` — matches a test method by name within the current suite
- `#ClassName` — matches by simple class name (e.g., `#AdoptionServiceTest` matches `dev.kensa.example.AdoptionServiceTest`)
- `#ClassName.methodName` — combines both: navigate to suite, then expand method

**Simple example:**

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
@Notes("Payment gateway uses idempotency keys — retries with the same key are safe.")
class PaymentTest : KensaTest, WithKotest { ... }
```

</TabItem>
<TabItem value="java" label="Java">

```java
@Notes("Payment gateway uses idempotency keys — retries with the same key are safe.")
class PaymentTest implements KensaTest, WithAssertJ { ... }
```

</TabItem>
</Tabs>

**Multiline example with formatting and links:**

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

Kotlin annotation values must be compile-time constants, so `.trimIndent()` cannot be used. Start content on the line immediately after the opening `"""` with no leading indentation. Blank lines become paragraph breaks; single newlines within a paragraph become line breaks.

```kotlin
@Notes("""
**Payment gateway** uses *idempotency keys* — retries with the same key are __safe__.
See [Stripe docs](https://stripe.com/docs/idempotency) for details.

~~Direct refunds are no longer supported.~~ Use the [refund flow](#processRefund) instead.
For error-path behaviour see [RefundEdgeCasesTest](#RefundEdgeCasesTest).
""")
class PaymentTest : KensaTest, WithKotest { ... }
```

</TabItem>
<TabItem value="java" label="Java">

Java 15+ text blocks preserve newlines and allow `trimIndent()`-style indentation stripping via the closing `"""`.

```java
@Notes("""
    **Payment gateway** uses *idempotency keys* — retries with the same key are __safe__.
    See [Stripe docs](https://stripe.com/docs/idempotency) for details.

    ~~Direct refunds are no longer supported.~~ Use the [refund flow](#processRefund) instead.
    For error-path behaviour see [RefundEdgeCasesTest](#RefundEdgeCasesTest).
    """)
class PaymentTest implements KensaTest, WithAssertJ { ... }
```

</TabItem>
</Tabs>

---

### `@Highlight`

Highlights a specific field, parameter, or method return value throughout the report output.

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `value` | `String` | `""` | Optional label override |

**Targets:** `FUNCTION`, `FIELD`, `VALUE_PARAMETER`, `PROPERTY_GETTER`

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
@Highlight
private val transactionId = "txn-abc-123"
```

</TabItem>
<TabItem value="java" label="Java">

```java
@Highlight
private final String transactionId = "txn-abc-123";
```

</TabItem>
</Tabs>

---

### `@Issue`

Links a test or class to one or more issue tracker tickets. Kensa appends the key to the URL configured via [`issueTrackerUrl`](./configuration#issue-tracker).

**Targets:** `CLASS`, `FUNCTION` (repeatable)

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
@Issue("PROJ-42", "PROJ-43")
@Test
fun `refund is processed within 24 hours`() { ... }
```

</TabItem>
<TabItem value="java" label="Java">

```java
@Issue({"PROJ-42", "PROJ-43"})
@Test
void refundIsProcessedWithin24Hours() { ... }
```

</TabItem>
</Tabs>

---

### `@Sources`

Tells Kensa to parse additional source classes referenced in your tests (e.g., shared helper objects). Required when sentence extraction needs to follow into classes outside the test file.

**Targets:** `CLASS`

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
@Sources(PaymentSteps::class, OrderFixtures::class)
class PaymentTest : KensaTest, WithKotest { ... }
```

</TabItem>
<TabItem value="java" label="Java">

```java
@Sources({PaymentSteps.class, OrderFixtures.class})
class PaymentTest implements KensaTest, WithAssertJ { ... }
```

</TabItem>
</Tabs>

---

## UI Behaviour

### `@AutoOpenTab`

Sets which report tab is open by default for a specific test or class. Overrides the global `autoOpenTab` configuration.

**Targets:** `FUNCTION`, `CLASS`

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
@AutoOpenTab(Tab.SequenceDiagram)
class OrderFlowTest : KensaTest, WithKotest { ... }
```

</TabItem>
<TabItem value="java" label="Java">

```java
@AutoOpenTab(Tab.SequenceDiagram)
class OrderFlowTest implements KensaTest, WithAssertJ { ... }
```

</TabItem>
</Tabs>

**`Tab` values:** `CapturedInteractions`, `CapturedOutputs`, `Givens`, `Parameters`, `SequenceDiagram`, `None`

---

### `@UseSetupStrategy`

Overrides the global `setupStrategy` for a specific test or class, controlling how setup interactions appear in the sequence diagram.

**Targets:** `FUNCTION`, `CLASS`

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
@UseSetupStrategy(SetupStrategy.Grouped)
@Test
fun `order is fulfilled via warehouse`() { ... }
```

</TabItem>
<TabItem value="java" label="Java">

```java
@UseSetupStrategy(SetupStrategy.Grouped)
@Test
void orderIsFulfilledViaWarehouse() { ... }
```

</TabItem>
</Tabs>

**`SetupStrategy` values:** `Grouped`, `Ungrouped`, `Ignored` — see [Configuration](./configuration#report-layout) for details.
