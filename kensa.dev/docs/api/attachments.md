---
sidebar_position: 4.5
description: How to attach typed plugin-defined data to a test for display in custom report tabs.
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Attachments

`Attachments` is a typed per-test bag for plugin-defined data. It sits alongside `CapturedOutputs` and `CapturedInteractions` on the test context but, unlike them, carries **no framework semantics**: Kensa neither validates nor renders the contents. A producer puts a value under a `TypedKey`; a tab renderer (or other consumer) retrieves it with the same key.

Use attachments when you are building a custom tab and need somewhere to stash data captured during the test that the renderer will format itself. The framework's first internal consumer is the UI testing module, which attaches captured browser screenshots for the screenshots tab.

---

## When to use what

| Capture mechanism | When |
|---|---|
| `CapturedOutputs` | The value participates in assertions, sentence rendering, or the standard report tabs. Typed via `CapturedOutput<T>`. |
| `CapturedInteractions` | The value is part of the sequence diagram. Pushed by interaction interceptors. |
| `Attachments` | The value is opaque to the framework — a tab plugin owns the rendering end-to-end. |
| `LogQueryService` | The data lives outside the JVM (log files, container stdout) and is fetched by correlation key after the run. |

---

## Example use cases

A few representative examples — each is something the framework has no reason to model, but a custom tab can render meaningfully:

- **Browser screenshots** — captured during a UI test, rendered as an image gallery. *(The first internal consumer; provided by the `framework-uitesting` module.)*
- **HTTP request/response recordings** — captured from a stub server (WireMock, MockServer) for replay or visual inspection in a "Recorded Calls" tab.
- **Generated business artifacts** — PDFs, CSVs, generated images that the system under test produced during the run, surfaced for download or preview.
- **Failure-point diagnostics** — on test failure, eagerly snapshot external state (a `LogQueryService` query result, a thread dump, a container description) and attach it. Freezes the data as it was at the moment of failure rather than at report-render time.
- **Trace/span exports** — OpenTelemetry spans collected for the test, rendered in a custom waterfall view.

The common shape: capture during the test → put into attachments → a tab renderer pulls and formats it.

---

## Defining a key and attaching data

Define a `TypedKey` once and share it between producer and consumer.

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
val mySnapshotKey = TypedKey<MySnapshot>("my-snapshot")

class SomeTest : KensaTest, WithKotest {

    @Test
    fun `something happens`() {
        whenever { ctx ->
            val snapshot = capture()
            ctx.attachments.put(mySnapshotKey, snapshot)
        }
        then { /* assertions */ }
    }
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
static final TypedKey<MySnapshot> MY_SNAPSHOT = new TypedKey<>("my-snapshot");

@Test
void somethingHappens() {
    whenever(ctx -> {
        MySnapshot snapshot = capture();
        ctx.getAttachments().put(MY_SNAPSHOT, snapshot);
    });
    then(/* assertions */);
}
```

</TabItem>
</Tabs>

Two keys are equal iff their `name` values match — the type parameter is erased at runtime, so the `name` alone identifies the entry.

---

## Reading from a tab renderer

A custom tab renderer receives the attachment via `KensaTabContext.attachments`:

```kotlin
class MyTabRenderer : KensaTabRenderer {
    override fun render(ctx: KensaTabContext): String? {
        val snapshot = ctx.attachments.getOrNull(mySnapshotKey) ?: return null
        return formatAsHtml(snapshot)
    }

    override fun mediaType(): String = "text/html"
}
```

`render` returns `null` to omit the tab for this invocation — that is the normal "no data for this test" path; a renderer should return no content rather than fail.

---

## API Reference

### `TypedKey<T>`

```kotlin
data class TypedKey<T : Any>(val name: String)
```

### `Attachments` methods

| Method | Description |
|--------|-------------|
| `put(key: TypedKey<T>, value: T)` | Store a value under the typed key |
| `getOrNull(key: TypedKey<T>): T?` | Retrieve, returns `null` if absent |
