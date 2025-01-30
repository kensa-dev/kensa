package dev.kensa.output

import dev.kensa.Configuration
import dev.kensa.Kensa
import dev.kensa.context.TestContainer
import dev.kensa.output.template.Template
import dev.kensa.output.template.Template.Mode.TestFile
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
        Template(configuration.outputDir.resolve("index.html"), Template.Mode.IndexFile, configuration.issueTrackerUrl, configuration.autoOpenTab, configuration.sectionOrder, configuration.dictionary.acronyms).apply {
            containers.forEach { container ->
                addIndex(container, Template.asIndex())
            }
            write()
        }
    }
}

interface TestWriter {
    fun write(container: TestContainer)
}

class DefaultTestWriter(private val configuration: Configuration) : TestWriter {
    override fun write(container: TestContainer) {
        Template(Kensa.configuration.outputDir.resolve("${container.testClass.name}.html"), TestFile, configuration.issueTrackerUrl, configuration.autoOpenTab, configuration.sectionOrder, configuration.dictionary.acronyms).apply {
            addJsonScript(container, Template.asJsonScript(Kensa.configuration.renderers))
            write()
        }
    }
}