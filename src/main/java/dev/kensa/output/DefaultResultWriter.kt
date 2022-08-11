package dev.kensa.output

import dev.kensa.Kensa
import dev.kensa.context.TestContainer
import dev.kensa.util.IoUtil

class DefaultResultWriter : ResultWriter {
    override fun write(containers: Set<TestContainer>) {
        val configuration = Kensa.configuration
        val outputDir = configuration.outputDir

        IoUtil.recreate(outputDir)
        MultiFileWriter()(containers, configuration)
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