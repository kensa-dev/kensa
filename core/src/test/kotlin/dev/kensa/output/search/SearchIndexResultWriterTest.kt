package dev.kensa.output.search

import com.eclipsesource.json.Json
import dev.kensa.Configuration
import dev.kensa.output.ResultWriter
import dev.kensa.output.json.fakeTestContainer
import dev.kensa.output.json.fakeTestInvocation
import dev.kensa.output.json.fakeTestMethodContainer
import dev.kensa.render.diagram.ComponentDiagramFactory
import dev.kensa.util.NamedValue
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.readText

class SearchIndexResultWriterTest {

    private class SampleTest {
        @Suppress("unused") fun alpha() = Unit
    }

    private val sampleClass = SampleTest::class.java
    private val alpha = sampleClass.getDeclaredMethod("alpha")

    private fun containers() = listOf(
        fakeTestContainer(
            testClass = sampleClass,
            methodContainers = listOf(
                fakeTestMethodContainer(
                    method = alpha,
                    invocations = listOf(fakeTestInvocation(fixturesNamesAndValues = listOf(NamedValue("OrderIdFx", "acme-order-12345")))),
                ),
            ),
        ),
    )

    @Test
    fun `dataOnly writer produces search-index json as a sibling of indices json`(@TempDir tempDir: Path) {
        val sourceDir = tempDir.resolve("sources").resolve("uiTest")
        val configuration = Configuration().apply {
            outputDir = sourceDir
            dataOnly = true
        }

        ResultWriter(configuration, ComponentDiagramFactory()).write(containers())

        val file = sourceDir.resolve("search-index.json")
        file.shouldExist()
        sourceDir.resolve("indices.json").shouldExist()

        val json = Json.parse(file.readText()).asObject()
        json.getInt("schemaVersion", -1) shouldBe 1
        val terms = json.get("terms").asArray()
        terms.size() shouldBe 1
        terms[0].asObject().getString("value", null) shouldBe "acme-order-12345"
    }

    @Test
    fun `non-dataOnly writer also produces search-index json`(@TempDir tempDir: Path) {
        val configuration = Configuration().apply {
            outputDir = tempDir
            dataOnly = false
        }

        ResultWriter(configuration, ComponentDiagramFactory()).write(containers())

        val file = tempDir.resolve("search-index.json")
        file.shouldExist()

        val json = Json.parse(file.readText()).asObject()
        json.get("terms").asArray().size() shouldBe 1
    }
}
