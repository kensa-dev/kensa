package dev.kensa.render.diagram

import dev.kensa.Configuration
import dev.kensa.render.diagram.directive.UmlBox.Companion.surroundingBox
import dev.kensa.render.diagram.directive.UmlParticipant.Companion.participant
import dev.kensa.state.CapturedInteractionBuilder.Companion.from
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Party
import dev.kensa.state.SetupStrategy
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test

internal class SequenceDiagramFactoryTest {

    private fun interactions() = CapturedInteractions(SetupStrategy.Ungrouped)

    private fun party(name: String): Party = object : Party { override fun asString() = name }

    private fun capture(interactions: CapturedInteractions, fromName: String, toName: String, descriptor: String = "sends a request") {
        interactions.capture(from(party(fromName)).to(party(toName)).with("payload", descriptor))
    }

    @Test
    fun `create returns null when interactions are empty`() {
        val factory = SequenceDiagramFactory(emptyList())

        factory.create(interactions()).shouldBeNull()
    }

    @Test
    fun `create returns null when only SD-MARKER entries exist and no primary participant configured`() {
        val factory = SequenceDiagramFactory(emptyList())
        val interactions = interactions()
        interactions.divider("Something")

        factory.create(interactions).shouldBeNull()
    }

    @Test
    fun `create injects participant X when only SD-MARKER entries exist and primary participant is X`() {
        val interactions = interactions()
        interactions.divider("Something")

        val markup = buildMarkup(emptyList(), participant("X"), interactions)

        markup.shouldNotBeNull()
        markup shouldContain "participant X"
        markup shouldContain "==Something=="
    }

    @Test
    fun `create does not inject primary participant when umlDirectives already declare participants`() {
        val interactions = interactions()
        interactions.divider("Something")

        val markup = buildMarkup(participant("Existing").asUml(), participant("Primary"), interactions)

        markup.shouldNotBeNull()
        markup shouldContain "participant Existing"
        markup shouldNotContain "participant Primary"
    }

    @Test
    fun `create does not inject primary participant when real interactions exist`() {
        val interactions = interactions()
        capture(interactions, "A", "B")

        val markup = buildMarkup(emptyList(), participant("Primary"), interactions)

        markup.shouldNotBeNull()
        markup shouldNotContain "participant Primary"
        markup shouldContain "A"
        markup shouldContain "B"
    }

    @Test
    fun `create renders SD-MARKER between two real interactions normally`() {
        val interactions = interactions()
        capture(interactions, "A", "B")
        interactions.divider("Midpoint")
        capture(interactions, "B", "A")

        val markup = buildMarkup(emptyList(), null, interactions)

        markup.shouldNotBeNull()
        markup shouldContain "==Midpoint=="
        markup shouldNotContain "participant"
    }

    @Test
    fun `buildMarkup preserves umlDirectives declaration order for 4 participants`() {
        val directives = listOf(
            participant("Alpha"),
            participant("Bravo"),
            participant("Charlie"),
            participant("Delta")
        )
        val interactions = interactions()
        capture(interactions, "Alpha", "Bravo")

        val markup = buildMarkup(directives.flatMap { it.asUml() }, null, interactions)

        markup.shouldNotBeNull()
        val idxAlpha = markup.indexOf("participant Alpha")
        val idxBravo = markup.indexOf("participant Bravo")
        val idxCharlie = markup.indexOf("participant Charlie")
        val idxDelta = markup.indexOf("participant Delta")
        idxAlpha shouldBeGreaterThan -1
        idxBravo shouldBeGreaterThan idxAlpha
        idxCharlie shouldBeGreaterThan idxBravo
        idxDelta shouldBeGreaterThan idxCharlie
        val interactionLineStart = markup.indexOf("Alpha -> Bravo")
        interactionLineStart shouldBeGreaterThan idxDelta
    }

    @Test
    fun `rendered SVG places first-declared participant leftmost when 4 participants are declared`() {
        val directives = listOf(
            participant("Alpha"),
            participant("Bravo"),
            participant("Charlie"),
            participant("Delta")
        )
        val interactions = interactions()
        capture(interactions, "Alpha", "Bravo")

        val diagram = SequenceDiagramFactory(directives).create(interactions)

        diagram.shouldNotBeNull()
        val svg = diagram.toString()
        val idxAlpha = svg.indexOf(">Alpha<")
        val idxBravo = svg.indexOf(">Bravo<")
        val idxCharlie = svg.indexOf(">Charlie<")
        val idxDelta = svg.indexOf(">Delta<")
        idxAlpha shouldBeGreaterThan -1
        idxBravo shouldBeGreaterThan idxAlpha
        idxCharlie shouldBeGreaterThan idxBravo
        idxDelta shouldBeGreaterThan idxCharlie
    }

    @Test
    fun `factory observes umlDirectives populated via deprecated setter after factory construction`() {
        val configuration = Configuration()
        val factory = SequenceDiagramFactory(configuration.sequenceDiagram.directives)
        val interactions = interactions()
        capture(interactions, "Echo", "Foxtrot")

        @Suppress("DEPRECATION")
        configuration.umlDirectives = listOf(participant("Echo"), participant("Foxtrot"))

        val diagram = factory.create(interactions)

        diagram.shouldNotBeNull()
        val svg = diagram.toString()
        svg.indexOf(">Echo<") shouldBeGreaterThan -1
        svg.indexOf(">Foxtrot<") shouldBeGreaterThan svg.indexOf(">Echo<")
    }

    @Test
    fun `factory observes sequenceDiagram block applied after factory construction`() {
        val configuration = Configuration()
        val factory = SequenceDiagramFactory(configuration.sequenceDiagram.directives)
        val interactions = interactions()
        capture(interactions, "Echo", "Foxtrot")

        configuration.sequenceDiagram.participant("Echo")
        configuration.sequenceDiagram.participant("Foxtrot")

        val diagram = factory.create(interactions)

        diagram.shouldNotBeNull()
        val svg = diagram.toString()
        svg.indexOf(">Echo<") shouldBeGreaterThan -1
        svg.indexOf(">Foxtrot<") shouldBeGreaterThan svg.indexOf(">Echo<")
    }

    @Test
    fun `rendered SVG preserves declaration order when first two participants are wrapped in a surroundingBox`() {
        val directives = listOf(
            surroundingBox("Frontend", participant("Alpha"), participant("Bravo")),
            participant("Charlie"),
            participant("Delta")
        )
        val interactions = interactions()
        capture(interactions, "Alpha", "Charlie")

        val diagram = SequenceDiagramFactory(directives).create(interactions)

        diagram.shouldNotBeNull()
        val svg = diagram.toString()
        val idxAlpha = svg.indexOf(">Alpha<")
        val idxBravo = svg.indexOf(">Bravo<")
        val idxCharlie = svg.indexOf(">Charlie<")
        val idxDelta = svg.indexOf(">Delta<")
        idxAlpha shouldBeGreaterThan -1
        idxBravo shouldBeGreaterThan idxAlpha
        idxCharlie shouldBeGreaterThan idxBravo
        idxDelta shouldBeGreaterThan idxCharlie
        svg shouldContain "Frontend"
    }
}
