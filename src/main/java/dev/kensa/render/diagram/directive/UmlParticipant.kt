package dev.kensa.render.diagram.directive

import dev.kensa.state.Party

class UmlParticipant private constructor(
        private val type: String,
        private val name: String,
        private val colour: String = "",
        private val alias: String = ""
) : UmlDirective {
    fun withColour(colour: String): UmlParticipant = UmlParticipant(type, name, colour, alias)

    fun withAlias(alias: String): UmlParticipant = UmlParticipant(type, name, colour, alias)

    override fun asUml(): List<String> =
            listOf("$type $name ${fmtAlias(alias)} $colour".replace("\\s+".toRegex(), " ").trim { it <= ' ' })

    private fun fmtAlias(alias: String): String = if (alias.isEmpty()) "" else "as \"$alias\""

    companion object {
        fun participant(name: String): UmlParticipant = UmlParticipant("participant", name)

        fun actor(name: String): UmlParticipant = UmlParticipant("actor", name)

        fun boundary(name: String): UmlParticipant = UmlParticipant("boundary", name)

        fun control(name: String): UmlParticipant = UmlParticipant("control", name)

        fun entity(name: String): UmlParticipant = UmlParticipant("entity", name)

        fun database(name: String): UmlParticipant = UmlParticipant("database", name)

        fun collections(name: String): UmlParticipant = UmlParticipant("collections", name)

        fun participant(party: Party): UmlParticipant = UmlParticipant("participant", party.asString())

        fun actor(party: Party): UmlParticipant = actor(party.asString())

        fun boundary(party: Party): UmlParticipant = boundary(party.asString())

        fun control(party: Party): UmlParticipant = control(party.asString())

        fun entity(party: Party): UmlParticipant = entity(party.asString())

        fun database(party: Party): UmlParticipant = database(party.asString())

        fun collections(party: Party): UmlParticipant = collections(party.asString())
    }
}