package dev.kensa.output

import dev.kensa.Configuration
import dev.kensa.context.TestContainer
import dev.kensa.output.template.FileTemplate.IndexFileTemplate
import dev.kensa.output.template.FileTemplate.TestFileTemplate
import dev.kensa.util.IoUtil
import java.nio.file.Path

class ResultWriter(private val configuration: Configuration) {

    init {
        IoUtil.recreate(configuration.outputDir)
    }

    fun write(containers: List<TestContainer>) {
        writeIndices(containers.sortedBy { it.testClass.name })
        IoUtil.copyResource("/kensa.js", configuration.outputDir)
        IoUtil.copyResource("/favicon.ico", configuration.outputDir)

        println(
            """
                Kensa Output :
                ${configuration.outputDir.resolve("index.html")}
            """.trimIndent()
        )
    }

    fun writeTest(container: TestContainer) {
        TestFileTemplate(configuration, container).write()
    }

    private fun writeIndices(containers: List<TestContainer>) {
        IndexFileTemplate(configuration).apply {
            containers.forEach { addIndex(it) }
            write()
        }
    }
}