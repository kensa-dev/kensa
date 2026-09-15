---
title: Example Projects
sidebar_label: Examples
sidebar_position: 96
description: The Kensa example projects - the in-repo adoptabot module and the three Clearwave showcases for JUnit, Spring Boot and TestNG - what each one demonstrates, and where to read its report.
---

# Examples

The example projects are complete, runnable test suites that show Kensa features working together rather than in isolation: fixtures, captured interactions, sequence diagrams, custom tabs, async assertions, UI tests and the Spring Boot starter, each in a realistic setting. The Clearwave reports are published from CI after every run, so you can read a finished report before you write a test.

| Example | Where it lives | Framework | What it demonstrates | Report |
|---|---|---|---|---|
| adoptabot | [`examples/adoptabot`](https://github.com/kensa-dev/kensa/tree/master/examples/adoptabot) in the Kensa repository | JUnit 6, Kotlin and Java | Fixtures, captured interactions with a custom renderer, custom and log tabs, notes | Run locally |
| clearwave-example | [kensa-dev/clearwave-example](https://github.com/kensa-dev/clearwave-example) | JUnit 6, Kotlin and Java | http4k stubs, fixtures and captured outputs, async assertions, the field assertion DSL, Playwright and Selenium UI tests, site mode, System View | [Live report](https://clearwave.kensa.dev) |
| clearwave-spring-example | [kensa-dev/clearwave-spring-example](https://github.com/kensa-dev/clearwave-spring-example) | JUnit 5, Spring Boot 3.5 | The Spring Boot starter and web module, auto-captured HTTP traffic with custom parties, async supplier callbacks | [Live report](https://clearwave-spring.kensa.dev) |
| clearwave-testng-example | [kensa-dev/clearwave-testng-example](https://github.com/kensa-dev/clearwave-testng-example) | TestNG, Kotlin and Java | The clearwave-example service tests on TestNG | [Live report](https://clearwave-testng.kensa.dev) |

## Adoptabot

Adoptabot is a small robot adoption service built with http4k and Jackson: clients list robots and shelters, adopt a robot and register as adopters. It lives in the Kensa repository so it always builds against the current source, and it is the one module in the repository that targets JDK 21 (everything published targets 17).

What it shows:

- Kotlin tests implementing `KensaTest` with `WithKotest`, and a Java test (`UserAuthenticationTest`) using `WithHamcrest`. See [Assertion Bridges](./api/assertion-bridges.md).
- A `FixtureContainer` (`AdoptabotFixtures`) with primary fixtures and fixtures derived from them, registered once with `registerFixtures` and read in tests through `fixtures(...)`. See [Fixtures](./api/fixtures.md).
- Interactions captured between a `Client` and `AdoptionService` party, which drive the sequence diagram; the report opens on that tab (`autoOpenTab = Tab.SequenceDiagram`). See [Configuration](./api/configuration.md#sequence-diagrams).
- A custom `InteractionRenderer` (`ResponseRenderer`) that pretty-prints the JSON body and exposes status and headers as attribute groups. It is the renderer reproduced on the [Interaction Renderers](./api/interaction-renderers.md) page.
- `@RenderedValue` on a field, `@Notes` with Markdown and links to other tests, and `///` statement notes inside a test body. See [Annotations](./api/annotations.md) and [Sentence Hints](./api/sentence-hints.md).
- `@KensaTab` at class and method level: a custom `KensaTabRenderer`, and two `LogsTabRenderer` tabs (a suite-scoped raw log file and a per-invocation indexed one) fed by a `LogQueryService` registered in `konfigure`. See [Log Tabs](./api/log-tabs.md).
- Kensa configured from a JUnit extension (`AdoptabotExtension`) that also starts and stops the server.

Run it from the root of the Kensa repository:

```bash
./gradlew :adoptabot:test
```

The report is written to `examples/adoptabot/build/kensa-output/index.html`.

## Clearwave (JUnit 6)

Clearwave is a fictional telecoms provider with two services: a `FeasibilityService` that asks two suppliers whether broadband can be delivered to an address, and an `OrderService` that places an order and processes the suppliers' notifications. Both are built with http4k, and the suppliers (`OpenNetwork`, which speaks JSON, and `FibreVision`, which speaks XML) are http4k stubs started by the test extension. A small React page for the feasibility check is the target of the UI tests.

What it shows:

- A shared `ClearwaveExtension` that registers fixtures and captured outputs and calls `konfigure` once: `packageDisplay` and `packageDisplayRoot`, acronyms such as FTTP and PSTN, request and response interaction renderers, and the sequence diagram as the opening tab. An abstract `ClearwaveTest` base class carries `@UseSetupStrategy(SetupStrategy.Grouped)`. See [Configuration](./api/configuration.md) and [Annotations](./api/annotations.md#usesetupstrategy).
- Fixtures declared in Kotlin (`TelecomsFixtures`) and in Java (`JavaTelecomsFixtures`), which the [Fixtures](./api/fixtures.md) page is written from, and typed [captured outputs](./api/outputs.md) with a highlighted order confirmation.
- Interactions captured against a `TelecomsParty` for every hop, so each test has a sequence diagram and a [component diagram](./component-diagrams.md), and the report sidebar has a System View for each source.
- A per-test `TrackingId` sent as a header on every request, which the stubs use to route primed scenarios and captured exchanges back to the right test, so the suite runs in parallel.
- `thenEventually` against the notifications the stubs fire back to the `OrderService`. See [Async Assertions](./api/async-assertions.md).
- `@Notes` with Markdown tables and links between test classes.
- `FieldDslExamplesTest`, which uses the [Field Assertion DSL](./field-assertion-dsl/overview.mdx) for JSON and XML, declares its field objects with `@Sources` and renders them with `@RenderedValueWithHint`. See [Annotations](./api/annotations.md#sources).
- Java versions of the two service tests using `WithHamcrest`.
- UI tests in their own `uiTest` source set: `FeasibilityUiPlaywrightTest` extends `KensaPlaywrightUiTest` and `FeasibilityUiSeleniumTest` extends `KensaSeleniumUiTest`, each with its own `UserStub`, and both take named screenshots with `autoScreenshotOnFailure` on. See [UI Testing](./ui-testing/overview.md), the [UI quickstart](./ui-testing/quickstart.md) and [Screenshots](./ui-testing/screenshots.md).
- The [Gradle plugin](./build-plugins/gradle-plugin.md) in [site mode](./build-plugins/site-mode.md), with `test` and `uiTest` as output source sets and a `sourceTitles` label for each.

Run it from a clone of the repository. The service tests need no browser:

```bash
./gradlew test
```

The UI tests need Chrome installed and, once, the Playwright browsers:

```bash
./gradlew installPlaywrightBrowsers
./gradlew uiTest
```

`./gradlew test uiTest assembleKensaSite` runs both and assembles the site at `build/kensa-site`; that is the command the publishing workflow uses. Open it with `kensa --dir build/kensa-site` (see the [CLI](./cli.md)), or read the published copy at [clearwave.kensa.dev](https://clearwave.kensa.dev).

## Clearwave Spring

The same domain ported to Spring Boot Web: the two services are `@RestController` and `@Service` beans calling the suppliers through `RestTemplate`, the suppliers are two small Spring Boot stub applications, and the `OrderService` receives the suppliers' asynchronous notifications on a callback endpoint of the same application. There are no UI tests.

What it shows:

- The `kensa-spring-boot-starter` and `kensa-spring-boot-starter-web` modules. An abstract `ClearwaveSpringTest` base class composes `@SpringBootTest(webEnvironment = RANDOM_PORT)` with `@ExtendWith(KensaSpringExtension::class, KensaExtension::class)`, in that order. See [Spring Boot Starter](./integrations/spring-boot-starter.md).
- `kensa.*` properties in the test `application.yml` (title, setup strategy, opening tab, package display), with the settings the property binding does not cover (acronyms, renderers) applied by `konfigure` in a `@TestConfiguration` bean's `@PostConstruct`.
- HTTP traffic captured automatically by the web module's interceptors, with the auto-configured `HandlerInterceptor` and `ClientHttpRequestInterceptor` beans replaced by party-aware ones so the sequence diagram reads `Customer`, `FeasibilityService`, `OpenNetwork` and `FibreVision` rather than the default `Client` and `Server`. See [customising the interceptors](./integrations/spring-boot-starter.md#customising-the-interceptors).
- `thenEventually` for the supplier callbacks, with `interactions.captureTimePassing` marking the wait on the sequence diagram, and an `@ExpandableRenderedValue` function supplying the expected notification lifecycle.
- The same fixtures, captured outputs and `@Notes` as the http4k version.

Run it from a clone of the repository:

```bash
./gradlew test
```

The report is written to `build/kensa-output`; open it with `kensa --dir build/kensa-output`.

## Clearwave TestNG

The clearwave-example service tests, Kotlin and Java, on TestNG. The domain, the stubs, the tracking-id pattern and the Kensa configuration are unchanged; only the runner differs, so the two projects are a direct comparison of the JUnit and TestNG adapters.

What it shows:

- Tests implementing `dev.kensa.testng.KensaTest`. Kensa's own `KensaTestNgListener` is discovered through `ServiceLoader`, so the base class registers only the project's `ClearwaveTestNgListener` with `@Listeners`: an `ISuiteListener` that registers fixtures and captured outputs, calls `konfigure` and starts the stubs in `onStart`. See the [TestNG quickstart](./quickstart/testng-quickstart.md).
- Java tests with `WithHamcrest`, the field assertion DSL example with `@Sources`, `thenEventually`, `@Notes` and `@UseSetupStrategy(SetupStrategy.Grouped)`, as in the JUnit project.
- The Gradle plugin in site mode with a single `test` source.

Run it from a clone of the repository:

```bash
./gradlew test assembleKensaSite
```

The site is assembled at `build/kensa-site`; open it with `kensa --dir build/kensa-site`.

## In the wild

[Malai](https://malaibank.com/) is a demonstration banking platform, independent of Kensa, that publishes its own end-to-end flows as a live Kensa report: [malaibank.com/end-to-end-flows](https://malaibank.com/end-to-end-flows). It is linked from the site's front page as the sequence diagrams of the calls the system actually made, which is the use the report is built for. Nothing on that site is maintained by the Kensa project; it is here as a real report from a real system, with a domain the Clearwave examples do not cover.
