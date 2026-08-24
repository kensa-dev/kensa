---
sidebar_position: 99
title: Roadmap - 1.0, Kage, Hub and Replay
description: Where Kensa is heading - the road to 1.0, and the stub server, output server and replay work that follows it.
---

# Roadmap

Kensa is currently at **0.9.0** - see [GitHub releases](https://github.com/kensa-dev/kensa/releases) for detailed release notes.

## Next: v1.0

The 1.0 release is about stability, not features:

- **API freeze** - shipped in 0.9.0. The supported surface is now sealed by the compiler rather than by convention: implementation packages are `internal`, and the integration SPI requires an explicit opt-in. See [`COMPATIBILITY.md`](https://github.com/kensa-dev/kensa/blob/master/COMPATIBILITY.md) for what semantic versioning will and will not cover.
- **Documentation** - a completeness and accuracy pass across the whole site, plus versioned docs from 1.0 onwards.
- **Quality** - broader test coverage across the framework integrations.

No new features land in 1.0. Feature work resumes afterwards.

## After 1.0: past the test run

Kensa exists so that testers, analysts and product owners can see an honest account of what an application actually does - generated from the tests that ran, not from a document somebody maintained alongside them. The work now in progress extends that in three directions: to the **stubs the test drives**, to **where the reports land** for the people who read them, and to **watching the behaviour happen** rather than reading about it afterwards.

None of these has been released, and they are at different stages. They are described here so you can see where Kensa is going, not because they are ready to use.

**All three are open source, under the same licence as the framework.** There is no paid tier, no
hosted edition and no feature held back - if it ships, you can run it yourself.

### Kage - an out-of-process stub server

**Kage** (影, "shadow" - a stub is a shadow of the real service it stands in for) makes the stub a deployable service rather than something assembled inside the test JVM.

In-process stubs are quick to start with and get expensive later: they cannot be shared across teams, they cannot be deployed alongside the system under test, and nothing outside the test can observe what passed through them. Kage moves the stub out of the test process and gives it an event stream the test subscribes to.

- **An event protocol** - a defined websocket stream of inbound endpoint hits and outbound sends, so any conforming server can be driven by a Kensa test.
- **A reference server** - plugin-based, with applications contributing routes for their own third-party dependencies. Functional and composable in the http4k mould; no annotations and no DI container.
- **A test-side SDK** - subscribe to the stream, extract values from captured messages, and assert over them through the same `ThenSpec` surface you already use, so stub interactions land in the report alongside everything else.
- **Correlation by tracking id** - every event carries an identifier for the test run it belongs to. Parallel tests can share one server without interfering, and a system that already threads a correlation header for tracing works with Kage without test-specific code.
- **Fault injection** - errors, delays and malformed responses introduced mid-flight, including across a real mutual-TLS hop between live services, so failure paths can be tested against the real transport rather than a mock of it.

### Hub - a front door for the people who read the reports

Today a Kensa report usually reaches a product owner through the CI server that built it. CI servers
are built for developers: they are organised around builds and agents, they assume you know which
project produced what, and nothing about them is designed for someone who just wants to read what an
application does.

**Kensa Hub** is a self-hostable server that is the document store for Kensa. Reports stop being
build artefacts you go and fetch and become documents that live somewhere permanent, with an address
you can put in a ticket, a wiki page or a release note.

- **A browser view of every team's reports** - zero install, no build server to navigate.
- **A curated catalogue** for readers who are not developers, separate from the raw aggregate
  engineers want.
- **Cross-bundle search** - find a behaviour across every team's tests at once, rather than knowing
  in advance which repository implemented it.
- **Grouping by delivery programme** - reports organised by the unit analysts and product owners
  actually recognise, which typically spans many teams and many services.
- **A durable address** - link to a scenario from a ticket or a wiki page and it stays there, still
  interactive, rather than expiring with the build that produced it.

### Replay - scenarios you can run and watch

A passing acceptance test proves the behaviour. It does not let a product owner or a tester *see* it,
or poke at it. **Replay** is a separate application that runs scenarios against a deployed
environment, with Kensa reports linking out to the matching scenario.

It deliberately does **not** run inside your test. Driving a live environment from within a test run
turned out to be the wrong shape - the test stays a test, and Replay is its own surface.

- **A scenario catalogue** - browse and search the scenarios available for an environment, grouped
  and tagged, with the steps and fixtures that make them up.
- **Interactive sessions** - run a scenario step by step against a chosen environment, vary the
  values that were marked as safe to vary, and send ad-hoc messages alongside the captured ones.
- **Live event view** - watch messages flow through the systems as the scenario runs, rather than
  reading about it afterwards.
- **Evidence capture** - export what happened during a run, so a demo can become a record.

Replay is in active development in its own right; the shape above is what exists today, not a
commitment to a released feature set.

## Recently shipped

### v0.9.0
- API freeze ahead of 1.0; `COMPATIBILITY.md` and the support matrix
- `@KensaExperimental` and `@KensaInternalApi` opt-in markers
- `@Epic` and the report overview page - a landing view per source where every panel segment is a filter
- Replay links on issue badges when a report is served with a `replayUrl`
- **Breaking:** implementation packages are now `internal`; `issueTrackerUrl` is nullable
- Configuration surface completed and tidied - the Java builder now reaches everything the Kotlin DSL does
- Parallel-invocation and polling-block context fixes

### v0.8.x
- **MCP server** in the CLI - structured access to test results for AI agents
- Fixture values from shared objects (`by fixtures(...)`), rendered as fixture tokens
- Chained references through value containers; qualified enum constants render cleanly
- UI testing support (Playwright and Selenium)
- **Breaking:** the legacy UI was removed

### v0.7.x
- The modern UI became the default
- **Breaking:** all deprecated API components were removed
