# <img src="./Logo.svg" alt="Kensa Logo" style="width: 40px; vertical-align: middle;"/> Kensa

![Latest Release](https://img.shields.io/github/v/release/kensa-dev/kensa)

**Kensa** is a BDD testing framework for Kotlin and Java. Write Given-When-Then tests directly in code — no Gherkin files, no step definitions. Kensa parses your test source at runtime to produce rich HTML reports and sequence diagrams.

Check out the [documentation](https://kensa.dev) for quickstarts, API reference, and examples.

| Write this… | …get this |
|---|---|
| ![Kensa test written in Kotlin](kensa.dev/static/img/code-example.png) | ![Kensa generated HTML report with sequence diagram](kensa.dev/static/img/report-example.png) |

## Features

- **Code-first BDD** — Given-When-Then structure in plain Kotlin/Java; no external DSL files
- **HTML reports** — generated directly from test source, always in sync with the code
- **Sequence diagrams** — visualise interactions between actors captured during test execution
- **Framework support** — JUnit 5, Kotest, TestNG
- **Assertion libraries** — Kotest, AssertJ, Hamcrest, HamKrest

## Getting Started

Add the dependency for your test framework:

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("dev.kensa:kensa-junit:<version>")   // JUnit 5
    // or
    testImplementation("dev.kensa:kensa-kotest:<version>")  // Kotest runner

    // Assertions bridge (pick one or more)
    testImplementation("dev.kensa:kensa-kotest:<version>")
    testImplementation("dev.kensa:kensa-assertj:<version>")
}
```

Find the latest version on the [releases page](https://github.com/kensa-dev/kensa/releases).

See the [Kotlin quickstart](https://kensa.dev/docs/quickstart/kotlin-quickstart) or [Java quickstart](https://kensa.dev/docs/quickstart/java-quickstart) for a full setup walkthrough.

## Tooling

### CLI — serve reports locally

Every release ships pre-built binaries for macOS (Intel + Apple Silicon), Linux, and Windows.
Download `kensa-<os>-<arch>` from the [latest release](https://github.com/kensa-dev/kensa/releases/latest), then:

```bash
kensa serve <path-to-report-dir>
```

This starts a local HTTP server and opens your HTML reports in the browser.

### Claude Code skill — AI-assisted test review

Every release also ships `kensa-test.skill`, a [Claude Code](https://claude.ai/code) skill that reviews Kensa tests for idiomatic style, fluency violations, and best-practice patterns.

Install it once:

```bash
claude plugin install kensa-test.skill
```

Then invoke it in any Claude Code session:

```
/kensa-test review this test
```

The skill checks for fluent English in rendered test bodies, correct use of Fixtures and CapturedOutputs, semantic assertion naming, composable setup toolboxes, and the typed context/mixin pattern for multi-stub tests.
