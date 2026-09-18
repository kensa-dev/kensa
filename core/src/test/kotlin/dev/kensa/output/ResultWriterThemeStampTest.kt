package dev.kensa.output

import dev.kensa.Configuration
import dev.kensa.render.diagram.ComponentDiagramFactory
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.readText

class ResultWriterThemeStampTest {

    @Test
    fun `report index stamps the theme before first paint`(@TempDir tempDir: Path) {
        val configuration = Configuration().apply {
            outputDir = tempDir
            dataOnly = false
        }

        ResultWriter(configuration, ComponentDiagramFactory()).write(emptyList())

        val html = tempDir.resolve("index.html").readText()

        html.shouldContain("theme=(light|dark)")
        html.shouldContain("classList.add('dark')")
        html.indexOf("theme=(light|dark)") shouldBeLessThan html.indexOf("kensa.js")
    }
}
