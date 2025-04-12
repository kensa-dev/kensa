package dev.kensa.render.diagram.directive

class UmlHideUnlinked : UmlDirective {
    override fun asUml(): List<String> {
        return listOf("hide unlinked")
    }

    companion object {
        fun hideUnlinkedParticipants() = UmlHideUnlinked()
    }
}