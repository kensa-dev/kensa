package dev.kensa.service.logs

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path
import kotlin.io.path.writeText

class RawLogFileQueryServiceTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `queryAll returns full file as a single record`() {
        val log = tempDir.resolve("app.log")
        log.writeText(
            """
            started
            doing work
            finished
            """.trimIndent(),
            UTF_8
        )

        val service = RawLogFileQueryService(
            sources = listOf(FileSource(id = "appLog", path = log))
        )

        val records = service.queryAll("appLog")
        records.shouldHaveSize(1)
        records.single().sourceId shouldBe "appLog"
        records.single().identifier shouldBe ""
        records.single().text shouldBe "started\ndoing work\nfinished"
    }

    @Test
    fun `query returns only matching lines when identifier provided`() {
        val log = tempDir.resolve("app.log")
        log.writeText(
            """
            INFO started
            ERROR boom
            INFO continuing
            ERROR boom again
            """.trimIndent(),
            UTF_8
        )

        val service = RawLogFileQueryService(
            sources = listOf(FileSource(id = "appLog", path = log))
        )

        val records = service.query("appLog", "ERROR")
        records.shouldHaveSize(1)

        val record = records.single()
        record.sourceId shouldBe "appLog"
        record.identifier shouldBe "ERROR"
        record.text shouldBe "ERROR boom\nERROR boom again"
    }

    @Test
    fun `queryAll returns empty list for missing file or unknown source`() {
        val missing = tempDir.resolve("missing.log")

        val service = RawLogFileQueryService(
            sources = listOf(FileSource(id = "missingSource", path = missing))
        )

        service.queryAll("missingSource").shouldBeEmpty()
        service.queryAll("unknown").shouldBeEmpty()
    }

    @Test
    fun `tailLines limits returned content to last N lines`() {
        val log = tempDir.resolve("tail.log")
        log.writeText(
            """
            one
            two
            three
            four
            """.trimIndent(),
            UTF_8
        )

        val service = RawLogFileQueryService(
            sources = listOf(FileSource(id = "appLog", path = log)),
            tailLines = 2
        )

        val all = service.queryAll("appLog")
        all.shouldHaveSize(1)
        all.single().text shouldBe "three\nfour"
    }

    @Test
    fun `tailLines of zero returns single empty record`() {
        val log = tempDir.resolve("tail0.log")
        log.writeText(
            """
            one
            two
            """.trimIndent(),
            UTF_8
        )

        val service = RawLogFileQueryService(
            sources = listOf(FileSource(id = "appLog", path = log)),
            tailLines = 0
        )

        val all = service.queryAll("appLog")
        all.shouldHaveSize(1)
        all.single().text shouldBe ""
    }

    @Test
    fun `query returns empty list for unknown source or missing file`() {
        val missing = tempDir.resolve("missing.log")

        val service = RawLogFileQueryService(
            sources = listOf(FileSource(id = "missingSource", path = missing))
        )

        service.query("unknown", "ERROR").shouldBeEmpty()
        service.query("missingSource", "ERROR").shouldBeEmpty()
    }

    @Test
    fun `query filtering is applied after tailing`() {
        val log = tempDir.resolve("tail-filter.log")
        log.writeText(
            """
            INFO one
            ERROR old
            INFO two
            INFO three
            ERROR new
            """.trimIndent(),
            UTF_8
        )

        val service = RawLogFileQueryService(
            sources = listOf(FileSource(id = "appLog", path = log)),
            tailLines = 2
        )

        // Tail is: "INFO three", "ERROR new" so only the new ERROR should remain
        val records = service.query("appLog", "ERROR")
        records.shouldHaveSize(1)
        records.single().text shouldBe "ERROR new"
    }
}