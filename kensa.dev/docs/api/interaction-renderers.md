---
sidebar_position: 6
description: How to implement and register InteractionRenderer to control how exchanged values appear in Kensa's Captured Interactions tab and sequence diagrams.
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Interaction Renderers

When Kensa records interactions between actors it needs to know how to display the exchanged values in the **Captured Interactions** tab and in sequence diagrams. For types it doesn't recognise — HTTP responses, domain objects, binary payloads — you provide an `InteractionRenderer<T>`.

---

## The `InteractionRenderer` Interface

```kotlin
interface InteractionRenderer<T> {
    fun render(value: T, attributes: Attributes): List<RenderedInteraction>
    fun renderAttributes(value: T): List<RenderedAttributes>
}
```

| Method | Purpose |
|--------|---------|
| `render` | Returns the body content of the interaction — what appears in the main panel |
| `renderAttributes` | Returns supplementary key/value groups shown alongside the interaction (e.g. status code, headers) |

### `RenderedInteraction`

```kotlin
data class RenderedInteraction(val name: String, val value: String, val language: Language = PlainText)
```

`language` controls syntax highlighting in the report. Supported values are `Language.Json`, `Language.Xml`, and `Language.PlainText`.

### `RenderedAttributes`

```kotlin
data class RenderedAttributes(val name: String, val attributes: Set<NamedValue>)
```

Each `RenderedAttributes` groups a set of `NamedValue(name, value)` pairs under a heading. You can return multiple groups — one for status, one for headers, etc.

---

## Example — HTTP Response Renderer

The following renderer is taken from the [adoptabot example](https://github.com/kensa-dev/kensa/tree/master/examples/adoptabot). It formats the response body as pretty-printed JSON and exposes the HTTP status code and headers as separate attribute groups.

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
object ResponseRenderer : InteractionRenderer<Response> {

    override fun render(value: Response, attributes: Attributes): List<RenderedInteraction> {
        return listOf(
            RenderedInteraction("Response Body", value.bodyString().prettyPrintJson(), Language.Json)
        )
    }

    override fun renderAttributes(value: Response): List<RenderedAttributes> {
        return listOf(
            RenderedAttributes(
                "Status",
                setOf(NamedValue("Status", value.status.code.toString()))
            ),
            RenderedAttributes(
                "Headers",
                value.headers.map { NamedValue(it.first, it.second) }.toSet()
            )
        )
    }
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
public class ResponseRenderer implements InteractionRenderer<Response> {

    @Override
    public List<RenderedInteraction> render(Response value, Attributes attributes) {
        return List.of(
            new RenderedInteraction("Response Body", prettyPrintJson(value.bodyString()), Language.Json)
        );
    }

    @Override
    public List<RenderedAttributes> renderAttributes(Response value) {
        Set<NamedValue> statusAttrs = Set.of(
            new NamedValue("Status", String.valueOf(value.status().code()))
        );
        Set<NamedValue> headerAttrs = value.headers().stream()
            .map(h -> new NamedValue(h.getFirst(), h.getSecond()))
            .collect(Collectors.toSet());
        return List.of(
            new RenderedAttributes("Status", statusAttrs),
            new RenderedAttributes("Headers", headerAttrs)
        );
    }
}
```

</TabItem>
</Tabs>

---

## Registering a Renderer

Register the renderer during Kensa configuration, mapping it to the type it handles.

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
// DSL style — type is inferred from the renderer
Kensa.konfigure {
    withRenderers {
        interactionRenderer(ResponseRenderer)
    }
}

// Builder style — type is explicit
Kensa.configure()
    .withInteractionRenderer(Response::class, ResponseRenderer)
```

</TabItem>
<TabItem value="java" label="Java">

```java
Kensa.configure()
    .withInteractionRenderer(Response.class, new ResponseRenderer());
```

</TabItem>
</Tabs>

Configuration is typically placed in a test base class, a JUnit 5 `@BeforeAll`, or a Kotest `ProjectConfig`. See [Configuration](./configuration) for the full setup.

---

## The `Attributes` Parameter

`render` receives an `Attributes` map alongside the value. This carries interaction metadata set by Kensa at record time — including the interaction group and arrow style for the sequence diagram. You can read values from it if your renderer needs to vary output based on context, but most renderers ignore it and focus solely on the value.

```kotlin
// Reading an attribute — rarely needed in practice
val group: String? = attributes.group
val arrowStyle: ArrowStyle = attributes.arrowStyle
```

---

## API Reference

### `InteractionRenderer<T>`

| Method | Signature | Description |
|--------|-----------|-------------|
| `render` | `(value: T, attributes: Attributes) -> List<RenderedInteraction>` | Body content for the interaction panel |
| `renderAttributes` | `(value: T) -> List<RenderedAttributes>` | Supplementary attribute groups shown alongside the interaction |

### `RenderedInteraction`

| Parameter | Type | Description |
|-----------|------|-------------|
| `name` | `String` | Label for this content block |
| `value` | `String` | The content to display |
| `language` | `Language` | Syntax highlighting hint — `Json`, `Xml`, or `PlainText` (default) |

### `RenderedAttributes`

| Parameter | Type | Description |
|-----------|------|-------------|
| `name` | `String` | Heading for this attribute group |
| `attributes` | `Set<NamedValue>` | Key/value pairs to display within the group |

### `NamedValue`

```kotlin
data class NamedValue(val name: String, val value: Any?)
```
