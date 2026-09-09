---
title: Reusable Setup with SetupStep
sidebar_label: Setup Steps
sidebar_position: 4.8
description: "SetupStep: reusable, named test setup with the givens/actions/verify triple and the imperative setup(scope) API."
---

# Setup Steps

`given(action)` is fine for a one-off block. When the same setup is needed across several tests (open an account, seed a queue, wait for a downstream system to settle) write it once as a `SetupStep` and register it wherever it's needed.

A `SetupStep` can do more than a plain given block: it can act on the system, wait for state, and verify the result, all before `whenever` runs. Interactions recorded before `whenever` runs are captured as setup, not as behaviour under test, and by default (`SetupStrategy.Ungrouped`) they still render inline in the sequence diagram alongside everything else. `SetupStrategy.Grouped` collects them into a labelled "Setup" box instead, `Ignored` drops them from the diagram entirely, and `withSetupStrategy` (global configuration) or `@UseSetupStrategy` (per test or class) picks between the three.

## The triple

The simplest `SetupStep` overrides three methods:

| Method | Builder | Runs against |
|---|---|---|
| `givens()` | `buildGivens { }` | `GivensContext` |
| `actions()` | `buildActions { add(...) }` | `ActionContext` |
| `verify()` | `verify { }` | `CollectorContext` |

```kotlin
private fun anAccountIsOpened() = object : HamkrestSetupStep {
    override fun givens() = buildGivens { }

    override fun actions() = buildActions {
        add { account = "opened-account" }
    }

    override fun verify() = verify {
        assertThat(account, equalTo("opened-account"))
    }
}
```

The block passed to `buildGivens` and `buildActions` is a builder: it runs at build time, before any action executes. A value read from `outputs` inside that block reads pre-execution state, not whatever the step's own actions are about to produce. If you need to read captured outputs as part of a step, use `setup(scope)` below, or split the step into two.

## `setup(scope)`

For anything that doesn't fit the triple, such as reading outputs mid-step, polling for a condition, or interleaving multiple actions, override `setup` directly against `SetupScope`:

| Member | Type | Semantics |
|---|---|---|
| `fixtures` | `Fixtures` | Same fixtures instance as the rest of the test |
| `outputs` | `CapturedOutputs` | Same captured outputs instance as the rest of the test |
| `given(action)` | `Action<GivensContext>` | Executes immediately |
| `action(action)` | `Action<ActionContext>` | Executes immediately |
| `collect(collector)` | `StateCollector<T>` | Executes immediately, returns the collected value |
| `verify(block)` | `(CollectorContext) -> Unit` | Executes immediately, once |
| `verifyEventually(duration, interval, check)` | n/a | Retries `check` on the calling thread until it stops throwing, or rethrows the last error once `duration` elapses. Defaults: 10 seconds, 25 milliseconds |

`verifyEventually` relates to `verify` the way `thenEventually` relates to `then`: `verify` runs its block once, `verifyEventually` polls the same shape of check until it passes or the deadline is reached. From Java, call the overload that takes two `java.time.Duration` values and an `Action<CollectorContext>`.

```kotlin
private fun theBalanceSettles() = object : HamkrestSetupStep {
    override fun setup(scope: SetupScope) = with(scope) {
        action {
            Thread {
                Thread.sleep(50)
                settled = "settled"
            }.apply { isDaemon = true }.start()
        }

        verifyEventually(5.seconds) {
            if (settled == null) throw AssertionError("balance not settled yet")
        }

        val seen = collect(StateCollector { settled!! })

        action { account = seen.replace("settled", "settled-account") }
    }
}
```

The default `setup` body, the one that runs when you override `givens`/`actions`/`verify` instead, is written entirely in terms of this same scope. Overriding it explicitly with exactly those three lines behaves identically to leaving `setup` alone and overriding the triple:

```kotlin
override fun setup(scope: SetupScope) {
    scope.given { givens().buildWith(it).executeWith(it) }
    scope.action { actions().buildWith(it).executeWith(it) }
    scope.verify { verify().verifyWith(it) }
}
```

So the triple is sugar over `setup(scope)`, not a separate mechanism, and overriding one form or the other affects only that step. `thenEventually` from your assertion mixin also works inside `setup`, polling on the same terms it does anywhere else in the test.

## Registering steps

`given` accepts a single step:

```kotlin
given(anAccountIsOpened())
```

or a chain built with `and`:

```kotlin
given(anAccountIsOpened().and(theAccountIsFunded()))
```

`SetupStep.and(other)` builds a `SetupSteps` chain that runs both steps in order under a single `given`. The test-level `and(step)` is a separate call, registering another step as its own `given` right after:

```kotlin
given(anAccountIsOpened())
and(theAccountIsFunded())
```

Both forms run the same two steps in the same order; the chain form groups them as one `given` call, the test-level form as two.

Each assertion mixin has a matching `SetupStep` flavour so a step declared with it also gets `then`/`thenEventually`:

| Flavour | Interface |
|---|---|
| Kotest | `KotestSetupStep` |
| Hamkrest | `HamkrestSetupStep` |
| Hamcrest | `HamcrestSetupStep` |

## Migrating from `withTestContext`

`TestContextUtil.withTestContext` is deprecated and marked `@KensaInternalApi`. `SetupStep` is its replacement:

| `withTestContext` | inside `override fun setup(scope: SetupScope) = with(scope) { ... }` |
|---|---|
| `withTestContext { execute(action) }` | `action(action)` |
| `withTestContext { execute(collector) }` | `collect(collector)` |

## Why there's no givens-to-`ActionContext` bridge

There's no way to run an `ActionContext` action from inside `given(action)` itself, only from `setup(scope)` or the `actions()` triple slot. It isn't a grouping concern: an interaction stays marked as setup until `whenever` runs, no matter which context recorded it, so nothing here could land in the diagram as under test. The reason is structural instead: `GivensContext` carries only `fixtures` and `outputs`, not `interactions`, so a `given` action has nothing to record an interaction against or use to drive the system under test. `setup(scope)`'s `action` already runs against the step's own `ActionContext` at the right point in the lifecycle, so a bridge from `given` would only duplicate what it already provides.
