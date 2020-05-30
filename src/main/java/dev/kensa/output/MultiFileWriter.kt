package dev.kensa.output

import dev.kensa.Configuration
import dev.kensa.context.TestContainer
import dev.kensa.output.template.Template.Companion.asIndex
import dev.kensa.output.template.Template.Companion.asJsonScript
import dev.kensa.output.template.Template.Mode.MultiFile
import dev.kensa.output.template.Template.Mode.TestFile

class MultiFileWriter : (Set<TestContainer>, Configuration) -> Unit {
    override fun invoke(containers: Set<TestContainer>, configuration: Configuration) {
        configuration.createTemplate("index.html", MultiFile).also { index ->
            containers.forEach { container ->
                configuration.createTemplate(container.testClass.qualifiedName + ".html", TestFile).also { script ->
                    script.addJsonScript(container, asJsonScript(configuration.renderers))
                    script.write()
                }
                index.addIndex(container, asIndex())
            }
            index.write()
        }
    }
}