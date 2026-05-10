---
sidebar_position: 3
description: "Reference for the kensa-hamkrest-test-support-xml module: XML field classes backed by W3C DOM and javax.xml.xpath."
---

# XML Fields

**Artifact:** `dev.kensa:kensa-hamkrest-test-support-xml`

Source:
[`test-support/hamkrest/xml/src/main/kotlin/dev/kensa/hamkrest/testsupport/field/xml/`](https://github.com/kensa-dev/kensa/blob/master/test-support/hamkrest/xml/src/main/kotlin/dev/kensa/hamkrest/testsupport/field/xml/)

No external XML library is required. Extraction is backed by the JDK's W3C DOM (`org.w3c.dom.Node`) and `javax.xml.xpath`. The field classes import `MatcherField` from `dev.kensa.hamkrest.testsupport.field` — the only difference from the kotest counterparts is the package of the base interface.

---

## Field classes

All XML fields implement `MatcherField<Node, R>` and are open for subclassing.

### `XmlField<T>`

The base class. Evaluates an XPath expression against a `Node` and applies a `transform` to the matched node.

```kotlin
val aProvider = XmlField(
    compile("/order/@provider"),
    "Provider"
) { node -> Provider.fromString(node.nodeValue) }
```

**Constructors:**

| Signature | Notes |
|-----------|-------|
| `XmlField(expression: XPathExpression, transform: (Node) -> T?)` | Name derived from class simple name |
| `XmlField(expression: XPathExpression, name: String, transform: (Node) -> T?)` | Explicit name |
| `XmlField(expressionProvider: () -> XPathExpression, transform: (Node) -> T?)` | Lazy expression |
| `XmlField(expressionProvider: () -> XPathExpression, name: String?, transform: (Node) -> T?)` | Lazy + explicit name |

**`path` property:** returns the XPath string when the expression is an `XPathExpressionWrapper`; `null` otherwise.

### `XmlTextField<T>`

Extracts `textContent` from the matched node and applies a `transform`. Prefer `XmlStringField` for the identity case.

```kotlin
val anAmount = XmlTextField(compile("/order/amount"), "Amount") { it.toBigDecimal() }
```

### `XmlStringField`

Returns the matched node's `textContent` verbatim.

```kotlin
val status = XmlStringField(compile("/FeasibilityResponse/Status"), "Status")
```

Constructors accept either a pre-compiled `XPathExpression` or a lazy `() -> XPathExpression`; `name` is optional.

### `XmlNodeField`

Returns the matched `Node` itself, for cases where downstream code needs the raw DOM node.

### `XmlListField<T>`

Extracts an ordered `List<T>` by evaluating the XPath as a node-set and mapping each node through `transform`.

```kotlin
val profileTypes = XmlListField(
    compile("/FeasibilityResponse/Profiles/Profile/Type"),
    "Profile Types"
) { it.textContent }
```

Use with `withListOf` for exact ordered assertions. hamkrest's `withListOf` uses `equalTo(list)` — elements must be in the same order.

### `XmlSetField<T>`

Like `XmlListField<T>` but deduplicates results into a `Set<T>`. Use with `withSetOf` — set equality is order-insensitive.

---

## `XPathExpressionWrapper`

```kotlin
class XPathExpressionWrapper(
    private val xPathExpression: XPathExpression,
    val path: String
) : XPathExpression
```

Wraps a compiled `XPathExpression` and retains the original path string. Wrap all compiled expressions with this class so that:

1. The `path` property on `XmlField` returns the XPath string (useful for tooling and report rendering).
2. The path is readable when debugging test failures.

```kotlin
private fun compile(path: String): XPathExpression =
    XPathExpressionWrapper(XPathFactory.newInstance().newXPath().compile(path), path)
```

Declare a private `compile` helper in each field object to avoid repeating the wrapping boilerplate.

---

## Node extension helpers

Defined in `XmlExtensions.kt` and used internally by the field classes. Also available for direct use:

| Function | Description |
|----------|-------------|
| `Node.getNodeOrNull(path: XPathExpression): Node?` | Returns the single matched node, or `null`. |
| `Node.getNodes(path: XPathExpression): List<Node>` | Returns all matched nodes in document order. |

---

## Worked example

Declare fields in a companion object or Kotlin `object`:

```kotlin
import dev.kensa.hamkrest.testsupport.field.xml.XmlField
import dev.kensa.hamkrest.testsupport.field.xml.XmlListField
import dev.kensa.hamkrest.testsupport.field.xml.XmlStringField
import dev.kensa.hamkrest.testsupport.field.xml.XPathExpressionWrapper

object FibreVisionResponseFields {
    val status                    = XmlStringField(compile("/FeasibilityResponse/Status"), "Status")
    val firstProfileType          = XmlStringField(compile("/FeasibilityResponse/Profiles/Profile[1]/Type"), "Profile Type")
    val firstProfileDownloadSpeed = XmlField(
        compile("/FeasibilityResponse/Profiles/Profile[1]/DownloadSpeed"),
        "Download Speed"
    ) { it.textContent.toInt() }

    val profileTypes = XmlListField(
        compile("/FeasibilityResponse/Profiles/Profile/Type"),
        "Profile Types"
    ) { it.textContent }

    private fun compile(path: String): XPathExpression =
        XPathExpressionWrapper(XPathFactory.newInstance().newXPath().compile(path), path)
}
```

Then in the test, parse the response document and compose the matcher using hamkrest's `and` operator:

```kotlin
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.and
import dev.kensa.hamkrest.testsupport.field.of
import dev.kensa.hamkrest.testsupport.field.withListOf

@Test
fun `FibreVision XML response — asserted via XmlField DSL`() {
    given(bothSuppliersAreServiceable())
    whenever(aFeasibilityCheckIsRequestedForTheServiceAddress())

    then(theFibreVisionResponseDocument(),
        (status of "SERVICEABLE")
            and (firstProfileType of "FTTC")
            and (firstProfileDownloadSpeed of 80)
            and profileTypes.withListOf("FTTC")
    )
}
```
