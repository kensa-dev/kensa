---
sidebar_position: 3
description: Step-by-step guide to adding Kensa to a Kotlin project with Kotest, writing your first Given-When-Then test, and viewing the generated HTML report.
---

# Quickstart — Kotest

This guide walks through setting up Kensa with Kotest and writing your first test. The Given–When–Then DSL is identical to the JUnit and TestNG variants — only the dependency, the base class, and the listener registration differ.

## 1. Apply the Kensa Gradle Plugin

The Kensa Gradle plugin wires the Kotlin compiler plugin (so `@RenderedValue` and `@ExpandableSentence` capture values) and pulls the right `kensa-core` variant onto your compilation. Apply it alongside Kotlin:

```kotlin title="build.gradle.kts"
plugins {
    kotlin("jvm") version "2.4.0"            // the plugin enforces a minimum Kotlin version — see the compatibility matrix
    id("dev.kensa.gradle-plugin") version "<plugin-version>"
}

repositories { mavenCentral() }
```

The plugin and `kensa-core` version independently — see the [compatibility matrix](../build-plugins/gradle-plugin.md#kensa-core-compatibility) for the supported pairings.

## 2. Add Test Dependencies

`kensa-framework-kotest` declares Kotest as `compileOnly`, so your project supplies the Kotest version — add the Kotest BOM and JUnit Platform runner alongside it:

```kotlin title="build.gradle.kts"
dependencies {
    testImplementation(platform("dev.kensa:kensa-bom:<kensa-core-version>"))
    testImplementation("dev.kensa:kensa-framework-kotest")
    testImplementation("dev.kensa:kensa-assertions-kotest")     // then()/and() overloads for Kotest matchers

    testImplementation(platform("io.kotest:kotest-bom:<kotest-version>"))
    testImplementation("io.kotest:kotest-runner-junit5-jvm")
}

tasks.test {
    useJUnitPlatform()
}
```

Find the latest Kensa version on [GitHub releases](https://github.com/kensa-dev/kensa/releases). Kensa supports Kotest 6.

## 3. Register the Kensa Listener

Unlike the JUnit and TestNG integrations, the Kotest listener is not discovered automatically — register `KensaKotestListener` in your Kotest `ProjectConfig`:

```kotlin title="src/test/kotlin/io/kotest/provided/ProjectConfig.kt"
package io.kotest.provided

import dev.kensa.kotest.KensaKotestListener
import io.kotest.core.config.AbstractProjectConfig

object ProjectConfig : AbstractProjectConfig() {
    override val extensions = listOf(KensaKotestListener())
}
```

## 4. Write a Test

Extend `KensaTest` (from `dev.kensa.kotest`) and mix in an assertions bridge. `KensaTest` is a base **class** built on Kotest's `AnnotationSpec` style — test methods are annotated with `@Test` (inherited from `AnnotationSpec`), so tests look just like their JUnit counterparts. Other Kotest spec styles (`FunSpec`, `StringSpec`, …) are not supported — Kensa parses named test methods to build report sentences.

```kotlin
import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.GivensContext
import dev.kensa.RenderedValue
import dev.kensa.StateCollector
import dev.kensa.kotest.KensaTest
import dev.kensa.kotest.WithKotest
import io.kotest.matchers.shouldBe

class LoanDecisionTest : KensaTest(), WithKotest {

    @RenderedValue
    private val applicantName = "Alice"

    @RenderedValue
    private val requestedAmount = 10_000

    private val service = LoanService()
    private lateinit var applicant: Applicant
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
        applicant = Applicant(applicantName, creditScore = 750, amount = requestedAmount)
    }

    private fun anApplicantWithPoorCredit() = Action<GivensContext> {
        applicant = Applicant(applicantName, creditScore = 300, amount = requestedAmount)
    }

    // --- Action ---

    private fun theLoanServiceProcessesTheApplication() = Action<ActionContext> {
        result = service.process(applicant)
    }

    // --- State ---

    private fun theLoanResult() = StateCollector { result }
}
```

### What's happening here

| Element | Purpose |
|---|---|
| `KensaTest` | Base class (extends Kotest's `AnnotationSpec`) providing the Given–When–Then DSL |
| `WithKotest` | Adds `then()` / `and()` overloads that accept Kotest matchers |
| `@RenderedValue` | Field value is captured and shown in the HTML report |
| `Action<GivensContext>` | Lambda that runs during `given()` — sets up test state |
| `Action<ActionContext>` | Lambda that runs during `whenever()` — exercises the system |
| `StateCollector<T>` | Lambda that returns a value for `then()` to assert against |

For shared, lazily-created test data — reusable across tests and rendered in the report's Fixtures tab — see [Fixtures](../api/fixtures.md).

## 5. Run & View the Report

Run your tests normally with Gradle:

```bash
./gradlew test
```

By default, reports are written to a `kensa-output` directory in the system temp folder. Configure a fixed location in your test setup (the `ProjectConfig` from step 3 is a natural home):

```kotlin
Kensa.konfigure {
    outputDir = Path("build/kensa-output")
}
```

Then open `index.html` in a browser, or use the Kensa CLI to serve them:

```bash
kensa --dir build/kensa-output
```

## Kotest-specific notes

- **Tags.** Use Kotest's `io.kotest.core.annotation.Tags` at class level — see [Tags](../tags.md) for how they surface in reports.
- **Disabled tests.** AnnotationSpec's `@Ignore` marks a test disabled; Kensa records it as a disabled invocation in the report.

---

## Other Frameworks

If you're using JUnit, see the [Kotlin Quickstart](kotlin-quickstart) or [Java Quickstart](java-quickstart); for TestNG, see the [TestNG Quickstart](testng-quickstart) — the DSL and assertions are the same.
