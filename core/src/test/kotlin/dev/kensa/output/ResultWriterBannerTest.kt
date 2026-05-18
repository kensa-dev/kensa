package dev.kensa.output

import dev.kensa.Configuration
import dev.kensa.render.diagram.ComponentDiagramFactory
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths

class ResultWriterBannerTest {

    @Test
    fun `banner prints absolute path when outputDir is relative`(@TempDir tempDir: Path) {
        val absolute = tempDir.resolve("kensa-output")
        val relative = Paths.get("").toAbsolutePath().relativize(absolute)
        val configuration = Configuration().apply {
            outputDir = relative
            dataOnly = false
        }

        val output = captureStdout {
            ResultWriter(configuration, ComponentDiagramFactory()).write(emptyList())
        }

        Paths.get(bannerLine(output, "index.html")).isAbsolute shouldBe true
    }

    @Test
    fun `banner prints absolute path in dataOnly mode when outputDir is relative`(@TempDir tempDir: Path) {
        val absolute = tempDir.resolve("kensa-output")
        val relative = Paths.get("").toAbsolutePath().relativize(absolute)
        val configuration = Configuration().apply {
            outputDir = relative
            dataOnly = true
        }

        val output = captureStdout {
            ResultWriter(configuration, ComponentDiagramFactory()).write(emptyList())
        }

        Paths.get(bannerLine(output, "indices.json")).isAbsolute shouldBe true
    }

    private fun bannerLine(output: String, suffix: String): String =
        output.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.endsWith(suffix) }
            ?: fail("no banner line ending in '$suffix' in output:\n$output")

    private fun captureStdout(block: () -> Unit): String {
        val buffer = ByteArrayOutputStream()
        val original = System.out
        System.setOut(PrintStream(buffer))
        try {
            block()
        } finally {
            System.setOut(original)
        }
        return buffer.toString()
    }
}
