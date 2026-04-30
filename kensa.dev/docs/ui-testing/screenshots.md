---
sidebar_position: 3
description: How screenshots are captured in Kensa UI tests, stored on disk, and rendered in the HTML report.
---

# Screenshots

## Capturing screenshots

Call `screenshot("label")` from any method in your `UserStub` subclass — for example, the Playwright user stub from the clearwave example:

```kotlin reference title="FeasibilityUser.kt — Playwright variant"
https://github.com/kensa-dev/clearwave-kensa-example/blob/main/src/uiTest/kotlin/com/clearwave/ui/FeasibilityUser.kt#L11-L49
```

Each call appends a labelled PNG to the Screenshots tab for that test invocation.

## Auto-capture on failure

Set `uiTesting.autoScreenshotOnFailure = true` in `Kensa.konfigure` to capture an extra screenshot (labelled `"On failure"`) automatically whenever a test fails:

```kotlin reference title="FeasibilityUiPlaywrightTest.kt — companion setup"
https://github.com/kensa-dev/clearwave-kensa-example/blob/main/src/uiTest/kotlin/com/clearwave/ui/FeasibilityUiPlaywrightTest.kt#L115-L124
```

## Storage layout

PNGs are written to disk under the report output directory. The path follows the same convention as other Kensa tab content:

```
<outputDir>/tabs/<ClassName>/<methodName>/invocation-N/Screenshots-screenshots/
```

The HTML report references images by relative URL, so the report works correctly under path-prefixed serving (e.g. IntelliJ's built-in browser).

## Viewing screenshots in the report

Open the **Screenshots** tab in a test invocation. Screenshots appear in capture order, labelled with the string passed to `screenshot()`. Click any image to view it full-size. Press **Escape** to close the lightbox.

## Capture method

| Driver | Technique |
|--------|-----------|
| Playwright | Full-page capture via `setFullPage(true)` |
| Selenium (Chromium) | Full-page capture via CDP `Page.captureScreenshot` with `captureBeyondViewport: true` |
| Selenium (other browsers) | Falls back to `TakesScreenshot` (viewport only) |
