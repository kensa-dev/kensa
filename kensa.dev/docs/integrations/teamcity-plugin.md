---
sidebar_position: 1
description: Embed Kensa HTML reports, human-readable test names, and failure narratives directly inside TeamCity with the Kensa Integration build feature.
---

# TeamCity Plugin

The Kensa TeamCity plugin surfaces your BDD test output where your team already triages builds — inside TeamCity itself. Add one build feature, run a build, and three things light up without any path configuration.

## Features

### Kensa Report tab

A dedicated **Kensa Report** tab is added to each build that published Kensa output as an artifact. The tab embeds the full HTML report inline, with an "Open in new tab" link for full-page rendering. No separate URL to remember or share — the report travels with the build.

{/* TODO: screenshot — Kensa Report tab embedded in a TC build page */}

### First-class test results

Every Kensa test appears in TeamCity's **Tests** tab under its human-readable Given-When-Then display name — the same sentence your team reads in the HTML report. Tests slot into TC's standard history graphs, flaky-test detection, and trend tracking exactly like any other test result.

{/* TODO: screenshot — Tests tab showing GWT display names */}

### Failure summaries

When a Kensa test fails, the Given-When-Then narrative, captured values, and exception are attached as that test's **failure detail** — the text you see immediately when you click a red test. No need to open the report separately to understand what went wrong.

{/* TODO: screenshot — failure detail panel showing GWT narrative + exception */}

## Install

Download the plugin zip from [GitHub Releases](https://github.com/kensa-dev/teamcity-plugin/releases) and install it via **Administration > Plugins** in your TeamCity server, or install directly from the JetBrains Marketplace:

{/* TODO: add JetBrains Marketplace URL once the plugin is listed */}

## Configure

In any build configuration, go to **Build Features** and add **Kensa Integration**. That is all that is required for the common case — the plugin resolves the Kensa output directory automatically across:

- single-sourceset projects (`build/kensa-output/`)
- [site mode](../build-plugins/site-mode.md) multi-sourceset projects (`build/kensa-site/`)
- multi-module Gradle layouts

If your output lands somewhere non-standard, an explicit path override is available in the build feature settings.

The build must publish Kensa output as an artifact for the Report tab to appear. The test results and failure summaries are emitted via service messages from the agent and do not require artifact publishing.

## Requirements

| Requirement | Minimum |
|---|---|
| TeamCity server build | `98000` |
| Kensa output | Any build that runs Kensa-instrumented tests |

## Links

- [Source repository](https://github.com/kensa-dev/teamcity-plugin)
- [Releases](https://github.com/kensa-dev/teamcity-plugin/releases)
- [Kensa on GitHub](https://github.com/kensa-dev/kensa)
{/* TODO: JetBrains Marketplace listing URL */}
