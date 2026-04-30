---
sidebar_position: 1
description: Overview of UI testing in Kensa — browser-driven BDD tests with page objects, screenshot capture, and HTML report integration.
---

# UI Testing — Overview

:::note[Availability]
UI testing support lands in **0.8.x**.
:::

## What it is

Kensa UI testing lets you write Given-When-Then browser tests in the same style as your existing Kensa tests. Each test drives a real browser through a typed page-object called a **user stub**. Screenshots taken during the test are automatically attached to the HTML report in a dedicated **Screenshots** tab.

## Why

The same machinery that makes Kensa useful for integration tests — sentence parsing, captured values, rich HTML reports — applies to UI flows without any special configuration. No Gherkin files, no separate screenshot tooling.

## Available drivers

| Driver | JUnit 5 artifact | JUnit 6 artifact |
|--------|-----------------|-----------------|
| Playwright | `dev.kensa:kensa-framework-playwright-junit5` | `dev.kensa:kensa-framework-playwright-junit6` |
| Selenium | `dev.kensa:kensa-framework-selenium-junit5` | `dev.kensa:kensa-framework-selenium-junit6` |

The `-junit5` and `-junit6` artifacts share the same source — choose the one that matches your JUnit dependency. Currently JUnit-only; Kotest and TestNG support to follow.

## Key building blocks

| Type | Package | Purpose |
|------|---------|---------|
| `KensaPlaywrightUiTest<U>` | `dev.kensa.playwright.junit` | Base class for Playwright UI tests — driver creation handled |
| `KensaSeleniumUiTest<U>` | `dev.kensa.selenium.junit` | Base class for Selenium UI tests — driver creation handled |
| `KensaUiTest<U>` | `dev.kensa.uitesting.junit` | Lower-level base class if you want full control over driver creation |
| `UserStub<D>` | `dev.kensa.uitesting` | Base class for page objects; exposes typed `driver` |
| `BrowserDriver` | `dev.kensa.uitesting` | Common interface implemented by `PlaywrightBrowserDriver` and `SeleniumBrowserDriver` |
| `screenshot(label)` | `dev.kensa.uitesting` | Called from a `UserStub` method to capture the current page state |
| Screenshots tab | — | Report tab that renders all captured screenshots for a test invocation |

In the common case, extend `KensaPlaywrightUiTest<U>` or `KensaSeleniumUiTest<U>` and implement just `createUser(driver)`. The base class supplies a sensible headless driver. Override `configureLaunchOptions` (Playwright) or `configureChromeOptions` (Selenium) to customise.

The framework creates a fresh driver and user per test method, then disposes the driver after the test.

`theUser` is the typed user stub instance available inside test methods.
