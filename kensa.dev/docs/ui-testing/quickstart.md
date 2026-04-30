---
sidebar_position: 2
description: Step-by-step guide to writing a Kensa UI test with Playwright or Selenium, including dependencies, user stubs, and running the tests.
---

# UI Testing — Quickstart

This guide walks through setting up a UI test using Playwright. A [Selenium variant](#5-selenium-variant) is shown at the end.

## 1. Add Dependencies

Pick the artifacts that match your JUnit version — `-junit6` for JUnit 6, `-junit5` for JUnit 5. The clearwave example places UI testing in its own source set; the dependencies declaration below shows both drivers wired up for that source set:

```kotlin reference title="build.gradle.kts — UI testing dependencies"
https://github.com/kensa-dev/clearwave-kensa-example/blob/main/build.gradle.kts#L67-L72
```

The version-catalog entries those `libs.*` references resolve to:

```toml reference title="gradle/libs.versions.toml"
https://github.com/kensa-dev/clearwave-kensa-example/blob/main/gradle/libs.versions.toml#L11-L19
```

Find the latest version on [GitHub releases](https://github.com/kensa-dev/kensa/releases).

:::note[Playwright browser installation]
Before running Playwright tests for the first time, install the browsers once. Add a Gradle task that runs the Playwright CLI:

```kotlin reference title="build.gradle.kts — installPlaywrightBrowsers"
https://github.com/kensa-dev/clearwave-kensa-example/blob/main/build.gradle.kts#L130-L137
```

Then run `./gradlew installPlaywrightBrowsers` once. Selenium 4.x auto-manages ChromeDriver — no extra setup needed.
:::

## 2. Define a User Stub

A user stub is a page object that extends `UserStub<D>`, where `D` is your `BrowserDriver` type. It exposes a typed `driver` property — `driver.page` for Playwright, `driver.webDriver` for Selenium. Call `screenshot("label")` at any point to capture the current page state.

The clearwave example pairs each driver with its own user stub class in the same file:

```kotlin reference title="FeasibilityUser.kt — Playwright variant"
https://github.com/kensa-dev/clearwave-kensa-example/blob/main/src/uiTest/kotlin/com/clearwave/ui/FeasibilityUser.kt#L11-L49
```

## 3. Write a UI Test

Extend `KensaPlaywrightUiTest<U>` — driver creation is handled for you (headless Chromium by default). Provide `createUser(driver)` and mix in `WithKotest` so the `then(collector, matcher)` DSL is available. The `theUser` property gives you the typed user stub inside each test method.

Configure Kensa in a `@BeforeAll` companion method — set `sourceLocations` to point at your UI test sources so sentence parsing works correctly.

```kotlin reference title="FeasibilityUiPlaywrightTest.kt"
https://github.com/kensa-dev/clearwave-kensa-example/blob/main/src/uiTest/kotlin/com/clearwave/ui/FeasibilityUiPlaywrightTest.kt
```

### What's happening here

| Element | Purpose |
|---------|---------|
| `KensaPlaywrightUiTest<U>` | Base class — creates a Playwright driver per test |
| `WithKotest` | Mixin enabling the `then(collector, matcher)` DSL |
| `createUser(driver)` | Wraps the driver in your user stub |
| `theUser` | Typed user stub instance; available in `given`/`and`/`whenever` blocks |
| `uiTesting.autoScreenshotOnFailure` | If `true`, captures a screenshot automatically on test failure |
| `sourceLocations` | Tells Kensa where to find your UI test sources for sentence parsing |
| Named givens (`theFeasibilityServiceCanOfferFibre`, `theUserOpensTheFeasibilityPage`, …) | Wrap the implementation away from the rendered test body so the report reads as fluent prose |
| `then(collector, matcher)` with named matchers (`shouldShowAtLeastOneProfile`) | Keeps assertions semantic in the report — never raw matcher calls in the test body |

### Customising the driver

To launch headed, slow Playwright down for debugging, or pass extra args, override `configureLaunchOptions`:

```kotlin
override fun configureLaunchOptions(options: BrowserType.LaunchOptions) {
    options.setHeadless(false).setSlowMo(100.0)
}
```

If you need full control over driver creation (different browser type, custom `BrowserContext`, etc.), extend `KensaUiTest<U>` directly and implement `createDriver()` / `createUser()` yourself.

## 4. Run the Tests

If you've placed UI tests in a custom source set (e.g., `uiTest`):

```bash
./gradlew uiTest
```

Or run them alongside regular tests:

```bash
./gradlew test
```

After the run, open `build/kensa-output-ui/index.html` in a browser, or serve it with the Kensa CLI:

```bash
kensa --dir build/kensa-output-ui
```

Each test invocation includes a **Screenshots** tab in the HTML report showing all captured screenshots in order.

## 5. Selenium Variant

For Selenium, extend `KensaSeleniumUiTest<U>`. Defaults to headless Chrome with CI-friendly flags. Override `configureChromeOptions` to customise.

```kotlin reference title="FeasibilityUiSeleniumTest.kt"
https://github.com/kensa-dev/clearwave-kensa-example/blob/main/src/uiTest/kotlin/com/clearwave/ui/FeasibilityUiSeleniumTest.kt
```

The Selenium user stub accesses `driver.webDriver` instead of `driver.page`. The `given`/`and`/`whenever`/`then` DSL and `screenshot()` calls are identical:

```kotlin reference title="FeasibilityUser.kt — Selenium variant"
https://github.com/kensa-dev/clearwave-kensa-example/blob/main/src/uiTest/kotlin/com/clearwave/ui/FeasibilityUser.kt#L51-L95
```
