package dev.kensa.render.diagram

import dev.kensa.util.KensaMap
import net.sourceforge.plantuml.FileFormat.SVG
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import java.io.ByteArrayOutputStream

private val keyPattern = "(.*) from (\\w+) to (\\w+)".toRegex(RegexOption.IGNORE_CASE)

class ComponentDiagramFactory {

    fun create(entries: Set<KensaMap.Entry>): ComponentDiagram? =
        componentMarkupFor(entries)?.let { ComponentDiagram(renderSvg(it)) }

    private fun renderSvg(markup: String): String =
        ByteArrayOutputStream().use { os ->
            SourceStringReader(markup).outputImage(os, FileFormatOption(SVG))
            os.toString()
        }.withSafariSafeLengthAdjust()
}

internal fun componentMarkupFor(entries: Set<KensaMap.Entry>): String? {
    val edges = entries
        .mapNotNull { keyPattern.matchEntire(it.key) }
        .map { it.groupValues[2].trim() to it.groupValues[3].trim() }
        .filter { it.first.isNotBlank() && it.second.isNotBlank() }
        .distinct()

    if (edges.isEmpty()) return null

    val nodes = LinkedHashSet<String>()
    edges.forEach { (from, to) ->
        nodes.add(from)
        nodes.add(to)
    }

    val edgeSet = edges.toSet()
    val emitted = mutableSetOf<Pair<String, String>>()
    val edgeLines = buildList {
        for ((from, to) in edges) {
            if (from to to in emitted) continue
            val reverse = to to from
            if (from != to && reverse in edgeSet) {
                add("[$from] <--> [$to]")
                emitted += reverse
            } else {
                add("[$from] --> [$to]")
            }
            emitted += from to to
        }
    }

    return buildString {
        appendLine("@startuml")
        appendLine("!pragma layout elk")
        appendLine("skinparam BackgroundColor transparent")
        nodes.forEach { appendLine("[$it]") }
        edgeLines.forEach { appendLine(it) }
        append("@enduml")
    }
}
