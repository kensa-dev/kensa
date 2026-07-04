---
sidebar_position: 1
description: Overview of Kensa's public API covering the Given-When-Then DSL, framework integrations (JUnit 5 & 6, Kotest, TestNG), and assertion library mixins.
---

# API Overview

Kensa's public API is organised around a few core concepts. Start here, then dive into the reference pages below.

## Core Concepts

| Concept | What it does |
|---------|-------------|
| [Configuration](./configuration) | Global setup — output directory, issue tracker URL, custom renderers, and more |
| [Fixtures](./fixtures) | Type-safe, lazily-created test data shared across `given`, `whenever`, and `then` |
| [Factory Fixtures](./factory-fixtures) | Parameterised `@Fixture` factory functions rewritten by the compiler plugin |
| [Captured Outputs](./outputs) | Store and retrieve values produced during the action under test |
| [Annotations](./annotations) | Control what appears in the HTML report — rendered values, notes, issues, and more |
| [Async Assertions](./async-assertions) | `thenEventually` / `thenContinually` — polling assertions for asynchronous behaviour |
| [Attachments](./attachments) | Attach arbitrary typed values to a test invocation for custom tabs |
| [Sentence Hints](./sentence-hints) | Comment-based hints that reshape how a statement renders in the report |
| [Interaction Renderers](./interaction-renderers) | Custom renderers for values exchanged between actors in sequence diagrams |
| [Log Tabs](./log-tabs) | Pull container/service logs into per-test report tabs |

## DSL at a Glance

Every framework integration exposes the same setup/action DSL via its `KensaTest`; the assertion side comes from the [mixin](#assertion-library-mixins) you choose:

| Method | Provided by | Context type | Purpose |
|--------|-------------|-------------|---------|
| `given(action)` / `and(action)` | `KensaTest` | `Action<GivensContext>` | Set up test state; chain additional setup steps |
| `whenever(action)` (alias `` `when` ``) | `KensaTest` | `Action<ActionContext>` | Exercise the system under test |
| `then(collector, matcher)` | assertion mixin | `StateCollector<T>` | Assert on extracted state |
| `then(collector) { block }` / `and(...)` | assertion mixin | `StateCollector<T>` | Assert using a lambda block; chain further assertions |
| [`thenEventually(...)` / `thenContinually(...)`](./async-assertions) | assertion mixin | `StateCollector<T>` | Poll the collector until the assertion passes (or keeps passing) within a timeout |

## Framework Integration

| Framework | Artifact | Registration |
|-----------|----------|-------------|
| JUnit 5 | `kensa-framework-junit5` | Implement the `dev.kensa.junit.KensaTest` interface — extension and lifecycle listener register automatically |
| JUnit 6 | `kensa-framework-junit6` | As JUnit 5 |
| Kotest | `kensa-framework-kotest` | Extend the `dev.kensa.kotest.KensaTest` base class; add `KensaKotestListener` in `ProjectConfig` |
| TestNG | `kensa-framework-testng` | Implement the `dev.kensa.testng.KensaTest` interface — listener auto-discovered via ServiceLoader |

## Assertion Library Mixins

Mix one or more interfaces alongside `KensaTest` to gain assertion methods:

| Interface | Library |
|-----------|---------|
| `WithKotest` | Kotest matchers |
| `WithAssertJ` | AssertJ |
| `WithHamcrest` | Hamcrest (Java) |
| `WithHamkrest` | HamKrest (Kotlin) |
