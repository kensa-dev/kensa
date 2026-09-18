package dev.kensa.service.logs

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CompositeLogQueryServiceTest {

    @Test
    fun `sources concatenates delegate sources in registration order`() {
        val first = FakeLogQueryService(listOf(LogSource(id = "first", file = "first.log", present = true)))
        val second = FakeLogQueryService(listOf(LogSource(id = "second", file = "second.log", present = false)))

        val service = CompositeLogQueryService(
            linkedMapOf(
                "first" to first,
                "second" to second
            )
        )

        service.sources() shouldBe listOf(
            LogSource(id = "first", file = "first.log", present = true),
            LogSource(id = "second", file = "second.log", present = false)
        )
    }

    @Test
    fun `sources reports a multi source delegate once however many ids it is registered under`() {
        val delegate = FakeLogQueryService(
            listOf(
                LogSource(id = "app", file = "app-container", present = true),
                LogSource(id = "worker", file = "worker-container", present = true)
            )
        )

        val service = CompositeLogQueryService(
            linkedMapOf(
                "app" to delegate,
                "worker" to delegate
            )
        )

        service.sources() shouldBe listOf(
            LogSource(id = "app", file = "app-container", present = true),
            LogSource(id = "worker", file = "worker-container", present = true)
        )
    }

    private class FakeLogQueryService(private val fakeSources: List<LogSource>) : LogQueryService {
        override fun query(sourceId: String, identifier: String): List<LogRecord> = emptyList()
        override fun queryAll(sourceId: String): List<LogRecord> = emptyList()
        override fun sources(): List<LogSource> = fakeSources
    }
}
