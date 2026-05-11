---
sidebar_position: 4
description: Component diagrams give a top-down view of which actors talk to which, derived automatically from the same captured interactions that drive sequence diagrams.
---

# Component Diagrams

A component diagram is a top-down view of which actors communicate with which others across a test run. It is derived from the same captured interactions that drive sequence diagrams, so no additional instrumentation is required. If your tests already produce sequence diagrams, you already get component diagrams.

## Per-test view

When a test has recorded interactions between two or more actors, a **Component Diagram** tab appears in the HTML report next to the Sequence Diagram tab. The tab is hidden for tests that have no interactions.

## System View

A **System View** entry in the report sidebar shows an aggregate component diagram built from every interaction recorded during the test run, giving a consolidated picture of how the components in your system are connected. The entry is not shown when no interactions were captured.

In [site mode](./build-plugins/site-mode.md) — where multiple sourcesets (Gradle) or executions (Maven) feed a single aggregated site — each source root in the sidebar has its own **System View** entry. The diagram for each source is built only from that source's interactions; sources are not collapsed into a single shared architecture (each sourceset typically exercises a different slice of your system, so a combined view would misrepresent both).

## How edges are computed

- Edges are `(from, to)` pairs extracted from captured interactions.
- Repeat invocations between the same pair are deduplicated to a single edge.
- Self-loops (an actor invoking itself) are kept.
- Bidirectional pairs render as two separate arrows — A → B and B → A — each pointing in the direction it was recorded.

## Configuration

There is no configuration specific to component diagrams today. The `SetupStrategy` option — documented in [Configuration](./api/configuration.md) — controls whether setup interactions are included, in the same way it does for sequence diagrams.

## Limitations

- Components are rendered as plain `[Name]` boxes. Typed glyphs (database, queue, and so on) are not yet supported in the component view.
- Edges carry no labels and no call counts.
- Very large diagrams are not automatically split or simplified.
