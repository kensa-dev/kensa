package dev.kensa.service.logs.docker

import dev.kensa.service.logs.LogPatterns
import dev.kensa.service.logs.LogRecord
import io.kotest.matchers.collections.shouldBeEmpty
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
            delimiterLine = DELIMITER,
            idPattern = ID_PATTERN,
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
            idPattern = ID_PATTERN,
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
            delimiterLine = DELIMITER,
            idPattern = ID_PATTERN,
            runner = fakeRunner
        )

        service.query("app", "anything").shouldHaveSize(0)
    }

    @Test
    fun `queryAll returns all blocks for a source`() {
        val fakeRunner = DockerLogsRunner { _ ->
            sequenceOf(
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
            sources = listOf(DockerCliLogQueryService.DockerSource(id = "app", container = "app-container")),
            delimiterLine = DELIMITER,
            idPattern = ID_PATTERN,
            runner = fakeRunner
        )

        val all = service.queryAll("app")
        all.shouldHaveSize(2)
        all.map { it.identifier }.toSet() shouldBe setOf("AAA", "BBB")
    }

    @Test
    fun `delimiter match allows suffix after delimiter`() {
        val fakeRunner = DockerLogsRunner { _ ->
            sequenceOf(
                "*********** 2026-02-02T12:34:56Z",
                "TrackingId: AAA",
                "Payload: one",
                "*********** trailing",
            )
        }

        val service = DockerCliLogQueryService(
            sources = listOf(DockerCliLogQueryService.DockerSource(id = "app", container = "app-container")),
            delimiterLine = DELIMITER,
            idPattern = ID_PATTERN,
            runner = fakeRunner
        )

        service.query("app", "AAA").shouldHaveSize(1).first().text.shouldContain("2026-02-02T12:34:56Z")
    }

    @Test
    fun `flushes last block at EOF without closing delimiter`() {
        val fakeRunner = DockerLogsRunner { _ ->
            sequenceOf(
                "***********",
                "TrackingId: EOF",
                "Payload: last-one"
                // no closing delimiter
            )
        }

        val service = DockerCliLogQueryService(
            sources = listOf(DockerCliLogQueryService.DockerSource(id = "app", container = "app-container")),
            delimiterLine = DELIMITER,
            idPattern = ID_PATTERN,
            runner = fakeRunner
        )

        service.query("app", "EOF").shouldHaveSize(1).first().text.shouldContain("last-one")
    }

    @Test
    fun `unknown source returns empty`() {
        val fakeRunner = DockerLogsRunner { _ -> emptySequence() }

        val service = DockerCliLogQueryService(
            sources = listOf(DockerCliLogQueryService.DockerSource(id = "app", container = "app-container")),
            delimiterLine = DELIMITER,
            idPattern = ID_PATTERN,
            runner = fakeRunner
        )

        service.query("missing", "AAA").shouldBeEmpty()
        service.queryAll("missing").shouldBeEmpty()
    }

    private companion object {
        private const val DELIMITER = "***********"
        private val ID_PATTERN = LogPatterns.idField("TrackingId")
    }
}