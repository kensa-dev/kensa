---
name: kensa-test
description: >
  Review and improve Kensa BDD tests written in Kotlin. Use this skill whenever the user
  shares a Kensa test file, asks you to review or improve a Kensa test, or mentions Given-When-Then
  tests in a Kensa project. Kensa tests use the KensaTest interface with given()/whenever()/then()
  DSL, @RenderedValue, @ExpandableSentence, Fixtures, and produce HTML reports with sequence diagrams.
  Trigger for: "review this test", "improve this Kensa test", "what's wrong with this test",
  "make this test more idiomatic", or any time a user pastes Kensa test code.
---

# Kensa Test Reviewer

You are an expert in writing idiomatic Kensa BDD tests. Review tests, identify violations, and
produce improved versions with clear explanations.

## What is Kensa?

Kensa is a Kotlin BDD framework where Given-When-Then structure is written directly in code (no
Gherkin). It parses test source via ANTLR to render sentences in HTML reports. **Critical: the
report shows actual source tokens in test bodies and `@ExpandableSentence` bodies — so everything
in those rendered contexts must read as fluent English.**

## KensaTest Interface

Test classes implement `KensaTest`, which provides the Given-When-Then DSL and direct access to
the current invocation's data:

- `fixtures` — the fixture values for this invocation
- `outputs` — the captured outputs for this invocation
- `fixturesAndOutputs` — combined access (destructured as `(fixtures, outputs)`)

All three are **scoped to the current test invocation**. Parallel runs each get their own isolated
instance — never shared across tests.

## On-Demand References

Read these files only when the relevant topic appears in the test being reviewed:

| When you see… | Read |
|---|---|
| `interactions.capture(...)`, sequence diagrams, `from().to().with()` | `references/interactions.md` |
| `SetupStep`, `SetupSteps`, `KotestSetupStep`, `buildGivens`, `buildActions`, `@UseSetupStrategy` | `references/setup-steps.md` |
| `FixtureContainer`, multi-dependency fixtures, `givens[...]` | `references/fixtures.md` |
| `CapturedOutputContainer`, `capturedOutput<T>`, `outputs[key]`, `registerCapturedOutputs` | `references/captured-outputs.md` |
| `@RenderedValue`, `@RenderedValueWithHint`, `@RenderedValueContainer`, `@Issue`, `@Notes` | `references/rendered-value.md` |

## The Best Practices

### BP-1: Rendered code must read as fluent English

Rendered contexts = test method bodies + `@ExpandableSentence` bodies.

**Never allow:**
- Variable assignments: `val response = client(request)`
- Loops: `for (item in list) { ... }`
- Complex chained expressions with intermediate results
- Raw lambda bodies inline

**Good:**
```kotlin
@Test
fun canIssueAnLcWhenCreditAndSanctionsArePositive() {
    given(aComplianceApprovedCounterparty())
    and(anApplicantWithSufficientCreditLimit())
    whenever(aClientSubmitsAnLcApplication())
    then(theLcResult(), shouldBeApproved())
}
```

### BP-2: Action lambdas must not appear in rendered contexts

Action lambdas contain implementation code — keep them out of test bodies and `@ExpandableSentence` bodies.

**Where NOT to put them:** inline in test body, or inside `@ExpandableSentence` functions.

**Where to put them:** in a regular private function that *returns* the Action — only the function
name appears in the report.

**Bad** — lambda rendered in @ExpandableSentence:
```kotlin
@ExpandableSentence
private fun aClientSubmitsAnLcApplicationFor(@RenderedValue applicantId: String): Action<ActionContext> {
    return Action { (_, interactions) ->
        paymentStub.prepareFor(interactions)   // rendered when expanded
        holder.result = portal.submit(...)     // rendered when expanded
    }
}
```

**Good** — only the function name is rendered:
```kotlin
private fun aClientSubmitsAnLcApplication() = Action<ActionContext> { (_, interactions) ->
    paymentStub.prepareFor(interactions)
    holder.result = portal.submit(fixtures[lcRequest])
}
```

**When IS @ExpandableSentence appropriate?** Only for multi-step *assertion* sequences where the
individual steps should be visible on expansion. Never on a function that creates/returns an Action.

```kotlin
@ExpandableSentence
private fun verifyLcWasApprovedWith(@RenderedValue expectedPrefix: String) {
    then(theLcResult(), shouldBeApproved())
    and(theIssuedLcNumber()) { shouldStartWith(expectedPrefix) }
    and(theApplicantId()) { shouldBe(fixtures[applicantId]) }
}
```

### BP-3: Use Fixtures for test data, @RenderedValue for outputs

Prefer the type-safe `Fixtures` system over mutable fields for test data. See `references/fixtures.md`
for setup patterns.

`fixtures[key]` and `outputs[key]` can be used freely in rendered contexts — Kensa substitutes
them with resolved values in the report.

For output produced during the action (e.g. a response assigned by the system under test), use
`@RenderedValue`:

```kotlin
// Immutable value known at construction time
@RenderedValue
val expectedState = OrderState.Confirmed

// Mutable output set during the action phase
@RenderedValue
private lateinit var result: ServiceResponse
```

Only use a `@RenderedValueContainer` inner class when many mutable fields are repeated across
multiple tests.

### BP-4: Build a composable toolbox — don't repeat setup logic

Kensa's design enables a reusable toolbox of `Action<GivensContext>`, `Action<ActionContext>`,
`SetupStep`, and assertion functions shared across multiple test classes.

A well-designed test suite has:
- An abstract base class per domain consolidating `@ExtendWith`, `@UseSetupStrategy`, `@Sources`,
  `@RenderedValueWithHint`, `@KensaTab`, `KensaTest`, and `WithKotest` — concrete classes extend it
- A `FixtureContainer` object with **only fixture definitions** — never builders or helpers
- A `CapturedOutputContainer` object for system-generated values
- A `SetupStep` class providing named entry points like `theOrderHasProgressedTo(state)`,
  built from state transitions the app must be driven through

**Extension functions for request builders** — when a request is assembled from multiple fixtures,
define it as an extension function on `Fixtures` (or `KensaTest`/`FixturesAndOutputs`), *not*
inside the `FixtureContainer`. These typically use a builder and accept an optional lambda for
test-specific overrides:

```kotlin
// In a dedicated object — keeps FixtureContainer clean
object RequestBuilders {
    fun Fixtures.aServiceRequest(block: ServiceRequestBuilder.() -> Unit = {}) =
        ServiceRequestBuilder().apply {
            withCorrelationId = get(CorrelationIdFx)
            withCustomerId = get(CustomerIdFx)
            withProduct = get(ProductFx)
            apply(block)               // test can override individual fields
        }.build()
}
```

**`@Sources`** — when test bodies reference field descriptor types from classes outside the test
module (e.g. a shared assertion-helper module defines the field matchers used in `thatHas(...)` calls),
the ANTLR parser must know where to look for their tokens. Without `@Sources` those values won't
be substituted in the report sentence:

```kotlin
@Sources(OrderFields::class, NotificationFields::class)
abstract class MyDomainTest : KensaTest, WithKotest
```

### BP-5: Wrap raw assertions in semantic functions

Never expose implementation matchers in rendered contexts. The report should read: "should be approved",
not the underlying type assertion.

**Bad:**
```kotlin
then(theLcResult()) { shouldBeInstanceOf<LcApplicationResult.Approved>() }
```

**Good:**
```kotlin
then(theLcResult(), shouldBeApproved())

private fun shouldBeApproved() = Matcher<LcApplicationResult> { result ->
    MatcherResult(
        result is LcApplicationResult.Approved,
        { "Expected Approved but was $result" },
        { "Expected not to be Approved" }
    )
}
```

### BP-6: Use typed context objects and interface mixins for scenario helpers

Rendered test bodies must read as fluent English. That means **no qualifier prefixes**:

```kotlin
// Bad — "steps." breaks fluency in the report
given(steps.theOrderHasProgressedTo(OrderState.Dispatched))
whenever(orchestrationStub.sends(aPlaceOrderRequest()))

// Good — reads as natural prose
given(theOrderHasProgressedTo(OrderState.Dispatched))
whenever(orchestration.sends(aPlaceOrderRequest()))
```

The qualifier disappears because `with(context)` brings all helpers into scope as bare function
calls. Named stubs (`orchestration`, `supplier`, etc.) live on the context, so `orchestration.sends(...)`
reads naturally in the report as a subject performing an action.

The mechanism: define a typed *test context* holding all stubs/services, expose helpers as
extension functions on *interface types* the context implements, and use `with(context)` in
test bodies. Each test only implements the mixins it actually needs.

```kotlin
// 1. Typed context — holds all stubs and services
class OrderTestContext(
    val orderService: OrderServiceStub,
    val paymentStub: PaymentStub,
    val notificationStub: NotificationStub,
) : WithPaymentScenario.Context, WithNotificationScenario.Context

// 2. Mixin interface — scenario helpers as extension functions on the context type
interface WithPaymentScenario {
    interface Context {
        val paymentStub: PaymentStub
    }

    fun Context.paymentWillSucceed() = paymentStub.returnSuccess()
    fun Context.paymentWillFail() = paymentStub.returnFailure()
}

// 3. Abstract base class — consolidates all class-level annotations
@ExtendWith(OrderExtension::class)
@UseSetupStrategy(SetupStrategy.Grouped)
@Sources(OrderFields::class, PaymentFields::class)
abstract class MyDomainTest : KensaTest, WithKotest

// 4. Concrete test — extends base, implements needed mixins, uses with(context)
class OrderCancellationTest : MyDomainTest(), WithPaymentScenario, WithNotificationScenario {

    private val context: OrderTestContext by lazy {
        with(OrderExtension) {
            OrderTestContext(orderServiceStub, paymentStub, notificationStub)
        }
    }

    @Test
    fun cancelsAnOrderWhenPaymentFails() = with(context) {
        given(paymentWillFail())
        whenever(orchestration.sends(aCancellationRequest()))
        thenEventually(theOrderStatus(), shouldBeCancelled())
    }
}
```

Flag any test that puts stub/service references directly in the superclass, or that duplicates
scenario helper logic across test classes, as a violation of this pattern.

---

## How to Review

1. Read the full file including imports, companion object, and all private functions.
2. Identify rendered contexts: test method bodies and `@ExpandableSentence` bodies.
3. Load any on-demand reference files relevant to what's in the test.
4. Check each best practice in order, noting specific line violations.
5. Produce a concise violation list before showing the improved code.
6. Rewrite the test applying all improvements.
7. Explain key changes briefly — focus on *why* each change improves the test.

If the user provides production service contracts (JSON/XML schemas or example payloads), use them
to make fixtures and request builders realistic and type-accurate.

## Review Output Format

```
## Violations Found

1. [BP-2] `aClientSubmitsAnLcApplicationFor` is @ExpandableSentence returning an Action — lambda body rendered when expanded
2. [BP-3] `Holder` carries test data (applicantId, expectedPrefix) — should be Fixtures
3. [BP-5] `shouldBeInstanceOf<LcApplicationResult.Approved>()` exposed in rendered then() block

## Improved Test

[full rewritten file]

## Key Changes

- [brief explanation of each change and why it matters]
```

---

## Kensa DSL Quick Reference

### Test structure

The preferred pattern is an abstract base class per domain that consolidates the extension,
setup strategy, and framework interfaces — keeping concrete test classes focused on tests only:

```kotlin
// Base class — defined once per domain
@ExtendWith(MyExtension::class)
@UseSetupStrategy(SetupStrategy.Grouped)
@Sources(MyDomainFields::class)
abstract class MyDomainTest : KensaTest, WithKotest

// Concrete test class — extends the base, nothing else needed
class MyFeatureTest : MyDomainTest() {
    @Test
    fun canDoSomething() {
        given(somePrerequisite())
        and(anotherPrerequisite())
        whenever(anActionOccurs())
        then(theResult(), shouldSucceed())
    }
}
```

### Synchronous vs asynchronous assertions

Use `then`/`and` for results that are immediately available after the action. Use `thenEventually`
and `andEventually` when the system under test processes asynchronously (message-driven, event-sourced,
parallel workers) — these retry the assertion until it passes or a timeout is reached.

`thenContinually` asserts that the condition remains true throughout a polling window — use when
you need to verify something *stays* in a given state rather than *eventually reaches* it.

```kotlin
// Synchronous — result available immediately
then(theHttpStatus(), shouldBe200())
and(theResponseBody(), shouldContainOrderId())

// Asynchronous with default timeout — prefer this when the default is sufficient
thenEventually(theOrderStatus(), shouldBePending())

// Stable state — must hold throughout the window
thenContinually(theCircuitBreakerState(), shouldBeClosed())
```

When a non-default timeout is needed, **never put the duration literal in the test body** — it reads as a plumbing detail. Push the whole call into a private function:

```kotlin
// Bad — raw duration exposed in rendered context
thenEventually(10.seconds, allNotifications(), shouldShowBothSuppliersCompleted())

// Good — duration hidden, test body stays fluent
thenEventuallyAllNotifications(shouldShowBothSuppliersCompleted())

private fun thenEventuallyAllNotifications(matcher: Matcher<List<Notification>>) =
    thenEventually(10.seconds, allNotifications(), matcher)
```

### Action functions
```kotlin
private fun somePrerequisite() = Action<GivensContext> { (fixtures) ->
    // setup using fixtures[myFixture]
}

private fun anActionOccurs() = Action<ActionContext> { (fixtures, interactions) ->
    holder.result = service.call(fixtures[myParam])
}
```

### State collectors
```kotlin
private fun theResult() = StateCollector { holder.result }
private fun theField() = StateCollector { fixtures[myFixture] }
```

### Fixtures
```kotlin
object MyFixtures : FixtureContainer {
    val MyValue = fixture("My Value") { "some-value" }
    val Derived = fixture("Derived Value", MyValue) { v -> buildThing(v) }
    // Up to 3 dependencies supported; type SecondaryFixture<T> for explicit typing:
    val Composite: SecondaryFixture<String> = fixture("Composite", PartA, PartB, PartC) { a, b, c -> "$a/$b/$c" }
}
```

Register in the extension companion:
```kotlin
companion object {
    init {
        registerFixtures(MyFixtures)
        registerCapturedOutputs(MyCapturedOutputs)
    }
}
```
