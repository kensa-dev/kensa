package dev.kensa.output

import dev.kensa.Configuration
import dev.kensa.context.TestContainer

enum class OutputStyle(private val writer: (Set<TestContainer>, Configuration) -> Unit) {
    SingleFile(SingleFileWriter()), MultiFile(MultiFileWriter()), Site(SiteWriter());

    fun write(containers: Set<TestContainer>, configuration: Configuration) {
        writer(containers, configuration)
    }
}