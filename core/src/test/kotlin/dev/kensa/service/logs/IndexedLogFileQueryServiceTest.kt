package dev.kensa.service.logs

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path
import kotlin.io.path.writeText

class IndexedLogFileQueryServiceTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `query returns empty list when identifier not present`() {
        val log = tempDir.resolve("a.log")
        log.writeText(
            """
               $DELIMITER
                id: abc
                hello
               $DELIMITER
            """.trimIndent(),
            UTF_8
        )

        val service = IndexedLogFileQueryService(
            source = FileSource(id = "A", path = log),
            idPattern = ID_PATTERN,
            delimiterLine = DELIMITER
        )

        service.query("A", "missing").shouldBeEmpty()
    }

    @Test
    fun `query returns records for identifier and unknown source is empty`() {
        val log = tempDir.resolve("two.log")
        log.writeText(
            """
               $DELIMITER
                id: shared
                from-two
               $DELIMITER
               $DELIMITER
                id: other
                something-else
               $DELIMITER
            """.trimIndent(),
            UTF_8
        )

        val service = IndexedLogFileQueryService(
            source = FileSource(id = "TWO", path = log),
            idPattern = ID_PATTERN,
            delimiterLine = DELIMITER
        )

        service.query("TWO", "shared").should { records ->
            records shouldHaveSize 1
            records.single().sourceId shouldBe "TWO"
            records.single().identifier shouldBe "shared"
            records.single().text shouldContain "from-two"
        }

        service.query("TWO", "other").should { records ->
            records shouldHaveSize 1
            records.single().text shouldContain "something-else"
        }

        service.query("missing", "shared").shouldBeEmpty()
        service.queryAll("missing").shouldBeEmpty()
    }

    @Test
    fun `does not emit record when id field is blank or missing`() {
        val log = tempDir.resolve("bad.log")
        log.writeText(
            """
               $DELIMITER
                id:
                should-be-ignored
               $DELIMITER
               $DELIMITER
                notId: abc
                also-ignored
               $DELIMITER
            """.trimIndent(),
            UTF_8
        )

        val service = IndexedLogFileQueryService(
            source = FileSource(id = "A", path = log),
            idPattern = ID_PATTERN,
            delimiterLine = DELIMITER
        )

        service.query("A", "abc").shouldBeEmpty()
        service.queryAll("A").shouldBeEmpty()
    }

    @Test
    fun `block delimiter comparison is trim and regex based and preserves full delimiter line`() {
        val log = tempDir.resolve("trim.log")
        log.writeText(
            """
                 $DELIMITER   2026-02-02T12:34:56Z
                id: x

                payload

                 $DELIMITER    extra-junk
            """.trimIndent(),
            UTF_8
        )

        val service = IndexedLogFileQueryService(
            source = FileSource(id = "A", path = log),
            idPattern = ID_PATTERN,
            delimiterLine = DELIMITER
        )

        service.query("A", "x").should { records ->
            records shouldHaveSize 1

            val record = records.single()
            record.text.startsWith(DELIMITER) shouldBe true
            record.text.contains("2026-02-02T12:34:56Z") shouldBe true
            record.text.contains("payload") shouldBe true
        }
    }

    @Test
    fun `flushes last block at EOF without closing delimiter`() {
        val log = tempDir.resolve("eof.log")
        log.writeText(
            """
               $DELIMITER
                id: eof
                last-one
            """.trimIndent(),
            UTF_8
        )

        val service = IndexedLogFileQueryService(
            source = FileSource(id = "A", path = log),
            idPattern = ID_PATTERN,
            delimiterLine = DELIMITER
        )

        service.query("A", "eof") shouldHaveSize 1
    }

    @Test
    fun `queryAll returns all blocks across identifiers for a source`() {
        val log = tempDir.resolve("all.log")
        log.writeText(
            """
               $DELIMITER
                id: a
                one
               $DELIMITER
               $DELIMITER
                id: b
                two
               $DELIMITER
            """.trimIndent(),
            UTF_8
        )

        val service = IndexedLogFileQueryService(
            source = FileSource(id = "A", path = log),
            idPattern = ID_PATTERN,
            delimiterLine = DELIMITER
        )

        val all = service.queryAll("A")
        all.shouldHaveSize(2)
        all.map { it.identifier }.toSet() shouldBe setOf("a", "b")
    }

    private companion object {
        private const val DELIMITER = "---BLOCK---"
        private val ID_PATTERN = LogPatterns.idField("id")
    }
}