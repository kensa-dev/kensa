package dev.kensa.render.diagram

import dev.kensa.render.diagram.directive.UmlParticipant.Companion.participant
import dev.kensa.state.CapturedInteractionBuilder.Companion.from
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Party
import dev.kensa.state.SetupStrategy
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
        val factory = SequenceDiagramFactory(emptyList(), null)

        factory.create(interactions()).shouldBeNull()
    }

    @Test
    fun `create returns null when only SD-MARKER entries exist and no primary participant configured`() {
        val factory = SequenceDiagramFactory(emptyList(), null)
        val interactions = interactions()
        interactions.divider("Something")

        factory.create(interactions).shouldBeNull()
    }

    @Test
    fun `create injects participant X when only SD-MARKER entries exist and primary participant is X`() {
        val interactions = interactions()
        interactions.divider("Something")

        val markup = buildMarkup(emptyList(), "X", interactions)

        markup.shouldNotBeNull()
        markup shouldContain "participant X"
        markup shouldContain "==Something=="
    }

    @Test
    fun `create does not inject primary participant when umlDirectives already declare participants`() {
        val interactions = interactions()
        interactions.divider("Something")

        val markup = buildMarkup(participant("Existing").asUml(), "Primary", interactions)

        markup.shouldNotBeNull()
        markup shouldContain "participant Existing"
        markup shouldNotContain "participant Primary"
    }

    @Test
    fun `create does not inject primary participant when real interactions exist`() {
        val interactions = interactions()
        capture(interactions, "A", "B")

        val markup = buildMarkup(emptyList(), "Primary", interactions)

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
}
