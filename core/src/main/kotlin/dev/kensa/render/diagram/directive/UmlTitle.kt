package dev.kensa.render.diagram.directive

class UmlTitle : UmlDirective {
    private val titleLines: List<String>

    private constructor(title: String) {
        titleLines = listOf(title)
    }

    private constructor(title: String, vararg otherLines: String) {
        titleLines = listOf(title, *otherLines)
    }

    override fun asUml(): List<String> = listOf("title") + titleLines + "end title"

    companion object {
        @JvmStatic
        fun title(title: String): UmlTitle = UmlTitle(title)

        @JvmStatic
        fun title(title: String, vararg otherLines: String): UmlTitle = UmlTitle(title, *otherLines)
    }
}
