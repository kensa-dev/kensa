package dev.kensa.render.diagram

import dev.kensa.state.CapturedInteractionBuilder.Companion.from
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Party
import dev.kensa.state.SetupStrategy
import dev.kensa.util.KensaMap
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test

internal class ComponentDiagramFactoryTest {

    private val factory = ComponentDiagramFactory()

    private fun interactions() = CapturedInteractions(SetupStrategy.Ungrouped)

    private fun party(name: String): Party = object : Party { override fun asString() = name }

    private fun capture(interactions: CapturedInteractions, fromName: String, toName: String, descriptor: String = "sends a request") {
        interactions.capture(from(party(fromName)).to(party(toName)).with("payload", descriptor))
    }

    @Test
    fun `generates null markup for empty entries`() {
        componentMarkupFor(emptySet<KensaMap.Entry>()) shouldBe null
    }

    @Test
    fun `generates markup for single A to B interaction`() {
        val interactions = interactions()
        capture(interactions, "A", "B")

        val markup = componentMarkupFor(interactions.entrySet())

        markup.shouldNotBeNull()
        markup shouldContain "[A]"
        markup shouldContain "[B]"
        markup shouldContain "[A] --> [B]"
        markup shouldContain "@startuml"
        markup shouldContain "@enduml"
    }

    @Test
    fun `deduplicates repeat edges`() {
        val interactions = interactions()
        capture(interactions, "A", "B", "sends a request")
        capture(interactions, "A", "B", "sends another request")

        val markup = componentMarkupFor(interactions.entrySet())

        markup.shouldNotBeNull()
        val count = markup.lines().count { it.trim() == "[A] --> [B]" }
        count shouldBe 1
    }

    @Test
    fun `collapses bidirectional pair to single edge`() {
        val interactions = interactions()
        capture(interactions, "A", "B")
        capture(interactions, "B", "A")

        val markup = componentMarkupFor(interactions.entrySet())

        markup.shouldNotBeNull()
        markup shouldContain "[A] <--> [B]"
        markup shouldNotContain "[A] --> [B]"
        markup shouldNotContain "[B] --> [A]"
    }

    @Test
    fun `ignores SD-MARKER entries`() {
        val interactions = interactions()
        capture(interactions, "A", "B")
        interactions.captureTimePassing("Some Time Later")

        val markup = componentMarkupFor(interactions.entrySet())

        markup.shouldNotBeNull()
        markup shouldContain "[A]"
        markup shouldContain "[B]"
        markup shouldNotContain "SD-MARKER"
        markup shouldNotContain "Some Time Later"
    }

    @Test
    fun `renders self-loop`() {
        val interactions = interactions()
        capture(interactions, "A", "A")

        val markup = componentMarkupFor(interactions.entrySet())

        markup.shouldNotBeNull()
        val nodeCount = markup.lines().count { it.trim() == "[A]" }
        nodeCount shouldBe 1
        markup shouldContain "[A] --> [A]"
    }

    @Test
    fun `create returns ComponentDiagram with non-empty svg for non-empty entries`() {
        val interactions = interactions()
        capture(interactions, "A", "B")

        val diagram = factory.create(interactions.entrySet())

        diagram.shouldNotBeNull()
        val svg = diagram.toString()
        svg shouldContain "<svg"
    }

    @Test
    fun `create renders svg with spacingAndGlyphs so safari lays out labels correctly`() {
        val interactions = interactions()
        capture(interactions, "A", "B")

        val diagram = factory.create(interactions.entrySet())

        diagram.shouldNotBeNull()
        val svg = diagram.toString()
        svg shouldContain """lengthAdjust="spacingAndGlyphs" textLength"""
        svg shouldNotContain """lengthAdjust="spacing" textLength"""
    }

    @Test
    fun `create returns null for empty entries`() {
        factory.create(emptySet<KensaMap.Entry>()) shouldBe null
    }
}
