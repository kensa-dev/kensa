---
sidebar_position: 2
description: Step-by-step guide to adding Kensa to a Java project with JUnit 5, writing your first Given-When-Then test, and viewing the generated HTML report.
---

# Quickstart — Java & JUnit 5

This guide walks through setting up Kensa in a Java project with JUnit 5 and writing your first test.

## 1. Add Dependencies

```groovy title="build.gradle"
dependencies {
    testImplementation 'dev.kensa:kensa-junit:<version>'

    // Pick one assertions bridge (or use multiple)
    testImplementation 'dev.kensa:kensa-assertj:<version>'    // AssertJ
    testImplementation 'dev.kensa:kensa-hamcrest:<version>'   // Hamcrest
}
```

Find the latest version on [GitHub releases](https://github.com/kensa-dev/kensa/releases).

Implement `KensaTest` in your test class to get the Given–When–Then DSL. No `@ExtendWith` is needed — the `KensaExtension` is pulled in automatically via the interface. The lifecycle listener is registered via the JUnit Platform `ServiceLoader`.

## 2. Write a Test

Implement `KensaTest` and mix in an assertions bridge. Test methods follow the
Given–When–Then structure using the `given()`, `whenever()`, and `then()` DSL.

```java
import dev.kensa.Action;
import dev.kensa.ActionContext;
import dev.kensa.GivensContext;
import dev.kensa.RenderedValue;
import dev.kensa.StateCollector;
import dev.kensa.junit.KensaTest;
import dev.kensa.assertj.WithAssertJ;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoanDecisionTest implements KensaTest, WithAssertJ {

    @RenderedValue
    private final String applicantName = "Alice";

    @RenderedValue
    private final int requestedAmount = 10_000;

    private final LoanService service = new LoanService();
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
        return ctx -> ctx.getFixtures().add(new Applicant(applicantName, 750, requestedAmount));
    }

    private Action<GivensContext> anApplicantWithPoorCredit() {
        return ctx -> ctx.getFixtures().add(new Applicant(applicantName, 300, requestedAmount));
    }

    // --- Action ---

    private Action<ActionContext> theLoanServiceProcessesTheApplication() {
        return ctx -> {
            Applicant applicant = ctx.getFixtures().get(Applicant.class);
            result = service.process(applicant);
        };
    }

    // --- State ---

    private StateCollector<LoanResult> theLoanResult() {
        return () -> result;
    }
}
```

### What's happening here

| Element | Purpose |
|---|---|
| `KensaTest` | Provides the Given–When–Then DSL; registers the JUnit extension |
| `WithAssertJ` | Adds `then()` / `and()` overloads that accept AssertJ assertions |
| `@RenderedValue` | Field value is captured and shown in the HTML report |
| `Action<GivensContext>` | Lambda that runs during `given()` — sets up fixtures |
| `Action<ActionContext>` | Lambda that runs during `whenever()` — exercises the system |
| `StateCollector<T>` | Supplier that returns a value for `then()` to assert against |

## 3. Chain Multiple Steps

Use `and()` to chain additional setup or assertions:

```java
@Test
void canApproveLoanWithUnderwritingApproval() {
    given(anApplicantWithGoodCredit());
    and(anApprovalFromUnderwriting());

    whenever(theLoanServiceProcessesTheApplication());

    then(theLoanResult(), r -> assertThat(r.getStatus()).isEqualTo(LoanStatus.Approved));
    and(theLoanReference(), ref -> assertThat(ref).startsWith("LN-"));
}
```

## 4. Run & View the Report

Run your tests normally with Gradle:

```bash
./gradlew test
```

By default, reports are written to a `kensa-output` directory in the system temp folder. Configure a fixed location in your test setup:

```java
Kensa.configure()
    .withOutputDir("build/kensa");
```

Then open `index.html` in a browser, or use the Kensa CLI to serve them:

```bash
kensa --dir build/kensa
```

---

## Kotlin

The Kotlin API is identical in structure. See the [Kotlin Quickstart](kotlin-quickstart) for Kotlin-specific setup and idiomatic patterns.
