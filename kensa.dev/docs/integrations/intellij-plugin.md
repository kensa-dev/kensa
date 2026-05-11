---
sidebar_position: 2
description: Open Kensa HTML reports from inside IntelliJ IDEA — gutter icons on tests, console hyperlinks, and live notifications when a report lands.
---

# IntelliJ Plugin

[![JetBrains Marketplace](https://img.shields.io/jetbrains/plugin/v/31099?style=flat-square&color=3cad6e&labelColor=1a3a2a&label=marketplace)](https://plugins.jetbrains.com/plugin/31099)

The Kensa IntelliJ plugin puts your Kensa reports one click away from every test method in the IDE. No copy-pasting URLs, no hunting through `build/` directories.

## Features

### Gutter icons

Pass/fail icons appear next to `@Test` and `@ParameterizedTest` methods and their containing classes, updated automatically as tests run. Click any icon to open the corresponding Kensa HTML report in your browser, navigating directly to that test or class.

### Console hyperlinks

The `Kensa Output :` marker that Kensa writes to the test console becomes a clickable link — open the report directly from the run window.

### Live notifications

A balloon notification fires when a new report is written, with an _Open Report_ action. No need to wait for the test run to finish before browsing.

### CI report support

Configure a CI report URL template in **Settings → Tools → Kensa** to open remote reports when no local report is available. If both are present, a popup lets you pick.

## Install

Install directly from the [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/31099):

<iframe width="384" height="319" src="https://plugins.jetbrains.com/embeddable/card/31099" title="Kensa on the JetBrains Marketplace" frameBorder="0"></iframe>

From IntelliJ IDEA: **Settings → Plugins → Marketplace**, search for "Kensa", click Install.

Manual install: download the plugin zip from [GitHub Releases](https://github.com/kensa-dev/intellij-plugin/releases) and use **Settings → Plugins → ⚙ → Install plugin from disk...**

## Requirements

| Requirement | Minimum |
|---|---|
| IntelliJ IDEA | `2025.3` (build 253) |
| Kensa-instrumented tests | Any project with Kensa configured |

## Links

- [JetBrains Marketplace listing](https://plugins.jetbrains.com/plugin/31099)
- [Source repository](https://github.com/kensa-dev/intellij-plugin)
- [Releases](https://github.com/kensa-dev/intellij-plugin/releases)
- [Kensa on GitHub](https://github.com/kensa-dev/kensa)
