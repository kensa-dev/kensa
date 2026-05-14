package dev.kensa

import dev.kensa.render.diagram.directive.UmlBox
import dev.kensa.render.diagram.directive.UmlDirective
import dev.kensa.render.diagram.directive.UmlHideUnlinked
import dev.kensa.render.diagram.directive.UmlParticipant
import dev.kensa.render.diagram.directive.UmlTitle
import dev.kensa.state.Party

class SequenceDiagramConfiguration {

    internal val directives: MutableList<UmlDirective> = mutableListOf()
    val primary: PrimaryConfiguration = PrimaryConfiguration()

    internal fun reset() {
        directives.clear()
        primary.reset()
    }

    fun title(text: String, vararg otherLines: String) {
        directives.add(UmlTitle.title(text, *otherLines))
    }

    fun hideUnlinked() {
        directives.add(UmlHideUnlinked.hideUnlinkedParticipants())
    }

    fun participant(name: String): ParticipantHandle = addAndTrack(UmlParticipant.participant(name))
    fun actor(name: String): ParticipantHandle = addAndTrack(UmlParticipant.actor(name))
    fun boundary(name: String): ParticipantHandle = addAndTrack(UmlParticipant.boundary(name))
    fun control(name: String): ParticipantHandle = addAndTrack(UmlParticipant.control(name))
    fun entity(name: String): ParticipantHandle = addAndTrack(UmlParticipant.entity(name))
    fun database(name: String): ParticipantHandle = addAndTrack(UmlParticipant.database(name))
    fun collections(name: String): ParticipantHandle = addAndTrack(UmlParticipant.collections(name))
    fun queue(name: String): ParticipantHandle = addAndTrack(UmlParticipant.queue(name))

    fun participant(party: Party): ParticipantHandle = participant(party.asString())
    fun actor(party: Party): ParticipantHandle = actor(party.asString())
    fun boundary(party: Party): ParticipantHandle = boundary(party.asString())
    fun control(party: Party): ParticipantHandle = control(party.asString())
    fun entity(party: Party): ParticipantHandle = entity(party.asString())
    fun database(party: Party): ParticipantHandle = database(party.asString())
    fun collections(party: Party): ParticipantHandle = collections(party.asString())
    fun queue(party: Party): ParticipantHandle = queue(party.asString())

    fun box(title: String = "", colour: String = "", block: BoxConfiguration.() -> Unit) {
        val box = BoxConfiguration().apply(block)
        val built = UmlBox.surroundingBox(title, *box.snapshot().toTypedArray()).let {
            if (colour.isNotEmpty()) it.withColour(colour) else it
        }
        directives.add(built)
    }

    private fun addAndTrack(initial: UmlParticipant): ParticipantHandle {
        val index = directives.size
        directives.add(initial)
        return ParticipantHandle(initial) { update -> directives[index] = update }
    }

    class ParticipantHandle internal constructor(
        initial: UmlParticipant,
        private val onUpdate: (UmlParticipant) -> Unit,
    ) {
        private var current: UmlParticipant = initial

        fun withColour(colour: String): ParticipantHandle = apply {
            current = current.withColour(colour)
            onUpdate(current)
        }

        fun withAlias(alias: String): ParticipantHandle = apply {
            current = current.withAlias(alias)
            onUpdate(current)
        }
    }
}

class PrimaryConfiguration {
    var participant: UmlParticipant? = null
        private set

    fun participant(name: String): SequenceDiagramConfiguration.ParticipantHandle = setAndTrack(UmlParticipant.participant(name))
    fun actor(name: String): SequenceDiagramConfiguration.ParticipantHandle = setAndTrack(UmlParticipant.actor(name))
    fun boundary(name: String): SequenceDiagramConfiguration.ParticipantHandle = setAndTrack(UmlParticipant.boundary(name))
    fun control(name: String): SequenceDiagramConfiguration.ParticipantHandle = setAndTrack(UmlParticipant.control(name))
    fun entity(name: String): SequenceDiagramConfiguration.ParticipantHandle = setAndTrack(UmlParticipant.entity(name))
    fun database(name: String): SequenceDiagramConfiguration.ParticipantHandle = setAndTrack(UmlParticipant.database(name))
    fun collections(name: String): SequenceDiagramConfiguration.ParticipantHandle = setAndTrack(UmlParticipant.collections(name))
    fun queue(name: String): SequenceDiagramConfiguration.ParticipantHandle = setAndTrack(UmlParticipant.queue(name))

    fun participant(party: Party): SequenceDiagramConfiguration.ParticipantHandle = participant(party.asString())
    fun actor(party: Party): SequenceDiagramConfiguration.ParticipantHandle = actor(party.asString())
    fun boundary(party: Party): SequenceDiagramConfiguration.ParticipantHandle = boundary(party.asString())
    fun control(party: Party): SequenceDiagramConfiguration.ParticipantHandle = control(party.asString())
    fun entity(party: Party): SequenceDiagramConfiguration.ParticipantHandle = entity(party.asString())
    fun database(party: Party): SequenceDiagramConfiguration.ParticipantHandle = database(party.asString())
    fun collections(party: Party): SequenceDiagramConfiguration.ParticipantHandle = collections(party.asString())
    fun queue(party: Party): SequenceDiagramConfiguration.ParticipantHandle = queue(party.asString())

    internal fun reset() {
        participant = null
    }

    private fun setAndTrack(initial: UmlParticipant): SequenceDiagramConfiguration.ParticipantHandle {
        participant = initial
        return SequenceDiagramConfiguration.ParticipantHandle(initial) { update -> participant = update }
    }
}

class BoxConfiguration {
    private val participants: MutableList<UmlParticipant> = mutableListOf()

    fun participant(name: String): SequenceDiagramConfiguration.ParticipantHandle = addAndTrack(UmlParticipant.participant(name))
    fun actor(name: String): SequenceDiagramConfiguration.ParticipantHandle = addAndTrack(UmlParticipant.actor(name))
    fun boundary(name: String): SequenceDiagramConfiguration.ParticipantHandle = addAndTrack(UmlParticipant.boundary(name))
    fun control(name: String): SequenceDiagramConfiguration.ParticipantHandle = addAndTrack(UmlParticipant.control(name))
    fun entity(name: String): SequenceDiagramConfiguration.ParticipantHandle = addAndTrack(UmlParticipant.entity(name))
    fun database(name: String): SequenceDiagramConfiguration.ParticipantHandle = addAndTrack(UmlParticipant.database(name))
    fun collections(name: String): SequenceDiagramConfiguration.ParticipantHandle = addAndTrack(UmlParticipant.collections(name))
    fun queue(name: String): SequenceDiagramConfiguration.ParticipantHandle = addAndTrack(UmlParticipant.queue(name))

    fun participant(party: Party): SequenceDiagramConfiguration.ParticipantHandle = participant(party.asString())
    fun actor(party: Party): SequenceDiagramConfiguration.ParticipantHandle = actor(party.asString())
    fun boundary(party: Party): SequenceDiagramConfiguration.ParticipantHandle = boundary(party.asString())
    fun control(party: Party): SequenceDiagramConfiguration.ParticipantHandle = control(party.asString())
    fun entity(party: Party): SequenceDiagramConfiguration.ParticipantHandle = entity(party.asString())
    fun database(party: Party): SequenceDiagramConfiguration.ParticipantHandle = database(party.asString())
    fun collections(party: Party): SequenceDiagramConfiguration.ParticipantHandle = collections(party.asString())
    fun queue(party: Party): SequenceDiagramConfiguration.ParticipantHandle = queue(party.asString())

    internal fun snapshot(): List<UmlParticipant> = participants.toList()

    private fun addAndTrack(initial: UmlParticipant): SequenceDiagramConfiguration.ParticipantHandle {
        val index = participants.size
        participants.add(initial)
        return SequenceDiagramConfiguration.ParticipantHandle(initial) { update -> participants[index] = update }
    }
}
