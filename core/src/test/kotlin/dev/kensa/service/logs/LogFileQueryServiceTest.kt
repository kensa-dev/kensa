package dev.kensa.service.logs

import dev.kensa.service.logs.LogFileQueryService.FileSource
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path
import kotlin.io.path.writeText

class LogFileQueryServiceTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `query returns empty list when identifier not present`() {
        val log = tempDir.resolve("a.log")
        log.writeText(
            """
               $DELIMITER
                $ID_FIELD: abc
                hello
               $DELIMITER
            """.trimIndent(),
            UTF_8
        )

        val service = LogFileQueryService(
            sources = listOf(FileSource(id = "A", path = log)),
            delimiterLine = DELIMITER,
            idField = ID_FIELD
        )

        service.query("A", "missing").shouldBeEmpty()
    }

    @Test
    fun `query returns records for identifier across multiple sources and ignores missing files`() {
        val log1 = tempDir.resolve("one.log")
        val log2 = tempDir.resolve("two.log")
        val missing = tempDir.resolve("missing.log")

        log1.writeText(
            """
               $DELIMITER
                $ID_FIELD: shared
                from-one
               $DELIMITER
            """.trimIndent(),
            UTF_8
        )

        log2.writeText(
            """
               $DELIMITER
                $ID_FIELD: shared
                from-two
               $DELIMITER
               $DELIMITER
                $ID_FIELD: other
                something-else
               $DELIMITER
            """.trimIndent(),
            UTF_8
        )

        val service = LogFileQueryService(
            sources = listOf(
                FileSource(id = "ONE", path = log1),
                FileSource(id = "MISSING", path = missing),
                FileSource(id = "TWO", path = log2),
            ),
            delimiterLine = DELIMITER,
            idField = ID_FIELD
        )

        service.query("ONE", "shared").should { records ->
            records.map { it.sourceId } shouldContainExactly listOf("ONE")
            records.map { it.identifier }.distinct() shouldContainExactly listOf("shared")
        }

        service.query("TWO", "shared").should { records ->
            records.forAll { it.text shouldContain "from-" }
        }

        service.query("TWO", "other").map { it.sourceId } shouldContainExactly listOf("TWO")

        service.query("MISSING", "missing").shouldBeEmpty()
    }

    @Test
    fun `does not emit record when id field is blank or missing`() {
        val log = tempDir.resolve("bad.log")
        log.writeText(
            """
               $DELIMITER
                $ID_FIELD:
                should-be-ignored
               $DELIMITER
               $DELIMITER
                notId: abc
                also-ignored
               $DELIMITER
            """.trimIndent(),
            UTF_8
        )

        val service = LogFileQueryService(
            sources = listOf(FileSource(id = "A", path = log)),
            delimiterLine = DELIMITER,
            idField = ID_FIELD
        )

        service.query("A", "abc").shouldBeEmpty()
    }

    @Test
    fun `block delimiter comparison is trim and startsWith based and block text is trimmed`() {
        val log = tempDir.resolve("trim.log")
        log.writeText(
            """
                 $DELIMITER   2026-02-02T12:34:56Z
                $ID_FIELD: x

                payload

                 $DELIMITER    extra-junk
            """.trimIndent(),
            UTF_8
        )

        val service = LogFileQueryService(
            sources = listOf(FileSource(id = "A", path = log)),
            delimiterLine = DELIMITER,
            idField = ID_FIELD
        )

        service.query("A", "x").should { records ->
            records shouldHaveSize 1

            val record = records.single()
            record.text.startsWith(DELIMITER) shouldBe true
            record.text.contains("payload") shouldBe true
        }
    }

    @Test
    fun `flushes last block at EOF without closing delimiter`() {
        val log = tempDir.resolve("eof.log")
        log.writeText(
            """
               $DELIMITER
                $ID_FIELD: eof
                last-one
            """.trimIndent(),
            UTF_8
        )

        val service = LogFileQueryService(
            sources = listOf(FileSource(id = "A", path = log)),
            delimiterLine = DELIMITER,
            idField = ID_FIELD
        )

        service.query("A", "eof") shouldHaveSize 1
    }

    private companion object {
        const val DELIMITER = "---BLOCK---"
        const val ID_FIELD = "id"
    }
}