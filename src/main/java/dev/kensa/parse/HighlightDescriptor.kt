package dev.kensa.parse

import dev.kensa.sentence.TokenType.Highlighted
import dev.kensa.util.NamedValue

data class HighlightDescriptor(val value: Any?, val name: String, val colourIndex: String) {
    
    constructor(nameAndValue: NamedValue, colourIndex: String) : this(value = nameAndValue.value, name = nameAndValue.name, colourIndex = colourIndex)

    fun asCss(): Set<String> = Highlighted.asCss().let { setOf(it, "$it-$colourIndex") }

    companion object {
        const val NO_COLOUR_INDEX = "none"
        const val AMBIGUOUS_COLOUR_INDEX = "ambiguous"
    }
}
