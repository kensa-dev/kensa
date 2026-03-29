# Kensa @RenderedValue Reference

`@RenderedValue` and related annotations control how values appear in report sentences.

## Value Rendering

Whenever Kensa renders a value — from a `@RenderedValue` property, a fixture, or captured output —
it checks whether a renderer has been registered for that type. If one exists it is used; otherwise
Kensa falls back to `toString()`.

Renderers are registered via the Kensa configuration. Kotlin DSL (in a `Configuration` block):
```kotlin
withRenderers {
    valueRenderer<MyType> { value -> value.someProperty }
}
```

Or via the fluent Java-style configurator:
```kotlin
Kensa.configure()
    .withValueRenderer(MyType::class) { value -> value.someProperty }
```

This means you rarely need to override `toString()` on domain types just for reporting — register
a renderer instead. It also applies to fixture and output values referenced in test sentences.

## @RenderedValue Forms

### On a `lateinit var` field
The field's value (via `toString()`) appears in sentences wherever the field name is used.
Kensa reads the value at end of test, so it's safe for mutable output set during the action:
```kotlin
@RenderedValue
private lateinit var result: LcApplicationResult
```

### On a `val` field
The value appears in sentences wherever the field name is used:
```kotlin
@RenderedValue
private val expectedStatus = "APPROVED"
```

### On a no-arg function
The return value appears in sentences wherever the function is called:
```kotlin
@RenderedValue
private fun currentTimestamp() = clock.now().toString()
```

### On a parameter of `@ExpandableSentence`
The argument value appears in the expanded sentence:
```kotlin
@ExpandableSentence
private fun verifyLcWasApprovedWith(@RenderedValue expectedPrefix: String) {
    then(theIssuedLcNumber()) { shouldStartWith(expectedPrefix) }
}
```

## @RenderedValueWithHint

Use for wrapper types (e.g. `JsonPath`, `XmlPath`) that should show the identifier name in
sentences with the underlying path/value as a hover hint:

```kotlin
@RenderedValueWithHint(
    type = JsonPath::class,
    valueStrategy = UseIdentifierName,
    hintParam = "path",
    hintStrategy = HintFromProperty
)
class MyTest : KensaTest, WithKotest { ... }
```

This annotation is placed on the **test class**. It tells Kensa:
- When it encounters a value of type `JsonPath`, render the variable's identifier name (not the path)
- Use the `path` property of `JsonPath` as the hover hint text

### `valueStrategy` options
- `UseIdentifierName` — render the variable/parameter name (e.g. `issuedLcNumber`)
- `UseToString` — render `value.toString()` (default behaviour without the annotation)

### `hintStrategy` options
- `HintFromProperty` — read the hint from a property on the object (named by `hintParam`)
- `HintFromToString` — use `value.toString()` as the hint

## @Issue

Links test results to issue tracker tickets. Appears as a badge in the HTML report.

```kotlin
@Issue("PROJ-101")                      // single ticket
@Issue("PROJ-101", "PROJ-202")          // multiple tickets
```

Place on the test method or the test class:
```kotlin
@Test
@Issue("TF-42")
fun canIssueAnLcWhenCreditAndSanctionsArePositive() { ... }
```

## @Notes

Attaches a freeform note to a test class. Rendered as a styled card above the test list in the HTML
report. Supports inline markdown for rich text and internal report navigation links.

**Target:** `CLASS` only.

### Markdown syntax

| Syntax | Result |
|--------|--------|
| `**text**` | Bold |
| `*text*` | Italic |
| `__text__` | Underline |
| `~~text~~` | Strikethrough |
| `[label](https://...)` | External link (opens in new tab) |
| `[label](#methodName)` | Scroll to and expand a test method in the same suite |
| `[label](#ClassName)` | Navigate to another suite by simple class name |
| `[label](#ClassName.methodName)` | Navigate to that suite and expand the named method |

### Kotlin

Annotation values must be compile-time constants — `.trimIndent()` cannot be used. Start content
on the line immediately after the opening `"""` with **no leading indentation**. Blank lines become
paragraph breaks; single newlines within a paragraph become line breaks.

```kotlin
@Notes("""
**Payment gateway** uses *idempotency keys* — retries with the same key are __safe__.
See [Stripe docs](https://stripe.com/docs/idempotency) for details.

~~Direct refunds are no longer supported.~~ Use the [refund flow](#processRefund) instead.
For error-path behaviour see [RefundEdgeCasesTest](#RefundEdgeCasesTest).
""")
class PaymentTest : KensaTest, WithKotest { ... }
```

### Java

Java 15+ text blocks allow indentation stripping via the closing `"""` position:

```java
@Notes("""
    **Payment gateway** uses *idempotency keys* — retries with the same key are __safe__.
    See [Stripe docs](https://stripe.com/docs/idempotency) for details.

    ~~Direct refunds are no longer supported.~~ Use the [refund flow](#processRefund) instead.
    For error-path behaviour see [RefundEdgeCasesTest](#RefundEdgeCasesTest).
    """)
class PaymentTest implements KensaTest, WithAssertJ { ... }
```

### Common mistakes

**Adding leading indentation in Kotlin** — content must start at column 0:
```kotlin
// Bad — indented content becomes part of the note text
@Notes("""
    Some note text
""")

// Good — no leading indent
@Notes("""
Some note text
""")
```

**Using @Notes on a method** — it only applies to `CLASS`:
```kotlin
// Bad
@Test
@Notes("This test covers the happy path")
fun `payment is processed`() { ... }

// Good — put it on the class
@Notes("This suite covers payment processing happy paths.")
class PaymentTest : KensaTest, WithKotest { ... }
```

---

## @RenderedValueContainer

Use when multiple mutable output fields are repeated across several test classes.
The annotation makes each property render when mentioned in the test body:

```kotlin
@RenderedValueContainer
private inner class Holder {
    lateinit var result: LcApplicationResult
    lateinit var lcNumber: String
}

private lateinit var holder: Holder
```

For a single mutable output field, prefer `@RenderedValue lateinit var` directly on the class
rather than a container.

## Common Mistakes

**Using @RenderedValue on a field that holds test data** — prefer Fixtures for test data:
```kotlin
// Bad — test data as mutable field
@RenderedValue
private var applicantId: String = ""

// Good — use fixtures
val applicantId = fixture("Applicant ID") { "CORP-001" }
```

**Using @RenderedValueContainer when only one field is needed** — prefer a direct `@RenderedValue`:
```kotlin
// Unnecessary
@RenderedValueContainer
private inner class Holder {
    lateinit var result: LcApplicationResult
}

// Better
@RenderedValue
private lateinit var result: LcApplicationResult
```
