package dev.kensa

import dev.kensa.render.diagram.directive.UmlBox
import dev.kensa.render.diagram.directive.UmlHideUnlinked
import dev.kensa.render.diagram.directive.UmlParticipant
import dev.kensa.render.diagram.directive.UmlTitle
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

internal class SequenceDiagramConfigurationTest {

    @Test
    fun `participant adds an UmlParticipant directive in declaration order`() {
        val sd = SequenceDiagramConfiguration()
        sd.participant("Alpha")

        sd.directives shouldHaveSize 1
        sd.directives[0].shouldBeInstanceOf<UmlParticipant>()
        sd.directives[0].asUml().joinToString("\n") shouldContain "participant Alpha"
    }

    @Test
    fun `multiple participant calls preserve order`() {
        val sd = SequenceDiagramConfiguration()
        sd.participant("Alpha")
        sd.participant("Bravo")
        sd.participant("Charlie")
        sd.participant("Delta")

        sd.directives shouldHaveSize 4
        sd.directives[0].asUml().joinToString("\n") shouldContain "participant Alpha"
        sd.directives[1].asUml().joinToString("\n") shouldContain "participant Bravo"
        sd.directives[2].asUml().joinToString("\n") shouldContain "participant Charlie"
        sd.directives[3].asUml().joinToString("\n") shouldContain "participant Delta"
    }

    @Test
    fun `actor adds an actor directive`() {
        val sd = SequenceDiagramConfiguration()
        sd.actor("User")

        sd.directives[0].asUml().joinToString("\n") shouldContain "actor User"
    }

    @Test
    fun `boundary adds a boundary directive`() {
        val sd = SequenceDiagramConfiguration()
        sd.boundary("Edge")

        sd.directives[0].asUml().joinToString("\n") shouldContain "boundary Edge"
    }

    @Test
    fun `control adds a control directive`() {
        val sd = SequenceDiagramConfiguration()
        sd.control("Controller")

        sd.directives[0].asUml().joinToString("\n") shouldContain "control Controller"
    }

    @Test
    fun `entity adds an entity directive`() {
        val sd = SequenceDiagramConfiguration()
        sd.entity("Order")

        sd.directives[0].asUml().joinToString("\n") shouldContain "entity Order"
    }

    @Test
    fun `database adds a database directive`() {
        val sd = SequenceDiagramConfiguration()
        sd.database("DB")

        sd.directives[0].asUml().joinToString("\n") shouldContain "database DB"
    }

    @Test
    fun `collections adds a collections directive`() {
        val sd = SequenceDiagramConfiguration()
        sd.collections("Items")

        sd.directives[0].asUml().joinToString("\n") shouldContain "collections Items"
    }

    @Test
    fun `queue adds a queue directive`() {
        val sd = SequenceDiagramConfiguration()
        sd.queue("Topic")

        sd.directives[0].asUml().joinToString("\n") shouldContain "queue Topic"
    }

    @Test
    fun `box block wraps nested participants`() {
        val sd = SequenceDiagramConfiguration()
        sd.box("Frontend") {
            participant("A")
            participant("B")
        }

        sd.directives shouldHaveSize 1
        sd.directives[0].shouldBeInstanceOf<UmlBox>()
        val lines = sd.directives[0].asUml()
        lines[0] shouldContain "box \"Frontend\""
        lines.any { it.contains("participant A") } shouldBe true
        lines.any { it.contains("participant B") } shouldBe true
        lines.last() shouldBe "end box"
    }

    @Test
    fun `title adds a UmlTitle directive`() {
        val sd = SequenceDiagramConfiguration()
        sd.title("My Title")

        sd.directives shouldHaveSize 1
        sd.directives[0].shouldBeInstanceOf<UmlTitle>()
        sd.directives[0].asUml().joinToString("\n") shouldContain "My Title"
    }

    @Test
    fun `hideUnlinked adds the UmlHideUnlinked directive`() {
        val sd = SequenceDiagramConfiguration()
        sd.hideUnlinked()

        sd.directives shouldHaveSize 1
        sd.directives[0].shouldBeInstanceOf<UmlHideUnlinked>()
    }

    @Test
    fun `primary participant is captured separately from directives`() {
        val sd = SequenceDiagramConfiguration()
        sd.primary.participant("Service")

        sd.primary.participant?.asUml()?.joinToString("\n") shouldContain "participant Service"
        sd.directives.none { it.asUml().any { line -> line.contains("participant Service") } } shouldBe true
    }

    @Test
    fun `primary actor sets a styled actor as the fallback`() {
        val sd = SequenceDiagramConfiguration()
        sd.primary.actor("User").withColour("#Blue")

        sd.primary.participant?.asUml()?.joinToString("\n") shouldContain "actor User"
        sd.primary.participant?.asUml()?.joinToString("\n") shouldContain "#Blue"
    }

    @Test
    fun `setting primary twice replaces previous primary`() {
        val sd = SequenceDiagramConfiguration()
        sd.primary.participant("First")
        sd.primary.database("Second")

        sd.primary.participant?.asUml()?.joinToString("\n") shouldContain "database Second"
        sd.primary.participant?.asUml()?.joinToString("\n") shouldNotContain "First"
    }

    @Test
    fun `participant returns a handle that chains withColour and withAlias inline`() {
        val sd = SequenceDiagramConfiguration()
        sd.participant("Alpha").withColour("#FF0000").withAlias("A1")

        sd.directives shouldHaveSize 1
        val uml = sd.directives[0].asUml().joinToString("\n")
        uml shouldContain "#FF0000"
        uml shouldContain "A1"
    }

    @Test
    fun `deprecated umlDirectives setter writes into sequenceDiagram directives`() {
        @Suppress("DEPRECATION")
        val configuration = Configuration().apply {
            umlDirectives = listOf(
                UmlParticipant.participant("X"),
                UmlParticipant.actor("Y"),
                UmlHideUnlinked.hideUnlinkedParticipants()
            )
        }

        configuration.sequenceDiagram.directives shouldHaveSize 3
        configuration.sequenceDiagram.directives[0].asUml().joinToString("\n") shouldContain "participant X"
        configuration.sequenceDiagram.directives[1].asUml().joinToString("\n") shouldContain "actor Y"
        configuration.sequenceDiagram.directives[2].shouldBeInstanceOf<UmlHideUnlinked>()
    }

    @Test
    fun `deprecated umlDirectives getter reads from sequenceDiagram directives`() {
        val configuration = Configuration().apply {
            sequenceDiagram {
                participant("Alpha")
                actor("Beta")
            }
        }

        @Suppress("DEPRECATION")
        val directives = configuration.umlDirectives
        directives shouldHaveSize 2
        directives[0].asUml().joinToString("\n") shouldContain "participant Alpha"
        directives[1].asUml().joinToString("\n") shouldContain "actor Beta"
    }

    @Test
    fun `box participants do not appear at the top level directive list`() {
        val sd = SequenceDiagramConfiguration()
        sd.box("Frontend") {
            participant("A")
            participant("B")
        }
        sd.participant("C")

        sd.directives shouldHaveSize 2
        sd.directives[0].shouldBeInstanceOf<UmlBox>()
        sd.directives[1].shouldBeInstanceOf<UmlParticipant>()
        sd.directives[1].asUml().joinToString("\n") shouldNotContain "participant A"
    }

    @Test
    fun `sequenceDiagram extension applies block to configuration sequenceDiagram`() {
        val configuration = Configuration()
        configuration.sequenceDiagram {
            title("Hello")
            primary.participant("Main")
            participant("Alpha")
            hideUnlinked()
        }

        configuration.sequenceDiagram.primary.participant?.asUml()?.joinToString("\n") shouldContain "participant Main"
        configuration.sequenceDiagram.directives shouldHaveSize 3
        configuration.sequenceDiagram.directives[0].shouldBeInstanceOf<UmlTitle>()
        configuration.sequenceDiagram.directives[1].shouldBeInstanceOf<UmlParticipant>()
        configuration.sequenceDiagram.directives[2].shouldBeInstanceOf<UmlHideUnlinked>()
    }

    @Test
    fun `box with colour produces box with colour in markup`() {
        val sd = SequenceDiagramConfiguration()
        sd.box("Frontend", "#LightBlue") {
            participant("A")
        }

        val lines = sd.directives[0].asUml()
        lines[0] shouldContain "box \"Frontend\""
        lines[0] shouldContain "#LightBlue"
    }

    @Test
    fun `box participant with chained withColour reflects colour in box markup`() {
        val sd = SequenceDiagramConfiguration()
        sd.box("Frontend") {
            participant("A").withColour("#Red")
        }

        val lines = sd.directives[0].asUml()
        lines.any { it.contains("#Red") } shouldBe true
    }

    @Test
    fun `back-to-back sequenceDiagram blocks replace, not accumulate`() {
        val configuration = Configuration().apply {
            sequenceDiagram {
                primary.participant("FirstPrimary")
                participant("Alpha")
                participant("Bravo")
            }
            sequenceDiagram {
                primary.participant("SecondPrimary")
                participant("Charlie")
            }
        }

        configuration.sequenceDiagram.primary.participant?.asUml()?.joinToString("\n") shouldContain "participant SecondPrimary"
        configuration.sequenceDiagram.directives shouldHaveSize 1
        configuration.sequenceDiagram.directives[0].asUml().joinToString("\n") shouldContain "participant Charlie"
        configuration.sequenceDiagram.directives.none {
            it.asUml().any { line -> line.contains("Alpha") || line.contains("Bravo") }
        } shouldBe true
    }

    @Test
    fun `sequenceDiagram block with no primary clears a previously set primary`() {
        val configuration = Configuration().apply {
            sequenceDiagram {
                primary.participant("Main")
            }
            sequenceDiagram {
                participant("Alpha")
            }
        }

        configuration.sequenceDiagram.primary.participant.shouldBeNull()
    }
}
