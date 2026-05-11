---
sidebar_position: 1
description: Step-by-step guide to adding Kensa to a Kotlin project with JUnit 5, writing your first Given-When-Then test, and viewing the generated HTML report.
---

# Quickstart — Kotlin & JUnit 5

This guide walks through setting up Kensa in a Kotlin project with JUnit 5 and writing your first test.

## 1. Apply the Kensa Gradle Plugin

The Kensa Gradle plugin wires the Kotlin compiler plugin (so `@RenderedValue` and `@ExpandableSentence` capture values) and pulls the right `kensa-core` variant onto your compilation. Apply it alongside Kotlin:

```kotlin title="build.gradle.kts"
plugins {
    kotlin("jvm") version "2.3.21"           // minimum Kotlin enforced by the plugin
    id("dev.kensa.gradle-plugin") version "<plugin-version>"
}

repositories { mavenCentral() }
```

The plugin and `kensa-core` version independently — see the [compatibility matrix](../build-plugins/gradle-plugin.md#kensa-core-compatibility) for the supported pairings.

## 2. Add Test Dependencies

Pick a JUnit variant and one or more assertions bridges. The `kensa-bom` lines up versions across the framework and assertions artifacts so you don't have to repeat them:

```kotlin title="build.gradle.kts"
dependencies {
    testImplementation(platform("dev.kensa:kensa-bom:<kensa-core-version>"))
    testImplementation("dev.kensa:kensa-framework-junit5")    // for JUnit 5; use kensa-framework-junit6 for JUnit 6

    // Pick one assertions bridge (or use multiple)
    testImplementation("dev.kensa:kensa-assertions-kotest")     // Kotest matchers
    testImplementation("dev.kensa:kensa-assertions-hamkrest")   // HamKrest
}
```

The Gradle plugin adds `kensa-core` (with the `core-hooks` capability the compiler plugin needs) onto your compilation — you don't need to declare it here. Find the latest kensa-core version on [GitHub releases](https://github.com/kensa-dev/kensa/releases).

:::note[Migrating from 0.6.x]

If you were previously adding `-Xskip-prerelease-check` to your Kotlin compile task for Kensa, you can remove it — 0.7.0 no longer requires it. Its inclusion in earlier versions was a mistake.

:::

Implement `KensaTest` in your test class to get the Given–When–Then DSL. No `@ExtendWith` is needed — the `KensaExtension` is pulled in automatically via the interface. The lifecycle listener is registered via the JUnit Platform `ServiceLoader`.

## 3. Write a Test

Implement `KensaTest` and mix in an assertions bridge. Test methods follow the
Given–When–Then structure using the `given()`, `whenever()`, and `then()` DSL.

```kotlin
import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.GivensContext
import dev.kensa.RenderedValue
import dev.kensa.StateCollector
import dev.kensa.junit.KensaTest
import dev.kensa.kotest.WithKotest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class LoanDecisionTest : KensaTest, WithKotest {

    @RenderedValue
    private val applicantName = "Alice"

    @RenderedValue
    private val requestedAmount = 10_000

    private val service = LoanService()
    private lateinit var result: LoanResult

    @Test
    fun canApproveLoanForApplicantWithGoodCredit() {
        given(anApplicantWithGoodCredit())
        whenever(theLoanServiceProcessesTheApplication())
        then(theLoanResult()) { status shouldBe LoanStatus.Approved }
    }

    @Test
    fun canDeclineLoanForApplicantWithPoorCredit() {
        given(anApplicantWithPoorCredit())
        whenever(theLoanServiceProcessesTheApplication())
        then(theLoanResult()) { status shouldBe LoanStatus.Declined }
    }

    // --- Givens ---

    private fun anApplicantWithGoodCredit() = Action<GivensContext> {
        it.fixtures.add(Applicant(applicantName, creditScore = 750, amount = requestedAmount))
    }

    private fun anApplicantWithPoorCredit() = Action<GivensContext> {
        it.fixtures.add(Applicant(applicantName, creditScore = 300, amount = requestedAmount))
    }

    // --- Action ---

    private fun theLoanServiceProcessesTheApplication() = Action<ActionContext> { ctx ->
        val applicant = ctx.fixtures.get<Applicant>()
        result = service.process(applicant)
    }

    // --- State ---

    private fun theLoanResult() = StateCollector { result }
}
```

### What's happening here

| Element | Purpose |
|---|---|
| `KensaTest` | Provides the Given–When–Then DSL; registers the JUnit extension |
| `WithKotest` | Adds `then()` / `and()` overloads that accept Kotest matchers |
| `@RenderedValue` | Field value is captured and shown in the HTML report |
| `Action<GivensContext>` | Lambda that runs during `given()` — sets up fixtures |
| `Action<ActionContext>` | Lambda that runs during `whenever()` — exercises the system |
| `StateCollector<T>` | Lambda that returns a value for `then()` to assert against |

## 4. Chain Multiple Steps

Use `and()` to chain additional setup or assertions:

```kotlin
@Test
fun canApproveLoanWithUnderwritingApproval() {
    given(anApplicantWithGoodCredit())
    and(anApprovalFromUnderwriting())

    whenever(theLoanServiceProcessesTheApplication())

    then(theLoanResult()) { status shouldBe LoanStatus.Approved }
    and(theLoanReference()) { shouldStartWith("LN-") }
}
```

## 5. Record Interactions (Sequence Diagrams)

Capture calls between components in your `whenever()` action using `interactions.capture()`.
Kensa renders these as a sequence diagram in the HTML report.

```kotlin
private fun theLoanServiceProcessesTheApplication() = Action<ActionContext> { ctx ->
    val applicant = ctx.fixtures.get<Applicant>()

    ctx.interactions.capture(
        from(Client).to(LoanService).with("process(${applicant.name})", "Loan Request")
    )

    result = service.process(applicant)

    ctx.interactions.capture(
        from(LoanService).to(Client).with(result, "Loan Result")
    )
}
```

## 6. Run & View the Report

Run your tests normally with Gradle:

```bash
./gradlew test
```

By default, reports are written to a `kensa-output` directory in the system temp folder. Configure a fixed location in your test setup:

```kotlin
Kensa.konfigure {
    outputDir = Path("build/kensa")
}
```

Then open `index.html` in a browser, or use the Kensa CLI to serve them:

```bash
kensa --dir build/kensa
```

---

## Other Frameworks

Kensa also supports **Kotest** and **TestNG**. The Given-When-Then DSL is identical — only the dependency and setup differs. See the [Java Quickstart](java-quickstart) for Java-specific setup, or browse the [example projects](https://github.com/kensa-dev/kensa/tree/master/examples) on GitHub.
