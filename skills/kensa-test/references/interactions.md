# Kensa Interactions Reference

Interactions record HTTP (or other) exchanges between actors, powering the sequence diagrams in
HTML reports.

## Core API

```kotlin
interactions.capture(
    from(Client)
        .to(TradePortalService)
        .with(requestBody, "Request Label")
        .returning(responseBody, "Response Label")
        .with(Attributes.of("language", Json))
)
```

- `from(actor)` / `to(actor)` — actors are any object; their `.toString()` or a registered name
  appears as the lifeline label in the sequence diagram.
- `.with(body, label)` — captures the outbound payload. Label appears on the arrow.
- `.returning(body, label)` — captures the inbound/response payload. Label appears on the return arrow.
- `.with(Attributes.of(...))` — attaches metadata (e.g. content type) shown in the report.

## Common Attribute Types

```kotlin
Attributes.of("language", Json)
Attributes.of("language", Xml)
Attributes.of("language", PlainText)
Attributes.of("language", Html)
```

## Capturing Both Sides

Typically the stub or service wrapper captures the interaction, not the test itself. The
`interactions` object is passed into the Action context:

```kotlin
private fun aClientSubmitsAnLcApplication() = Action<ActionContext> { (fixtures, interactions) ->
    complianceStub.prepareFor(interactions)   // stub records its own capture
    creditStub.prepareFor(interactions)
    holder.result = tradePortal.submit(fixtures[lcRequest])
}
```

The stub's `prepareFor` method would call `interactions.capture(...)` when the stub receives a request.

## Capturing in Stub/Service Classes

```kotlin
class ComplianceStub(private val server: WireMockServer) {
    fun prepareFor(interactions: Interactions) {
        server.stubFor(post("/compliance/check")
            .willReturn(aResponse().withStatus(200).withBody("""{"approved":true}""")))

        // Register a callback so the capture happens at request time:
        server.addMockServiceRequestListener { request, response ->
            interactions.capture(
                from(TradePortal)
                    .to(ComplianceService)
                    .with(request.bodyAsString, "Compliance Check Request")
                    .returning(response.body, "Compliance Check Response")
                    .with(Attributes.of("language", Json))
            )
        }
    }
}
```

## Actor Naming

Actors are usually singleton objects or enums in the test's companion/extension:

```kotlin
object Client
object TradePortalService
object ComplianceService
object CreditService
```

Their class simple name is used as the lifeline label unless a custom `toString()` is provided.

## Sequence Diagram Order

Interactions are rendered in the order they are captured. If timing matters, ensure the stub
registrations happen in the correct order relative to the action.
