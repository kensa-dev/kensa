---
sidebar_position: 4
description: Step-by-step guide to adding Kensa to a Kotlin or Java project with TestNG, writing your first Given-When-Then test, and viewing the generated HTML report.
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Quickstart — TestNG

This guide walks through setting up Kensa with TestNG and writing your first test. The Given–When–Then DSL is identical to the JUnit variants — only the framework artifact and the lifecycle hook differ.

A full reference project is published at [kensa-dev/clearwave-testng-example](https://github.com/kensa-dev/clearwave-testng-example) — the TestNG counterpart of [clearwave-example](https://github.com/kensa-dev/clearwave-example).

## 1. Apply the Kensa Gradle Plugin

The Kensa Gradle plugin wires the Kotlin compiler plugin (so `@RenderedValue` and `@ExpandableSentence` capture values for Kotlin tests) and aggregates results into a multi-sourceset site. Apply it alongside Kotlin if you're writing Kotlin tests; pure-Java projects can still apply it to get site-mode reporting:

```kotlin title="build.gradle.kts"
plugins {
    kotlin("jvm") version "2.4.0"            // Kotlin tests only — the plugin enforces a minimum, see the compatibility matrix
    id("dev.kensa.gradle-plugin") version "<plugin-version>"
}

repositories { mavenCentral() }
```

The plugin and `kensa-core` version independently — see the [compatibility matrix](../build-plugins/gradle-plugin.md#kensa-core-compatibility) for the supported pairings.

## 2. Add Test Dependencies

Pull in `kensa-framework-testng` plus `testng` itself — the framework artifact declares TestNG as `compileOnly`, so the consuming project picks the version. Pair it with one or more assertions bridges:

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin title="build.gradle.kts"
dependencies {
    testImplementation(platform("dev.kensa:kensa-bom:<kensa-core-version>"))
    testImplementation("dev.kensa:kensa-framework-testng")
    testImplementation("org.testng:testng:7.12.0")              // kensa-framework-testng has TestNG as compileOnly

    // Pick one assertions bridge (or use multiple)
    testImplementation("dev.kensa:kensa-assertions-kotest")     // Kotest matchers
    testImplementation("dev.kensa:kensa-assertions-hamkrest")   // HamKrest
}

tasks.test {
    useTestNG()
}
```

</TabItem>
<TabItem value="java" label="Java">

```groovy title="build.gradle"
dependencies {
    testImplementation platform('dev.kensa:kensa-bom:<kensa-core-version>')
    testImplementation 'dev.kensa:kensa-framework-testng'
    testImplementation 'org.testng:testng:7.12.0'               // kensa-framework-testng has TestNG as compileOnly

    // Pick one assertions bridge (or use multiple)
    testImplementation 'dev.kensa:kensa-assertions-assertj'     // AssertJ
    testImplementation 'dev.kensa:kensa-assertions-hamcrest'    // Hamcrest
}

test {
    useTestNG()
}
```

</TabItem>
</Tabs>

Find the latest Kensa version on [GitHub releases](https://github.com/kensa-dev/kensa/releases).

:::note[ServiceLoader auto-discovery]

The Kensa lifecycle listener `dev.kensa.testng.KensaTestNgListener` is auto-discovered by TestNG via `META-INF/services/org.testng.ITestNGListener` in the published jar. Do **not** add it to `@Listeners(...)` yourself — that would register it twice and every captured interaction would appear in the report duplicated.

:::

## 3. Write a Test

Implement `KensaTest` (from `dev.kensa.testng`, **not** `dev.kensa.junit`) and mix in an assertions bridge. Test methods follow the Given–When–Then structure using the `given()`, `whenever()`, and `then()` DSL:

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.GivensContext
import dev.kensa.RenderedValue
import dev.kensa.StateCollector
import dev.kensa.kotest.WithKotest
import dev.kensa.testng.KensaTest
import io.kotest.matchers.shouldBe
import org.testng.annotations.Test

class LoanDecisionTest : KensaTest, WithKotest {

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

</TabItem>
<TabItem value="java" label="Java">

```java
import dev.kensa.Action;
import dev.kensa.ActionContext;
import dev.kensa.GivensContext;
import dev.kensa.RenderedValue;
import dev.kensa.StateCollector;
import dev.kensa.assertj.WithAssertJ;
import dev.kensa.testng.KensaTest;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoanDecisionTest implements KensaTest, WithAssertJ {

    @RenderedValue
    private final String applicantName = "Alice";

    @RenderedValue
    private final int requestedAmount = 10_000;

    private final LoanService service = new LoanService();
    private Applicant applicant;
    private LoanResult result;

    @Test
    void canApproveLoanForApplicantWithGoodCredit() {
        given(anApplicantWithGoodCredit());
        whenever(theLoanServiceProcessesTheApplication());
        then(theLoanResult(), r -> assertThat(r.getStatus()).isEqualTo(LoanStatus.Approved));
    }

    @Test
    void canDeclineLoanForApplicantWithPoorCredit() {
        given(anApplicantWithPoorCredit());
        whenever(theLoanServiceProcessesTheApplication());
        then(theLoanResult(), r -> assertThat(r.getStatus()).isEqualTo(LoanStatus.Declined));
    }

    // --- Givens ---

    private Action<GivensContext> anApplicantWithGoodCredit() {
        return ctx -> applicant = new Applicant(applicantName, 750, requestedAmount);
    }

    private Action<GivensContext> anApplicantWithPoorCredit() {
        return ctx -> applicant = new Applicant(applicantName, 300, requestedAmount);
    }

    // --- Action ---

    private Action<ActionContext> theLoanServiceProcessesTheApplication() {
        return ctx -> result = service.process(applicant);
    }

    // --- State ---

    private StateCollector<LoanResult> theLoanResult() {
        return ctx -> result;
    }
}
```

</TabItem>
</Tabs>

### What's happening here

| Element | Purpose |
|---|---|
| `dev.kensa.testng.KensaTest` | Provides the Given–When–Then DSL. **Different package** from the JUnit interface — make sure you don't pick up `dev.kensa.junit.KensaTest` by mistake |
| `WithKotest` / `WithAssertJ` | Adds `then()` / `and()` overloads that accept Kotest matchers or AssertJ assertions |
| `@RenderedValue` | Field value is captured and shown in the HTML report |
| `Action<GivensContext>` | Lambda that runs during `given()` — sets up test state |
| `Action<ActionContext>` | Lambda that runs during `whenever()` — exercises the system |
| `StateCollector<T>` | Returns a value for `then()` to assert against |

## 4. Chain Multiple Steps

Use `and()` to chain additional setup or assertions:

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

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

</TabItem>
<TabItem value="java" label="Java">

```java
@Test
void canApproveLoanWithUnderwritingApproval() {
    given(anApplicantWithGoodCredit());
    and(anApprovalFromUnderwriting());

    whenever(theLoanServiceProcessesTheApplication());

    then(theLoanResult(), r -> assertThat(r.getStatus()).isEqualTo(LoanStatus.Approved));
    and(theLoanReference()).startsWith("LN-");
}
```

The single-argument `and()` returns a fluent AssertJ assertion — declare the collector as a typed collector interface from `dev.kensa.assertj` (here `StringStateCollector theLoanReference()`) to get the right assertion type. See the [Java Quickstart](java-quickstart) for the full pattern.

</TabItem>
</Tabs>

## 5. Adding Your Own TestNG Listeners

If you need to start/stop fixtures around the whole suite — for example a stub HTTP server, a database container, or any other long-lived resource — implement a TestNG `ISuiteListener` and register it via `@Listeners` on an abstract base class:

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
import dev.kensa.testng.KensaTest
import dev.kensa.kotest.WithKotest
import org.testng.ISuite
import org.testng.ISuiteListener
import org.testng.annotations.Listeners

class MyAppListener : ISuiteListener {
    override fun onStart(suite: ISuite) {
        // start stubs, register fixtures, call Kensa.konfigure { ... }
    }

    override fun onFinish(suite: ISuite) {
        // close stubs and other resources
    }
}

@Listeners(MyAppListener::class)
abstract class MyAppTest : KensaTest, WithKotest
```

</TabItem>
<TabItem value="java" label="Java">

```java
import dev.kensa.testng.KensaTest;
import dev.kensa.assertj.WithAssertJ;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.annotations.Listeners;

public class MyAppListener implements ISuiteListener {
    @Override
    public void onStart(ISuite suite) {
        // start stubs, register fixtures, call Kensa.configure()...
    }

    @Override
    public void onFinish(ISuite suite) {
        // close stubs and other resources
    }
}

@Listeners(MyAppListener.class)
abstract class MyAppTest implements KensaTest, WithAssertJ {}
```

</TabItem>
</Tabs>

`KensaTestNgListener` itself is auto-discovered (see the note in step 2), so listing it alongside your own listener is unnecessary — and will produce duplicated captures if you do.

## 6. Run & View the Report

Run your tests normally with Gradle:

```bash
./gradlew test
```

By default, reports are written to a `kensa-output` directory in the system temp folder. Configure a fixed location in your test setup:

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
Kensa.konfigure {
    outputDir = Path("build/kensa-output")
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
// withOutputDir requires an absolute path
Kensa.configure()
    .withOutputDir(Paths.get("build/kensa-output").toAbsolutePath());
```

</TabItem>
</Tabs>

Then open `index.html` in a browser, or use the Kensa CLI to serve them:

```bash
kensa --dir build/kensa-output
```

## TestNG-specific notes

- **Class instance reuse.** TestNG creates one instance per class by default. State stored in test-class fields therefore leaks between methods. Either keep test-class fields immutable and put per-test state into a Kensa fixture (which is invocation-scoped), or annotate the class with `@Test(singleThreaded = true)` and re-initialise fields in `@BeforeMethod`.
- **`@DataProvider`.** TestNG's parametrised-test mechanism (`@Test(dataProvider = "name")`) works as usual — the Kensa listener fires per parameter row.
- **TestNG version.** `kensa-framework-testng` is built against TestNG `7.12.x`; other 7.x releases are expected to work.

---

## Other Frameworks

If you're using JUnit instead of TestNG, see the [Kotlin Quickstart](kotlin-quickstart) or [Java Quickstart](java-quickstart); for Kotest, see the [Kotest Quickstart](kotest-quickstart) — the DSL and assertions are the same.
