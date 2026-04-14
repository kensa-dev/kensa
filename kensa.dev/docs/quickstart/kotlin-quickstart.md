---
sidebar_position: 1
description: Step-by-step guide to adding Kensa to a Kotlin project with JUnit 5, writing your first Given-When-Then test, and viewing the generated HTML report.
---

# Quickstart — Kotlin & JUnit 5

This guide walks through setting up Kensa in a Kotlin project with JUnit 5 and writing your first test.

## 1. Add Dependencies

```kotlin title="build.gradle.kts"
dependencies {
    testImplementation("dev.kensa:kensa-junit:<version>")

    // Pick one assertions bridge (or use multiple)
    testImplementation("dev.kensa:kensa-kotest:<version>")      // Kotest matchers
    testImplementation("dev.kensa:kensa-assertj:<version>")     // AssertJ
    testImplementation("dev.kensa:kensa-hamkrest:<version>")    // HamKrest
}
```

Find the latest version on [GitHub releases](https://github.com/kensa-dev/kensa/releases).

:::important Kotlin compiler option required

Kensa uses explicit backing fields (`-Xexplicit-backing-fields`), which is a pre-release Kotlin feature. This causes the compiled bytecode to be stamped as pre-release, so any project compiling against Kensa must suppress the version check. Add `-Xskip-prerelease-check` to your Kotlin compile task, otherwise you will see errors of the form _"Class 'dev.kensa.X' was compiled by a pre-release version of Kotlin and cannot be loaded by this version of the compiler"_:

```kotlin title="build.gradle.kts"
tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-prerelease-check")
    }
}
```

:::

Implement `KensaTest` in your test class to get the Given–When–Then DSL. No `@ExtendWith` is needed — the `KensaExtension` is pulled in automatically via the interface. The lifecycle listener is registered via the JUnit Platform `ServiceLoader`.

## 2. Write a Test

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

## 3. Chain Multiple Steps

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

## 4. Record Interactions (Sequence Diagrams)

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

## 5. Run & View the Report

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
