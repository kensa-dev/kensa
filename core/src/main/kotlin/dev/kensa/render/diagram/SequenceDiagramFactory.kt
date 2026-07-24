package dev.kensa.render.diagram

import dev.kensa.render.diagram.directive.UmlDirective
import dev.kensa.render.diagram.directive.UmlParticipant
import dev.kensa.state.CapturedInteractions
import dev.kensa.util.Attributes.Key.InteractionId
import dev.kensa.util.KensaMap
import net.sourceforge.plantuml.FileFormat.SVG
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import java.io.ByteArrayOutputStream
import java.util.*

private val keyPattern = "(.*) from (\\w+) to (\\w+)".toRegex()
private val valuePatterns = listOf(
    "^\\.\\.\\..*\\.\\.\\.$".toRegex(),
    "^==.*==$".toRegex()
)
private val participantNamePattern = "^(participant|actor|boundary|control|entity|database|collections|queue)\\s+(\\S+)".toRegex()

private const val SETUP_GROUP_COLOUR = "#ECECEC"
private const val TEST_GROUP_COLOUR = "#FFFFFF"

class SequenceDiagramFactory(
    private val directivesProvider: () -> List<UmlDirective>,
    private val primaryProvider: () -> UmlParticipant? = { null }
) {

    fun create(interactions: CapturedInteractions): SequenceDiagram? =
        buildMarkup(directivesProvider().flatMap { it.asUml() }, primaryProvider(), interactions)
            ?.let { SequenceDiagram(renderSvg(it)) }

    private fun renderSvg(plantUmlMarkup: String): String =
        ByteArrayOutputStream().use { os ->
            SourceStringReader(plantUmlMarkup).outputImage(os, FileFormatOption(SVG))
            os.toString()
        }.withSafariSafeLengthAdjust()
}

// WebKit mislays glyphs under lengthAdjust="spacing" when browser font metrics diverge from
// PlantUML's Java-side measurement; spacingAndGlyphs renders correctly in all engines.
internal fun String.withSafariSafeLengthAdjust(): String =
    replace("""lengthAdjust="spacing" """, """lengthAdjust="spacingAndGlyphs" """)

internal fun buildMarkup(participants: List<String>, primary: UmlParticipant?, interactions: CapturedInteractions): String? {
    val events = eventsFrom(interactions)
    if (events.isEmpty()) return null

    val effective = mergePrimary(primary, participants)

    if (effective.isEmpty() && !hasRealInteractions(interactions)) return null

    return (effective + events).joinToString(
        "\n",
        """
            @startuml

            skinparam BackgroundColor transparent
            skinparam SequenceGroupBackgroundColor lightgray
            skinparam SequenceGroupBodyBackGroundColor white

        """.trimIndent(),
        "\n@enduml"
    )
}

private fun mergePrimary(primary: UmlParticipant?, participants: List<String>): List<String> {
    if (primary == null) return participants
    val primaryLines = primary.asUml()
    val primaryName = participantNames(primaryLines).singleOrNull() ?: return primaryLines + participants
    return if (primaryName in participantNames(participants)) participants else primaryLines + participants
}

private fun participantNames(lines: List<String>): Set<String> =
    lines.mapNotNullTo(mutableSetOf()) { participantNamePattern.find(it)?.groupValues?.get(2) }

private fun hasRealInteractions(interactions: CapturedInteractions): Boolean =
    interactions.entrySet().any { keyPattern.matches(it.key.lowercase(Locale.getDefault())) }

private fun eventsFrom(interactions: CapturedInteractions): List<String> =
    buildList {
        interactions.entrySet()
            .filter(IsSvgCompatible)
            .map(ToGroupedSvg)
            .groupByTo(LinkedHashMap(), { it.first }, { it.second })
            .forEach { (group, lines) ->
                group?.takeIf { it.isNotEmpty() }?.let { g ->
                    if (group == "Setup") {
                        add("group $SETUP_GROUP_COLOUR $g")
                    } else {
                        add("group#gold $TEST_GROUP_COLOUR $g")
                    }

                    addAll(lines)
                    add("end")
                } ?: addAll(lines)
            }
    }

object IsSvgCompatible : (KensaMap.Entry) -> Boolean {
    override fun invoke(entry: KensaMap.Entry) =
        (keyPattern.matches(entry.key.lowercase(Locale.getDefault())) || valuePatterns.any { p -> p.matches(entry.value.toString()) })

}

object ToGroupedSvg : (KensaMap.Entry) -> Pair<String?, String> {
    override fun invoke(entry: KensaMap.Entry): Pair<String?, String> =
        keyPattern.matchEntire(entry.key)?.let { result ->
            val interactionId = entry.attributes.get<String>(InteractionId)
                ?: error("Missing $InteractionId attribute for sequence diagram interaction")

            val interactionName = result.groupValues[1].trim()
            val from = result.groupValues[2].trim()
            val to = result.groupValues[3].trim()

            Pair(
                entry.attributes.group,
                """$from ${entry.attributes.arrowStyle.value} ${to}:<text class=sequence_diagram_clickable sequence_diagram_interaction_id="$interactionId">$interactionName</text>"""
            )
        } ?: Pair(entry.attributes.group, entry.value.toString().trim())
}
