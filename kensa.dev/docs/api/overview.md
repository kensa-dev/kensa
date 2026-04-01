---
sidebar_position: 1
description: Overview of Kensa's public API covering the Given-When-Then DSL, framework integrations (JUnit 5, Kotest, TestNG), and assertion library mixins.
---

# API Overview

Kensa's public API is organised around a few core concepts. Start here, then dive into the reference pages below.

## Core Concepts

| Concept | What it does |
|---------|-------------|
| [Configuration](./configuration) | Global setup — output directory, issue tracker URL, custom renderers, and more |
| [Fixtures](./fixtures) | Type-safe, lazily-created test data shared across `given`, `whenever`, and `then` |
| [Captured Outputs](./outputs) | Store and retrieve values produced during the action under test |
| [Annotations](./annotations) | Control what appears in the HTML report — rendered values, notes, issues, and more |
| [Interaction Renderers](./interaction-renderers) | Custom renderers for values exchanged between actors in sequence diagrams |

## DSL at a Glance

All framework integrations expose the same DSL via `KensaTest`:

| Method | Context type | Purpose |
|--------|-------------|---------|
| `given(action)` | `Action<GivensContext>` | Set up fixtures and initial state |
| `and(action)` | `Action<GivensContext>` | Chain additional setup steps |
| `whenever(action)` | `Action<ActionContext>` | Exercise the system under test |
| `then(collector, matcher)` | `StateCollector<T>` | Assert on extracted state |
| `then(collector) { block }` | `StateCollector<T>` | Assert using a lambda block |

## Framework Integration

| Framework | Artifact | Registration |
|-----------|----------|-------------|
| JUnit 5 | `kensa-junit` | Automatic via ServiceLoader |
| Kotest | `kensa-kotest` | Manual — add `KensaKotestListener` in `ProjectConfig` |
| TestNG | `kensa-testng` | Automatic via ServiceLoader |

## Assertion Library Mixins

Mix one or more interfaces alongside `KensaTest` to gain assertion methods:

| Interface | Library |
|-----------|---------|
| `WithKotest` | Kotest matchers |
| `WithAssertJ` | AssertJ |
| `WithHamcrest` | Hamcrest (Java) |
| `WithHamkrest` | HamKrest (Kotlin) |
