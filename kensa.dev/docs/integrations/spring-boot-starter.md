---
sidebar_position: 3
description: Wire Kensa into Spring Boot with a single annotation, bind kensa.* properties from application.yml, and auto-capture HTTP interactions for sequence diagrams.
---

# Spring Boot Starter

The `kensa-spring-boot-starter` collapses the JUnit and Kensa boilerplate that Spring Boot users normally write to make Kensa run inside their tests. One annotation replaces the usual stack of `@SpringBootTest` + `@ExtendWith(KensaExtension::class)`, and `kensa.*` properties in `application.yml` flow straight into Kensa's runtime configuration.

A separate `kensa-spring-boot-starter-web` module auto-captures HTTP traffic — incoming MockMvc / WebTestClient requests and outgoing `RestTemplate` / `WebClient` calls — so sequence diagrams populate without a single hand-written `Kensa.capture(...)` call.

This integration is Kotlin-only and targets Spring Boot 3.x with JUnit 5.

## 1. Add the starter

```kotlin title="build.gradle.kts"
dependencies {
    testImplementation("dev.kensa:kensa-spring-boot-starter:<kensa-version>")

    // Optional: auto-capture HTTP traffic for sequence diagrams
    testImplementation("dev.kensa:kensa-spring-boot-starter-web:<kensa-version>")
}
```

The starter pulls in `kensa-core`, `kensa-framework-junit5`, and the Spring + JUnit bits it needs. You do not need to declare them yourself.

## 2. Annotate the test

```kotlin
import dev.kensa.spring.KensaTest

@KensaTest
class OrderApiTest {

    @Test
    fun `creates an order`() {
        // ...
    }
}
```

`@KensaTest` is composed of `@SpringBootTest`, `@ExtendWith(KensaSpringExtension::class)`, and `@ExtendWith(KensaExtension::class)`. The extensions run in that order so Spring builds the context first, the bridge binds `kensa.*` properties, and Kensa's lifecycle picks up the populated configuration.

If you already have your own `@SpringBootTest` meta-annotation, compose Kensa into it directly:

```kotlin
@SpringBootTest
@ExtendWith(KensaSpringExtension::class, KensaExtension::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MyTest
```

## 3. Configure via `application.yml`

Any `kensa.*` property in your application environment is bound to Kensa's runtime configuration before the lifecycle starts writing output.

```yaml title="src/test/resources/application.yml"
kensa:
  title-text: "Order Service"
  output-dir: build/kensa-output
  setup-strategy: Grouped
  auto-open-tab: SequenceDiagram
  package-display: HideCommonPackages
  issue-tracker-url: "https://jira.example.com/browse/"
  output-enabled: true
```

| Property | Type | Maps to |
| --- | --- | --- |
| `kensa.output-dir` | Path | `Configuration.outputDir` |
| `kensa.output-enabled` | Boolean | `Configuration.isOutputEnabled` |
| `kensa.title-text` | String | `Configuration.titleText` |
| `kensa.issue-tracker-url` | URL | `Configuration.issueTrackerUrl` |
| `kensa.tab-size` | Int | `Configuration.tabSize` |
| `kensa.auto-open-tab` | `Tab` enum | `Configuration.autoOpenTab` |
| `kensa.auto-expand-notes` | Boolean | `Configuration.autoExpandNotes` |
| `kensa.setup-strategy` | `SetupStrategy` enum | `Configuration.setupStrategy` |
| `kensa.flatten-output-packages` | Boolean | `Configuration.flattenOutputPackages` |
| `kensa.package-display` | `PackageDisplay` enum | `Configuration.packageDisplay` |
| `kensa.package-display-root` | String | `Configuration.packageDisplayRoot` |

Spring's relaxed binding applies — `kensa.setup-strategy: grouped` works as well as `kensa.setupStrategy: Grouped`.

Unset properties leave the corresponding `Configuration` field at its existing default; the starter only writes through values that were actually declared.

## 4. Auto-capture HTTP traffic

Add `kensa-spring-boot-starter-web` and the starter wires three interceptors as soon as their target classes are on the classpath:

- `HandlerInterceptor` — captures incoming MockMvc / WebTestClient requests on `@RestController` endpoints.
- `RestTemplateCustomizer` — adds a `ClientHttpRequestInterceptor` to every `RestTemplate` Spring builds.
- `WebClientCustomizer` — adds an `ExchangeFilterFunction` to every `WebClient.Builder` Spring builds.

Each interceptor records request and response as Kensa interactions between two participants — `Client` and `Server` by default — so a sequence diagram of the request flow renders without further code. The interceptors are no-ops outside a Kensa test, so production traffic is never captured.

If you need different participant names, register your own interceptor as a `@Bean` — the auto-config backs off via `@ConditionalOnMissingBean`:

```kotlin
@Bean
fun kensaHandlerInterceptor(): KensaHandlerInterceptor =
    KensaHandlerInterceptor(client = HttpEndpoint("Browser"), server = HttpEndpoint("OrderApi"))
```

## What about Feign / AMQP / Kafka?

Not yet. The capture pattern is straightforward to extend — Feign `RequestInterceptor`, `RabbitTemplate` AOP advice, `KafkaTemplate` `ProducerInterceptor` — but those interceptor modules are deferred until someone asks for them. Open an issue if you want one to land sooner.

## What about JUnit 6?

The starter targets JUnit 5 because that is what Spring Boot 3.x ships. A JUnit 6 variant will follow when Spring Boot 4 lands.
