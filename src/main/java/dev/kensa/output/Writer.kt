package dev.kensa.output

import dev.kensa.Configuration
import dev.kensa.context.TestContainer
import dev.kensa.output.template.FileTemplate.IndexFileTemplate
import dev.kensa.output.template.FileTemplate.TestFileTemplate
import dev.kensa.util.IoUtil
import java.nio.file.Path
import java.util.Comparator
import java.util.TreeSet

class ResultWriter(private val outputDir: Path, private val configuration: Configuration) {

    init {
        IoUtil.recreate(outputDir)
    }

    fun write(containers: List<TestContainer>) {
        writeIndices(containers.sortedBy { it.testClass.name })
        IoUtil.copyResource("/kensa.js", outputDir)
        IoUtil.copyResource("/favicon.ico", outputDir)

        println(
            """
                Kensa Output :
                ${outputDir.resolve("index.html")}
            """.trimIndent()
        )
    }

    private fun writeIndices(containers: List<TestContainer>) {
        IndexFileTemplate(configuration).apply {
            containers.forEach { addIndex(it) }
            write()
        }
    }
}

class TestWriter(private val configuration: Configuration) {
    fun write(container: TestContainer) {
        TestFileTemplate(configuration, container).write()
    }
}