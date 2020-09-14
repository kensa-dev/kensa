package dev.kensa.parse

import dev.kensa.Colour
import dev.kensa.TextStyle
import dev.kensa.TextStyle.TextWeightNormal
import java.util.*

data class EmphasisDescriptor(val textStyles: Set<TextStyle> = setOf(TextWeightNormal), val textColour: Colour = Colour.Default, val backgroundColor: Colour = Colour.Default) {

    fun asCss(): Set<String> {
        return if (Default == this) emptySet() else (
                textStyles.mapTo(TreeSet()) { it.asCss() }.apply {
                    textColour.takeUnless { it == Colour.Default }?.let { add(it.asCss()) }
                    backgroundColor.takeUnless { it == Colour.Default }?.let { add(it.asCss()) }
                })
    }

    companion object {
        val Default = EmphasisDescriptor()
    }
}