package dev.kensa.output

import dev.kensa.Configuration
import dev.kensa.context.TestContainer
import dev.kensa.output.template.Template
import dev.kensa.output.template.Template.Companion.asIndex
import dev.kensa.output.template.Template.Companion.asJsonScript

class SingleFileWriter : (Set<TestContainer>, Configuration) -> Unit {
    override fun invoke(containers: Set<TestContainer>, configuration: Configuration) {
        configuration.createTemplate("index.html", Template.Mode.SingleFile).apply {
            containers.forEach { container ->
                addJsonScript(container, asJsonScript(configuration.renderers))
                addIndex(container, asIndex())
            }
            write()
        }
    }
}