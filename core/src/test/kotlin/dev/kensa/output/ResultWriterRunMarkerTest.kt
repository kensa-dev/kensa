package dev.kensa.output

import com.eclipsesource.json.Json
import dev.kensa.Configuration
import dev.kensa.render.diagram.ComponentDiagramFactory
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.readText

class ResultWriterRunMarkerTest {

    @Test
    fun `creating the writer marks the run as started`(@TempDir tempDir: Path) {
        val before = Instant.now()
        ResultWriter(configuration(tempDir), ComponentDiagramFactory())

        val marker = Json.parse(tempDir.resolve("run.json").readText()).asObject()
        Instant.parse(marker.getString("startedAt", null)).toEpochMilli() shouldBeGreaterThanOrEqual before.toEpochMilli()
        marker.getLong("pid", -1) shouldBe ProcessHandle.current().pid()
        marker.getString("hostname", "").shouldNotBeBlank()
        marker.get("finishedAt").isNull shouldBe true
        tempDir.resolve("indices.json").shouldNotExist()
    }

    @Test
    fun `writing the results marks the run as finished after the indices are on disk`(@TempDir tempDir: Path) {
        val writer = ResultWriter(configuration(tempDir), ComponentDiagramFactory())
        val startedAt = Json.parse(tempDir.resolve("run.json").readText()).asObject().getString("startedAt", null)

        writer.write(emptyList())

        val marker = Json.parse(tempDir.resolve("run.json").readText()).asObject()
        marker.getString("startedAt", null) shouldBe startedAt
        marker.getLong("pid", -1) shouldBe ProcessHandle.current().pid()
        Instant.parse(marker.getString("finishedAt", null)).toEpochMilli() shouldBeGreaterThanOrEqual Instant.parse(startedAt).toEpochMilli()
        tempDir.resolve("indices.json").shouldExist()
    }

    private fun configuration(dir: Path) = Configuration().apply {
        outputDir = dir
        dataOnly = true
    }
}
