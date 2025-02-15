package dev.kensa.output

import dev.kensa.Configuration
import dev.kensa.context.TestContainer
import dev.kensa.output.template.FileTemplate.IndexFileTemplate
import dev.kensa.output.template.FileTemplate.TestFileTemplate
import dev.kensa.util.IoUtil
import java.nio.file.Path

class ResultWriter(private val outputDir: Path, private val indexWriter: IndexWriter) {

    init {
        IoUtil.recreate(outputDir)
    }

    fun write(containers: Set<TestContainer>) {
        indexWriter.write(containers)
        IoUtil.copyResource("/kensa.js", outputDir)
        IoUtil.copyResource("/favicon.ico", outputDir)

        println(
            """
                Kensa Output :
                ${outputDir.resolve("index.html")}
            """.trimIndent()
        )
    }
}

interface IndexWriter {
    fun write(containers: Set<TestContainer>)
}

class DefaultIndexWriter(private val configuration: Configuration) : IndexWriter {
    override fun write(containers: Set<TestContainer>) {
        IndexFileTemplate(configuration).apply {
            containers.forEach { addIndex(it) }
            write()
        }
    }
}

interface TestWriter {
    fun write(container: TestContainer)
}

class DefaultTestWriter(private val configuration: Configuration) : TestWriter {
    override fun write(container: TestContainer) {
        TestFileTemplate(configuration, container).write()
    }
}