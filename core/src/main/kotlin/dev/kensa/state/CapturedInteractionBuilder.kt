package dev.kensa.state

import dev.kensa.render.diagram.directive.ArrowStyle
import dev.kensa.util.Attributes
import dev.kensa.util.Attributes.Companion.of
import dev.kensa.util.Attributes.Key.Arrow
import dev.kensa.util.Attributes.Key.Group
import dev.kensa.util.Attributes.Key.InteractionId
import dev.kensa.util.Attributes.Key.Seam
import java.time.Instant
import java.util.UUID

class CapturedInteractionBuilder private constructor(private val fromParty: Party) {
    private var attributes = of()
    private var toParty: Party? = null
    private var group: String? = null
    private var content: Any? = null
    private var contentDescriptor: String? = null
    private var arrowStyle : ArrowStyle = ArrowStyle.UmlSynchronous
    private var timestamp : Long? = null
    private var seam: SeamDefinition? = null

    private val interactionId: String = UUID.randomUUID().toString()

    fun arrowStyle(arrowStyle: ArrowStyle) = apply { this.arrowStyle = arrowStyle }

    fun to(toParty: Party): CapturedInteractionBuilder = apply { this.toParty = toParty }

    fun group(group: String): CapturedInteractionBuilder = apply { this.group = group }

    fun underTest(isUnderTest: Boolean): CapturedInteractionBuilder = apply { if (group == null && isUnderTest) group = "Test" }

    fun with(timestamp: Instant) = with(timestamp.toEpochMilli())

    fun with(timestamp: Long) = apply { this.timestamp = timestamp }

    fun with(content: Any, contentDescriptor: String): CapturedInteractionBuilder = apply {
        this.content = content
        this.contentDescriptor = contentDescriptor
    }

    fun with(attributes: Attributes): CapturedInteractionBuilder = apply { this.attributes = attributes }

    fun seam(definition: SeamDefinition): CapturedInteractionBuilder = apply {
        seam = definition
        when (definition) {
            is Inbound ->
                if (toParty == null) toParty = definition.owner
                else require(toParty == definition.owner) {
                    "Inbound seam '${definition.id}' must arrive at ${definition.owner.asString()} but 'to' is ${toParty?.asString()}"
                }
            is Outbound -> require(fromParty == definition.owner) {
                "Outbound seam '${definition.id}' must originate from ${definition.owner.asString()} but 'from' is ${fromParty.asString()}"
            }
        }
    }

    fun applyTo(interactions: CapturedInteractions) {
        attributes = group?.let { attributes.merge(of(Group, it, Arrow, arrowStyle, InteractionId, interactionId)) }
            ?: attributes.merge(of(Arrow, arrowStyle, InteractionId, interactionId))

        seam?.let { attributes = attributes.merge(of(Seam, it)) }

        val key = "$contentDescriptor __idx __from ${fromParty.asString()} to ${toParty!!.asString()}"

        interactions.putWithUniqueKey(key, content, timestamp ?: System.currentTimeMillis(), attributes)
    }

    companion object {
        @JvmStatic
        fun from(party: Party): CapturedInteractionBuilder = CapturedInteractionBuilder(party)
    }
}