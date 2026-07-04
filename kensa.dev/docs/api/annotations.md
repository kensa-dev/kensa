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

Renders **only the return value** of the annotated method or property — the body is hidden. Use it when many fields need verification but they are not individually meaningful as named matchers; the data itself is the unit of meaning. The method may also perform comparison work; only the return value reaches the report.

When a field *is* domain-important on its own, prefer a named matcher (see [Writing Fluent Tests](../writing-fluent-tests)).

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `renderAs` | `RenderedValueStyle` | `Default` | `Default` (value renderer / flat list) or `Tabular` (table layout) |
| `headers` | `Array<String>` | `[]` | Column headers when `renderAs = Tabular` |

**Targets:** `FIELD`, `VALUE_PARAMETER`, `FUNCTION`, `PROPERTY_GETTER`

---

#### Default style

Renders the return value via its registered value renderer. If the value is iterable, the renderer is invoked on each item and items appear as a flat list in the report. Use this when the items are meaningful values on their own — enum states, identifiers, or labels — and you want each one visible without a table:

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
@ExpandableRenderedValue
private fun theDispatchedLifecycle(): List<DispatchStatus> {
    val actual = courier.observedLifecycle()
    actual shouldContainExactly listOf(ACKNOWLEDGED, COMMITTED, DISPATCHED, DELIVERED)
    return actual
}

// In the test:
then(theShipment(), shouldHaveCompletedDispatch())
and(theDispatchedLifecycle())
```

</TabItem>
<TabItem value="java" label="Java">

```java
@ExpandableRenderedValue
private List<DispatchStatus> theDispatchedLifecycle() {
    List<DispatchStatus> actual = courier.observedLifecycle();
    assertThat(actual).containsExactly(ACKNOWLEDGED, COMMITTED, DISPATCHED, DELIVERED);
    return actual;
}

// In the test:
then(theShipment(), shouldHaveCompletedDispatch());
and(theDispatchedLifecycle());
```

</TabItem>
</Tabs>

The report expands to show each `DispatchStatus` value; the test body shows one call. The helper body is not rendered.

---

#### Tabular style

Renders the return value as a labelled table. The default table-renderer behaviour: an `Iterable<Pair<*, *>>` becomes two-column rows (the renderer special-cases `kotlin.Pair` and `Triple` — from Java, construct `new kotlin.Pair<>(a, b)`). Provide explicit `headers` to label the columns. For richer table shapes, register a custom `TableRenderer<T>` for your type.

Use this when you need to verify a full set of named fields and want the BA to see field name alongside expected value:

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
@ExpandableRenderedValue(renderAs = Tabular, headers = ["Field", "Expected"])
private fun theShipmentFields(): List<Pair<String, String>> {
    val dispatched = courier.lastDispatchedShipment()
    return listOf(
        "PostCode"     to fixtures[PostCodeFx],
        "CountryCode"  to fixtures[CountryCodeFx],
        "ServiceLevel" to fixtures[ServiceLevelFx],
        // all fields
    ).also { fields ->
        fields.forEach { (field, expected) ->
            dispatched.field(field) shouldBe expected
        }
    }
}

// In the test:
then(courier.hasDispatched(aShipmentWith(theShipmentFields())))
```

</TabItem>
<TabItem value="java" label="Java">

```java
import kotlin.Pair;

@ExpandableRenderedValue(renderAs = RenderedValueStyle.Tabular, headers = {"Field", "Expected"})
private List<Pair<String, String>> theShipmentFields() {
    ShipmentRecord dispatched = courier.lastDispatchedShipment();
    List<Pair<String, String>> fields = List.of(
        new Pair<>("PostCode",     fixtures().get(PostCodeFx)),
        new Pair<>("CountryCode",  fixtures().get(CountryCodeFx)),
        new Pair<>("ServiceLevel", fixtures().get(ServiceLevelFx))
        // all fields
    );
    fields.forEach(f -> assertThat(dispatched.field(f.getFirst())).isEqualTo(f.getSecond()));
    return fields;
}

// In the test:
then(courier.hasDispatched(aShipmentWith(theShipmentFields())));
```

</TabItem>
</Tabs>

The report shows a two-column table headed "Field / Expected". The test body stays a single line.

---

#### When to use `@ExpandableRenderedValue` vs a named matcher

| Situation | Use |
|---|---|
| Field is domain-important on its own | Named matcher — `aPostCode of value` |
| Many fields; collection is the unit of meaning; items are meaningful values | `@ExpandableRenderedValue` (Default) |
| Many fields; want a labelled table of field name to expected value | `@ExpandableRenderedValue(renderAs = Tabular)` |

See [Writing Fluent Tests](../writing-fluent-tests) for the full narrative, worked bad/good examples, and the review checklist.

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

Resolution is hierarchy-aware: a directive declared for a supertype (class or interface) applies to every subtype, unless a more specific directive is also declared. This means a single annotation on a sealed parent or common interface covers its whole hierarchy.

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `type` | `KClass<*>` | — | The type this hint applies to. Matches the exact type and all subtypes; the most specific declared type wins. |
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

**Targets:** `CLASS`, `FUNCTION`, `ANNOTATION_CLASS` — pass multiple ids in one annotation (`vararg`)

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

### `@ParameterizedTestDescription`

Marks one parameter of a parameterised test as the invocation's **display description**. The parameter's per-invocation value becomes the label for that invocation in the report, and the parameter is excluded from normal parameter-value substitution in the rendered sentence.

**Targets:** `VALUE_PARAMETER`

```kotlin
@ParameterizedTest
@CsvSource(
    "a standard loan is approved, 15000, 36",
    "a high-value loan needs underwriting, 75000, 60",
)
fun canProcessLoanApplications(
    @ParameterizedTestDescription description: String,
    amount: Int,
    termMonths: Int,
) { ... }
```

Each invocation appears in the report under its description ("a standard loan is approved", …) instead of the raw argument list.

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

#### Requirements for a referenced class or object

For Kensa to discover and render values from a referenced class/object — for example a shared `object` holding `@RenderedValue` or `@ExpandableSentence` functions — **all** of the following must hold:

1. **Referenced or inherited.** The class/object is listed in `@Sources(...)` on the test, **or** is a superclass/interface of the test (those are followed automatically — you only need `@Sources` for classes outside the test's own type hierarchy).
2. **In its own source file, named after the class.** Kensa locates a class's source from its fully-qualified name (`com/example/PaymentSteps.kt`). A declaration that **shares a file with a differently-named class** — e.g. a helper `object` declared inside your test's `.kt` file — cannot be located and is silently skipped. Put shared helpers in their own file (`PaymentSteps.kt`).
3. **Within a configured source location.** The file must live under one of your [`sourceLocations`](./configuration.md) roots — Kensa only parses the source roots you configure.
4. **Instrumented by the Kensa compiler plugin.** Value capture for `@RenderedValue`/`@ExpandableSentence` is performed by the compiler plugin, so the class/object must be in a source set the plugin is applied to. If you customise the plugin's source-set/directory list, ensure it includes the helper's directory — otherwise the value is never captured.

:::tip Diagnosing a missing requirement
If a `@RenderedValue` function from a referenced class renders as the **literal call text** (the de-camel-cased function name) instead of its value, one of the above is usually unmet — most commonly the helper shares a file with a differently-named class (requirement 2), or `@Sources` was not added at all (requirement 1).
:::

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

**`Tab` values:** `CapturedInteractions`, `CapturedOutputs`, `Parameters`, `SequenceDiagram`, `None`

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

---

## Flow Tagging

:::warning[Experimental]
Org-flow tagging supports cross-repo flow grouping in the (in-development) Kensa hub server. The API may change before it stabilises.
:::

### `@OrgFlow`

Marks a test as the canonical slice of a named org-wide business flow. One tag per test — the test-to-flow relationship is 1:1.

**Targets:** `FUNCTION`

```kotlin
@OrgFlow(category = "Payments", name = "Card checkout", product = "Web")
@Test
fun canCheckOutWithACard() { ... }
```

### `@OrgFlowMarker`

Meta-annotation for a typed alternative to `@OrgFlow` string arguments. Put it on your own annotation whose single member is an enum implementing `dev.kensa.context.OrgFlowSpec`; Kensa resolves the test's org-flow from the enum constant:

```kotlin
enum class CheckoutFlow(
    override val category: String,
    override val flowName: String,
    override val attributes: Map<String, String> = emptyMap(),
) : OrgFlowSpec {
    CardCheckout("Payments", "Card checkout"),
}

@OrgFlowMarker
@Target(AnnotationTarget.FUNCTION)
annotation class Flow(val value: CheckoutFlow)

@Flow(CheckoutFlow.CardCheckout)
@Test
fun canCheckOutWithACard() { ... }
```

**Targets:** `ANNOTATION_CLASS`

---

## Documented Elsewhere

| Annotation | Page |
|---|---|
| `@KensaTab` | [Log Tabs](./log-tabs#wiring-the-tab) — custom report tabs |
| `@Fixture` | [Factory Fixtures](./factory-fixtures) — parameterised fixture factory functions |
| `@RenderedValueWithHint` | [Field Assertion DSL — Report Rendering](../field-assertion-dsl/report-rendering) |
