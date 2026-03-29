# Kensa Fixtures Reference

## Why Fixtures?

The `Fixtures` system is type-safe, immutable, and accessible in all context types
(`GivensContext`, `ActionContext`, `CollectorContext`). They are automatically rendered in test bodies. They replace fields and
properties for test data, making test classes smaller and easier to understand and report output more accurate.

## Defining Fixtures

**Fixtures must always be defined inside a `FixtureContainer` object. Never define them elsewhere
(e.g. companion objects, top-level, or directly in test classes).**

Registration of the container during initialisation is what allows Kensa to give each test
invocation its own scoped fixture values — even when tests run in parallel.

### Simple fixture
```kotlin
val ApplicantId = fixture("Applicant ID") { "CORP-001" }
```

### Derived fixture (depends on another)
```kotlin
val LcRequest = fixture("LC Application Request", ApplicantId, BeneficiaryId) { id, bId ->
    LcApplicationRequest(applicantId = id, beneficiaryId = bId, amount = BigDecimal("50000"))
}
```

Up to 3 dependencies are supported as overloads.

### Fixture keys must be globally unique across all registered containers.
Use descriptive keys like `"LCApplicationRequest"` not `"Request"`.

## FixtureContainer

All fixtures must be defined inside a `FixtureContainer` object:

```kotlin
object TradeFinanceFixtures : FixtureContainer {
    val ApplicantId = fixture("Applicant ID") { "CORP-001" }
    val BeneficiaryId = fixture("Beneficiary ID") { "SUPP-042" }
    val LcRequest = fixture("LC Application Request", applicantId, beneficiaryId) { id, bId ->
        LcApplicationRequest(applicantId = id, beneficiaryId = bId, amount = BigDecimal("50000"))
    }
    val ExpectedLcNumberPrefix = fixture("Expected LC Number Prefix") { "LC-2024" }
}
```

## Registering Fixtures in a JUnit Extension

Fixture containers must be registered in a JUnit extension:

```kotlin
class TradeFinanceExtension : BeforeAllCallback, AfterAllCallback, AutoCloseable {
    init {
        registerFixtures(TradeFinanceFixtures)
    }

    override fun beforeAll(context: ExtensionContext) {
        // start stubs, services, etc.
    }

    override fun afterAll(context: ExtensionContext) {
        // stop services
    }

    override fun close() {
        // cleanup
    }
}
```

Tests then use `@ExtendWith(TradeFinanceExtension::class)`.

## Accessing Fixtures in Tests

`KensaTest` exposes `fixtures` directly in the test body. The instance is scoped to the current
invocation — parallel test runs each get their own isolated copy. Here we reference a registered
fixture `CreditLimit` in a test body:

```kotlin
@Test
fun `an applicant submits an LC application`() {
    given(anApplicantWithACreditLimitOf(fixtures[CreditLimit]))
}
```

## Accessing Fixtures in Contexts

```kotlin
// In a GivensContext action:
private fun anApplicantWithSufficientCreditLimit() = Action<GivensContext> { (fixtures) ->
    creditStub.configureLimit(fixtures[applicantId], limit = BigDecimal("100000"))
}

// In an ActionContext action:
private fun aClientSubmitsAnLcApplication() = Action<ActionContext> { (fixtures, interactions) ->
    holder.result = tradePortal.submit(fixtures[lcRequest])
}

// In a StateCollector:
private fun theApplicantId() = StateCollector { fixtures[applicantId] }
```

## Fixtures in Rendered Contexts

`fixtures[key]` and `outputs[key]` can appear freely in test bodies and `@ExpandableSentence`
bodies. Kensa substitutes the resolved value in the report — so the report shows the actual
applicant ID, not the variable name `applicantId`.

## When to Use Fixtures vs Mutable Fields

| Use `Fixtures` for… | Use `@RenderedValue lateinit var` for… |
|---|---|
| Input test data (IDs, request bodies, config) | Single mutable output produced during the action |
| Values shared across Given/When/Then phases | A response/result that can't be known until action runs |

For a single mutable output field, annotate it directly on the class — Kensa reads the value at
end of test for rendering:
```kotlin
@RenderedValue
private lateinit var result: LcApplicationResult
```

Only use a `@RenderedValueContainer` inner class when multiple mutable fields are repeated across
several tests — the annotation makes each property render when mentioned in the test body:
```kotlin
@RenderedValueContainer
private inner class Holder {
    lateinit var result: LcApplicationResult
    lateinit var lcNumber: String
}

private lateinit var holder: Holder
```
## Extension Functions for Request Builders

`FixtureContainer` is only for fixture *definitions*. When you need to assemble a complex request
object from multiple fixtures (e.g., a service instruction that populates a dozen fields), define
it as an extension function on `Fixtures`, `KensaTest`, or `FixturesAndOutputs` — not inside the
container. Put these in a dedicated object, not the companion object or test class.

The pattern uses a builder with an optional override block so individual tests can vary specific
fields without duplicating the whole setup:

```kotlin
object ServiceRequestBuilders {
    fun Fixtures.aNewServiceRequest(block: ServiceRequestBuilder.() -> Unit = {}) =
        ServiceRequestBuilder().apply {
            withCorrelationId = get(CorrelationIdFx)
            withCustomerId = get(CustomerIdFx)
            withProductCode = get(ProductFx)
            withOrderNotes = get(OrderNotesFx)
            apply(block)           // test can override e.g. withProductCode = SpecialProduct
        }.build()
}
```

When the builder needs both input fixtures *and* captured outputs (e.g. a follow-up request that
references a system-generated ID from an earlier step), use `FixturesAndOutputs` as the receiver:

```kotlin
fun FixturesAndOutputs.aFollowUpRequest(block: FollowUpBuilder.() -> Unit = {}) =
    FollowUpBuilder().apply {
        val (fixtures, outputs) = this@aFollowUpRequest
        withOriginalId = outputs[IssuedOrderIdCo]
        withCustomerId = fixtures[CustomerIdFx]
        apply(block)
    }.build()
```

## Deprecated: `givens` Map

The `givens` map was an older mechanism for passing values between Given/When/Then actions. It is
still functional but deprecated — flag any use of `givens[...]` in a test as a violation and
migrate it to `Fixtures`.

```kotlin
// Deprecated — do not use
givens["applicantId"] = "CORP-001"
val id = givens["applicantId"]

// Correct — use fixtures
val applicantId = fixture("Applicant ID") { "CORP-001" }
```
