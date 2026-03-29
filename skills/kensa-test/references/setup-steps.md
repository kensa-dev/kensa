# Kensa SetupSteps Reference

## Why SetupSteps?

`SetupSteps` drive the application under test to a required state **before** the actual action
under test (the `whenever` call) begins. A step encapsulates all three phases needed to reach
that state: preconditions (`givens`), driving actions (`actions`), and verification that the
state was actually reached (`verify`). If any action fails or a `verify` assertion fails, the
step fails and the test stops immediately.

Typical use: driving an order or document through lifecycle stages as a prerequisite. Steps are
usually collected in their own class with an entry-point function like:

```kotlin
fun theOrderHasProgressedTo(state: OrderState): SetupStep = ...
```

## The SetupStep Interface

```kotlin
interface SetupStep {
    fun givens(): GivensBlockBuilder   // preconditions — same as Action<GivensContext>
    fun actions(): ActionBlockBuilder  // drive the app — same as Action<ActionContext>
    fun verify(): VerificationBlockBuilder  // assert the state was reached
}
```

All three methods have default no-op implementations — only override what the step needs.

When using Kotest assertions, implement `KotestSetupStep` instead of `SetupStep` directly:

```kotlin
interface KotestSetupStep : SetupStep, WithKotest
```

## Defining a SetupStep

Steps are defined as anonymous objects returned by private factory functions:

```kotlin
private fun anIssuedLc() = object : KotestSetupStep {
    override fun givens() = buildGivens {
        add(Action<GivensContext> {
            complianceStub.willClear()
            creditStub.willApprove()
        })
    }

    override fun actions() = buildActions {
        add(Action<ActionContext> { (fixtures, interactions) ->
            complianceStub.prepareFor(interactions)
            issuanceStub.prepareFor(interactions)
            val result = tradePortal.submitLetterOfCredit(fixtures[originalRequest])
            holder.issuedLcNumber = (result as LcApplicationResult.Approved).lcNumber
        })
    }

    override fun verify() = verify {
        // LC issued — holder.issuedLcNumber is now populated
    }
}
```

## Chaining Steps with `and`

Steps are combined using `and`, which produces a `SetupSteps` sequence:

```kotlin
given(anIssuedLc().and(aBeneficiaryAmendmentConsent()))
```

Multiple steps execute in order. A failure in any step's `actions` or `verify` block halts the
chain.

## Passing SetupSteps to `given`

`KensaTest.given` accepts either a single `SetupStep` or a `SetupSteps` chain:

```kotlin
given(anIssuedLc())                              // single step
given(anIssuedLc().and(aBeneficiaryAmendmentConsent()))  // chained
```

## The Toolbox Pattern

With careful design, `SetupStep` classes become a reusable **toolbox** of composable building
blocks shared across many tests. This is the idiomatic Kensa approach and should be actively
encouraged.

Steps have full access to `fixtures` and `outputs` (via the `ActionContext` / `GivensContext`
destructuring), and are typically constructed with references to the stubs/services they need:

```kotlin
class OrderSetupSteps(
    private val orderService: OrderService,
    private val paymentStub: PaymentStub,
    private val holder: Holder,
) {
    fun theOrderHasProgressedTo(state: OrderState) = object : KotestSetupStep {
        override fun givens() = buildGivens {
            add(Action<GivensContext> {
                paymentStub.willAuthorise()
            })
        }

        override fun actions() = buildActions {
            add(Action<ActionContext> { (fixtures, interactions) ->
                paymentStub.prepareFor(interactions)
                holder.orderId = orderService.advance(fixtures[orderRequest], state).id
            })
        }

        override fun verify() = verify {
            holder.orderId shouldNotBe null
        }
    }

    fun anOrderWithApprovedPayment() = object : KotestSetupStep {
        override fun givens() = buildGivens {
            add(Action<GivensContext> { paymentStub.willAuthorise() })
        }
        override fun actions() = buildActions {
            add(Action<ActionContext> { (fixtures, interactions) ->
                paymentStub.prepareFor(interactions)
                holder.orderId = orderService.create(fixtures[orderRequest]).id
            })
        }
        override fun verify() = verify { holder.orderId shouldNotBe null }
    }
}
```

Tests compose from the toolbox — each test reads clearly because it names *what* state it needs,
not *how* to reach it. Crucially, **the toolbox must be reachable without a qualifier prefix** in
the rendered test body, so `given(theOrderHasProgressedTo(OrderState.Dispatched))` reads as fluent
English rather than `given(steps.theOrderHasProgressedTo(...))` which breaks the sentence.

The idiomatic way to achieve this is via the **context mixin pattern** (see BP-6 in SKILL.md):
the `SetupSteps` class is held on the typed test context, and its entry-point functions are
re-exposed as bare extension functions on the context interface. The test calls them unqualified
inside `with(context) { ... }`:

```kotlin
// Context mixin exposes the step unqualified:
interface WithOrderScenario {
    interface Context {
        val orderSteps: OrderSetupSteps
    }
    fun Context.theOrderHasProgressedTo(state: OrderState) = orderSteps.theOrderHasProgressedTo(state)
}

// Test body — reads as fluent prose:
with(context) {
    given(theOrderHasProgressedTo(OrderState.Dispatched))
    given(anOrderWithApprovedPayment().and(aShipmentConfirmed()))
}
```

### Where to instantiate the toolbox

The `SetupSteps` instance lives on the typed test context object, with stubs injected from
the shared extension companion:

```kotlin
class OrderTestContext(
    val orderService: OrderService,
    val paymentStub: PaymentStub,
    val orderSteps: OrderSetupSteps = OrderSetupSteps(orderService, paymentStub),
) : WithOrderScenario.Context
```

Givens/actions/assertions defined across the toolbox class can also be extracted into standalone
`Action<GivensContext>` or `Action<ActionContext>` functions and shared directly with test classes
that need them — the same composability applies at every level.

## SetupStrategy: Controlling Diagram Display

Setup-phase interactions appear in the sequence diagram. `@UseSetupStrategy` controls how:

```kotlin
@UseSetupStrategy(SetupStrategy.Grouped)   // setup interactions in a labelled box
@UseSetupStrategy(SetupStrategy.Ungrouped) // setup interactions inline, no box
@UseSetupStrategy(SetupStrategy.Ignored)   // setup interactions hidden
```

Apply at class or method level. `Grouped` is recommended — it clearly separates prerequisites
from the action under test in the diagram.
