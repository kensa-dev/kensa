---
sidebar_position: 3
description: How to define and use Kensa Fixtures — type-safe, lazily-created test data shared across given, whenever, and then steps, with support for dependent fixtures and highlighted values.
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Fixtures

Kensa Fixtures are collections of type-safe, lazily-created test data values. Each test invocation has its own discrete set of Fixtures. They are shared across `given`, `whenever`, and `then` steps via the context objects.

:::tip[Full example]
The code on this page is taken from [clearwave-example](https://github.com/kensa-dev/clearwave-example) — a complete working project that demonstrates fixtures, captured outputs, sequence diagrams, and async assertions.
:::

---

## Defining Fixtures

Fixtures must be defined inside a `FixtureContainer` object. This can be an instance of a Java class or a Kotlin object.
They carry a string key which must be unique — and a factory that creates the value.

Fixtures can depend on up to three other fixtures; the factory for dependent fixtures will receive the resolved parent values at creation time.

It is best to define fixtures as static/public properties, which you can then import by name in your tests.

### Primary Fixtures

A primary fixture has no dependencies — its factory takes no arguments.

The key design principle is **granularity**: define one fixture per meaningful field rather than one fixture per domain object. This lets each field appear by name in the rendered report wherever it is referenced in the test body.

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/kotlin/com/clearwave/support/TelecomsFixtures.kt#L12-L65
```

</TabItem>
<TabItem value="java" label="Java">

Java tests access fixtures via static aliases that delegate to the `FixtureContainer` singleton. Import them statically for idiomatic `SCREAMING_SNAKE_CASE` usage in test code.

```java reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/java/com/clearwave/support/JavaTelecomsFixtures.java#L14-L47
```

</TabItem>
</Tabs>

### Secondary Fixtures

A secondary fixture depends on one or more parent fixtures. Its factory receives the resolved parent values.

**2 parents:**

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/kotlin/com/clearwave/support/TelecomsFixtures.kt#L66-L68
```

</TabItem>
<TabItem value="java" label="Java">

```java
createFixture("Appointment Slot", appointmentDate, appointmentTimeSlot,
    (date, slot) -> new AppointmentSlot(date, slot))
```

</TabItem>
</Tabs>

**3 parents — composite object built from individual field fixtures:**

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/kotlin/com/clearwave/support/TelecomsFixtures.kt#L47-L49
```

</TabItem>
<TabItem value="java" label="Java">

```java
createFixture("Voice Profile", voiceDownloadSpeed, voiceUploadSpeed, voiceSupplier,
    (dl, ul, sup) -> new LineProfile("FTTP", dl, ul, "Full Fibre 900 with Voice", sup))
```

</TabItem>
</Tabs>

**More than 3 dependencies** — construct a `SecondaryFixture` directly. Its factory lambda receives the full `Fixtures` map, so it can read any number of fixtures; `Parents` declares at most three of them for dependency tracking:

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/kotlin/com/clearwave/support/TelecomsFixtures.kt#L28-L39
```

</TabItem>
<TabItem value="java" label="Java">

```java
new SecondaryFixture<ServiceAddress>(
    "Service Address",
    fixtures -> new ServiceAddress(
        fixtures.get(postcode), fixtures.get(addressLine1),
        fixtures.get(town), fixtures.get(county)
    ),
    new Parents.Three<>(postcode, addressLine1, town),
    false
)
```

</TabItem>
</Tabs>

### Parameter-Derived Fixtures

A parameter fixture derives its value **per invocation** from a named parameter of a parameterised test. Like every fixture it is registered by name, so it still resolves in `fixtures[…]` interpolation and renders by its display name — but instead of a fixed factory it is bound to a test parameter (by name) and a transform. Kensa seeds it from the invocation's arguments *before the test body runs*, so the value is available both in the test body and in the rendered sentence.

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
object GreetingFixtures : FixtureContainer {
    // bound to the `userName` parameter; the transform runs once per invocation
    val Greeting = parameterFixture("Greeting", from = "userName") { name: String ->
        "Hello, ${name.replaceFirstChar(Char::uppercase)}"
    }
}

@ParameterizedTest
@ValueSource(strings = ["alice", "bob"])
fun `greets the user`(userName: String) {
    then(theBanner(), equalTo(fixtures[Greeting]))   // "Hello, Alice" / "Hello, Bob"
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
public class GreetingFixtures implements FixtureContainer {
    public static final ParameterFixture<String> GREETING =
        createParameterFixture("Greeting", "userName", (String name) -> "Hello, " + name);
}

@ParameterizedTest
@ValueSource(strings = {"alice", "bob"})
void greetsTheUser(String userName) {
    then(theBanner(), is(fixtures(GREETING)));   // "Hello, alice" / "Hello, bob"
}
```

</TabItem>
</Tabs>

The `from` value must match the test method's parameter name. The fixture is seeded only for invocations that actually supply that parameter; reading it in any other context raises a clear error. Secondary fixtures may depend on a parameter fixture — they resolve the seeded value like any other parent.

:::note
A parameter fixture derives from a single parameter. Reach for one when a value must be **both** computed from a test argument **and** referenced by name in the report; for a value you only need inside the test, use the parameter directly or a [captured output](./outputs).
:::

:::tip[Factory fixtures]
To vary a fixture's construction **per call** — for example calling a factory inline with a parameterised-test parameter, `matches(myFixture(p1))` — use a [factory fixture](./factory-fixtures) instead.
:::

---

### Co-locating fixtures with a test

A `FixtureContainer` need not be a separate object — a test class's own `companion object` can implement it, keeping test-local fixtures next to the test that uses them. Register it in the test's `init` block:

```kotlin
import dev.kensa.fixture.FixtureRegistry.registerFixtures

class OrderPricingTest : KensaTest, WithHamkrest {

    init {
        registerFixtures(Companion)
    }

    @Test
    fun rendersFixturesDeclaredInTheCompanion() {
        then(theGreeting(), equalTo(fixtures[Greeting]))
    }

    private fun theGreeting() = StateCollector { "Hello ${fixtures[FirstName]}" }

    companion object : FixtureContainer {
        val FirstName = fixture("FirstName") { "Jane" }
        val Greeting  = fixture("Greeting", FirstName) { name -> "Hello $name" }
    }
}
```

Because the companion is a singleton, re-running `init` on each per-method test instance re-registers the same instances harmlessly — registration is idempotent for a given fixture. The `FixtureRegistry` is global for the test run and is **not** reset between tests, so keep fixture **keys and names unique across the suite**; co-locating them per test makes that easy. Registering from a shared JUnit extension (alongside `Kensa.konfigure { }`) works identically — call `registerFixtures(...)` there instead of in each `init`.

## Using Fixtures in Tests

### In `given` and `whenever` actions

Access fixtures through the context destructured in each action lambda:

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/kotlin/com/clearwave/FeasibilityServiceTest.kt#L104-L113
```

```kotlin reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/kotlin/com/clearwave/OrderServiceTest.kt#L125-L137
```

</TabItem>
<TabItem value="java" label="Java">

```java reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/java/com/clearwave/FeasibilityServiceJavaTest.java#L100-L111
```

```java reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/java/com/clearwave/OrderServiceJavaTest.java#L113-L157
```

</TabItem>
</Tabs>

### In the test body — the key rendering pattern

The most important place to use fixture references is **directly in the test body**, passed as named arguments into assertion helpers. Kensa parses the test source code and when it sees `fixtures[voiceDownloadSpeed]` (Kotlin) or `fixtures(VOICE_DOWNLOAD_SPEED)` (Java) in the sentence it substitutes the fixture's display name — *Voice Download Speed* — rather than the raw value `900`. This makes reports self-documenting.

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/kotlin/com/clearwave/FeasibilityServiceTest.kt#L51-L62
```

```kotlin reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/kotlin/com/clearwave/OrderServiceTest.kt#L65-L77
```

</TabItem>
<TabItem value="java" label="Java">

```java reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/java/com/clearwave/FeasibilityServiceJavaTest.java#L48-L58
```

```java reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/java/com/clearwave/OrderServiceJavaTest.java#L54-L65
```

</TabItem>
</Tabs>

The assertion helpers simply accept the fixture values as ordinary parameters — they have no special knowledge of Kensa:

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/kotlin/com/clearwave/FeasibilityServiceTest.kt#L172-L184
```

```kotlin reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/kotlin/com/clearwave/OrderServiceTest.kt#L194-L206
```

</TabItem>
<TabItem value="java" label="Java">

```java reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/java/com/clearwave/FeasibilityServiceJavaTest.java#L178-L197
```

```java reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/java/com/clearwave/OrderServiceJavaTest.java#L190-L211
```

</TabItem>
</Tabs>

---

## Grouping Fixtures with `WithFixturesSuite`

When many tests share a large `FixtureContainer`, importing every fixture individually creates noise. `WithFixturesSuite` lets you declare one shared interface per container; test classes implement that interface and gain scoped, IDE-assisted block access to all its fixtures.

:::note
`WithFixturesSuite` is a Kotlin-only feature. Java tests access fixtures via static imports directly.
:::

**Without `WithFixturesSuite`** — each test file imports fixtures individually:

```kotlin
import dev.kensa.fixture.TelecomsFixtures.AccountNumber
import dev.kensa.fixture.TelecomsFixtures.LineProfile
import dev.kensa.fixture.TelecomsFixtures.AppointmentSlot
// ... one import per fixture

class FeasibilityServiceTest : KensaTest, WithHamkrest {
    @Test
    fun `can check feasibility`() {
        then(theAccountNumber(), equalTo(fixtures[AccountNumber]))
    }
}
```

**With `WithFixturesSuite`** — one shared interface, block syntax in tests:

Step 1 — declare the interface once (e.g. in a shared support file):

```kotlin
interface WithTelecomsFixtures : WithFixturesSuite<TelecomsFixtures> {
    override val fixturesObject get() = TelecomsFixtures
}
```

Step 2 — implement it in test classes; access fixtures via a block lambda:

```kotlin
class FeasibilityServiceTest : KensaTest, WithTelecomsFixtures, WithHamkrest {
    @Test
    fun `can check feasibility`() {
        then(theAccountNumber(), equalTo(fixtures { AccountNumber }))
    }

    private fun theAccountNumber() = StateCollector { fixtures { AccountNumber } }
}
```

The block lambda (`fixtures { AccountNumber }`) is scoped to the `FixtureContainer` type, so the IDE offers autocomplete over exactly those fixtures — nothing more.

### `WithFixturesSuite` API

| Member | Description |
|--------|-------------|
| `val fixturesObject: F` | Override to return the `FixtureContainer` singleton |
| `fun <T> fixtures(block: F.() -> Fixture<T>): T` | Retrieves (lazily creates) a fixture via a scoped block |

---

## Fixture API Reference

### `fixture()` factory (Kotlin)

```kotlin
// Primary — no dependencies
fun <T> fixture(key: String, highlighted: Boolean = false, factory: () -> T): PrimaryFixture<T>

// Secondary — 1 parent
fun <T, P1> fixture(key: String, parent: Fixture<P1>, highlighted: Boolean = false, factory: (P1) -> T): SecondaryFixture<T>

// Secondary — 2 parents
fun <T, P1, P2> fixture(key: String, parent1: Fixture<P1>, parent2: Fixture<P2>, highlighted: Boolean = false, factory: (P1, P2) -> T): SecondaryFixture<T>

// Secondary — 3 parents
fun <T, P1, P2, P3> fixture(key: String, parent1: Fixture<P1>, parent2: Fixture<P2>, parent3: Fixture<P3>, highlighted: Boolean = false, factory: (P1, P2, P3) -> T): SecondaryFixture<T>

// Parameter-derived — bound to a test parameter by name
fun <T, P> parameterFixture(key: String, from: String, highlighted: Boolean = false, transform: (P) -> T): ParameterFixture<T>
```

### `createFixture()` factory (Java)

```java
createFixture(String key, Supplier<T> factory)
createFixture(String key, boolean highlighted, Supplier<T> factory)
createFixture(String key, Fixture<P1> parent, Function<P1, T> factory)
createFixture(String key, Fixture<P1> parent, boolean highlighted, Function<P1, T> factory)
createFixture(String key, Fixture<P1> parent1, Fixture<P2> parent2, BiFunction<P1, P2, T> factory)
createFixture(String key, Fixture<P1> parent1, Fixture<P2> parent2, boolean highlighted, BiFunction<P1, P2, T> factory)
createFixture(String key, Fixture<P1> parent1, Fixture<P2> parent2, Fixture<P3> parent3, Function3<P1, P2, P3, T> factory)
createFixture(String key, Fixture<P1> parent1, Fixture<P2> parent2, Fixture<P3> parent3, boolean highlighted, Function3<P1, P2, P3, T> factory)

// Parameter-derived — bound to a test parameter by name
createParameterFixture(String key, String from, Function<P, T> transform)
createParameterFixture(String key, String from, boolean highlighted, Function<P, T> transform)
```

### `Fixtures` map

| Method / operator | Description |
|-------------------|-------------|
| `fixtures[fixture]` | Get (and lazily create) the fixture value |
| `fixtures.values()` | All fixture values as `List<NamedValue>` |
| `fixtures.highlightedValues()` | Only highlighted fixture values |

---

## Highlighting

Set `highlighted = true` on any fixture to have its value appear prominently in the report. This is useful for correlation IDs and other values that should stand out across all interactions.

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin reference
https://github.com/kensa-dev/clearwave-example/blob/master/src/test/kotlin/com/clearwave/support/TelecomsFixtures.kt#L14-L15
```

</TabItem>
<TabItem value="java" label="Java">

```java
createFixture("Tracking Id", /* highlighted = */ true, TrackingId::new)
```

</TabItem>
</Tabs>

Highlighted values are also accessible separately via `fixtures.highlightedValues()`, which Kensa uses to render them at the top of the report.
