package dev.kensa.tabs.logs

import dev.kensa.attachments.Attachments
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.service.logs.LogQueryService
import dev.kensa.service.logs.LogRecord
import dev.kensa.tabs.DefaultKensaTabServices
import dev.kensa.tabs.KensaTabContext
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.nio.file.Path

class LogsTabRendererTest {

    private class FakeLogQueryService(private val records: List<LogRecord>) : LogQueryService {
        override fun query(sourceId: String, identifier: String): List<LogRecord> =
            records.filter { it.sourceId == sourceId && it.identifier == identifier }

        override fun queryAll(sourceId: String): List<LogRecord> = records.filter { it.sourceId == sourceId }
    }

    private val renderer = LogsTabRenderer()

    private fun contextFor(vararg records: LogRecord, identifier: String? = "inv-1"): KensaTabContext =
        KensaTabContext(
            tabId = "logs",
            tabName = "Logs",
            invocationIdentifier = identifier,
            testClass = "com.example.SomeTest",
            testMethod = "someMethod",
            invocationIndex = 0,
            invocationDisplayName = "display",
            invocationState = "Passed",
            fixtures = Fixtures(),
            capturedOutputs = CapturedOutputs(),
            attachments = Attachments(),
            services = DefaultKensaTabServices().apply {
                register(LogQueryService::class) { FakeLogQueryService(records.toList()) }
            },
            outputDir = Path.of("."),
            sourceId = "app"
        )

    @Test
    fun `renderTab reports zero entries and no text when the query finds nothing`() {
        val content = renderer.renderTab(contextFor())!!

        content.text.shouldBeNull()
        content.entries shouldBe 0
        content.records.shouldBeNull()
    }

    @Test
    fun `renderTab reports the matching records and their count`() {
        val ctx = contextFor(
            LogRecord("app", "inv-1", "first"),
            LogRecord("app", "inv-1", "second"),
            LogRecord("app", "inv-2", "other")
        )

        val content = renderer.renderTab(ctx)!!

        content.entries shouldBe 2
        content.records shouldBe listOf(LogRecord("app", "inv-1", "first"), LogRecord("app", "inv-1", "second"))
        content.text shouldBe "first\nsecond"
    }

    @Test
    fun `renderTab text is identical to render`() {
        val records = arrayOf(LogRecord("app", "inv-1", "first"), LogRecord("app", "inv-1", "second"))

        renderer.renderTab(contextFor(*records))!!.text shouldBe renderer.render(contextFor(*records))
    }

    @Test
    fun `render returns null when the query finds nothing`() {
        renderer.render(contextFor()).shouldBeNull()
    }

    @Test
    fun `renderTab returns nothing when the tab has no source id`() {
        val ctx = contextFor(LogRecord("app", "inv-1", "first")).copy(sourceId = "")

        renderer.renderTab(ctx).shouldBeNull()
    }
}
