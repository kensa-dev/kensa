---
title: Report Overview Page for Kotlin & Java Test Runs
sidebar_label: Overview
sidebar_position: 1
description: The Overview page is the default landing view of a Kensa HTML report. It covers timing, results, tags, packages, durations, failures, epics and issues, interactions, and specification density, with every panel a filter.
---

# Overview

## What it is

The Overview is the default landing view of a Kensa HTML report. For a run with a single test source it opens automatically; for a run with several sources a switcher lets you pick which one's overview you're looking at. In the report tree it sits as an **Overview** node above **System View**.

## Panels

Each panel summarises one aspect of the run:

- **Timing:** wall clock, total elapsed, speed-up, time saved, peak concurrency, and a concurrency curve for the whole run.
- **Results:** a donut of Passed, Failed, Disabled, and Not executed.
- **Results by tag:** results broken down by tag.
- **Results by package:** results broken down by package.
- **Tests by duration:** a distribution of how long tests took.
- **Slowest tests:** the individual tests that took longest.
- **Failures:** failing tests, with a hint when failures concentrate in one area.
- **Epics & issues:** the epics and issues linked from the run's tests.
- **Interactions by participant:** captured interactions grouped by actor.
- **Specification density:** assertions per test, the share of tests using expandables, the count of parameterised tests, and the weakest class by this measure.

A panel is hidden when there is no data for it. For example, Epics & issues doesn't appear if no test in the run carries `@Epic` or `@Issue`.

## Timing is whole-run; everything else follows the filter

Timing always reflects the entire run and is not affected by the tree filter. Every other panel reflects only the tests currently matched by the filter, so narrowing the tree narrows the panels alongside it.

Clicking a segment or row in a panel appends a term to the filter: `state:`, `tag:`, `pkg:`, `epic:`, or `issue:`, depending on the panel. The Results donut's "Not executed" segment filters with `state:notexecuted`, with the space stripped out, because a filter term cannot contain one.

## Wall clock vs total elapsed

Wall clock is the time from the first test starting to the last test finishing. Total elapsed is the sum of every individual test's own running time. The ratio between the two is what parallel execution saved you.
