package dev.kensa.output

import com.eclipsesource.json.JsonValue
import dev.kensa.Configuration
import dev.kensa.KensaException
import dev.kensa.context.TestContainer
import dev.kensa.output.json.JsonTransforms.toJsonWith
import dev.kensa.output.template.Template.Companion.asIndex
import dev.kensa.output.template.Template.Mode.Site
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW

class SiteWriter : (Set<TestContainer>, Configuration) -> Unit {
    override fun invoke(containers: Set<TestContainer>, configuration: Configuration) {
        fun writeToFile(path: Path, jv: JsonValue) {
            try {
                jv.writeTo(Files.newBufferedWriter(path, UTF_8, CREATE_NEW))
            } catch (e: IOException) {
                throw KensaException("Unable to write Json to file", e)
            }
        }

        configuration.createTemplate("index.html", Site).apply {
            containers.forEach { container ->
                val path = configuration.outputDir.resolve(container.javaClass.name + ".json")
                writeToFile(path, container.transform(toJsonWith(configuration.renderers)))
                addIndex(container, asIndex())
            }
            write()
        }
    }
}