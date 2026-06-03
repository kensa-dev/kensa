package dev.kensa.output

import com.eclipsesource.json.Json
import dev.kensa.Configuration
import dev.kensa.render.diagram.ComponentDiagramFactory
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

class ResultWriterDataOnlyTest {

    @Test
    fun `dataOnly writer does not produce index_html, kensa_js or logo_svg`(@TempDir tempDir: Path) {
        val sourceDir = tempDir.resolve("sources").resolve("uiTest")
        val configuration = Configuration().apply {
            outputDir = sourceDir
            dataOnly = true
        }

        val writer = ResultWriter(configuration, ComponentDiagramFactory())
        writer.write(emptyList())

        sourceDir.resolve("index.html").shouldNotExist()
        sourceDir.resolve("kensa.js").shouldNotExist()
        sourceDir.resolve("logo.svg").shouldNotExist()
        sourceDir.resolve("configuration.json").shouldExist()
        sourceDir.resolve("indices.json").shouldExist()
    }

    @Test
    fun `non-dataOnly writer still produces all shell artifacts`(@TempDir tempDir: Path) {
        val configuration = Configuration().apply {
            outputDir = tempDir
            dataOnly = false
        }

        val writer = ResultWriter(configuration, ComponentDiagramFactory())
        writer.write(emptyList())

        tempDir.resolve("index.html").shouldExist()
        tempDir.resolve("kensa.js").shouldExist()
        tempDir.resolve("logo.svg").shouldExist()
    }

    @Test
    fun `dataOnly writer recreate scopes to per-source dir and leaves siblings untouched`(@TempDir tempDir: Path) {
        val siteRoot = tempDir.resolve("site")
        val uiSource = siteRoot.resolve("sources").resolve("uiTest").also { it.createDirectories() }
        val scenarioSource = siteRoot.resolve("sources").resolve("scenarioTest").also { it.createDirectories() }
        val sentinel = scenarioSource.resolve("sentinel.txt")
        sentinel.writeText("still here")

        val configuration = Configuration().apply {
            outputDir = uiSource
            dataOnly = true
        }
        ResultWriter(configuration, ComponentDiagramFactory()).write(emptyList())

        sentinel.shouldExist()
    }

    @Test
    fun `configuration json contains programme and service when set`(@TempDir tempDir: Path) {
        val configuration = Configuration().apply {
            outputDir = tempDir
            dataOnly = true
            programme = "Nexus"
            service = "Checkout"
        }

        ResultWriter(configuration, ComponentDiagramFactory()).write(emptyList())

        val json = Json.parse(tempDir.resolve("configuration.json").readText()).asObject()
        json.getString("programme", null) shouldBe "Nexus"
        json.getString("service", null) shouldBe "Checkout"
    }

    @Test
    fun `configuration json serialises programme and service as null when unset`(@TempDir tempDir: Path) {
        val configuration = Configuration().apply {
            outputDir = tempDir
            dataOnly = true
        }

        ResultWriter(configuration, ComponentDiagramFactory()).write(emptyList())

        val json = Json.parse(tempDir.resolve("configuration.json").readText()).asObject()
        json.get("programme").isNull shouldBe true
        json.get("service").isNull shouldBe true
    }
}
