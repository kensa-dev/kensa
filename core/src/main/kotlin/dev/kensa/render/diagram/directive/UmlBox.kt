package dev.kensa.render.diagram.directive

class UmlBox private constructor(private val participants: List<UmlParticipant>, private val title: String = "", private val colour: String = "") : UmlDirective {
    private constructor(vararg participants: UmlParticipant) : this(listOf(*participants))

    fun withTitle(title: String): UmlBox = UmlBox(participants, title, colour)

    fun withColour(colour: String): UmlBox = UmlBox(participants, title, colour)

    override fun asUml(): List<String> = listOf(buildTitle()) + participants.flatMap { it.asUml() } + "end box"

    private fun buildTitle(): String = "box ${title.takeIf { it.isNotEmpty() }?.let { "\"$it\"" }.orEmpty()} $colour".replace("\\s+".toRegex(), " ").trim()

    companion object {
        @JvmStatic
        fun surroundingBox(vararg participants: UmlParticipant): UmlBox = UmlBox(*participants)

        @JvmStatic
        fun surroundingBox(title: String, vararg participants: UmlParticipant): UmlBox = UmlBox(*participants).withTitle(title)
    }
}
