---
title: Assertion Bridges
sidebar_label: Assertion Bridges
sidebar_position: 5.4
description: Reference for the four assertion-library bridges (Kotest, Hamkrest, Hamcrest, AssertJ) - artifacts, what each then / thenEventually / thenContinually accepts, SetupStep flavours, and what each one brings onto the classpath.
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Assertion Bridges

`KensaTest` provides `given`, `whenever` and the rest of the authoring DSL, but it does not provide `then`. `then` comes from an assertion bridge: a small module that adapts one assertion library's matchers or assertions to Kensa's `StateCollector`, so that the assertion side of a test is written in the library you already use. Each bridge is a mixin interface (`WithKotest`, `WithHamkrest`, `WithHamcrest`, `WithAssertJ`) that you implement alongside `KensaTest`, and a Maven artifact that carries that library as a dependency.

All four share the same shape. `then(collector, ...)` runs the collector once against the test's `CollectorContext` and applies the assertion to the value; `and(...)` is the same call under a name that reads well when chained. The polling forms, [`thenEventually` and `thenContinually`](./async-assertions.md), re-run the collector and the assertion; their defaults are a 10-second window, a 25 ms interval and no initial delay. What differs between bridges is which argument forms exist, which polling forms exist, and what lands on your classpath. The [comparison table](#comparison) at the end has the summary.

Every bridge artifact is in the `kensa-bom`, so with the BOM imported you omit the version. The quickstarts show the BOM import for [Gradle](../quickstart/kotlin-quickstart.md#2-add-test-dependencies) and the same `kensa-bom` works as an imported `pom` in a Maven `dependencyManagement` block. A test class normally mixes in one bridge. Kotest and Hamkrest declare members with identical signatures (the block forms of `then`, `thenEventually` and `thenContinually`), so a class that mixes in both will not compile until it overrides each of them; Hamcrest or AssertJ alongside either is fine.

## Kotest

Artifact `dev.kensa:kensa-assertions-kotest`, package `dev.kensa.kotest`, mixin `WithKotest`.

<Tabs groupId="build-tool">
<TabItem value="gradle" label="Gradle (Kotlin DSL)">

```kotlin title="build.gradle.kts"
testImplementation("dev.kensa:kensa-assertions-kotest")
```

</TabItem>
<TabItem value="maven" label="Maven">

```xml title="pom.xml"
<dependency>
    <groupId>dev.kensa</groupId>
    <artifactId>kensa-assertions-kotest</artifactId>
    <scope>test</scope>
</dependency>
```

</TabItem>
</Tabs>

`then` and `and` take a collector plus either a Kotest `Matcher<T>` or an assertion block `T.() -> Unit` in which the collected value is `this`:

```kotlin
import dev.kensa.kotest.WithKotest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.startWith

class LoanDecisionTest : KensaTest, WithKotest {

    @Test
    fun `loan is approved`() {
        given(anApplicantWithGoodCredit())
        whenever(theLoanServiceProcessesTheApplication())
        then(theLoanResult()) { status shouldBe LoanStatus.Approved }
        and(theLoanReference(), startWith("LN-"))
    }

    private fun theLoanResult() = StateCollector { result }
    private fun theLoanReference() = StateCollector { result.reference }
}
```

Polling uses Kotest's own `eventually` and `continually` under a `runBlocking`, so the test method stays a plain function:

```kotlin
thenEventually(theLoanResult()) { status shouldBe LoanStatus.Approved }
thenContinually(2.seconds, theLoanReference(), startWith("LN-"))
```

Supported:

- `then` / `and` with a matcher, a block, or a `ThenSpec<T>`.
- `thenEventually` / `andEventually` with a matcher, a block, a `ThenSpec<T>`, or a multi-assertion `PollingScope` block. Timing is a leading `duration`, or the full `initialDelay` / `duration` / `interval` triple, all `kotlin.time.Duration`.
- `thenContinually` with a matcher, a block, a `ThenSpec<T>`, or a `PollingScope` block, with an optional leading `duration`. The single-collector block form requires the duration; the matcher form defaults it.
- `ThenSpec<T>` bundles a `collector`, a `matcher` and an optional `onMatch: CollectorContext.(T) -> Unit` that runs once, on the first successful match, so a spec can record an output or an interaction from the value it just matched.
- `PollingScope` offers `then` and `and` with the same matcher, block and spec forms, all polled in parallel inside one window. See [multiple assertions in one window](./async-assertions.md#multiple-assertions-in-one-window).
- Setup: `KotestSetupStep`, `KotestSetupScope` and the `kotestSetupStep { }` builder, whose block has the whole `WithKotest` surface in scope alongside `SetupScope`:

```kotlin
private fun anApprovedLoan() = kotestSetupStep {
    given { applicant = Applicant("Alice", creditScore = 750, amount = 10_000) }
    action { result = service.process(applicant) }
    then(theLoanResult()) { status shouldBe LoanStatus.Approved }
}
```

Not supported: calling it from Java. The block forms are Kotlin lambdas with receivers and the durations are `kotlin.time.Duration`, so `WithKotest` is a Kotlin-only mixin.

Classpath: `io.kotest:kotest-assertions-core-jvm` and `org.jetbrains.kotlinx:kotlinx-coroutines-core`, both as `api`, so they are on your compile classpath as well as at runtime. The coroutines dependency is what makes this bridge subject to the [coroutines floor](../support-matrix.md#coroutines-floor).

## Hamkrest

Artifact `dev.kensa:kensa-assertions-hamkrest`, package `dev.kensa.hamkrest`, mixin `WithHamkrest`.

<Tabs groupId="build-tool">
<TabItem value="gradle" label="Gradle (Kotlin DSL)">

```kotlin title="build.gradle.kts"
testImplementation("dev.kensa:kensa-assertions-hamkrest")
```

</TabItem>
<TabItem value="maven" label="Maven">

```xml title="pom.xml"
<dependency>
    <groupId>dev.kensa</groupId>
    <artifactId>kensa-assertions-hamkrest</artifactId>
    <scope>test</scope>
</dependency>
```

</TabItem>
</Tabs>

The surface is the same as the Kotest bridge with `com.natpryce.hamkrest.Matcher<T>` in place of Kotest's matcher. The matcher form calls Hamkrest's `assertThat(value, matcher)`; in the block form the collected value is `this` and you call `assertThat` yourself:

```kotlin
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.startsWith
import dev.kensa.hamkrest.WithHamkrest

class LoanDecisionTest : KensaTest, WithHamkrest {

    @Test
    fun `loan is approved`() {
        given(anApplicantWithGoodCredit())
        whenever(theLoanServiceProcessesTheApplication())
        then(theLoanStatus(), equalTo(LoanStatus.Approved))
        and(theLoanResult()) { assertThat(reference, startsWith("LN-")) }
    }

    @Test
    fun `loan is eventually approved`() {
        given(anApplicantWithGoodCredit())
        whenever(theLoanServiceProcessesTheApplication())
        thenEventually(theLoanStatus(), equalTo(LoanStatus.Approved))
        thenContinually(2.seconds, theLoanReference(), startsWith("LN-"))
    }
}
```

Supported:

- `then` / `and` with a matcher, a block, or a `ThenSpec<T>`.
- `thenEventually` / `andEventually` with a matcher, a block, a `ThenSpec<T>`, or a `PollingScope` block; timing is `duration` or the `initialDelay` / `duration` / `interval` triple as `kotlin.time.Duration`.
- `thenContinually` with a matcher, a block, a `ThenSpec<T>`, or a `PollingScope` block, with an optional leading `duration`. As with Kotest, the single-collector block form requires the duration.
- `ThenSpec<T>` and `PollingScope` behave as described for [Kotest](#kotest); the `onMatch` hook runs once on the first match.
- Setup: `HamkrestSetupStep`, `HamkrestSetupScope` and `hamkrestSetupStep { }`.

Not supported: calling it from Java, for the same reasons as the Kotest bridge.

Classpath: `com.natpryce:hamkrest` and `org.awaitility:awaitility-kotlin` as `api`. The single-collector `thenEventually` polls with Awaitility, on the test's own thread so that Kensa's thread-bound context stays in scope; the `PollingScope` forms run their checks on coroutines, which is why this bridge also carries `kotlinx-coroutines-core`, as an `implementation` dependency: on your runtime classpath, not your compile classpath, and subject to the [coroutines floor](../support-matrix.md#coroutines-floor).

## Hamcrest

Artifact `dev.kensa:kensa-assertions-hamcrest`, package `dev.kensa.hamcrest`, mixin `WithHamcrest`.

<Tabs groupId="build-tool">
<TabItem value="gradle" label="Gradle (Kotlin DSL)">

```kotlin title="build.gradle.kts"
testImplementation("dev.kensa:kensa-assertions-hamcrest")
```

</TabItem>
<TabItem value="maven" label="Maven">

```xml title="pom.xml"
<dependency>
    <groupId>dev.kensa</groupId>
    <artifactId>kensa-assertions-hamcrest</artifactId>
    <scope>test</scope>
</dependency>
```

</TabItem>
</Tabs>

This is the Java-oriented matcher bridge. Every method takes a collector and an `org.hamcrest.Matcher<? super T>`; there is no block form. Durations are `java.time.Duration`.

<Tabs groupId="lang">
<TabItem value="java" label="Java">

```java
import dev.kensa.hamcrest.WithHamcrest;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

class LoanDecisionTest implements KensaTest, WithHamcrest {

    @Test
    void loanIsApproved() {
        given(anApplicantWithGoodCredit());
        whenever(theLoanServiceProcessesTheApplication());
        then(theLoanStatus(), is(LoanStatus.Approved));
        and(theLoanReference(), startsWith("LN-"));
    }

    @Test
    void loanIsEventuallyApproved() {
        given(anApplicantWithGoodCredit());
        whenever(theLoanServiceProcessesTheApplication());
        thenEventually(theLoanStatus(), is(LoanStatus.Approved));
        thenContinually(Duration.ofSeconds(2), theLoanReference(), startsWith("LN-"));
    }

    private StateCollector<LoanStatus> theLoanStatus() {
        return ctx -> result.getStatus();
    }

    private StateCollector<String> theLoanReference() {
        return ctx -> result.getReference();
    }
}
```

</TabItem>
<TabItem value="kotlin" label="Kotlin">

```kotlin
import dev.kensa.hamcrest.WithHamcrest
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.startsWith

class LoanDecisionTest : KensaTest, WithHamcrest {

    @Test
    fun `loan is approved`() {
        given(anApplicantWithGoodCredit())
        whenever(theLoanServiceProcessesTheApplication())
        then(theLoanStatus(), `is`(LoanStatus.Approved))
        and(theLoanReference(), startsWith("LN-"))
    }

    private fun theLoanStatus() = StateCollector { result.status }
    private fun theLoanReference() = StateCollector { result.reference }
}
```

</TabItem>
</Tabs>

Supported:

- `then` / `and` with a matcher.
- `thenEventually` / `andEventually` with a matcher; timing is a leading `Duration`, or the `initialDelay` / `duration` / `interval` triple. Polls with Awaitility.
- `thenContinually` with a matcher and an optional leading `Duration`. The check repeats every 25 ms for the whole window; the interval is not configurable.
- Setup: `HamcrestSetupStep`, `HamcrestSetupScope` and, from Kotlin, `hamcrestSetupStep { }`. From Java, implement `HamcrestSetupStep` as an anonymous class or a named one; the builder's receiver lambda is not usable from Java.

Not supported: assertion blocks, `ThenSpec`, and the multi-assertion `PollingScope` forms of `thenEventually` and `thenContinually`.

Classpath: `org.hamcrest:hamcrest-core` (which depends on `org.hamcrest:hamcrest`, where `Matchers` lives) and `org.awaitility:awaitility-kotlin`, both as `api`. No coroutines dependency.

## AssertJ

Artifact `dev.kensa:kensa-assertions-assertj`, package `dev.kensa.assertj`, mixin `WithAssertJ`.

<Tabs groupId="build-tool">
<TabItem value="gradle" label="Gradle (Kotlin DSL)">

```kotlin title="build.gradle.kts"
testImplementation("dev.kensa:kensa-assertions-assertj")
```

</TabItem>
<TabItem value="maven" label="Maven">

```xml title="pom.xml"
<dependency>
    <groupId>dev.kensa</groupId>
    <artifactId>kensa-assertions-assertj</artifactId>
    <scope>test</scope>
</dependency>
```

</TabItem>
</Tabs>

AssertJ has no matcher type, so this bridge is shaped differently. `WithAssertJ` is a Java interface, and `then` has two forms:

- `then(collector, assertProvider)` takes a `java.util.function.Function<T, A>`. You write the assertion inside the function, and whatever it returns (normally the AssertJ assertion object) is returned to you, so you can keep chaining.
- `then(collector)` takes only the collector and returns an AssertJ assertion for the collected value. With a plain `StateCollector<T>` that is `ObjectAssert<T>`. To get a more specific assertion, declare the collector with one of the typed collector interfaces in `dev.kensa.assertj`: `StringStateCollector` gives a `StringAssert`, `IntegerStateCollector` an `IntegerAssert`, and so on.

`and` mirrors only the single-argument form.

<Tabs groupId="lang">
<TabItem value="java" label="Java">

```java
import dev.kensa.assertj.StringStateCollector;
import dev.kensa.assertj.WithAssertJ;

import static org.assertj.core.api.Assertions.assertThat;

class LoanDecisionTest implements KensaTest, WithAssertJ {

    @Test
    void loanIsApproved() {
        given(anApplicantWithGoodCredit());
        whenever(theLoanServiceProcessesTheApplication());
        then(theLoanResult(), r -> assertThat(r.getStatus()).isEqualTo(LoanStatus.Approved));
        and(theLoanReference()).startsWith("LN-");
    }

    @Test
    void loanIsEventuallyApproved() {
        given(anApplicantWithGoodCredit());
        whenever(theLoanServiceProcessesTheApplication());
        thenEventually(theLoanResult(), r -> assertThat(r.getStatus()).isEqualTo(LoanStatus.Approved));
        thenEventually(30L, ChronoUnit.SECONDS, theLoanReference(), ref -> assertThat(ref).startsWith("LN-"));
    }

    private StateCollector<LoanResult> theLoanResult() {
        return ctx -> result;
    }

    private StringStateCollector theLoanReference() {
        return ctx -> result.getReference();
    }
}
```

</TabItem>
<TabItem value="kotlin" label="Kotlin">

```kotlin
import dev.kensa.assertj.StringStateCollector
import dev.kensa.assertj.WithAssertJ
import org.assertj.core.api.Assertions.assertThat

class LoanDecisionTest : KensaTest, WithAssertJ {

    @Test
    fun `loan is approved`() {
        given(anApplicantWithGoodCredit())
        whenever(theLoanServiceProcessesTheApplication())
        then(theLoanResult()) { assertThat(it.status).isEqualTo(LoanStatus.Approved) }
        and(theLoanReference()).startsWith("LN-")
    }

    private fun theLoanResult() = StateCollector { result }
    private fun theLoanReference() = StringStateCollector { result.reference }
}
```

</TabItem>
</Tabs>

The typed collectors are `BigDecimal`, `BigInteger`, `Boolean`, `Byte`, `ByteArray`, `Character`, `CharArray`, `Class`, `Double`, `DoubleArray`, `File`, `Float`, `FloatArray`, `Instant`, `IntArray`, `Integer`, `LocalDate`, `LocalDateTime`, `LocalTime`, `Long`, `LongArray`, `ObjectArray<T>`, `OffsetDateTime`, `Optional<T>`, `OptionalDouble`, `OptionalInt`, `OptionalLong`, `Short`, `ShortArray`, `String`, `Throwable`, `Uri`, `Url` and `ZonedDateTime`, each named `<Type>StateCollector`.

Supported:

- `then` with a function, or with a collector alone; `and` with a collector alone.
- `thenEventually(collector, assertProvider)` with the default 10-second window, and `thenEventually(timeout, ChronoUnit, collector, assertProvider)` for a different one. The function must throw when the assertion fails (an AssertJ `assertThat(...)` chain does); Awaitility polls it until it stops throwing, then the function runs once more and its result is returned.

Not supported: `thenContinually`, `andEventually`, an initial delay or poll interval on `thenEventually`, and the multi-assertion `PollingScope` forms. There is no AssertJ `SetupStep` flavour either: a `SetupStep` that asserts with AssertJ uses `verify` or `verifyEventually` on [`SetupScope`](./setup-steps.md#setupscope) with a plain `assertThat` inside.

Classpath: `org.assertj:assertj-core` and `org.awaitility:awaitility-kotlin`, both as `api`. No coroutines dependency.

## Comparison

| Bridge | Artifact | `then` accepts | `thenEventually` | `thenContinually` | Java-friendly | Extra transitive deps |
|---|---|---|---|---|---|---|
| Kotest | `kensa-assertions-kotest` | `Matcher<T>`, block, `ThenSpec` | matcher, block, spec, `PollingScope`; `initialDelay` / `duration` / `interval` | matcher, block, spec, `PollingScope`; `duration` | No | `kotest-assertions-core-jvm`, `kotlinx-coroutines-core` |
| Hamkrest | `kensa-assertions-hamkrest` | `Matcher<T>`, block, `ThenSpec` | matcher, block, spec, `PollingScope`; `initialDelay` / `duration` / `interval` | matcher, block, spec, `PollingScope`; `duration` | No | `hamkrest`, `awaitility-kotlin`, `kotlinx-coroutines-core` (runtime only) |
| Hamcrest | `kensa-assertions-hamcrest` | `Matcher<? super T>` | matcher; `initialDelay` / `duration` / `interval` | matcher; `duration` | Yes | `hamcrest-core`, `awaitility-kotlin` |
| AssertJ | `kensa-assertions-assertj` | `Function<T, A>`, or collector alone | function; `timeout` + `ChronoUnit` | Not provided | Yes | `assertj-core`, `awaitility-kotlin` |

Every bridge also provides `and` for the single-shot forms and, except AssertJ, a `SetupStep` flavour (`KotestSetupStep`, `HamkrestSetupStep`, `HamcrestSetupStep`) with a matching `xxxSetupStep { }` builder. `andEventually` exists on Kotest, Hamkrest and Hamcrest; there is no `andContinually` anywhere.

The library versions each bridge is built against come from `gradle/libs.versions.toml` in the Kensa repository: Kotest 6.2.1, Hamkrest 1.8.0.1, Hamcrest 3.0, AssertJ 3.27.7, Awaitility 4.3.0 and kotlinx-coroutines 1.11.0. Older minors of the same major usually work but are not verified.

## Stability

The [Stability and Compatibility](../stability-and-compatibility.md) page lists the authoring DSL, `given` / `and` / `whenever` / `then` together with `Action`, `SetupStep` and `StateCollector`, as part of the stable surface governed by semantic versioning from 1.0.0. The bridges are where `then` and its polling variants come from, and no declaration in the four bridge modules carries `@KensaInternalApi` or `@KensaExperimental`. The assertion libraries themselves are not Kensa's to version: a bridge follows the matcher library's own compatibility, and a bump of that library is recorded on the [support matrix](../support-matrix.md).

## Related

- [Async Assertions](./async-assertions.md) for the semantics of `thenEventually` and `thenContinually`, multi-assertion windows, negative assertions and thread-local propagation into polling checks.
- [Setup Steps](./setup-steps.md) for the `SetupStep` flavours and the `SetupScope` API.
- [Field Assertion DSL](../field-assertion-dsl/overview.mdx) for the `kensa-kotest-test-support` and `kensa-hamkrest-test-support` matcher libraries that build on the Kotest and Hamkrest bridges.
