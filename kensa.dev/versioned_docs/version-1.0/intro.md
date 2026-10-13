---
title: Introduction to BDD for Kotlin & Java
sidebar_label: Introduction
sidebar_position: 1
description: Kensa is an acceptance-testing framework for Kotlin and Java. Write Given-When-Then tests in code, with no feature files, and get an HTML report with real values, captured messages and sequence diagrams.
---

# Introduction

Kensa is an acceptance-testing framework for Kotlin and Java. You write Given-When-Then tests in ordinary code on JUnit 5 or 6, Kotest or TestNG. Every run produces an HTML report that reads as sentences, with the values from that run, every message that crossed between services, and a sequence diagram drawn from those messages.

There are no feature files and no step definitions. Kensa reads the source of the test that ran, so the method names become the words and nothing sits between the specification and the code.

## A test and its report

This is a real test from the Clearwave example:

```kotlin title="OrderServiceTest.kt"
@Test
fun `voice and broadband order is successfully completed`() {
    given(openNetworkWillCompleteTheOrder())
    and(fibreVisionWillCompleteTheOrder())

    whenever(aVoiceAndBroadbandOrderIsPlaced())

    then(theOrderConfirmation(), shouldBePending())
    thenEventuallyAllNotifications(
        shouldShowBothSuppliersCompletedSuccessfully(
            voiceSupplier = fixtures[voiceSupplier],
            broadbandSupplier = fixtures[broadbandSupplier],
        )
    )
}
```

The report for it opens with the sentence *Given open network will complete the order*, shows each order notification as it arrived from the two suppliers, and draws the exchange between the customer, the order service and both suppliers. [Open the live report](https://clearwave.kensa.dev/#/test/test::com.clearwave.OrderServiceTest?method=voice%20and%20broadband%20order%20is%20successfully%20completed) to see it.

## What goes in the report

- **Sentences** built from the test method, with `@RenderedValue` fields and parameters replaced by the values the run used.
- **Interactions**: each message your test captured, with its payload one click away.
- **Sequence diagrams** per test, and [component diagrams](./component-diagrams.md) for the system-level view from the same interactions.
- **Fixtures and outputs**: the test data the run set up and anything the test recorded along the way.
- **Links to tickets** through `@Issue`, so a story, its test and the evidence from the last run sit together.

The report is static HTML. Any CI can publish it, and any test can be [embedded](./reports/embedding.md) in a wiki page or a ticket at a stable URL.

## How it differs from Cucumber

Cucumber keeps the specification in `.feature` files and maps each line onto code through step definitions. The mapping is a second thing to keep in sync, and nothing stops the words and the code drifting apart.

Kensa has one thing: the test. Rename a method and the report changes with it. [BDD Without Gherkin](/blog/bdd-without-gherkin) goes into what Gherkin is for and what it costs.

Kensa is built for acceptance tests that sit outside a deployed application. Push a message in, watch what comes out, and let the report show the traffic.

## Where to go next

- **Set up a project:** the quickstart for [Kotlin](./quickstart/kotlin-quickstart.md), [Java](./quickstart/java-quickstart.md), [Kotest](./quickstart/kotest-quickstart.md), [TestNG](./quickstart/testng-quickstart.md) or [Maven](./quickstart/maven-quickstart.md).
- **Read a full suite:** the [example projects](./examples.md) are complete suites you can run, with their reports published live.
- **Write tests that read well:** [Writing Fluent Tests](./writing-fluent-tests.md).
- **Plan an upgrade:** from 1.0 the authoring API is frozen under semantic versioning. [Stability and Compatibility](./stability-and-compatibility.md) sets out what that covers.
