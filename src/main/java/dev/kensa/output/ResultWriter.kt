package dev.kensa.output

import dev.kensa.Configuration
import dev.kensa.Kensa
import dev.kensa.context.TestContainer
import dev.kensa.output.template.Template
import dev.kensa.output.template.Template.Mode.TestFile
import dev.kensa.util.IoUtil
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class ResultWriter(private val outputDir: Path, private val writeIndexFile: (Set<TestContainer>) -> Unit) {

    init {
        IoUtil.recreate(outputDir)
    }

    fun write(containers: Set<TestContainer>) {
        writeIndexFile(containers)
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

object IndexFileWriter {
    operator fun invoke(configuration: Configuration): (Set<TestContainer>) -> Unit = { containers ->
        Template(configuration.outputDir.resolve("index.html"), Template.Mode.IndexFile, configuration.issueTrackerUrl, configuration.sectionOrder).apply {
            containers.forEach { container ->
                addIndex(container, Template.asIndex())
            }
            write()
        }
    }
}

object TestFileWriter {
    operator fun invoke(configuration: Configuration): (TestContainer) -> Unit = {container ->
        Template(Kensa.configuration.outputDir.resolve("${container.testClass.name}.html"), TestFile, configuration.issueTrackerUrl, configuration.sectionOrder).apply {
            addJsonScript(container, Template.asJsonScript(Kensa.configuration.renderers))
            write()
        }
    }
}