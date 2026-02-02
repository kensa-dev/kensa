package dev.kensa.service.logs.docker

import dev.kensa.service.logs.LogRecord
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class DockerCliLogQueryServiceTest {

    @Test
    fun `indexes blocks by TrackingId and preserves sourceId`() {
        val fakeRunner = DockerLogsRunner { _ ->
            sequenceOf(
                "noise before",
                "***********",
                "TrackingId: AAA",
                "Payload: one",
                "***********",
                "***********",
                "TrackingId: BBB",
                "Payload: two",
                "***********"
            )
        }

        val service = DockerCliLogQueryService(
            sources = listOf(
                DockerCliLogQueryService.DockerSource(id = "app", container = "app-container")
            ),
            delimiterLine = "***********",
            idField = "TrackingId",
            runner = fakeRunner
        )

        val a: List<LogRecord> = service.query("app", "AAA")
        a.shouldHaveSize(1)
        a[0].sourceId shouldBe "app"
        a[0].identifier shouldBe "AAA"
        a[0].text.contains("Payload: one") shouldBe true

        val b: List<LogRecord> = service.query("app", "BBB")
        b.shouldHaveSize(1)
        b[0].sourceId shouldBe "app"
        b[0].text.contains("Payload: two") shouldBe true
    }

    @Test
    fun `indexes across multiple docker sources`() {
        val fakeRunner = DockerLogsRunner { container ->
            when (container) {
                "app-container" -> sequenceOf(
                    "***********",
                    "TrackingId: AAA",
                    "Payload: app",
                    "***********"
                )

                "worker-container" -> sequenceOf(
                    "***********",
                    "TrackingId: AAA",
                    "Payload: worker",
                    "***********"
                )

                else -> emptySequence()
            }
        }

        val service = DockerCliLogQueryService(
            sources = listOf(
                DockerCliLogQueryService.DockerSource(id = "app", container = "app-container"),
                DockerCliLogQueryService.DockerSource(id = "worker", container = "worker-container")
            ),
            delimiterLine = "***********",
            idField = "TrackingId",
            runner = fakeRunner
        )

        service.query("worker", "AAA")
            .shouldHaveSize(1)
            .first().should {
                it.text.shouldContain("Payload: worker")
            }

        service.query("app", "AAA")
            .shouldHaveSize(1)
            .first().should {
                it.text.shouldContain("Payload: app")
            }
    }

    @Test
    fun `ignores blocks without id field`() {
        val fakeRunner = DockerLogsRunner { _ ->
            sequenceOf(
                "***********",
                "From: A",
                "Payload: no id here",
                "***********"
            )
        }

        val service = DockerCliLogQueryService(
            sources = listOf(
                DockerCliLogQueryService.DockerSource(id = "app", container = "app-container")
            ),
            delimiterLine = "***********",
            idField = "TrackingId",
            runner = fakeRunner
        )

        service.query("app", "anything").shouldHaveSize(0)
    }
}