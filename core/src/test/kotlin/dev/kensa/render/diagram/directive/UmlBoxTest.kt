package dev.kensa.render.diagram.directive

import dev.kensa.render.diagram.directive.UmlBox.Companion.surroundingBox
import dev.kensa.render.diagram.directive.UmlParticipant.Companion.actor
import dev.kensa.render.diagram.directive.UmlParticipant.Companion.participant
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class UmlBoxTest {

    @Test
    fun `can obtain simple box grammar wrapping participants`() {
        val box = surroundingBox(actor("A"), participant("B"), actor("C"))

        box.asUml() shouldBe listOf("box", "actor A", "participant B", "actor C", "end box")
    }

    @Test
    fun `can obtain box grammar wrapping participants with a title`() {
        val box = surroundingBox(actor("A"), participant("B"), actor("C")).withTitle("My Box")

        box.asUml() shouldBe listOf("box \"My Box\"", "actor A", "participant B", "actor C", "end box")
    }

    @Test
    fun `can obtain box grammar wrapping participants with a title and colour`() {
        val box = surroundingBox(actor("A"), participant("B"), actor("C")).withTitle("My Box").withColour("Red")

        box.asUml() shouldBe listOf("box \"My Box\" Red", "actor A", "participant B", "actor C", "end box")
    }
}