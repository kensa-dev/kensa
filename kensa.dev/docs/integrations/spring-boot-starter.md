---
sidebar_position: 3
description: Wire Kensa into a Spring Boot test with a single annotation, bind kensa.* properties from application.yml, and auto-capture HTTP interactions for sequence diagrams.
---

# Spring Boot Starter

The `kensa-spring-boot-starter` collapses the JUnit and Kensa boilerplate that a Spring Boot project normally needs to make Kensa run inside its tests. A second module, `kensa-spring-boot-starter-web`, auto-captures the HTTP traffic on MockMvc, WebTestClient, `RestTemplate`, and `WebClient` calls so sequence diagrams populate without a single hand-written `Kensa.capture(...)` call.

The integration is Kotlin-only and targets **Spring Boot 3.x with JUnit 5**. A JUnit 6 variant will follow when Spring Boot 4 lands.

## What you get

- **`@KensaTest`** — one annotation that composes `@SpringBootTest` with the JUnit lifecycle extensions Kensa needs.
- **`kensa.*` properties** — `application.yml` / `application.properties` values bind to Kensa's runtime configuration before the lifecycle starts writing output.
- **Opt-in HTTP capture** — add the `-web` module and Kensa hooks into Spring's request/response pipelines automatically. No code changes to your controllers, no manual capture calls.

Each step below maps to one of those features.

## 1. Add the dependencies

```kotlin title="build.gradle.kts"
dependencies {
    testImplementation("dev.kensa:kensa-spring-boot-starter:<kensa-version>")

    // Optional: auto-capture HTTP traffic for sequence diagrams
    testImplementation("dev.kensa:kensa-spring-boot-starter-web:<kensa-version>")
}
```

The starter brings `kensa-core` and `kensa-framework-junit5` along with the Spring-side glue. It does **not** transitively pin Spring Boot, Spring Framework, or any of the JUnit Jupiter artifacts — those scopes are `compileOnly` on Kensa's side so your project's own versions win.

That means you must declare JUnit Jupiter yourself, as you almost certainly already do:

```kotlin title="build.gradle.kts"
testImplementation(platform("org.junit:junit-bom:5.14.3"))
testImplementation("org.junit.jupiter:junit-jupiter-api")
testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
```

`spring-boot-starter-test` already brings these in for most projects, so usually nothing extra is needed.

## 2. Annotate the test

```kotlin
import dev.kensa.spring.KensaTest
import org.junit.jupiter.api.Test

@KensaTest
class OrderApiTest {

    @Test
    fun `creates an order`() {
        // ...
    }
}
```

`@KensaTest` is composed of `@SpringBootTest` + `@ExtendWith(KensaSpringExtension::class, KensaExtension::class)`. The three extensions fire in that order at `beforeAll`:

1. **`SpringExtension`** — builds the application context (loaded from `@SpringBootTest`).
2. **`KensaSpringExtension`** — reads the `kensa.*` properties from the `Environment` and mutates Kensa's runtime configuration accordingly.
3. **`KensaExtension`** — initialises the Kensa lifecycle, which now sees the populated configuration.

The ordering is the correctness anchor. If you compose your own meta-annotation, keep it the same:

```kotlin
@SpringBootTest
@ExtendWith(KensaSpringExtension::class, KensaExtension::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MyIntegrationTest
```

`SpringExtension` is registered automatically by `@SpringBootTest` so it does not appear in the `@ExtendWith` list.

## 3. Configure via `application.yml`

Any `kensa.*` property in your test environment is bound to Kensa's runtime configuration before the lifecycle starts writing output. Unset properties leave the corresponding `Configuration` field at its existing default — the starter only writes through values that were actually declared.

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

| Property | Type | `Configuration` field |
| --- | --- | --- |
| `kensa.output-dir` | Path | `outputDir` |
| `kensa.output-enabled` | Boolean | `isOutputEnabled` |
| `kensa.title-text` | String | `titleText` |
| `kensa.issue-tracker-url` | URL | `issueTrackerUrl` |
| `kensa.tab-size` | Int | `tabSize` |
| `kensa.auto-open-tab` | `Tab` enum | `autoOpenTab` |
| `kensa.auto-expand-notes` | Boolean | `autoExpandNotes` |
| `kensa.setup-strategy` | `SetupStrategy` enum | `setupStrategy` |
| `kensa.flatten-output-packages` | Boolean | `flattenOutputPackages` |
| `kensa.package-display` | `PackageDisplay` enum | `packageDisplay` |
| `kensa.package-display-root` | String | `packageDisplayRoot` |

Spring's relaxed binding applies — `kensa.setup-strategy: grouped` works just as well as `kensa.setupStrategy: Grouped`.

`@TestPropertySource` or `@DynamicPropertySource` on individual test classes work too, since binding runs against the test's full Spring `Environment`:

```kotlin
@KensaTest
@TestPropertySource(properties = ["kensa.title-text=One-off override"])
class WeirdEdgeCaseTest { ... }
```

## 4. Auto-capture HTTP traffic

Add `kensa-spring-boot-starter-web` and three interceptors register automatically the moment their target classes appear on the classpath:

| Interceptor type | Triggered by | What it captures |
| --- | --- | --- |
| `HandlerInterceptor` | `spring-webmvc` on classpath | Incoming requests on `@RestController` endpoints (MockMvc, WebTestClient, real HTTP) |
| `RestTemplateCustomizer` | `spring-web`'s `RestTemplate` class | Outgoing `RestTemplate` calls |
| `WebClientCustomizer` | `spring-webflux`'s `WebClient` class | Outgoing `WebClient` calls |

Each interceptor records the request and response as a pair of Kensa interactions between two participants — `Client` and `Server` by default — so a sequence diagram of the round-trip appears in the report automatically. Outside of an active Kensa test the interceptors short-circuit, so production traffic is never captured.

### Customising the interceptors

Each capture point is registered as a named bean. To plug in your own implementation, register a bean with the matching name and a broader Spring interface type — the auto-config gates each default on `@ConditionalOnMissingBean(name = …)` and looks the override up by qualifier, so any `HandlerInterceptor` / `ClientHttpRequestInterceptor` / `ExchangeFilterFunction` swaps in without further wiring. The canonical names are exposed as constants on `KensaWebAutoConfiguration.Companion`:

| Bean name | Override type | Constant |
| --- | --- | --- |
| `kensaHandlerInterceptor` | `org.springframework.web.servlet.HandlerInterceptor` | `KensaWebAutoConfiguration.HANDLER_INTERCEPTOR_BEAN` |
| `kensaClientHttpRequestInterceptor` | `org.springframework.http.client.ClientHttpRequestInterceptor` | `KensaWebAutoConfiguration.CLIENT_HTTP_INTERCEPTOR_BEAN` |
| `kensaExchangeFilterFunction` | `org.springframework.web.reactive.function.client.ExchangeFilterFunction` | `KensaWebAutoConfiguration.EXCHANGE_FILTER_FUNCTION_BEAN` |

The simplest customisation — re-label `Client` and `Server` on the default interceptor — is still a one-liner:

```kotlin
@Configuration
class TestKensaConfig {

    @Bean(KensaWebAutoConfiguration.HANDLER_INTERCEPTOR_BEAN)
    fun kensaHandlerInterceptor(): HandlerInterceptor =
        KensaHandlerInterceptor(
            client = HttpEndpoint("Browser"),
            server = HttpEndpoint("OrderApi"),
        )
}
```

For a domain-shaped sequence diagram (e.g. `Customer → OrderService → OpenNetwork`), provide a fully custom `HandlerInterceptor` / `ClientHttpRequestInterceptor` that picks parties off the request path or content type. Same registration pattern — just return your own class against the canonical name.

### Capture across thread boundaries (real Tomcat / `RANDOM_PORT`)

The default interceptors look up the active Kensa test context via a `ThreadLocal`. That works for in-thread test paths — `MockMvc`, `WebTestClient`, and any call your test makes on the JUnit test thread itself. It does **not** work when the call is dispatched to a separate thread pool: under `@SpringBootTest(webEnvironment = RANDOM_PORT)`, Tomcat handles each request on its own worker thread, so a `HandlerInterceptor` running there sees a null Kensa context and silently no-ops. The same goes for any outbound call the application makes from inside a request — that's still a Tomcat thread.

The fix is to correlate by request header instead of thread-local. Generate a unique tracking id per test (Kensa fixture works well), attach it as `X-Tracking-Id` on every outbound call, and have your custom interceptor look up the matching `CapturedInteractions` in a shared registry instead of `TestContextHolder`:

```kotlin
@Bean(KensaWebAutoConfiguration.CLIENT_HTTP_INTERCEPTOR_BEAN)
fun kensaClientHttpRequestInterceptor(registry: TrackingRegistry): ClientHttpRequestInterceptor =
    TrackingAwareClientInterceptor(registry)

// In your test's whenever Action:
openNetworkStub.register(fixtures[trackingId], interactions)  // before sending
restTemplate.exchange(uri, POST, requestWithTrackingIdHeader, ...)
```

The `clearwave-spring-example` project on GitHub has a complete worked example (`TrackingRegistry`, party-aware interceptors, end-to-end capture across `Customer → Service → Suppliers` with async notification callbacks).

### What gets captured

Each request becomes an `HttpCapturedRequest` (method, URI, headers, body) and each response an `HttpCapturedResponse` (status, headers, body) as the content payload of the Kensa interaction. The descriptor on the interaction is the request line (e.g. `HTTP GET /orders/42`) or the response status (`HTTP 200`), which is what shows up as the arrow label in sequence diagrams.

Body capture is currently best-effort:

- `RestTemplate` outgoing — full request body captured (it's an already-buffered `ByteArray`).
- `WebClient` outgoing and `HandlerInterceptor` incoming — body is **not** captured by default. Reactive streams and servlet inputstreams are non-idempotent; tapping them without a caching wrapper would corrupt the request. Headers and the request line are still recorded.

If you need full body capture on the servlet side, register a `ContentCachingRequestWrapper` filter in your test configuration; Kensa will then read the cached buffer.

## 5. Compatibility

| | Supported |
| --- | --- |
| Spring Boot | 3.0 – 3.5 (compiled against 3.5.x; works on any 3.x) |
| JUnit | Jupiter 5.10 + |
| Language | Kotlin only |
| Build tool | Gradle or Maven |

Kensa compiles against the Spring APIs that have been stable since Spring Boot 3.0: `Binder`, `SpringExtension`, `@AutoConfiguration`, `@ConditionalOn*`, `@ConfigurationProperties`. Your project's Spring Boot version wins at runtime — the starter declares all Spring dependencies as `compileOnly`, so no version floor is forced on you.

## Roadmap

These are intentional non-goals for the initial release. Open an issue if any of them is blocking you and we will look at sequencing.

- **Feign / RabbitMQ / Kafka interceptors** — same capture pattern, deferred until requested.
- **Wiremock stub generator** — reflect-on-`@RestController` style stub emission, deferred.
- **Header / payload obfuscation** — relevant once distributed capture exists; not part of this starter.
- **JUnit 6 variant** — waits for Spring Boot 4 GA.
- **TestRestTemplate / RestClient** — deferred until reported missing.

## Migrating from manual setup

If you already had Kensa running in a Spring Boot project by stacking `@ExtendWith` annotations:

```kotlin
@SpringBootTest
@ExtendWith(KensaExtension::class)
class OrderApiTest { ... }
```

…you can simplify to `@KensaTest` and move any programmatic `Kensa.konfigure { ... }` overrides into `application.yml` under the `kensa.*` prefix. The starter does not change Kensa's behaviour outside the Spring boundary, so tests that were green before should stay green; the win is purely ergonomic.
