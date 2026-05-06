---
sidebar_position: 3
description: How to write Kensa tests that read as domain prose — covering matcher design, @ExpandableRenderedValue, and the signals to look for in code review.
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Writing Fluent Tests

## The audience

Kensa reports are read by BAs, testers, and product owners — not only by developers. A BA opening a report after a regression run should be able to confirm that the dispatch service sent the correct carrier instruction without having to understand Kotlin. A tester designing the next sprint's acceptance criteria should be able to copy field names and expected values directly from the report.

That audience constraint is the design constraint. The test method body must read as domain prose. It describes *what the system does*, not *how the test verifies it*.

## How Kensa renders test bodies

Kensa parses the source text of each test method body using ANTLR and renders each token — every function call, argument, and nested expression — as part of the report sentence. There is no post-processing or summarisation: what you write is what the report shows.

This has two consequences.

**Readability and report quality are the same thing.** If the test body is hard to read in code, the report sentence will be hard to read too. There is no layer that translates implementation detail into prose.

**Render cost scales with AST size.** The more tokens in the test body, the more work Kensa does per invocation to resolve and render them. A test body with 40 inline assertions is 40 tokens to resolve; a test body with four named function calls is four. The best-practice advice below is therefore not just about aesthetics — it also keeps reports fast.

## Push assertion logic into named matchers

When a field is domain-important on its own — when a BA would ask "was the post code correct?" as a standalone question — it deserves a named matcher.

The discipline is: the test body names what it is checking; helper modules say how to check it. Kensa does not recurse into helper modules, so each helper function costs one parsed token in the body regardless of how much code is inside it.

### Bad — validation DSL inline in the body

The following test has 30+ field assertions nested six levels deep inside the `then` call. Every `item { shouldBe(...) }` block is a token in the report. The rendered sentence is unreadable, and report generation has to traverse the full AST:

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
@Test
fun dispatchesShipmentToConsignee() {
    given(aReadyToDispatchOrder())
    whenever(theDispatchServiceProcessesTheOrder())
    then(courier {
        hasDispatched {
            shipment {
                consignee {
                    address {
                        item(name = "PostCode")     { shouldBe(fixtures[PostCodeFx]) }
                        item(name = "CountryCode")  { shouldBe(fixtures[CountryCodeFx]) }
                        item(name = "Street")       { shouldBe(fixtures[StreetFx]) }
                        item(name = "City")         { shouldBe(fixtures[CityFx]) }
                        item(name = "ServiceLevel") { shouldBe(fixtures[ServiceLevelFx]) }
                        // ... 25 more item blocks
                    }
                }
            }
        }
    })
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
@Test
void dispatchesShipmentToConsignee() {
    given(aReadyToDispatchOrder());
    whenever(theDispatchServiceProcessesTheOrder());
    then(courier(c -> c
        .hasDispatched(d -> d
            .shipment(s -> s
                .consignee(cn -> cn
                    .address(a -> {
                        a.item("PostCode",     v -> v.shouldBe(fixtures().get(PostCodeFx)));
                        a.item("CountryCode",  v -> v.shouldBe(fixtures().get(CountryCodeFx)));
                        a.item("Street",       v -> v.shouldBe(fixtures().get(StreetFx)));
                        a.item("City",         v -> v.shouldBe(fixtures().get(CityFx)));
                        a.item("ServiceLevel", v -> v.shouldBe(fixtures().get(ServiceLevelFx)));
                        // ... 25 more
                    }))))));
}
```

</TabItem>
</Tabs>

### Good — flat call into a matcher DSL defined in a helper module

The helper functions `aShipment`, `thatHas`, `aPostCode`, `aCountryCode`, and `of` live in a shared matcher module. The test body is four lines. The report sentence says, in plain English, that the courier dispatched a shipment with specific field values:

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
@Test
fun dispatchesShipmentToConsignee() {
    given(aReadyToDispatchOrder())
    whenever(theDispatchServiceProcessesTheOrder())
    then(courier.hasDispatched(aShipment(
        thatHas(
            aPostCode     of fixtures[PostCodeFx],
            aCountryCode  of fixtures[CountryCodeFx],
            aStreet       of fixtures[StreetFx],
            aCity         of fixtures[CityFx],
            aServiceLevel of fixtures[ServiceLevelFx],
        )
    )))
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
@Test
void dispatchesShipmentToConsignee() {
    given(aReadyToDispatchOrder());
    whenever(theDispatchServiceProcessesTheOrder());
    then(courier.hasDispatched(aShipment(
        thatHas(
            aPostCode.of(fixtures().get(PostCodeFx)),
            aCountryCode.of(fixtures().get(CountryCodeFx)),
            aStreet.of(fixtures().get(StreetFx)),
            aCity.of(fixtures().get(CityFx)),
            aServiceLevel.of(fixtures().get(ServiceLevelFx))
        )
    )));
}
```

</TabItem>
</Tabs>

`aShipment`, `thatHas`, `aPostCode`, and `of` are defined in helper modules that Kensa does not parse. The full matcher graph could be hundreds of lines; each costs one token in the report. The BA sees: "courier has dispatched a shipment that has a post code of WC2N 5DU, a country code of GB, ...". That is the sentence a BA can verify.

## Repeated `shouldBe` is a DRY signal

Three or more inline `shouldBe` calls in a single test body is a signal that a named matcher is missing. Each repetition of the same shape — `field { shouldBe(expected) }` — represents an abstraction that has not been named.

The rule of thumb: up to two inline `shouldBe` calls are acceptable for one-off checks. A third is the prompt to ask: "what is the domain concept that unifies these checks?" Name it and move the logic out of the body.

This is standard DRY reasoning applied to BDD test bodies: repetition in the rendered sentence means repetition in the source, which means a missing abstraction.

## When fields aren't individually meaningful — use `@ExpandableRenderedValue`

Some messages have 30 fields where no single field stands as a domain concept on its own. What matters to the BA is that the full field set is correct. A single named matcher per field would produce 30 top-level sentence tokens; the test body would be unwieldy.

`@ExpandableRenderedValue` handles this case. The annotated method performs the comparison internally; Kensa renders only the return value. The test body stays a single line.

### Default style — flat list

Return an iterable; Kensa calls the registered renderer on each item and renders them as a flat list. Use this when the items are meaningful values on their own (enum states, identifiers, labels):

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

The report expands to show each status; the body shows one call.

### Tabular style — labelled table

Return an `Iterable<Pair<*, *>>`; Kensa renders it as a two-column table. Supply `headers` to label the columns. Use this when you need to verify a full set of named fields and want the BA to see field name alongside expected value:

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
@ExpandableRenderedValue(renderAs = Tabular, headers = ["Field", "Expected"])
private fun theShipmentFields(): List<Pair<String, String>> {
    val dispatched = courier.lastDispatchedShipment()
    return listOf(
        "PostCode"     to fixtures[PostCodeFx],
        "CountryCode"  to fixtures[CountryCodeFx],
        "Street"       to fixtures[StreetFx],
        "City"         to fixtures[CityFx],
        "ServiceLevel" to fixtures[ServiceLevelFx],
        // all 30 fields
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
@ExpandableRenderedValue(renderAs = RenderedValueStyle.Tabular, headers = {"Field", "Expected"})
private List<Pair<String, String>> theShipmentFields() {
    ShipmentRecord dispatched = courier.lastDispatchedShipment();
    List<Pair<String, String>> fields = List.of(
        Pair.of("PostCode",     fixtures().get(PostCodeFx)),
        Pair.of("CountryCode",  fixtures().get(CountryCodeFx)),
        Pair.of("Street",       fixtures().get(StreetFx)),
        Pair.of("City",         fixtures().get(CityFx)),
        Pair.of("ServiceLevel", fixtures().get(ServiceLevelFx))
        // all 30 fields
    );
    fields.forEach(f -> assertThat(dispatched.field(f.getFirst())).isEqualTo(f.getSecond()));
    return fields;
}

// In the test:
then(courier.hasDispatched(aShipmentWith(theShipmentFields())));
```

</TabItem>
</Tabs>

The report shows a two-column table headed "Field / Expected". The test body is one line. The BA can scan all 30 fields without touching the code.

For custom table shapes beyond `Pair`, register a `TableRenderer<T>` for your type — see the [annotations reference](./api/annotations#expandablerenderedvalue).

## Decision table

| Situation | Use |
|---|---|
| Field is domain-important on its own (BA asks about it in isolation) | Named matcher — `aPostCode of value` |
| Many fields; the collection is the unit of meaning; items are meaningful values | `@ExpandableRenderedValue` (Default) |
| Many fields; want a labelled table of field name to expected value | `@ExpandableRenderedValue(renderAs = Tabular)` |

## Review checklist

During code review, the following are concrete signals to act on:

- **Nesting depth > 3** inside a `then`, `whenever`, or `given` call — extract a flat matcher DSL.
- **More than 3 inline `shouldBe` or `shouldNotBe`** in a single test body — the repetitions are a missing matcher.
- **The same `item(name = ...) { shouldBe(...) }` shape repeated more than twice** — extract a named field descriptor and use `thatHas(field of value, ...)`.

Each signal points to the same resolution: move the structural logic out of the test body into a helper module that Kensa does not parse. The test body names what is being verified; helpers say how.
