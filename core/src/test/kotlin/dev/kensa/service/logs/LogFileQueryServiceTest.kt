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

        service.query("missing").shouldBeEmpty()
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


        service.query("shared").should { records ->
            records.map { it.sourceId } shouldContainExactly listOf("ONE", "TWO")
            records.map { it.identifier }.distinct() shouldContainExactly listOf("shared")
        }

        service.query("shared").should { records ->
            records.forAll { it.text shouldContain "from-" }
        }

        service.query("other").map { it.sourceId } shouldContainExactly listOf("TWO")
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

        service.query("abc").shouldBeEmpty()
    }

    @Test
    fun `block delimiter comparison is trim based and block text is trimmed`() {
        val log = tempDir.resolve("trim.log")
        log.writeText(
            // delimiter lines include extra spaces; class compares line.trim() to delimiterLine.trim()
            """
                 $DELIMITER  
                $ID_FIELD: x
    
                payload
    
                 $DELIMITER    
            """.trimIndent(),
            UTF_8
        )

        val service = LogFileQueryService(
            sources = listOf(FileSource(id = "A", path = log)),
            delimiterLine = DELIMITER,
            idField = ID_FIELD
        )

        service.query("x").should { records ->
            records shouldHaveSize 1

            // The stored block includes delimiterLine as appended (exact delimiterLine), and is trimmed overall.
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

        service.query("eof") shouldHaveSize 1
    }

    private companion object {
        const val DELIMITER = "---BLOCK---"
        const val ID_FIELD = "id"
    }
}