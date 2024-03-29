package dev.kensa.state

import dev.kensa.render.diagram.directive.ArrowStyle
import dev.kensa.util.Attributes
import dev.kensa.util.Attributes.Companion.of
import dev.kensa.util.Attributes.Key.Arrow
import dev.kensa.util.Attributes.Key.Group
import java.time.Instant

class CapturedInteractionBuilder private constructor(private val fromParty: Party) {
    private var attributes = of()
    private var toParty: Party? = null
    private var group: String? = null
    private var content: Any? = null
    private var contentDescriptor: String? = null
    private var arrowStyle : ArrowStyle = ArrowStyle.UmlSynchronous
    private var timestamp : Long? = null

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

    fun applyTo(interactions: CapturedInteractions) {
        attributes = group?.let { attributes.merge(of(Group, it, Arrow, arrowStyle)) }
            ?: attributes.merge(of(Arrow, arrowStyle))

        val key = "$contentDescriptor __idx __from ${fromParty.asString()} to ${toParty!!.asString()}"

        interactions.putWithUniqueKey(key, content, timestamp ?: System.currentTimeMillis(), attributes)
    }

    companion object {
        @JvmStatic
        fun from(party: Party): CapturedInteractionBuilder = CapturedInteractionBuilder(party)
    }
}