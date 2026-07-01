---
sidebar_position: 4
description: Parameterised fixture factory functions — call a @Fixture-annotated factory inline in a test with a parameterised-test parameter, and Kensa renders the seeded, argument-derived value as a fixture token.
---

# Factory Fixtures

A **factory fixture** is a parameterised [fixture](./fixtures) declared as a function rather than a `val`. You annotate the function with `@Fixture("Key")` and call it inline in a test — typically with a parameterised-test parameter — and Kensa renders the **seeded, argument-derived value** as a fixture token in the report.

Use a factory fixture when a fixture's construction must vary **per call** (for example, per parameterised-test argument) without declaring a separate `val` for every variant.

---

## Defining a factory fixture

Declare a function inside a `FixtureContainer`, annotate it with `@Fixture("Key")`, and build the value with the no-name `fixture { }` overload — the key is taken from the annotation, so you do not repeat it:

```kotlin
object MyFixtures : FixtureContainer {
    @Fixture("MyFixture")
    fun myFixture(param: String) = fixture { if (param == "Meh") "Bah" else "Yay" }
}
```

## Calling it in a test

Call the factory inline through the `fixtures[…]` accessor — the same way you read any other fixture. The driving use case is a **parameterised-test parameter**, and you can use several distinct parameters in one test:

```kotlin
class GreetingTest : KensaTest, WithHamkrest {

    init { registerFixtures(MyFixtures) }

    @ParameterizedTest
    @CsvSource("Meh,Wow")
    fun rendersTheSeededValues(p1: String, p2: String) {
        then(theGreeting(p1), equalTo(fixtures[myFixture(p1)]))   // renders "Bah"
        then(theGreeting(p2), equalTo(fixtures[myFixture(p2)]))   // renders "Yay"
    }
}
```

`fixtures[myFixture(p1)]` resolves the fixture, seeding its value under the `(key, args)` identity for this invocation, and returns that value. The rendered sentence shows the seeded value (e.g. `Bah`) as a fixture token — not the call text.

---

## Identity — a memoized singleton per `(key, args)`

Each distinct argument set is a **distinct, cached** fixture:

- `myFixture("a")` and `myFixture("b")` are two different fixtures.
- A repeated `myFixture("a")` returns the **same cached value** within the invocation.

Identity is by **value**, not call order, so multiple factory calls in a single test invocation never collide. The seed side (the factory's parameter value) and the render side (the resolved call-site argument) compute the **same** identity because the argument *is* what is passed to the factory.

The value is stored under a composite key of the form `Key(arg)` (for example `MyFixture(Meh)`), so distinct arguments appear as distinct entries in the fixtures view and in `fixtures["MyFixture(Meh)"]` interpolation.

---

## Compiler plugin requirement

Factory fixtures depend on the **Kensa compiler plugin** being applied to the source set that declares them. At compile time the plugin rewrites the no-name `fixture { }` inside a `@Fixture("Key")` function to inject the key and the factory's value parameters as the identity discriminator.

If the plugin is **not** applied, the no-name `fixture { }` fails loud at runtime rather than mis-rendering silently:

```
fixture { } without a key requires the Kensa compiler plugin on this source set …
```

Apply the plugin (the same one Kensa uses for `@RenderedValue` and `@ExpandableSentence`) via the [Gradle plugin](../build-plugins/gradle-plugin.md) or [Maven plugin](../build-plugins/maven-plugin.md) — both apply it to your configured source sets automatically. Factory fixtures need a bundled `kensa-core` ≥ 0.8.10 (Gradle/Maven plugin ≥ 0.9.8). Alternatively use the keyed `fixture("Key") { }` overload directly if you do not need the factory-function ergonomics.

---

## Scope and limits

- **Resolvable arguments.** The renderer can resolve a call-site argument only as far as Kensa already substitutes it: a **parameter reference**, a **captured field/variable**, or a **string/number literal**. Arbitrary computed expressions — including a plain local `val` — cannot be reconstructed from source. When an argument cannot be resolved the **assertion still passes** (it runs against the real value), but the fixture token in the report renders as `null` rather than the seeded value. Prefer a parameterised-test parameter or a literal as the factory argument.
- **Sane `equals`/`toString`.** Identity is by value, keyed on each argument's `toString()`, so arguments need stable `equals`/`toString`.
- **Kotlin body parser.** Inline factory-call recognition is provided for the Kotlin test-body parser.

---

See also: [Fixtures](./fixtures) for `val`-based primary, secondary, and parameter-derived fixtures.
