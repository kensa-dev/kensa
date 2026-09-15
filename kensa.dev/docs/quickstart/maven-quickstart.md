---
title: BDD Testing with Maven — Quickstart
sidebar_label: Quickstart — Maven
sidebar_position: 5
description: Step-by-step guide to adding Kensa to a Maven project with JUnit 5, writing your first Given-When-Then test, and viewing the generated HTML report.
---

# Quickstart — Maven

This guide walks through setting up Kensa in a Maven project with Java and JUnit 5 and writing your first test. The Given–When–Then DSL is identical to the Gradle variants — only the build wiring differs.

## 1. Apply the Kensa Maven Plugin

The Kensa Maven plugin provides one goal, `assemble-site`, which collects the bundles your test executions wrote and builds the report site on top of them. It does **not** wire system properties or dependencies for you: each surefire or failsafe execution names its own source id, and the plugin assembles whatever those executions produced. For a single `test` execution the wiring is:

```xml title="pom.xml"
<properties>
  <kensa.version>x.y.z</kensa.version>          <!-- kensa-core version -->
  <kensa.plugin.version>x.y.z</kensa.plugin.version>   <!-- kensa-maven-plugin version -->
</properties>

<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-surefire-plugin</artifactId>
      <version>3.5.4</version>
      <configuration>
        <systemPropertyVariables>
          <kensa.output.root>${project.build.directory}/kensa-site</kensa.output.root>
          <kensa.source.id>test</kensa.source.id>
        </systemPropertyVariables>
      </configuration>
    </plugin>

    <plugin>
      <groupId>dev.kensa</groupId>
      <artifactId>kensa-maven-plugin</artifactId>
      <version>${kensa.plugin.version}</version>
      <executions>
        <execution>
          <id>assemble-site</id>
          <phase>post-integration-test</phase>
          <goals><goal>assemble-site</goal></goals>
          <configuration>
            <expectedSourceIds>
              <expectedSourceId>test</expectedSourceId>
            </expectedSourceIds>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

`kensa.source.id` tells the Kensa runtime to write its bundle to `${kensa.output.root}/sources/test/`; `expectedSourceIds` tells the plugin which bundles to assemble. Keep the two in step. The plugin resolves from Maven Central from 0.9.16 and versions independently of `kensa-core` — see the [compatibility matrix](../build-plugins/maven-plugin.md#kensa-core-compatibility) for the supported pairings.

The Maven plugin does not apply the Kensa Kotlin compiler plugin. Java projects are unaffected — Java value capture goes through the runtime — but Kotlin tests under Maven lose `@RenderedValue` / `@ExpandableSentence` value capture; see [Limitations](../build-plugins/maven-plugin.md#limitations-relative-to-the-gradle-plugin).

## 2. Add Test Dependencies

Import the `kensa-bom` so the framework and assertions artifacts share a version, then pick a JUnit variant and one or more assertions bridges. `kensa-core` flows in transitively from the framework artifact — you don't need to declare it. JUnit itself is a `runtime`-scope dependency of the framework artifact, so declare it at test scope yourself for `@Test` to compile and for surefire to pick the JUnit Platform provider:

```xml title="pom.xml"
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>dev.kensa</groupId>
      <artifactId>kensa-bom</artifactId>
      <version>${kensa.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>dev.kensa</groupId>
    <artifactId>kensa-framework-junit5</artifactId>   <!-- for JUnit 5; use kensa-framework-junit6 for JUnit 6 -->
    <scope>test</scope>
  </dependency>

  <!-- Pick one assertions bridge (or use multiple) -->
  <dependency>
    <groupId>dev.kensa</groupId>
    <artifactId>kensa-assertions-assertj</artifactId>   <!-- AssertJ -->
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>dev.kensa</groupId>
    <artifactId>kensa-assertions-hamcrest</artifactId>  <!-- Hamcrest -->
    <scope>test</scope>
  </dependency>

  <dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.14.3</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```

Find the latest Kensa version on [GitHub releases](https://github.com/kensa-dev/kensa/releases).

## 3. Write a Test

Implement `KensaTest` and mix in an assertions bridge. No `@ExtendWith` is needed — the `KensaExtension` is pulled in automatically via the interface, and the lifecycle listener is registered via the JUnit Platform `ServiceLoader`. Test methods follow the Given–When–Then structure using the `given()`, `whenever()`, and `then()` DSL.

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

### What's happening here

| Element | Purpose |
|---|---|
| `KensaTest` | Provides the Given–When–Then DSL; registers the JUnit extension |
| `WithAssertJ` | Adds `then()` / `and()` overloads that accept AssertJ assertions |
| `@RenderedValue` | Field value is captured and shown in the HTML report |
| `Action<GivensContext>` | Lambda that runs during `given()` — sets up test state |
| `Action<ActionContext>` | Lambda that runs during `whenever()` — exercises the system |
| `StateCollector<T>` | Lambda that receives a `CollectorContext` and returns a value for `then()` to assert against |

Chaining further steps with `and()`, and the typed collectors that return a fluent AssertJ assertion, work exactly as in the [Java Quickstart](java-quickstart#3-chain-multiple-steps). For shared, lazily-created test data — reusable across tests and rendered in the report's Fixtures tab — see [Fixtures](../api/fixtures.md).

## 4. Run & View the Report

Run the build through `verify` so that `assemble-site` runs after the tests:

```bash
mvn verify
```

Surefire runs the tests in the `test` phase and writes the bundle to `target/kensa-site/sources/test/`; `assemble-site` then adds the shell and manifest in `post-integration-test`. Open `target/kensa-site/index.html` in a browser, or use the Kensa CLI to serve it:

```bash
kensa --dir target/kensa-site
```

`mvn test` alone stops before `post-integration-test`, so the bundle is written but the site is not assembled.

If you would rather not apply the plugin at all, leave out the system properties and the plugin block: Kensa then writes a single `kensa-output` directory to the system temp folder, and a fixed location is configured in code — `withOutputDir` requires an **absolute** path:

```java
Kensa.configure()
    .withOutputDir(Paths.get("target/kensa-output").toAbsolutePath());
```

## Beyond the happy path

The [Maven plugin](../build-plugins/maven-plugin.md) page covers the rest: several surefire or failsafe executions each with their own source id, `<sourceTitles>` for the sidebar labels, [multi-module builds](../build-plugins/maven-plugin.md#multi-module-builds) that share one site root, and the [limitations](../build-plugins/maven-plugin.md#limitations-relative-to-the-gradle-plugin) relative to the Gradle plugin. The disk layout the plugin assembles is described in [Site Mode](../build-plugins/site-mode.md).

Kotlin tests compile under Maven with the `kotlin-maven-plugin` and use the same DSL, but without the Kensa compiler plugin they do not get `@RenderedValue` / `@ExpandableSentence` value capture — see the [limitations](../build-plugins/maven-plugin.md#limitations-relative-to-the-gradle-plugin).

---

## Other Frameworks

The build wiring on this page is Maven; everything else is the same as the Gradle quickstarts. See the [Java Quickstart](java-quickstart) for chaining and typed collectors, the [Kotlin Quickstart](kotlin-quickstart) for Kotlin-idiomatic patterns, or the [Kotest Quickstart](kotest-quickstart) and [TestNG Quickstart](testng-quickstart) for the other runners — their dependency and listener setup carries over to a `pom.xml` unchanged, with surefire configured for the runner in question.
